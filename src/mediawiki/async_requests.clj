(ns mediawiki.async-requests)

(defn geocoords
  "returns a map of the geocoordinates of the pages. Puts nil in the 
  map if the page has no geocoordinates.

  Example:
  (geocoords ['http://en.wikipedia.org/wiki/Montreal'
              'http://fr.wikipedia.org/wiki/Paris'])
  -> [[45 -73.1] [48 2.35]]"
  [pages]
  nil)


