(ns mediawiki.requests
  (:require [mediawiki.utils :as utils]))

(defn geocoords
  "returns the list of geocoordinates of the pages. Put nil in the collection
  if the page has no coordinates."
  [pages]
  [[45 -73] [45 2.35]])

(defn language-links
  "returns all the language links of a given mediawiki page (including the
  language of the queried wiki). Languages of the pages can be inferred by the 
  url." 
  [pages]
  [["http://en.wikipedia.org/Montreal" "http://fr.wikipedia.org/Montreal"]
   ["http://en.wikipedia.org/Paris" "http://fr.wikipedia.org/Paris"]])

(defn image-links
  "returns all the images url of a given page."
  [pages]
  [["http://en.wikipedia.org/photo/omg.jpg"] 
   ["http://en.wikipedia.org/p/ok.jpg"]])

(defn introduction-html
  "returns the introductory text of a page (abstract). The text format is
  html."
  [pages]
  ["<p><b>Montreal is a great city</b></p>"])

(defn article-html
  "returns the article of the page in html format."
  [pages]
  ["<h2>History</h2><p> The history of Montreal is fun</p>"])

(defn depiction
  "returns the url of the image that acts as the depiction of the page"
  [pages]
  ["http://en.wikipedia.org/wiki/picture.jpeg"])

(defn categories
  "return the categories to which the given page belongs."
  [pages]
  [(into #{} ["Populated places in 1642" "Montreal"])])

(defn external-links
  "return the external urls from the given page. For inter-wiki links,
  used the inter-wiki-links function."
  [pages]
  [(into #{} ["http://stm.info" "http://meteo-montreal.com"])])

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



