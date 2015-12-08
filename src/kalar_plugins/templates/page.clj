(ns kalar-plugins.templates.page
  (:require [kalar-core.plugin :as plugin]
            [kalar-plugins.templates.hiccup :as hp]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [kalar-core.config :as config]
            [kalar-core.file :as kfile]
            [hiccup.page :as hpage]))

(defn- get-dst-path [^String src-path]
  (kfile/get-dst (str/replace src-path #"\..*$" ".html")))

(defn- compile-md [file]
  (let [file-path (.getAbsolutePath file)
        md (hp/load-markdown file-path)
        dst (get-dst-path file-path)
        fnc (-> md :template first)]
    (kfile/touch dst)
    (require (symbol (str/replace fnc  #"/.*"  "")))
    (spit dst ((var-get (resolve (symbol fnc))) md))))

(defn- create-neighbor-url [urls]
  (map (fn [p n] {:previous-page p :next-page n})
       (cons nil (drop-last urls))
       (concat (rest urls) '(nil))))

(defn- compile-serial-mds [dir]
  (let [files (.listFiles (io/file dir))
        mds (map #(merge (hp/load-md-excerpt (.getAbsolutePath %))
                         {:url (get-dst-path (.getAbsolutePath %))})
                 files)
        neighbor-urls (create-neighbor-url (map #(.getAbsolutePath (:url %)) mds))
        mds-with-neighbors (map (fn [m n] (merge m n)) mds neighbor-urls)
        ]
    (comment (partition-all))
    (println mds-with-neighbors)
    ))

(plugin/defkalar-plugin
  page
  plugin/KalarPlugin
  (load-plugin
    [this]
    (dorun
      (for [file (.listFiles (io/file (:page-dir (config/read-config))))]
        (compile-md file)))
    (compile-serial-mds (:posts-dir (config/read-config)))
    ))


