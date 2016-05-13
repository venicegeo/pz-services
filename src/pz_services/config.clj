(ns pz-services.config
  (:require [clojure.data.json :as json]
            [clojure.string :as string]
            [clojure.tools.logging :as log]))

(def defaults
  {:pz-kafka {:host "127.0.0.1:9092"
              :hostname "127.0.0.1"
              :port "9092"}
   :pz-elasticsearch {:host "127.0.0.1:9200"
                      :hostname "127.0.0.1"
                      :port "9200"}
   :pz-geoserver {:geoserver {:hostname "gsn-geose-LoadBala-15ZS4UFEZIERA-2099032436.us-east-1.elb.amazonaws.com"
                              :port "80"}
                  :s3 {:bucket "gsn-s3-test-geoserver-test"
                       :access_key_id "AKIAJ4ADPZGNOGUUFIBQ"
                       :secret_access_key "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"}
                  :postgres {:hostname "127.0.0.1"
                             :port "3000"
                             :database "vhjeswsgv6okntcp"
                             :username "dhzbaqemc08im6rd"
                             :password "xpsksvtuz5erbsfm"}}
   :pz-blobstore {:bucket "gsn-s3-test-app-blobstore-test"
                  :access_key_id "AKIAI5D2QWAJ6T6B7Q2Q"
                  :secret_access_key "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"}})

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

(defn get-db-config [{:keys [pz-geoserver]}]
  {:subprotocol "postgresql"
   :user (-> pz-geoserver :postgres :username)
   :password (-> pz-geoserver :postgres :password)
   :subname (format "//%s:%s/%s"
                    (-> pz-geoserver :postgres :hostname)
                    (-> pz-geoserver :postgres :port)
                    (-> pz-geoserver :postgres :database))})
