(ns tamaki.post.post
  (:require [me.raynes.fs :as fs]
            [clojure.string :as string]
            [tamaki.lwml.lwml :as lwml]
            [net.cgrand.enlive-html :as ehtml]
            [clojure.java.io :as io])
  (:import (java.io StringReader)
           (java.text SimpleDateFormat)))

(defn- post-seq
  "Returns the post files."
  ([post-dir]
   (letfn [(extract-date [filename] (first (re-seq #"^\d{4}-\d{1,2}-\d{1,2}" filename)))]
     (reverse (sort-by #(extract-date (fs/base-name %))
                       (filter #(and (fs/file? %) (nil? (re-seq #"^\..*$" (fs/base-name %)))) (file-seq post-dir)))))))

(defn- chain-urls [urls] (map (fn [p c n] {:previous p :current c :next n})
                             (cons nil (drop-last urls))
                             urls
                             (concat (rest urls) '(nil))))

(defn- normalize-path [path] (string/replace path #"[/]+" "/"))

(def ^:private date-formatter (new SimpleDateFormat "yyyy-MM-dd"))

(defn- compile-posts [site-root post-prefix dest post-dir compiler-map]
  (letfn [(convert-filename [ml-filename] (string/replace ml-filename
                                                          #"(\d{4})-(\d{1,2})-(\d{1,2})-(.+)\.[^\.]+$"
                                                          "$1/$2/$3/$4.html"))
          (build-suffix [post-prefix basename] (str post-prefix "/" (convert-filename basename)))
          (extract-date [text-includes-date]
            (let [maybe-date (first (re-seq #"\d{4}-\d?\d-\d?\d" text-includes-date))]
              (if (some? maybe-date) (.parse date-formatter maybe-date))))]
    (let [postfiles (-> post-dir io/file post-seq)
          compiled (map #(assoc % :date (-> % :src fs/base-name extract-date))
                        (filter #(some? %) (map #(lwml/compile-lwmlfile (fs/absolute %) compiler-map) postfiles)))
          neighbors (chain-urls (map #(normalize-path
                                        (str site-root "/" (build-suffix post-prefix (fs/base-name (:src %)))))
                                     compiled))]
      (map (fn [comp ne]
             (merge
               comp
               ne ; neighbor
               {:output (normalize-path (str dest "/" (build-suffix post-prefix (fs/base-name (:src comp)))))}))
           compiled
           neighbors))))

(defn- gen-pagenate [posts postnum-per-page pagenate-url-pattern build-dir site-root]
  (letfn [(create-pagenation [total pagenate-url-pattern]
            (let [suffixes (cons "index.html"
                                 (map #(string/replace pagenate-url-pattern #":num" (str %))
                                      (range 2 (+ 1 total))))
                  chained-urls (chain-urls (map #(normalize-path (str site-root "/" %)) suffixes))]
              (map (fn [chained-urls suffix]
                     (assoc chained-urls :output (normalize-path (str build-dir "/" suffix))))
                   chained-urls
                   suffixes)))
          (create-excerpt [html-text]
            (-> (ehtml/select (ehtml/html-resource (StringReader. html-text)) [:p]) first ehtml/text))]

    (let [pagenate-pages (partition-all postnum-per-page (map #(assoc % :excerpt (-> % :body create-excerpt)) posts))]
      (map (fn [pagenate posts-per-page]
             (assoc pagenate :posts posts-per-page))
           (create-pagenation (count pagenate-pages) pagenate-url-pattern)
           pagenate-pages))))

(defn write-posts [config]
  (let [context (:context config)
        build (:build config)
        post-context (:post-context config)
        post-dir (:posts config)
        renderers (:renderers config)
        pagenate-url (:pagenate-url config)
        postnum-per-page (:postnum-per-page config)
        pagenate-template (:pagenate-template config)
        posts (compile-posts context post-context build post-dir renderers)]
     (doseq [post posts]
       (let [output (:output post)
             template (-> post :meta :template)]
         (-> output fs/parent fs/mkdirs)
         (require (symbol (string/replace  template #"/.*"  "")))
         (spit output ((var-get (resolve (symbol template))) post config))))
     (doseq [page (gen-pagenate posts postnum-per-page pagenate-url build context)]
       (let [output (:output page)]
         (-> output fs/parent fs/mkdirs)
         (require (symbol (string/replace  pagenate-template #"/.*"  "")))
         (spit output ((var-get (resolve (symbol pagenate-template))) page config))))))
