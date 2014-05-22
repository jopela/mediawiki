(ns mediawiki.raw
  (:require [clj-http.client :as client]
            [mediawiki.utils :as utils]
            [clojure.core.reducers :as r]))

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
  (letfn [(http-get [e q] (:body (client/get e {:as :json-string-keys
                                                :query-params q})))]
    (loop [query-result {} req-body (http-get endpoint query-params)]
      (if-let [continue (req-body "query-continue")]
        (recur (utils/nested-merge query-result
                                  (select-keys req-body ["query"]))
               (http-get endpoint (merge query-params 
                                         (continue-handle continue))))
        (utils/nested-merge query-result (select-keys req-body ["query"]))))))

(defn rand-access-title
  "Performs a transformation on the result map so that random access
  by title handle is restored."
  [result-map]
  (let [pages (get-in result-map ["query" "pages"])
        new-pages (into {} (for [[page-id {title "title" :as item}] pages]
                             {title item}))]
    (assoc-in result-map ["query" "pages"] new-pages)))

(defn mediawiki-group-request
  "Performs an API request for this group of url and returns a map with url as
  keys and result as values. The function require the sequence of url that
  make the group (group-coll), a mapping of the parameters (specific-params) 
  that are specific to the query to make (prop=coordinates, etc, limits etc)
  and finally, a function (extractro-fn) that will be called on pages item to 
  extract the results. This function should take a value keyed by id and 
  return the corresponding result."
  [specific-params extract-fn group-coll]
  (if-let [handles (url-handle group-coll)]
    (let [endpoint (-> group-coll first utils/endpoint-url)
          api-params {:action "query" :format "json"}
          query-params (merge handles api-params specific-params)
          handle-t (-> group-coll first utils/handle-type)
          raw-result (serial-mediawiki-req endpoint query-params)
          look-up-rand-access (get-in (if (= :id handle-t) 
                                        raw-result
                                        (rand-access-title raw-result))
                                        ["query" "pages"])]
      (into {} (for [u group-coll
                     :let [h (-> u utils/handle vals first)
                           res (look-up-rand-access h)]
                     :when (not= res nil)] {u (extract-fn res)})))
     {}))

(defn mediawiki-request
  "Performs a specific request for a collection of url to the mediawiki(s) API(s).
  Requests are routed to the right endpoint based on the the url."
  [query-param extract-fn fold-partition-param group-size coll]
  (let [endpoint-group (into [] (vals (group-by utils/endpoint-url coll)))
        groups (reduce into [] (r/map (partial partition-all group-size) endpoint-group))
        req-fn (partial mediawiki-group-request query-param extract-fn)
        ; will perform the requests using map-reduce.
        responses (r/fold fold-partition-param merge merge (r/map req-fn groups))]
    (into [] (r/map responses coll))))

