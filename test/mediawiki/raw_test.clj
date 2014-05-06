(ns mediawiki.raw-test
  (:require [clojure.test :refer :all]
            [mediawiki.raw :refer :all]))

(deftest url-handle-test
  (testing "must return the handle (pageids or titles) for the collection of
           url."
    (testing "single pageid url collection."
      (is (= {:pageids "1111"} 
             (url-handle ["http://en.w.org/wiki/index.php?curid=1111"]))))
    (testing "single title url collection."
      (is (= {:titles "Montreal"} 
             (url-handle ["http://en.wikipedia.org/wiki/Montreal"]))))
    (testing "mutiple pageid url collection."
      (is (= {:pageids "1111|1112|1113"} 
             (url-handle ["http://en.w.org/wiki/index.php?curid=1111"
                          "http://en.w.org/wiki/index.php?curid=1112"
                          "http://en.w.org/wiki/index.php?curid=1113"]))))
    (testing "multiple titles url collection."
      (is (= {:titles "Montreal|Paris|Toronto"} 
             (url-handle ["http://en.w.org/wiki/Montreal"
                          "http://en.w.org/wiki/Paris"
                          "http://en.w.org/wiki/Toronto"]))))
    (testing "erroneous url collection"
      (is (nil? (url-handle ["htsd,///org..lol" "fas../.."]))))))
