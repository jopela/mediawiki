(ns mediawiki.utils-test
  (:require [clojure.test :refer :all]
            [mediawiki.utils :refer :all]
            [cheshire.core :refer :all]))

; test helper function.
(defn load-json-test
  [filename]
  (-> filename
      slurp
      parse-string))
;~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
(deftest endpoint-url-tests
  (testing "must return the endpoint where info about the gien page can be
           obtained."
    (testing "valid input"
      (are [x y] (= x y)
        "http://w.org/w/api.php" (endpoint-url "http://w.org/wiki/Montreal")
        "http://f.w.org/w/api.php" (endpoint-url "http://f.w.org/wiki/Coco")
        "http://r.w.org/w/api.php" (endpoint-url "http://r.w.org/w/Russia")))
    (testing "invalid input"
      (are [x y] (= x y)
        nil (endpoint-url "htj;/clawjurrr.howrg")
        nil (endpoint-url "../../wiki")
        nil (endpoint-url nil)
        nil (endpoint-url "htp//` - *$#@3.org/wiki")))))

(deftest handle-type-tests
  (testing "must return the handle type (either :id or :title) of the given
           url."
    (testing "id type"
      (is (= :id 
             (handle-type "http://en.wikipedia.org/index.php?curid=8918"))))

    (testing "title type"
      (is (= :title
             (handle-type "http://en.wikipedia.org/wiki/Montreal"))))

    (testing "encoded url"
      (is (= :title
             (handle-type "http://ru.wikipedia.org/wiki/%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0"))))

    (testing "erroneous url entry"
      (is (nil? (handle-type "htd;/lol.orgzzz"))))))

(deftest handle-test
  (testing "must return the handle dictionary of the given mediawiki url."
    (testing "id handle"
      (is (= {:pageids "1111"}
             (handle "http://en.wikipedia.org/wiki/index.php?curid=1111"))))
    (testing "title handle"
      (is (= {:titles "Montreal"}
             (handle "http://en.wikipedia.org/wiki/Montreal"))))
    (testing "encoded url"
      (is (= {:titles "Москва"}
             (handle "http://ru.wikipedia.org/wiki/%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0"))))
    (testing "invalid handle type"
      (is (nil? (handle "htp[:/loll.com"))))
    (testing "title handle that must be normalized"
      (is (= {:titles "Québec (provincia)"}
             (handle "http://it.wikipedia.org/wiki/Qu%C3%A9bec_(provincia)"))))))
    
(def mediawiki-value1 (load-json-test "./test/mediawiki/wiki-test/mediawiki-response1.json"))
(def mediawiki-value2 (load-json-test "./test/mediawiki/wiki-test/mediawiki-response2.json"))
(def mediawiki-merged (load-json-test "./test/mediawiki/wiki-test/mediawiki-merged.json"))
(deftest nested-merge-test
  (testing "must return a merged with the nesting function recursively
           applied."
    (testing "1 level deep nested map with sequentials as values."
      (is (= {:a {:b [1 2 3 4 5 6]}}
             (nested-merge {:a {:b [1 2 3]}} {:a {:b [4 5 6]}}))))
    (testing "1 level deep nested map with strings as values."
      (is (= {:a {:b "last"}} (nested-merge {:a {:b "first"}}
                                            {:a {:b "second"}}
                                            {:a {:b "last"}}))))
    (testing "real mediawiki return values."
      (is (= mediawiki-merged
             (nested-merge mediawiki-value1
                           mediawiki-value2))))))

(deftest cap-first-test
  (testing "Must return the same string with the first letter capitalised."
    (testing "lowercase input"
      (is (= "Aaaa" (cap-first "aaaa"))))
    (testing "Multiple words"
      (is (= "Aaaa Montreal" (cap-first "aaaa Montreal"))))
    (testing "Already cap-first"
      (is (= "Aaaa" (cap-first "Aaaa"))))))

(deftest normalize-title-test
  (testing "Must return new title with normalization rules applied."
    (testing "lowercase first letter title"
      (is (= "Montreal" (normalize-title "montreal"))))
    (testing "underscore in title"
      (is (= "Super Montreal City" (normalize-title "Super_Montreal_City"))))
    (testing "lowercase+underscore"
      (is (= "Super montreal city" (normalize-title "super_montreal_city")))))) 

