(defproject tamaki "0.3.0.2-SNAPSHOT"
  :description "Tamaki, a static site generator"
  :url "https://github.com/satokazuma/tamaki"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/mit-license.php"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [me.raynes/fs "1.4.6"]
                 [markdown-clj "0.9.91"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.8"]
                 [enlive "1.1.6"]
                 [compojure "1.5.1"]]

  :exclusions [org.slf4j/slf4j-simple]
  :profiles {:dev {:resource-paths ["dev-resources"]}})
