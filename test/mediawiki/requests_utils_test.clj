(ns mediawiki.requests-utils-test
  (:require [clojure.test :refer :all]
            [mediawiki.requests-utils :refer :all]))

(def extract-fn-imageinfo-in {"imageinfo" [{"timestamp" "2011-10-29T05:44:39Z"
                                            "url" "http://upload.wikimedia.org/wikipedia/commons/5/50/Montage_of_Toronto_7.jpg"}
                                           {"timestamp" "2010-11-03T05:04:29Z"
                                            "url" "http://upload.wikimedia.org/wikipedia/commons/archive/5/50/20111029054439%21Montage_of_Toronto_7.jpg"}
                                           {"timestamp" "2010-11-03T04:59:19Z" 
                                            "url" "http://upload.wikimedia.org/wikipedia/commons/archive/5/50/20101103050429%21Montage_of_Toronto_7.jpg"}]})
(def extract-fn-imageinfo-ex "http://upload.wikimedia.org/wikipedia/commons/5/50/Montage_of_Toronto_7.jpg")
(deftest extract-fn-imageinfo-test
  (testing "Must return the url with the most recent timestamp."
    (is (= extract-fn-imageinfo-ex (extract-fn-imageinfo extract-fn-imageinfo-in)))))

(deftest file-title-url-test
  (testing "Must return a url to access a file"
    (is (= "http://en.wikipedia.org/wiki/File:Montreal_Skyline_2011.jpeg" 
           (file-title-url "http://en.wikipedia.org/wiki/Montreal"
                           "Montreal_Skyline_2011.jpeg")))))

