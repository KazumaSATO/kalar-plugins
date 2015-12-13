(ns kalar-plugins.templates.page
  (:require [kalar-core.plugin :as plugin]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [markdown.core :as md]
            [clojure.string :as string]
            [net.cgrand.enlive-html :as ehtml]
            [kalar-core.config :as config]
            [kalar-core.file :as kfile])
  (:import (java.io StringWriter StringReader)
           (java.text SimpleDateFormat)))

(def ^:private date-formatter (SimpleDateFormat. "yyyy-MM-dd"))

(defn- format-date [date-str] (.parse date-formatter date-str))

(defn- extract-date-from-filename [filename]
  (-> (re-matcher  #"^\d{4}-\d{1,2}-\d{1,2}" filename) re-find format-date))


(defn load-markdown [^String file]
  (let [input    (new StringReader (slurp file))
        output   (new StringWriter)
        date     (extract-date-from-filename (.getName (io/file file)))
        metadata (md/md-to-html input output :parse-meta? true :heading-anchors true)
        html     (.toString output)
        url (if (nil? (-> metadata :link first))
              (string/replace
                (string/replace file (re-pattern (str "^" (kfile/find-resources-dir))) "")
                #"\..*$"
                ".html")
              (-> metadata :link first))
        dest-file (io/file (kfile/find-dest) (string/replace url #"^/" ""))]
    (merge metadata {:body html :url url :dest-file dest-file :date date})))

(defn load-md-excerpt [^String md]
  (let [compiled (load-markdown md)
        excerpt  (-> (ehtml/select (ehtml/html-resource (StringReader. (:body compiled))) [:p]) first ehtml/text)]
    (dissoc (merge compiled {:excerpt excerpt}) :body)))

(defn load-recent-posts [num]
  (let [posts-dir (-> (config/read-config) :posts-dir)
        mds (take num (-> (.listFiles (io/file posts-dir)) reverse))]
    (map #(load-markdown (.getAbsolutePath %)) mds)))

(defn- compile-md [file]
  (let [file-path (.getAbsolutePath file)
        md (load-markdown file-path)
        dst (:dest-file md)
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
        mds (map #(load-markdown (.getAbsolutePath %)) files)
        neighbor-urls (create-neighbor-url (map #(:url %) mds))
        mds-with-neighbors (map (fn [m n] (merge m n)) mds neighbor-urls)]
    (dorun
      (for [md mds-with-neighbors]
        (let [func (-> md :template first)]
          (kfile/touch (:dest-file md))
          (require (symbol (str/replace func  #"/.*"  "")))
          (spit (:dest-file md) ((var-get (resolve (symbol func))) md))
          )))))

(defn- gen-paginate-page [dir]
  (letfn [(create-paginate [total paginate-url-pattern]
            (cons "index.html"
                  (map #(str/replace paginate-url-pattern #":num" (str %)) (range 2 (+ 1 total)))))]
    (let [paginate (:paginate (config/read-config))
          mds (map #(load-md-excerpt (.getAbsolutePath %)) (-> dir io/file .listFiles reverse))
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


