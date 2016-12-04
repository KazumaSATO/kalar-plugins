(ns tamaki.post.post
  (:require [me.raynes.fs :as fs]
            [clojure.string :as string]
            [tamaki.lwml.lwml :as lwml]
            [net.cgrand.enlive-html :as ehtml]
            [clojure.java.io :as io])
  (:import (java.io StringReader)))

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

(defn- compile-posts [root-url post-prefix dest post-dir compiler-map]
  (letfn [(convert-filename [ml-filename] (string/replace ml-filename
                                                          #"(\d{4})-(\d{1,2})-(\d{1,2})-(.+)\.[^\.]+$"
                                                          "$1/$2/$3/$4.html"))
          (build-dest [basename]
            (let [suffix (convert-filename basename)]
                  (str dest "/" (if (= post-prefix "") "" (str post-prefix "/")) suffix)))]
    (let [postfiles (-> post-dir io/file post-seq)
          compiled (filter #(some? %) (map #(lwml/compile-lwmlfile (fs/absolute %) compiler-map) postfiles))
          neighbors (chain-urls (map #(str root-url
                                           "/"
                                           (if (= post-prefix "") "" (str post-prefix "/"))
                                           (-> (:src %) fs/base-name convert-filename))
                                     compiled))]
          (map (fn [comp ne] (merge comp ne  {:dest (build-dest (fs/base-name (:src comp)))} )) compiled neighbors))))

(defn- gen-pagenate [posts postnum-per-page pagenate-url-pattern build-dir root-url]
  (letfn [(create-pagenation [total pagenate-url-pattern]
            (let [suffixes (cons "index.html"
                                 (map #(string/replace pagenate-url-pattern #":num" (str %))
                                      (range 2 (+ 1 total))))
                  chained-urls (chain-urls (map #(str root-url "/" %) suffixes))]
              (map (fn [chained-urls suffix]
                     (assoc chained-urls :output (str build-dir "/" suffix)))
                   chained-urls
                   suffixes)))
          (create-excerpt [html-text]
            (-> (ehtml/select (ehtml/html-resource (StringReader. html-text)) [:p]) first ehtml/text))]

    (let [pagenate-pages (partition-all postnum-per-page (map #(assoc % :excerpt (-> % :body create-excerpt)) posts))]
      (map (fn [pagenate posts-per-page]
             (assoc pagenate :posts posts-per-page))
           (create-pagenation (count pagenate-pages) pagenate-url-pattern)
           pagenate-pages))))













































