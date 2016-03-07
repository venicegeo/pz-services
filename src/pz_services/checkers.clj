(ns pz-services.checkers
  (:require [clojure.data.json :as json]
            [clojure.java.jdbc :as j]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [org.httpkit.client :as client])
  (:import [com.amazonaws.services.s3 AmazonS3Client]
           [com.amazonaws.services.s3.model]
           [java.net InetAddress]))

(defn s3 [bucket]
  (log/debugf "s3: " bucket)
  (try
    (->
     (AmazonS3Client.)
     (.getObject bucket "refapp.json")
     .getObjectContent
     slurp
     string/trim
     json/read-str
     map?)
    (catch Exception e
      (log/errorf "Error requesting s3 %s: %s" bucket (.getMessage e)))))

(defn http [url]
  (log/debugf "http: " url)
  (try
    (= 200 (:status @(client/get (format "http://%s/geoserver/web" url) {:timeout 1500})))
    (catch Exception e
      (log/errorf "Error requesting %s: %s" url (.getMessage e)))))

(defn ping [host]
  (log/debugf "ping: " host)
  (try
    (.isReachable (InetAddress/getByName host) 1500)
    (catch Exception e
      (log/errorf "Error pinging %s: %s" host (.getMessage e)))))

(defn postgres [db]
  (log/debugf "postgres: " db)
  (try
    (when-let [results (seq (j/query db ["SELECT true"]))]
      (-> results first :bool))
    (catch Exception e
      (log/errorf "Error retrieving test data: %s" (.getMessage e)))))
