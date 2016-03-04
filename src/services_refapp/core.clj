(ns services-refapp.core
  (:require [clojure.data.json :as json]
            [clojure.java.jdbc :as j]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]
            [org.httpkit.server :as http]
            [org.httpkit.client :as client]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.util.response :as r])
  (:import [com.amazonaws.services.s3 AmazonS3Client]
           [com.amazonaws.services.s3.model])
  (:gen-class))

(def defaults {:pz-postgres {:host "127.0.0.1"
                             :port "3000"
                             :database "pgdb"
                             :username "refuser"
                             :password "kpnAQMU2Zd972qVF"}
               :pz-geoserver {:host "geoserver.piazzageo.io" :port "80"}
               :pz-blobstore {:bucket "pz-blobstore-staging"}})

(defn- extract-creds [vcap]
  (reduce
   (fn [m {:keys [name credentials]}]
     (assoc m (keyword name) credentials))
   {}
   vcap))

(def services
  (let [vcap (System/getenv "VCAP_SERVICES")]
    (if (string/blank? vcap)
      defaults
      (-> vcap (json/read-str :key-fn keyword) :user-provided extract-creds))))

(let [{:keys [pz-postgres]} services]
  (def db
    {:subprotocol "postgresql"
     :user (:username pz-postgres)
     :password (:password pz-postgres)
     :subname (format "//%s:%s/%s" (:host pz-postgres) (:port pz-postgres) (:database pz-postgres))}))

(defn s3-test [bucket]
  (log/infof "Accessing: %s" bucket)
  (try
    (.getObjectContent (.getObject (AmazonS3Client.) bucket "refapp.json"))
    (catch Exception e
      (log/errorf "Error requesting s3 test: %s" (.getMessage e)))))

(defn http-test [url]
  (log/infof "Requesting: %s" url)
  (try
    (:status @(client/get (format "http://%s/geoserver/web" url) {:timeout 300}))
    (catch Exception e
      (log/errorf "Error requesting %s: %s" url (.getMessage e))
      "{}")))

(http-test "geoserver.piazzageo.io")

(defn postgres-test []
  (try
    (vec
     (j/query
      db
      ["select * from refapp"]))
    (catch Exception e
      (log/errorf "Error retrieving test data: %s" (.getMessage e)))))

(defn- render [data]
  (-> data
      (json/write-str :key-fn name)
      r/response
      (r/content-type "application/json")))

(defn checkall [req]
  (log/infof " - request - %s" (:remote-addr req))
  (render {:db (postgres-test)
           :s3 (-> services :pz-blobstore :bucket s3-test slurp string/trim json/read-str)
           :geoserver (http-test (-> services :pz-geoserver :host))}))

(defroutes all-routes
  (GET "/" [] checkall))

(def app
  (-> all-routes
      wrap-content-type))

(defn- startup []
  (log/info "initiailizing")
  (http/run-server app {:port (java.lang.Integer. (or (System/getenv "PORT") 3333))}))

(defn -main [& args]
  (startup))
