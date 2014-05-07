(ns mediawiki.raw-test
  (:require [clojure.test :refer :all]
            [mediawiki.raw :refer :all]
            [cheshire.core :refer :all]))

; test helper function.
(defn load-json-test
  [filename]
  (-> filename
      slurp
      (parse-string true)))
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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

(deftest continue-handle-test
  (testing "Must return a map containing the parameters for continuation
           in the query-continue map."
    (testing "Single continuation parameter."
      (is (= {:llcontinue "9999|continued"}
             (continue-handle {:langlinks {:llcontinue "9999|continued"}}))))
    (testing "2 continuation parameters."
      (is (= {:llcontinue "999|con"
              :imcontinue "998|con"}
             (continue-handle {:langlinks {:llcontinue "999|con"
                                            :imcontinue "998|con"}}))))))

(def en-wikipedia-city-test ["http://en.wikipedia.org/w/api.php"
                             {:format "json"
                              :action "query"
                              :prop "langlinks"
                              :lllimit 10
                              :llprop "url"
                              :titles "Montreal"}])
(def en-wikipedia-city-expected 
  (load-json-test "./test/mediawiki/wiki-test/en-wikipedia-city-result.json"))
(deftest serial-mediawiki-req-test
  (testing "Must return all the content pertaining to the group from the
           mediawiki API."
    (is (= en-wikipedia-city-expected
           (apply serial-mediawiki-req en-wikipedia-city-test)))))


