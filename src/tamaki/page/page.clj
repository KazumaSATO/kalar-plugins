(ns tamaki.page.page
  (:require [clojure.java.io :as io]
            [tamaki.lwml.lwml :as lwml]
            [me.raynes.fs :as fs]
            [clojure.string :as str]
            ))

(defn- write-page [page build-dir]
  (let [output (io/file build-dir (str/replace (-> page :metadata :link) #"^/" ""))
        template  (-> page :metadata :template)]
    (-> output fs/parent fs/mkdirs)
    (require (symbol (str/replace  template #"/.*"  "")))
    (spit output ((var-get (resolve (symbol template))) page))))

(defn- compile-pages [page-dir build-dir compiler-map]
  "XXX ignore dot files and support subdirectories"
  (letfn [(render-page [page]
            "Renders a html page model from a model of lightweight markup language text."
            (let [metadata (:metadata page)]
              (assoc page :metadata (assoc metadata :link (-> metadata :link first))
                             :output (fs/file build-dir (str/replace (-> page :metadata :link first) #"^/" "")))))]
    (for [md (-> page-dir io/file .listFiles)]
      (render-page (lwml/compile-lwmlfile md compiler-map)))))