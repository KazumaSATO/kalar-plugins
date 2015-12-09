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
  "REFACTORING create current-page"
  (map (fn [p n] {:previous-page p :next-page n})
       (cons nil (drop-last urls))
       (concat (rest urls) '(nil))))

(defn- compile-serial-mds [dir]
  (let [files (.listFiles (io/file dir))
        mds (map #(hp/load-markdown (.getAbsolutePath %)) files)
        neighbor-urls (create-neighbor-url (map #(:url %) mds))
        mds-with-neighbors (map (fn [m n] (merge m n)) mds neighbor-urls)
        ]
    (println mds-with-neighbors)
    ))

(defn- gen-paginate-page [dir]
  (letfn [(create-paginate [total paginate-url-pattern]
            (cons "index.html"
                  (map #(str/replace paginate-url-pattern #":num" (str %)) (range 2 (+ 1 total)))))]
    (let [paginate (:paginate (config/read-config))
          mds (map #(hp/load-md-excerpt (.getAbsolutePath %)) (.listFiles (io/file dir)))
          mdchunks (partition-all paginate mds)
          paginate (create-paginate (count mdchunks) (:paginate-path (config/read-config)))
          paginate2 (create-neighbor-url paginate)
          result (map (fn [md p pn] (merge {:posts md} {:current-page p}  pn)) mdchunks paginate paginate2)
          func (:paginate-template (config/read-config))]
      (require (symbol (str/replace func  #"/.*"  "")))
      (dorun
        (for [chunk result]
          (let [dst  (kfile/get-dst  (:current-page chunk))]
            (kfile/touch dst)
            (spit dst ((var-get (resolve func)) chunk))))))))

(plugin/defkalar-plugin
  page
  plugin/KalarPlugin
  (load-plugin
    [this]
    (gen-paginate-page (:posts-dir (config/read-config)))
    (dorun
      (for [file (.listFiles (io/file (:page-dir (config/read-config))))]
        (compile-md file)))
    (compile-serial-mds (:posts-dir (config/read-config)))
    ))


