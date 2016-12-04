(ns tamaki.page.page
  (:require [clojure.java.io :as io]
            [tamaki.lwml.lwml :as lwml]
            [me.raynes.fs :as fs]
            [clojure.string :as str]
            ))

(defn- write-page [page]
  (let [output (:output page)
        template  (-> page :metadata :template)]
    (-> output fs/parent fs/mkdirs)
    (require (symbol (str/replace  template #"/.*"  "")))
    (spit output ((var-get (resolve (symbol template))) page))))

(defn- compile-page [page build-dir compiler-map]
  "XXX ignore dot files and support subdirectories"
  (letfn [(render-page [page]
            "Renders a html page model from a model of lightweight markup language text."
            (let [metadata (:metadata page)]
              (assoc page :metadata (assoc metadata :link (-> metadata :link first))
                             :output (fs/file build-dir (str/replace (-> page :metadata :link first) #"^/" "")))))]
    (render-page (lwml/compile-lwmlfile page compiler-map))))

(defn compile-pages [page-dir build-dir compiler-map]
  (doseq [pagefile (filter #(fs/file? %) (-> page-dir fs/file file-seq))]
    (write-page (compile-page pagefile build-dir compiler-map))))

