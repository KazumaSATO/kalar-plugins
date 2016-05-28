(defproject tamaki "0.2.1.2"
  :description "Tamaki, a static site generator"
  :url "https://github.com/satokazuma/tamaki"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/mit-license.php"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.ranceworks/nanao_2.11 "1.0.1"]
                 [clojure-csv "2.0.1"]
                 [me.raynes/fs "1.4.6"]
                 [markdown-clj "0.9.82"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.lesscss/lesscss "1.7.0.1.1"]
                 [enlive "1.1.6"]]
  :profiles {:dev {:resource-paths ["dev-resources"]}})
