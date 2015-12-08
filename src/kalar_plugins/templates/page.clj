(ns kalar-plugins.templates.page
  (:require [kalar-core.plugin :as plugin]
            [kalar-plugins.templates.hiccup :as hp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [kalar-core.config :as config]
            [kalar-core.file :as kfile]
            [hiccup.page :as hpage]))


(defn- compile-md [file]
  (let [file-path (.getAbsolutePath file)
        md (hp/load-markdown file-path)
        dst (kfile/get-dst (str/replace file-path #"\..*$" ".html"))
        fnc (-> md :template first)]
    (kfile/touch dst)
    (require (symbol (str/replace fnc  #"/.*"  "")))
    (spit dst ((var-get (resolve (symbol fnc))) md))))

(plugin/defkalar-plugin
  page
  plugin/KalarPlugin
  (load-plugin
    [this]
    (dorun
      (for [file (.listFiles (io/file (:page-dir (config/read-config))))]
        (compile-md file)))))


