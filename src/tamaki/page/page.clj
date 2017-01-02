(ns tamaki.page.page
  (:require [clojure.java.io :as io]
            [tamaki.lwml.lwml :as lwml]
            [me.raynes.fs :as fs]
            [clojure.string :as str]
            ))

(defn- write-page [page config]
  (let [output (:output page)
        template  (-> page :metadata :template)]
    (-> output fs/parent fs/mkdirs)
    (require (symbol (str/replace  template #"/.*"  "")))
    (spit output ((var-get (resolve (symbol template))) page config))))

(defn- compile-page [page context build-dir compiler-map]
  (letfn [(render-page [page]
            "Renders a html page model from a model of lightweight markup language text."
            (let [metadata (:metadata page)
                  link (str/replace (str context "/" (-> metadata :link first)) #"[/]+" "/")]
              (assoc page :metadata (assoc metadata :link link)
                          :output (fs/file build-dir (str/replace (-> metadata :link first) #"^/" "")))))]
    (render-page (lwml/compile-lwmlfile page compiler-map))))

(defn compile-pages [config]
  (let [page-dir (:pages config)
        build (:build config)
        context (:context config)
        renderers (:renderers config)]
    (doseq [pagefile (filter #(fs/file? %) (-> page-dir fs/file file-seq))]
      (write-page (compile-page pagefile context build renderers) config))))

