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
      (is (< (utils/benchmark (geocoords geocoords-10-urls)) 0.225)))
    (testing "speed test, medium coll"
      (is (< (utils/benchmark (geocoords geocoords-100-urls)) 0.5)))
    (testing "speed test, large coll"
      (is (< (utils/benchmark (geocoords geocoords-725-urls)) 1.0)))))

(def language-links-test-pages ["http://fr.wikipedia.org/wiki/Paris"
                                "http://en.wikipedia.org/wiki/Montreal"
                                "http://af.wikipedia.org/wiki/Kaapstad"
                                "http://it.wikipedia.org/wiki/Qu%C3%A9bec_(provincia)"
                                "http://ru.wikipedia.org/wiki/%D0%A1%D0%B5%D0%BD%D1%82-%D0%94%D0%B6%D0%BE%D0%BD_(%D0%9D%D1%8C%D1%8E-%D0%91%D1%80%D0%B0%D0%BD%D1%81%D1%83%D0%B8%D0%BA)"
                                "http://en.wikipedia.org/wiki/Humid_continental_climate"])
(def language-links-test-subset-expected #{"http://en.wikipedia.org/wiki/Paris" 
                                           "http://fr.wikipedia.org/wiki/Montr%C3%A9al" 
                                           "http://ka.wikipedia.org/wiki/%E1%83%99%E1%83%94%E1%83%98%E1%83%9E%E1%83%A2%E1%83%90%E1%83%A3%E1%83%9C%E1%83%98" 
                                           "http://lmo.wikipedia.org/wiki/Qu%C3%A9bec_(pruinsa_e_nasi%C3%B9)"
                                           "http://zh.wikipedia.org/wiki/%E5%9C%A3%E7%BA%A6%E7%BF%B0_(%E6%96%B0%E4%B8%8D%E4%BC%A6%E7%91%9E%E5%85%8BA)"
                                           "http://ko.wikipedia.org/wiki/%EC%8A%B5%EC%9C%A4_%EB%8C%80%EB%A5%99%EC%84%B1_%EA%B8%B0%ED%9B%84"})
(def language-links-test-invalid ["htp./asddasd.cccc23k2k2"])

(language-links [
                                "http://it.wikipedia.org/wiki/Qu%C3%A9bec_(provincia)"])

(deftest language-links-test
  (testing "Must return the list of language links associated with the 
           given wiki url."
    (testing "valid urls"
      (let [links (->> language-links-test-pages
                       language-links
                       flatten
                       (into #{}))]
        (is (every? language-links-test-subset-expected links))))
    (testing "invalid url"
      (is (= [nil] (language-links language-links-test-invalid))))))

;(deftest image-links-test
;  (testing "Must return all the images url of a given page."
;    (testing 

