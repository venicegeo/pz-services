(ns services-refapp.core
  (:require [clojure.data.json :as json]
            [clojure.java.jdbc :as j]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]
            [org.httpkit.server :as http]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [ring.util.response :as r])
  (:gen-class))

(defn- keyed-data [data key]
  (reduce
   (fn [m q]
     (assoc m (keyword (key q)) q))
   {}
   data))

(def defaults {:hostname "127.0.0.1"
               :port "3000"
               :database "pgdb"
               :username "refuser"
               :password "kpnAQMU2Zd972qVF"})

(def services
  (let [vcap (System/getenv "VCAP_SERVICES")]
    (log/info vcap)
    (if (string/blank? vcap)
      defaults
      (-> vcap (json/read-str :key-fn keyword) :user-provided (keyed-data :name) :pz-postgres :credentials))))

(def db
  {:subprotocol "postgresql"
   :user (:username services)
   :password (:password services)
   :subname (format "//%s:%s/%s" (:host services) (:port services) (:database services))})

(defn select []
  (vec
   (j/query
    db
    ["select * from refapp"])))

(defn- render [data]
  (-> data
      (json/write-str :key-fn name)
      r/response
      (r/content-type "application/json")))

(defn checkdb [req]
  (log/infof " - request - %s" (:remote-addr req))
  (render (select)))

(defroutes all-routes
  (GET "/" [] checkdb))

(def app
  (-> all-routes
      wrap-content-type))

(defn- startup []
  (log/info "initiailizing")
  (log/info (select))
  (http/run-server app {:port (java.lang.Integer. (or (System/getenv "PORT") 3333))}))

(defn -main [& args]
  (startup))
