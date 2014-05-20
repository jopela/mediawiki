(ns mediawiki.requests
  (:require [mediawiki.utils :as utils]
            [mediawiki.raw :as raw]
            [mediawiki.requests-utils :as requests-utils]
            [clojure.string :as string]
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
          fold-partition-param 2
          group-size 50]
      (raw/mediawiki-request params
                             extract-fn
                             fold-partition-param
                             group-size
                             pages))))

(defn image-links
  "returns all the images url of a given page."
  [pages]
  (letfn [(extract-fn-images [x] 
            (if-let [images (x "images")]
              (into [] (r/map (fn [x] 
                                (-> "title" 
                                    x 
                                    (string/replace #"File:" ""))) images))
              nil))]
    (let [params-images {:prop "images"
                         :imlimit 500}
          params-imageinfo {:prop "imageinfo"
                            :iiprop "timestamp|url"
                            :iilimit 500}
          fold-partition-param 2
          group-size 50
          img-fn (partial raw/mediawiki-request 
                          params-imageinfo
                          requests-utils/extract-fn-imageinfo
                          fold-partition-param
                          group-size)]
      (let [files (raw/mediawiki-request params-images
                                         extract-fn-images
                                         fold-partition-param
                                         group-size
                                         pages)
            pages-files (map vector pages files)
            files-urls (doall (for [[url coll] pages-files] (into [] 
                                                           (r/map (partial requests-utils/file-title-url url) coll))))]

        (r/fold 1 requests-utils/fold-combine requests-utils/fold-reduce (r/map img-fn files-urls))))))

(defn introduction-html
  "returns the introductory text of a page (abstract). The text format is
  html."
  [pages]
  (letfn [(extract-fn [x] (x "extract"))]
    (let [params {:prop "extracts"
                  :exlimit 20
                  :exintro "True"}
          fold-partition-param 2
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
  (letfn [(extract-fn-pageimages [x] (x "pageimage"))]
    (let [param-pageimages {:prop "pageimages"
                           :piprop "name"
                           :pilimit 50}
          fold-partition-param 2
          group-size-pageimages 50
          param-imageinfo {:prop "imageinfo"
                           :iilimit 500
                           :iiprop "timestamp|url"}
          group-size-imageinfo 50]
      (let [files (raw/mediawiki-request param-pageimages
                                         extract-fn-pageimages
                                         fold-partition-param
                                         group-size-pageimages
                                         pages)
            pages-files (map vector pages files)
            files-urls (into [] (r/map #(apply requests-utils/file-title-url %) pages-files))]
        (raw/mediawiki-request param-imageinfo
                               requests-utils/extract-fn-imageinfo
                               fold-partition-param
                               group-size-imageinfo
                               files-urls)))))

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
  (letfn [(extract-fn [x] 
            (if-let [iwlinks (x "iwlinks")]
              (into [] (r/map #(%1 "url") iwlinks))
              nil))]
    (let [params {:prop "iwlinks"
                  :iwurl "True"
                  :iwlimit 500}
          fold-partition-param 2
          group-size 50]
      (raw/mediawiki-request params
                             extract-fn
                             fold-partition-param
                             group-size
                             pages))))

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
