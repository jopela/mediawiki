(defproject mediawiki "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.9.1"]
                 [cheshire "5.3.1"]
                 [org.clojure/tools.cli "0.3.1"]]
  :main ^:skip-aot mediawiki.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
