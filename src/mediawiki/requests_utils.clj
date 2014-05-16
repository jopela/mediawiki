(ns mediawiki.requests-utils
  (:require [clj-time.coerce :as c]
            [clojure.core.reducers :as r]))

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

