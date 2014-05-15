(ns mediawiki.requests-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [mediawiki.requests :refer :all]
            [mediawiki.utils :as utils]))

(defn load-url
  "helper function to load url list from file."
  [filename]
  (with-open [rdr (io/reader filename)]
    (into [] (line-seq rdr))))

(load-url "./test/mediawiki/wiki-test/urllist10.txt")

(def geocoords-title-url-test ["http://en.wikipedia.org/wiki/Montreal"
                               "http://en.wikipedia.org/wiki/Toronto"
                               "http://en.wikipedia.org/wiki/Paris"
                               "http://en.wikipedia.org/wiki/Quebec"
                               "http://en.wikipedia.org/wiki/Sherbrooke"])
(def geocoords-title-url-test-expected [[45.5 -73.5667] [43.7 -79.4] [48.8567 2.3508] [53 -70] [45.4 -71.9]])
(def geocoords-id-url-test ["http://en.wikipedia.org/wiki/index.php?curid=7954681"
                            "http://en.wikipedia.org/wiki/index.php?curid=22989"
                            "http://en.wikipedia.org/wiki/index.php?curid=7954867"])
(def geocoords-id-url-test-expected [[45.5 -73.5667] [48.8567 2.3508] [53 -70]])
(def geocoords-title-url-no-coord-test ["http://en.wikipedia.org/wiki/Coal_seam_fire"
                                        "http://en.wikipedia.org/wiki/S-expression"])
(def geocoords-title-url-no-coord-test-expected [nil nil])
(def geocoords-invalid-url-test ["http:/ajcom" "http://en"])
(def geocoords-invalid-url-test-expected [nil nil])

(def geocoords-10-urls (load-url "./test/mediawiki/wiki-test/urllist10.txt"))
(def geocoords-100-urls (load-url "./test/mediawiki/wiki-test/urllist100.txt"))
(def geocoords-725-urls (load-url "./test/mediawiki/wiki-test/urllist725.txt"))

(deftest geocoords-test
  (testing "Must return a list of the coordinates corresponding to the urls in the collection"
    (testing "title urls"
      (is (= geocoords-title-url-test-expected (geocoords geocoords-title-url-test))))
    (testing "id urls"
      (is (= geocoords-id-url-test-expected (geocoords geocoords-id-url-test))))
    (testing "urls with no coordinates"
      (is (= geocoords-title-url-no-coord-test-expected (geocoords geocoords-title-url-no-coord-test))))
    (testing "invalid urls"
      (is (= geocoords-invalid-url-test-expected (geocoords geocoords-invalid-url-test-expected))))
    (testing "speed test, small coll"
      (is (< (utils/benchmark (geocoords geocoords-10-urls)) 0.5)))
    (testing "speed test, medium coll"
      (is (< (utils/benchmark (geocoords geocoords-100-urls)) 1.2)))
    (testing "speed test, large coll"
      (is (< (utils/benchmark (geocoords geocoords-725-urls)) 5)))))

(geocoords geocoords-725-urls)
    
