(ns mediawiki.requests-utils
  (:require [clj-time.coerce :as c]))

(defn extract-fn-imageinfo
  "returns the url of the most recent element of the imageinfo map"
