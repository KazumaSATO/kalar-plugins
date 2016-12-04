(ns tamaki.post.post
  (:require [tamaki-core.config :as config]
            [me.raynes.fs :as fs]
            [tamaki.lwml.markdown :as tmd]
            [tamaki.text.html :as thtml]
            [clojure.string :as string]
            [tamaki.lwml.lwml :as lwml]
            [tamaki.text.similarity :as simi]
            [net.cgrand.enlive-html :as ehtml]
            [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:import (java.io StringReader)))
; TODO
(defn build-postlink
  "Deprecated. Renders the path of a raw text file into the link of the html generated from the raw text."
  ([raw-file prefix]
   (letfn [(build-link [filename] (string/replace filename ; without extension
                                                  #"(\d{4})-(\d{1,2})-(\d{1,2})-(.+)$"
                                                  "/$1/$2/$3/$4.html"))]
     (let [html-uri (build-link (fs/name raw-file))]
       (str prefix html-uri))))
  ; TODO deprecated
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

(defn post-seq
  "Returns the post files."
  ([post-dir]
   (letfn [(extract-date [filename] (first (re-seq #"^\d{4}-\d{1,2}-\d{1,2}" filename)))]
     (reverse (sort-by #(extract-date (fs/base-name %))
                       (filter #(and (fs/file? %) (nil? (re-seq #"^\..*$" (fs/base-name %)))) (file-seq post-dir))))))
  ; TODO delete
  ([] (post-seq (io/file (:post-dir (config/read-config))))))

(defn calc-post-similarity
  ([] (calc-post-similarity (-> (config/read-config) :post-dir) (-> (config/read-config) :dest)))
  ([post-dir dest-root]
   (letfn [(path-body [post]
             (let [path (.getPath post)
                   md (read-postmd path dest-root)
                   body (thtml/extract-text (ehtml/html-resource (StringReader. (:body md))))]
               {:key path  :text body}))
           ]
     (let [key-texts (map #(path-body %) (post-seq (io/file post-dir)))]
       (for [key-text key-texts]
         {:post (:key key-text)
          :relation (map #(assoc {} :post (:key %) :score (:score %))
                         (simi/calc-similarity (:text key-text) (remove #(= (:key key-text) (:key %)) key-texts)))})))))

(def ^:private similarity-report "similarity.edn")

(defn report-post-similarity
  ([dest post-dir post-dest]
   (let [similarities (calc-post-similarity post-dir post-dest)]
     (spit dest (pr-str similarities))))
  ([]
   (let [similarities (calc-post-similarity)
         report-dir (io/file (-> (config/read-config) :report-dir))]
     (fs/mkdirs report-dir)
     (spit (io/file report-dir similarity-report) (pr-str similarities)))))

(defn read-similar-post
  ([report post]
    (let [similarity (edn/read-string (slurp report))
          relation (filter #(= (-> (:post %) io/file .getAbsolutePath) (-> post io/file .getAbsolutePath)) similarity)]
      (-> relation first :relation)))
  ([post]
   (let [report (io/file (:report-dir (config/read-config)) similarity-report)]
     (read-similar-post report post)
     )))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn compile-posts [root-url post-prefix dest post-dir compiler-map]
  (letfn [(convert-filename [ml-filename] (string/replace ml-filename
                                                          #"(\d{4})-(\d{1,2})-(\d{1,2})-(.+)\.[^\.]+$"
                                                          "$1/$2/$3/$4.html"))
          (chain-urls [urls] (map (fn [p c n] {:previous p :current c :next n})
                                  (cons nil (drop-last urls))
                                  urls
                                  (concat (rest urls) '(nil))))
          (make-post-format [basename]
            (let [suffix (convert-filename basename)
                  dest (str dest "/" (if (= post-prefix "") "" (str post-prefix "/")) suffix)]
              {:suffix suffix :dest dest}))
          ]
    (let [postfiles (-> post-dir io/file post-seq)
          compiled (filter #(some? %) (map #(lwml/compile-lwmlfile (fs/absolute %) compiler-map) postfiles))
          neighbors (chain-urls (map #(str root-url
                                           "/"
                                           (if (= post-prefix "") "" (str post-prefix "/"))
                                           (-> (:src %) fs/base-name convert-filename))
                                     compiled))]
          (map (fn [comp ne] (merge comp ne  (make-post-format (fs/base-name (:src comp))))) compiled neighbors))))


;(defn- create-excedprt [])













































