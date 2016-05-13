(ns pz-services.checkers
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as j]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [org.httpkit.client :as client])
  (:import [com.amazonaws.services.s3 AmazonS3Client]
           [com.amazonaws.auth BasicAWSCredentials]
           [com.amazonaws.services.s3.model ObjectMetadata]
           [java.util Properties]
           [org.apache.kafka.clients.producer KafkaProducer ProducerRecord]
           [java.net InetAddress]))

(defn s3 [{:keys [bucket access_key_id secret_access_key]}]
  (log/debugf "s3: %s" bucket)
  (every? true?
   [(try
      (->
       (AmazonS3Client. (BasicAWSCredentials. access_key_id secret_access_key))
       (.putObject bucket
                   "test.txt"
                   (io/input-stream (.getBytes "hello"))
                   (doto (ObjectMetadata.) (.setContentLength (count (.getBytes "hello")))))
       .getContentMd5
       string?)
      (catch Exception e
        (log/errorf "Error putting s3 %s: %s" bucket (.getMessage e))))
    (try
      (->
       (AmazonS3Client. (BasicAWSCredentials. access_key_id secret_access_key))
       (.getObject bucket "test.txt")
       .getObjectContent
       slurp
       string?)
      (catch Exception e
        (log/errorf "Error requesting s3 %s: %s" bucket (.getMessage e))))
    (try
      (->
       (AmazonS3Client. (BasicAWSCredentials. access_key_id secret_access_key))
       (.deleteObject bucket "test.txt")
       nil?)
      (catch Exception e
        (log/errorf "Error deleting s3 %s: %s" bucket (.getMessage e))))]))

(defn http [url]
  (log/debug url)
  (try
    (let [response @(client/get url {:timeout 1500})]
      (= 200 (:status response)))
    (catch Exception e
      (log/errorf "Error requesting %s: %s" url (.getMessage e)))))

(defn postgres [db]
  (log/debugf "postgres: %s" db)
  (try
    (when-let [results (seq (j/query db ["SELECT true"]))]
      (-> results first :bool))
    (catch Exception e
      (log/errorf "Error retrieving test data: %s" (.getMessage e)))))

(defn ping [host]
  (log/debugf "ping: %s" host)
  (try
    (.isReachable (InetAddress/getByName host) 1500)
    (catch Exception e
      (log/errorf "Error pinging %s: %s" host (.getMessage e)))))

(defn- as-properties
  [m]
  (let [props (Properties.)]
    (doseq [[n v] m] (.setProperty props n v))
    props))

(defn kafka-producer [address]
  (let [conf {"bootstrap.servers" address
              "acks" "all"
              "retries" "0"
              "batch.size" "0"
              "linger.ms" "1"
              "buffer.memory" "33554432"
              "key.serializer" "org.apache.kafka.common.serialization.StringSerializer"
              "value.serializer" "org.apache.kafka.common.serialization.StringSerializer"}]
    (KafkaProducer. (as-properties conf))))

(defn kafka [producer]
  (let [record (ProducerRecord. "pz-services-test" "hello")]
    (try
      @(.send producer record)
      true
      (catch Exception e
        (log/errorf "Error connection to kafka: %s" (.getMessage e))))))
