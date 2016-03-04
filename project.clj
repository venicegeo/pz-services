(defproject services-refapp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.374" :exclusions [org.clojure/core.memoize]]
                 [clj-time "0.11.0"]
                 [org.clojure/data.json "0.2.6"]
                 [compojure "1.4.0"]
                 [http-kit "2.1.19"]
                 [javax.servlet/servlet-api "2.5"]
                 [ring-middleware-format "0.7.0" :exclusions [commons-codec]]
                 [ring/ring-core "1.4.0" :exclusions [joda-time]]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [org.postgresql/postgresql "9.4.1208"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/slf4j-api "1.7.18"]
                 [org.slf4j/log4j-over-slf4j "1.7.18"]
                 [org.slf4j/jcl-over-slf4j "1.7.18"]
                 [ch.qos.logback/logback-classic "1.1.6"]
                 [clj-kafka "0.3.4" :exclusions [zookeeper-clj log4j org.apache.zookeeper/zookeeper]]
                 [zookeeper-clj "0.9.4" :exclusions [log4j]]]
  :main ^:skip-aot services-refapp.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
