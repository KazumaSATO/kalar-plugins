(ns tamaki.template.page
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [clojure.string :as str]
            [markdown.core :as md]
            [tamaki.page.page :as tpage]
            [clojure.string :as string]
            [net.cgrand.enlive-html :as ehtml]
            [tamaki-core.config :as config]
            [tamaki.lwml.markdown :as tmd]
            [tamaki-core.file :as tfile])
  (:import (java.io StringWriter StringReader)
           (java.text SimpleDateFormat)))

(def ^:private date-formatter (SimpleDateFormat. "yyyy-MM-dd"))

(defn page-seq
  "Returns the page files."
  ([page-dir]
   (filter #(fs/file? %) (file-seq page-dir)))
  ([] (page-seq (io/file (:page-dir (config/read-config))))))



(defn- write-page [page]
  (let [output (io/file (:dest (config/read-config)) (string/replace (-> page :metadata :link) #"^/" ""))
        template  (-> page :metadata :template)]
    (tfile/create-empty-file output)
    (require (symbol (str/replace  template #"/.*"  "")))
    (spit output ((var-get (resolve (symbol template))) page))))

(defn compile-mds
  ([page-root-dir]
   (doseq [md (-> page-root-dir io/file .listFiles)]
     (let [loaded (-> md .getAbsolutePath tmd/read-md)
           mod-loaded (tpage/render-page loaded)]
       (write-page mod-loaded))))
  ([] (compile-mds (:page-dir (config/read-config)))))

(defn build-postlink
  "Renders the path of a raw text file into the link of the html generated from the raw text."
  ([raw-file prefix]
   (letfn [(build-link [filename] (string/replace filename ; without extension
                                                  #"(\d{4})-(\d{1,2})-(\d{1,2})-(.+)$"
                                                  "/$1/$2/$3/$4.html"))]
     (let [html-uri (build-link (fs/name raw-file))]
       (str prefix html-uri))))
  ([raw-file] (build-postlink raw-file "")))

(defn read-postmd
  "a returned value example is as follows:
    {:src \"path/to/the/raw/file\"}"
  ([path post-root]
    (letfn [(extract-date-from-filename [filename] (-> (re-seq #"^\d{4}-\d{1,2}-\d{1,2}" filename) first))]
      (let [md (tmd/read-md path)
            filename (-> path io/file .getName)
            link (build-postlink filename)
            output (str post-root link)]
        (assoc md :link link :output output :date (extract-date-from-filename filename)))))
  ([path] (read-postmd path (:dest (config/read-config)))))

(defn- create-neighbor-link [links]
  (map (fn [p n] {:previous-page p :next-page n})
       (cons nil (drop-last links))
       (concat (rest links) '(nil))))

(defn- append-neightbor-links [posts]
  (let [neighbor-links (create-neighbor-link (map #(:link %) posts))]
    (map (fn [m n] (merge m n)) posts neighbor-links)))

(defn load-post-excerpt [md]
  (let [compiled (read-postmd md)
        excerpt  (-> (ehtml/select (ehtml/html-resource (StringReader. (:body compiled))) [:p]) first ehtml/text)]
    (dissoc (assoc compiled :excerpt excerpt) :body)))

(defn load-recent-posts
  ([num post-dir]
   (let [mds (take num (-> (.listFiles (io/file post-dir)) reverse))]
     (map #(read-postmd (.getAbsolutePath %)) mds)))
  ([]
   (load-recent-posts (:recent-post-num (config/read-config)) (-> (config/read-config) :post-dir))))

(defn gen-paginate-page [dir]
  ^{:doc "dir is the directory where posts are put."}
  (letfn [(create-paginate-files [total paginate-url-pattern]
            (cons "index.html"
                  (map #(str/replace paginate-url-pattern #":num" (str %)) (range 2 (+ 1 total)))))]
    (let [num-per-page (:posts-per-page (config/read-config))
          excerpts (map #(load-post-excerpt (.getAbsolutePath %)) (remove #(re-seq #"^\..+" (.getName %))
                                                                          (-> dir io/file .listFiles reverse)) )
          parted-excerpts (partition-all num-per-page excerpts)
          output-files (create-paginate-files (count parted-excerpts) (:pagination-pattern (config/read-config)))
          neighbor-links (create-neighbor-link output-files)
          pages (map (fn [excerpt current-page neighbors] (merge {:posts excerpt} {:current-page current-page}  neighbors))
                      parted-excerpts
                      output-files
                      neighbor-links)
          template (:pagination-template (config/read-config))]
      (require (symbol (str/replace template  #"/.*"  "")))
      (doseq [page pages]
        (let [dst (str (:dest (config/read-config)) "/" (:current-page page))]
          (tfile/create-empty-file dst)
          (spit dst ((var-get (resolve template)) page)))))))

(defn compile-postmds [dir]
  (let [posts (map #(-> % .getAbsolutePath read-postmd) (reverse (.listFiles (io/file dir))))
        posts-with-neighbors (append-neightbor-links posts)]
      (doseq [post posts-with-neighbors]
        (let [template (-> post :metadata :template)
              output (str (:dest (config/read-config)) (:link post))]
          (tfile/create-empty-file output)
          (require (symbol (str/replace template  #"/.*"  "")))
          (spit output ((var-get (resolve (symbol template))) post))))))
