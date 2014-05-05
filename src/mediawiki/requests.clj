(ns mediawiki.requests)



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
  [page]
  ["http://en.wikipedia.org/photo/omg.jpg" "http://en.wikipedia.org/p/ok.jpg"])

(defn introduction-html
  "returns the introductory text of a page (abstract). The text format is
  html."
  [page]
  "<p><b>Montreal is a great city</b></p>")

(defn article-html
  "returns the article of the page in html format."
  [page]
  "<h2>History</h2><p> The history of Montreal is fun</p>")

(defn depiction
  "returns the url of the image that acts as the depiction of the page"
  [page]
  "http://en.wikipedia.org/wiki/picture.jpeg")

(defn categories
  "return the categories to which the given page belongs."
  [page]
  (into #{} ["Populated places in 1642" "Montreal"]))

(defn external-links
  "return the external urls from the given page. For inter-wiki links,
  used the inter-wiki-links function."
  [page]
  (into #{} ["http://stm.info" "http://meteo-montreal.com"]))

(defn inter-wiki-links
  "return url links to other wiki's (wikivoyage, wikibooks etc) for the given
  page."
  [page]
  (into #{} ["http://en.wikivoyage.org/wiki/Montreal" 
             "http://en.wikibooks.org/wiki/Montreal"]))

