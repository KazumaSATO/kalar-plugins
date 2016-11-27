(ns tamaki.page.page
  (:require [clojure.java.io :as io]
            [tamaki.lwml.markdown :as tmd]
            [me.raynes.fs :as fs]
            [clojure.string :as str]
            ))

(defn render-page [txt-map]
  "TODO make this private. Renders a html page model from a model of lightweight markup language text."
  (let [metadata (:metadata txt-map)]
    (assoc txt-map :metadata (assoc metadata :link (-> metadata :link first)))
    ))

(defn- write-page [page build-dir]
  (let [output (io/file build-dir (str/replace (-> page :metadata :link) #"^/" ""))
        template  (-> page :metadata :template)]
    (-> output fs/parent fs/mkdirs)
    (require (symbol (str/replace  template #"/.*"  "")))
    (spit output ((var-get (resolve (symbol template))) page))))

(defn compile-pages [page-dir build-dir]
  "XXX ignore dot files and support subdirectories"
  (doseq [md (-> page-dir io/file .listFiles)]
    (let [loaded (-> md .getAbsolutePath tmd/read-md)
          mod-loaded (render-page loaded)]
      (write-page mod-loaded build-dir))))