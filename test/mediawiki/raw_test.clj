(ns mediawiki.raw-test
  (:require [clojure.test :refer :all]
            [mediawiki.raw :refer :all]
            [cheshire.core :refer :all]
            [clojure.core.reducers :as r]))

; test helper function.
(defn load-json-test
  [filename]
  (-> filename
      slurp
      (parse-string)))
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

; with continue.
(def en-wikipedia-city-test-con ["http://en.wikipedia.org/w/api.php"
                                 {:format "json"
                                  :action "query"
                                  :prop "langlinks"
                                  :lllimit 10
                                  :llprop "url"
                                  :titles "Montreal"}])
; with no continue.
(def en-wikipedia-city-test-nocon ["http://en.wikipedia.org/w/api.php"
                                   {:format "json"
                                    :action "query"
                                    :prop "langlinks"
                                    :lllimit 500
                                    :llprop "url"
                                    :titles "Montreal"}])
(def en-wikipedia-city-subset-expected ["http://de.wikipedia.org/wiki/Montreal" 
                                        "http://es.wikipedia.org/wiki/Montreal"
                                        "http://fr.wikipedia.org/wiki/Montr%C3%A9al"
                                        "http://it.wikipedia.org/wiki/Montr%C3%A9al"
                                        "http://pt.wikipedia.org/wiki/Montreal"])
(defn extract-urls-test 
  [x]
  (if-let [results (get-in x ["query" "pages"])]
    (letfn [(extract-fn [y]
              (if-let [langlinks (y "langlinks")]
                (into [] (r/map #(% "url") langlinks))
                nil))]
      (into [] (r/map extract-fn (vals results))))
    nil))

(deftest serial-mediawiki-req-test
  (testing "Must return all the content pertaining to the group from the
           mediawiki API"
    (testing "with continue requests"
      (let [[mtl-urls] (extract-urls-test (apply serial-mediawiki-req en-wikipedia-city-test-con))
            mtl-urls-set (into #{} mtl-urls)]
        (is (every? mtl-urls-set en-wikipedia-city-subset-expected))))
    (testing "no continue requests"
      (let [[mtl-urls] (extract-urls-test (apply serial-mediawiki-req en-wikipedia-city-test-nocon))
            mtl-urls-set (into #{} mtl-urls)]
        (is (every? mtl-urls-set en-wikipedia-city-subset-expected))))))
      
(def rand-access-api-res-test {"query"{"pages" {"1" {"pageid" 1
                                                   "title""Montreal"
                                                   "prop" {"value" "1"}}
                                               "2" {"pageid" 2
                                                   "title" "Paris"
                                                   "prop" {"value" "2"}}}}})
(def rand-access-api-res-ex {"query" {"pages" {"Montreal" {"pageid" 1
                                                         "title" "Montreal"
                                                         "prop" {"value" "1"}}
                                             "Paris" {"pageid" 2
                                                     "title""Paris"
                                                     "prop" {"value" "2"}}}}})

(deftest rand-access-title-test
  (testing "Must return a randomly accessible (by title) result map."
    (is (= rand-access-api-res-ex
           (rand-access-title rand-access-api-res-test)))))

(defn extract-coordinate-test
  [{coordinates "coordinates"}]
  (let [primary (first coordinates)]
    [(primary "lat") (primary "lon")]))

(def specific-params-test {:format "json" 
                           :action "query"
                           :prop "coordinates"
                           :coprimary "primary"
                           :colimit 10}) ; Think there is a bug in the mediawiki API
                                         ; that causes an incomplete result set to be 
                                         ; generated when colimit==1. Setting it to 10 for tests.
(def coll-valid-title-test ["http://en.wikipedia.org/wiki/Montreal"
                            "http://en.wikipedia.org/wiki/Paris"
                            "http://en.wikipedia.org/wiki/Toronto"
                            "http://en.wikipedia.org/wiki/Quebec"
                            ])
(def coll-valid-title-ex {"http://en.wikipedia.org/wiki/Montreal" [45.5 -73.5667]
                          "http://en.wikipedia.org/wiki/Paris" [48.8567 2.3508]
                          "http://en.wikipedia.org/wiki/Toronto" [43.7 -79.4]
                          "http://en.wikipedia.org/wiki/Quebec" [53 -70]
                          })
(def coll-valid-id-test ["http://en.wikipedia.org/wiki/index.php?curid=7954681"
                         "http://en.wikipedia.org/wiki/index.php?curid=22989"
                         "http://en.wikipedia.org/wiki/index.php?curid=7954867"
                         "http://en.wikipedia.org/wiki/index.php?curid=64646"])
(def coll-valid-id-ex {"http://en.wikipedia.org/wiki/index.php?curid=7954681" [45.5 -73.5667]
                       "http://en.wikipedia.org/wiki/index.php?curid=22989" [48.8567 2.3508]
                       "http://en.wikipedia.org/wiki/index.php?curid=7954867" [53 -70]
                       "http://en.wikipedia.org/wiki/index.php?curid=64646" [43.7 -79.4]})

(deftest mediawiki-group-request-test
  (testing "Must return the result of a group request."
    (testing "title handle group."
      (is (= coll-valid-title-ex
             (mediawiki-group-request specific-params-test
                                      extract-coordinate-test
                                      coll-valid-title-test))))
    (testing "id handle group."
      (is (= coll-valid-id-ex
             (mediawiki-group-request specific-params-test
                                      extract-coordinate-test
                                      coll-valid-id-test))))))

(def coll-mediawiki-request-test ["http://en.wikipedia.org/wiki/Montreal" 
                                  "http://en.wikipedia.org/wiki/Paris"
                                  "http://en.wikipedia.org/wiki/Toronto" "http://en.wikipedia.org/wiki/Quebec"
                                  "http://en.wikipedia.org/wiki/Sherbrooke" 
                                  "http://ru.wikipedia.org/wiki/%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0"
                              ])
(def coll-mediawiki-request-expected [[45.5 -73.5667] 
                                      [48.8567 2.3508] 
                                      [43.7 -79.4] 
                                      [53 -70] 
                                      [45.4 -71.9] 
                                      [55.7517 37.6178]])

(def fold-partition-param-test 2)
(def group-size-test 1)
(deftest mediawiki-request-test
  (testing "Must return, in order, the result of the mediawiki API query for each url in coll."
    (is (= coll-mediawiki-request-expected (mediawiki-request specific-params-test
                                                              extract-coordinate-test
                                                              fold-partition-param-test
                                                              group-size-test
                                                              coll-mediawiki-request-test)))))
