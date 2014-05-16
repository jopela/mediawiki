(ns mediawiki.requests
  (:require [mediawiki.utils :as utils]
            [mediawiki.raw :as raw]
            [clojure.core.reducers :as r]))

(defn geocoords
  "returns the list of geocoordinates of the pages. Put nil in the collection
  if the page has no coordinates."
  [pages]
  (letfn [(extract-fn [x]
           (if-let [coords (x "coordinates")]
            (let [{lat "lat" lon "lon"} (first coords )]
              [lat lon])
            nil))]
    (let [params {:prop "coordinates"
                  :colimit 500
                  :coprimary "primary"}
          fold-partition-param 2
          group-size 50]
      (raw/mediawiki-request params
                             extract-fn
                             fold-partition-param
                             group-size
                             pages))))

(defn language-links
  "returns all the language links of a given mediawiki page Languages of the 
  pages can be inferred by the 
  url." 
  [pages]
  (letfn [(extract-fn [x]
            (if-let [langlinks (x "langlinks")]
              (into [] (r/map #(%1 "url") langlinks))
              nil))]
    (let [params {:prop "langlinks"
                  :lllimit 500
                  :llprop "url"}
          fold-partition-param 4
          group-size 50]
      (raw/mediawiki-request params
                             extract-fn
                             fold-partition-param
                             group-size
                             pages))))

(defn image-links
  "returns all the images url of a given page."
  [pages]
  [["http://en.wikipedia.org/photo/omg.jpg"] 
   ["http://en.wikipedia.org/p/ok.jpg"]])

(defn introduction-html
  "returns the introductory text of a page (abstract). The text format is
  html."
  [pages]
  (letfn [(extract-fn [x] (x "extract"))]
    (let [params {:prop "extracts"
                  :exlimit 20
                  :exintro "True"}
          fold-partition-param 4
          group-size 50]
      (raw/mediawiki-request params
                             extract-fn
                             fold-partition-param
                             group-size
                             pages))))

(defn article-html
  "returns the article of the page in html format."
  [pages]
  (letfn [(extract-fn [x] (x "extract"))]
    (let [params {:prop "extracts"
                  :exlimit 1}
          fold-partition-param 2
          group-size 50]
      (raw/mediawiki-request params
                             extract-fn
                             fold-partition-param
                             group-size
                             pages))))

(defn depiction
  "return the url of the image that acts as the depiction of the page"
  [pages]
  nil)


(defn categories
  "return the categories to which the given page belongs."
  [pages]
  (letfn [(extract-fn [x]
            (if-let [categories (x "categories")]
              (into [] (r/map #(%1 "title") categories))
              nil))]
    (let [params {:prop "categories"
                  :cllimit 500}
          fold-partition-param 2
          group-size 50]
      (raw/mediawiki-request params 
                             extract-fn
                             fold-partition-param
                             group-size
                             pages))))

(defn external-links
  "return the external urls from the given page. For inter-wiki links,
  used the inter-wiki-links function."
  [pages]
  (letfn [(extract-fn [x]
            (if-let [extlinks (x "extlinks")]
              (into [] (r/map #(%1 "*") extlinks))
              nil))]
    (let [params {:prop "extlinks"
                  :ellimit 500
                  :elexpandurl "True"}
          fold-partition-param 2
          group-size 50]
      (raw/mediawiki-request params
                             extract-fn
                             fold-partition-param
                             group-size
                             pages))))

(defn inter-wiki-links
  "return url links to other wiki's (wikivoyage, wikibooks etc) for the given
  page."
  [pages]
  [(into #{} ["http://en.wikivoyage.org/wiki/Montreal" 
             "http://en.wikibooks.org/wiki/Montreal"])])

(defn all-properties
  "returns a seq of documents containing: geocoords, language-links imege-links
  introduction-html, article-html, depiction, categories, external-links
  and inter-wiki-links."
  [pages]
  [{:geocoords [45 -73]
    :language-links ["http://en.wiki.org" "http://fr.wiki.org"]
    :image-links ["http://jpeg.com" "http://png.com"]
    :introduction-html "<p>introduction text</p>"
    :article-html "<h1>full article</h1>"
    :depiction "http://depiction.jpeg"
    :categories ["cat1" "cat2"]
    :external-links ["http://extern.com"]
    :inter-wiki-links ["http://wiki1.com" "http://wiki2.com"]}])



