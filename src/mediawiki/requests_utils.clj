(ns mediawiki.requests-utils
  (:require [clj-time.coerce :as c]
            [clojure.core.reducers :as r]
            [mediawiki.raw :as raw]))


(defn simple-extract-request
  "template function for inter-wiki-links,external-links,categories"
  [k1 k2 params pages]
  (letfn [(extract-fn [x]
            (if-let [extracts (x k1)]
              (into [] (r/map #(%1 k2) extracts))
              nil))]
    (let [fold-partition-param 2
          group-size 50]
      (raw/mediawiki-request params
                             extract-fn
                             fold-partition-param
                             group-size
                             pages))))

(defn extract-fn-imageinfo
  "returns the url of the most recent item in image info."
  [x]
  (letfn [(update-ts [x] (update-in x ["timestamp"] c/to-long))]
    (if-let [imageinfo (x "imageinfo")]
      ((reduce (partial max-key #(% "timestamp")) {"timestamp" -1} (r/map update-ts imageinfo)) "url")
      nil)))

(defn file-title-url
  "returns a url for accessing the file associated with page."
  [page file]
  (try
    (let [url (java.net.URL. page)
          host (.getHost url)]
      (format "http://%s/wiki/File:%s" host file))
    (catch Exception e nil)))

(defn fold-reduce
  "reduce function used to 'fold into'"
  ([x y]
   (conj x y)))

(defn fold-combine
  "combine function used to 'fold-into'"
  ([] [])
  ([x y]
    (into x y)))
