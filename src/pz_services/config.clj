(ns pz-services.config
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]))

(def defaults
  {:pz-postgres {:host "127.0.0.1"
                 :port "3000"
                 :database "pgdb"
                 :username "refuser"
                 :password "kpnAQMU2Zd972qVF"}
   :pz-ping {:host "127.0.0.1"}
   :pz-geoserver {:host "geoserver.piazzageo.io" :port "80"}
   :pz-blobstore {:bucket "pz-blobstore-staging"}})

(defn- parse-vcap [vcap-str]
  (let [vcap (-> vcap-str (json/read-str :key-fn keyword) :user-provided)]
    (log/debugf "string: %s" vcap-str)
    (log/debugf "parsed: %s" vcap)
    (reduce
     (fn [m {:keys [name credentials]}]
       (assoc m (keyword name) credentials))
     {}
     vcap)))

(defn get-services []
  (let [vcap (System/getenv "VCAP_SERVICES")]
    (if (string/blank? vcap)
      defaults
      (parse-vcap vcap))))

(defn get-db-config [{:keys [pz-postgres]}]
  {:subprotocol "postgresql"
   :user (:username pz-postgres)
   :password (:password pz-postgres)
   :subname (format "//%s:%s/%s" (:host pz-postgres) (:port pz-postgres) (:database pz-postgres))})
