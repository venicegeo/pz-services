(defproject pz-services "0.1.0"
  :description "Status check for Piazza backing services."
  :url "http://github.com/venicegeo/pz-services"
  :license {:name "Apache License 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[ch.qos.logback/logback-classic "1.1.6"]
                 [com.amazonaws/aws-java-sdk "1.10.58" :exclusions [joda-time]]
                 [compojure "1.5.0"]
                 [clj-kafka "0.3.4" :exclusions [org.apache.zookeeper/zookeeper]]
                 [http-kit "2.1.19"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring-middleware-format "0.7.0" :exclusions [commons-codec]]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.apache.curator/curator-framework "3.1.0"]
                 [org.postgresql/postgresql "9.4.1208"]
                 [org.slf4j/jcl-over-slf4j "1.7.18"]
                 [org.slf4j/log4j-over-slf4j "1.7.18"]
                 [org.slf4j/slf4j-api "1.7.18"]]
  :main pz-services.core
  :jvm-opts ["-server"]
  :profiles {:uberjar {:aot :all}})
