(ns mediawiki.utils
  (:require [clojure.string :as string]))

(defn endpoint-url
  "returns the endpoint that should be queried given url"
  [page]
  (try
    (let [url (java.net.URL. page)
          host (.getHost url)]
      (format "http://%s/w/api.php" host))
    (catch Exception e nil)))

(defn handle-type
  "returns the 'handle' type (:id or :title) of the given url. Will return
  nil if url is invalid."
  [url]
  (try
    (let [url (java.net.URL. url)]
      (if (and (.getQuery url) (-> url 
                                 .getQuery 
                                 (string/split #"=") 
                                 first
                                 (= "curid"))) 
        :id
        :title))
    (catch Exception e nil)))

(defmulti handle 
  "returns the correct handle dictionary {:pageids <someid>} or 
  {:titles sometitle} depending on the handle type of the url"
  handle-type)
(defmethod handle :id [url]
 (assoc {} 
        :pageids 
        (-> url
            java.net.URL.
            .getQuery
            (string/split #"=")
            second)))
(defmethod handle :title [url]
  (let [parsed (java.net.URL. url)
        path (.getPath parsed)]
    (assoc {}
           :titles
           (-> path
               (string/split #"/")
               last
               (java.net.URLDecoder/decode "UTF-8")))))
(defmethod handle nil [url] nil)

(defn nested-merge
  "Functions to merge nested map. If the values to merge are sequences,
  use (into left right)."
  [& values]
  (cond
    (every? map? values) (apply merge-with nested-merge values)
    (every? sequential? values) (reduce #(into %1 %2) [] values)
    :else (last values)))

(defmacro benchmark
  "returns the time taken, in seconds, to execute the given expression."
  [expression]
  `(let [start (java.lang.System)]))

