(ns mediawiki.raw
  (:require [clj-http.client :as client]
            [mediawiki.utils :as utils]
            [clojure.core.reducers :as r]))

; Input is a list.
; Result is also a list. Order MUST be preserved in the final result.
; Requests must be FAST.
; Api must be scalable and support lists of size up to a very large limit.
; Must be implemented as a service.
; Must take it's lists of pages to work on from a message queue (rabbitMQ).

; Example input.
(def pages ["http://en.wikipedia.org/wiki/Montreal" 
            "http://fr.wikipedia.org/wiki/Montreal"
            "http://en.wikipedia.org/wiki/index.php?curid=1111"
            "http://ru.wikipedia.org/wiki/index.php?curid=1112"
            "http://ru.wikipedia.org/wiki/index.php?curid=1113"])

; Before ANY requests is made, we know that urls for requests will be
; grouped together. In that case we have 4 groups:

; ordering of urls might be lost here.
(def groups [["http://en.wikipedia.org/wiki/Montreal"]
             ["http://en.wikipedia.org/index.php?curid=1111"]
             ["http://fr.wikipedia.org/wiki/Montreal"]
             ["http://ru.wikipedia.org/wiki/index.php?curid=1111"
              "http://ru.wikipedia.org/wiki/index.php?curid=1112"
              "http://ru.wikipedia.org/wiki/index.php?curid=1113"]])

; The rule is that, within one group, url must all have the same endpoint,
; and must all contain either a title or an id handle.
;furthermore, groups of size larger then N must be broken down in subgroups
; size smaller or equal to N. in this example N = 2

; again, urls are mixed again
(def sized-groups [["http://en.wikipedia.org/wiki/Montreal"]
                   ["http://en.wikipedia.org/index.php?curid=1111"]
                   ["http://fr.wikipedia.org/wiki/Montreal"]
                   ["http://ru.wikipedia.org/wiki/index.php?curid=1111"
                    "http://ru.wikipedia.org/wiki/index.php?curid=1112"]
                   ["http://ru.wikipedia.org/wiki/index.php?curid=1113"]])

; These form the groups of url for which the requests will be sent in // or
; asynchronously.

; We need to decide how to handle collections of groups. We will do this
; using cojure reducers.

; Given the function that handle a single group. We have 
;(single-group ["http://url1.com" "http://url2.com" "http://url3"])
;-> a map of url, resul :
; {url1 result1
;  url2 result2
;  url3 result3}

; Result of that can be reduced with an into kind of reducing function.

; we then restore order with the reduced map by traversing the initial 
; collection.

; The only part I dont really know how to handle yet is the single group
; thing. Here is how it might look like.

;1) From the collection forming a group, reduce it into a handle dictionary.
;2) Build the query params depending on what we want from the API 
; (coordinates, image links etc.)
;3) make the initial query to the API.
;4) while result contains some kind of continue parameters:
;   make the query again and merge the results with the previous results.


; a VERY QUICK check online says that Fork/Join framework is a 
; 'calamity' for IO bound tasks. reducers is based on Fork/Join, therefore
; reducers are a calamity for IO bound tasks?

; I already have a lead which tells me that using http-kit with futures
; would yield significant performance imporvements

(def single-group
  ["http://ru.wikipedia.org/wiki/index.php?curid=1111"
  "http://ru.wikipedia.org/wiki/index.php?curid=1112"
  "http://ru.wikipedia.org/wiki/index.php?curid=1113"])

(defn url-handle
  "Returns a dictionary containing the proper url handle for the url group
  collection. A handle is used to specify to the API wich page we need 
  information from."
  [coll]
  (let [merge-with-fn (partial merge-with (fn [x y] (str x "|" y)))
        reduce-fn (comp (r/remove nil?) (r/map utils/handle))]
    (reduce merge-with-fn (reduce-fn coll))))

(defn continue-handle
  "compute the parameters for conitnueation from the query-continue map"
  [query-continue]
  (into {} (vals query-continue)))

(defn serial-mediawiki-req
  "Performs a requests to the mediawiki API, issuing continue requests and
  merging back the result if required."
  [endpoint query-params]
  (letfn [(http-get [e q] (:body (client/get e {:as :json
                                                :query-params q})))]
    (loop [query-result {} req-body (http-get endpoint query-params)]
      (if-let [continue (req-body :query-continue)]
        (recur (utils/nested-merge query-result
                                  (select-keys req-body [:query]))
               (http-get endpoint (merge query-params 
                                         (continue-handle continue))))
        (utils/nested-merge query-result (select-keys req-body [:query]))))))

(defn rand-access-title
  "Performs a transformation on the result map so that random access
  by title handle is restored."
  [result-map]
  (let [pages (get-in result-map [:query :pages])
        new-pages (into {} (for [[page-id {:keys [title] :as item}] pages]
                             {title item}))]
    (assoc-in result-map [:query :pages] new-pages)))

(defn mediawiki-group-request
  "Performs an API request for this group of url and returns a map with url as
  keys and result as values. The function require the sequence of url that
  make the group (group-coll), a mapping of the parameters (specific-params) 
  that are specific to the query to make (prop=coordinates, etc, limits etc)
  and finally, a function (extractro-fn) that will be called on pages item to 
  extract the results. This function should take a value keyed by id and 
  return the corresponding result."
  [group-coll specific-params extract-fn]
  (if-let [handles (url-handle group-coll)]
    (let [endpoint (-> group-coll first utils/endpoint-url)
          api-params {:action "query" :format "json"}
          query-params (merge handles api-params specific-params)
          handle-t (-> group-coll first utils/handle-type)
          raw-result (serial-mediawiki-req endpoint query-params)
          look-up-rand-access (get-in (if (= :id handle-t) 
                                        raw-result
                                        (rand-access-title raw-result))
                                        [:query :pages])]
      (into {} (for [u group-coll
                     :let [h (-> u utils/handle vals first)
                           res (look-up-rand-access h)]
                     :when (not= res nil)] {u (extract-fn res)})))
    {}))
      
