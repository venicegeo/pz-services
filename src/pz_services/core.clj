(ns pz-services.core
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]
            [org.httpkit.server :as http]
            [pz-services.config :as config]
            [pz-services.checkers :as check]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.util.response :as r])
  (:gen-class))

(defn- render [data]
  (-> data
      (json/write-str :key-fn name)
      r/response
      (r/content-type "application/json")))

(let [services (config/get-services)
      zk-client (-> services :pz-zookeeper :host check/zk-connect)
      kafka-producer (-> services :pz-kafka :host check/kafka-producer)]
  (defn- status [req]
    (log/debugf "request: %s" (:remote-addr req))
    (render
     {:postgres (-> services config/get-db-config check/postgres)
      :s3 (-> services :pz-blobstore :bucket check/s3)
      :geoserver-s3 (-> services :pz-geoserver :s3 :bucket check/s3)
      :kafka (check/kafka kafka-producer)
      :elasticsearch (check/http (format "http://%s" (-> services :pz-elasticsearch :host)))
      :geoserver (check/http (format "http://%s:%s/geoserver/web"
                                     (-> services :pz-geoserver :geoserver :hostname)
                                     (-> services :pz-geoserver :geoserver :port)))})))

(defroutes all-routes
  (GET "/" [] status))

(def app
  (-> all-routes
      wrap-content-type))

(defn- startup []
  (log/info "initiailizing")
  (http/run-server app {:port (java.lang.Integer. (or (System/getenv "PORT") 3333))}))

(defn -main [& args]
  (startup))
