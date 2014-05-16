(ns mediawiki.core
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]]))

(def queue-default "mediawiki_service_queue")
(def cli-opts 
  [["-u" 
    "--username USERNAME" 
    "username used to connect to the message broker."]
   ["-p"
    "--password PASSWORD"
    "password used to connect to the messabe broker."]
   ["-h"
    "--host HOST"
    "hostname of the message broker."]
   ["-q"
    "--queue"
    (format "name of the queue the service will take RPC calls from.
            Default is %s" queue-default)
    :default queue-default]])



(defn -main
  [& args]
  (println "Hello, World!"))
