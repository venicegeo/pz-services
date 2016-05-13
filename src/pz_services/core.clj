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
  (let [data* (if (map? data) data {:result data})]
    (-> data
        (json/write-str :key-fn name)
        r/response
        (r/content-type "application/json"))))

(let [services (config/get-services)
      kafka-producer (-> services :pz-kafka :host check/kafka-producer)]

  (defn- blobstore [req]
    (-> services :pz-blobstore check/s3))

  (defn- elasticsearch [req]
    (check/http (format "http://%s" (-> services :pz-elasticsearch :host))))

  (defn- geoserver [req]
    (check/http (format "http://%s:%s/geoserver/web"
                        (-> services :pz-geoserver :geoserver :hostname)
                        (-> services :pz-geoserver :geoserver :port))))
  (defn- geoserver-s3 [req]
    (-> services :pz-geoserver :s3 check/s3))

  (defn- kafka [req]
    (check/kafka kafka-producer))

  (defn- ping [req]
    (check/ping (-> services :pz-kafka :hostname)))

  (defn- postgres [req]
    (-> services config/get-db-config check/postgres))

  (defn- all [req]
    (log/debugf "request: %s" (:remote-addr req))
    (render
     {
      :blobstore     (blobstore req)
      :geoserver     (geoserver req)
      :geoserver-s3  (geoserver-s3 req)
      :elasticsearch (elasticsearch req)
      :kafka         (kafka req)
      :ping          (ping req)
      :postgres      (postgres req)})))

(defroutes all-routes
  (GET "/"              [] all)
  (GET "/blobstore"     [] blobstore)
  (GET "/elasticsearch" [] elasticsearch)
  (GET "/geoserver"     [] geoserver)
  (GET "/geoserver-s3"  [] geoserver-s3)
  (GET "/kafka"         [] kafka)
  (GET "/ping"          [] ping)
  (GET "/postgres"      [] postgres))

(def app
  (-> all-routes
      wrap-content-type))

(defn- startup []
  (log/info "initiailizing")
  (http/run-server app {:port (java.lang.Integer. (or (System/getenv "PORT") 3333))}))

(defn -main [& args]
  (startup))
