(ns mediawiki.utils-test
  (:require [clojure.test :refer :all]
            [mediawiki.utils :refer :all]))

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
             (handle-type "http://en.wikipedia.org/index.php?curid=8918")))
    (testing "title type"
      (is (= :title
             (handle-type "http://en.wikipedia.org/wiki/Montreal"))))
    (testing "erroneous url entry"
      (is (nil? (handle-type "htd;/lol.orgzzz")))))))
