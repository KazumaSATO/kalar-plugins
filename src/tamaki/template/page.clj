(ns tamaki.template.page
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [markdown.core :as md]
            [clojure.string :as string]
            [clojure.set :as set]
            [net.cgrand.enlive-html :as ehtml]
            [kalar-core.config :as config]
            [clojure-csv.core :as csv]
            [tamaki-core.file :as tfile])
  (:import (java.io StringWriter StringReader)
           (java.text SimpleDateFormat)))

(def ^:private date-formatter (SimpleDateFormat. "yyyy-MM-dd"))

(defn- load-md
  "Load a markdown file."
  ([md]
    (let [input (new StringReader (slurp md))
          output (new StringWriter)
          metadata (md/md-to-html input output :parse-meta? true :heading-anchors true)
          body (.toString output)]
      (merge {:body body :src md} {:metadata (merge metadata {:title (-> metadata :title first)
                                                      :link (-> metadata :link first)
                                                      :template (-> metadata :template first)})}))))

(defn- write-page [page]
  (let [output (io/file (:dest (config/read-config)) (string/replace (-> page :metadata :link) #"^/" ""))
        template  (-> page :metadata :template)]
    (tfile/create-empty-file output)
    (require (symbol (str/replace  template #"/.*"  "")))
    (spit output ((var-get (resolve (symbol template))) page))))

(defn- read-page
  ([page] (let [loaded-md  (load-md page)]
            (assoc loaded-md :link (first (:link loaded-md))
                             :template (first (:template loaded-md)))))
  ([page lang-cd]
    (let [loaded-md (load-md (name page))]
      (assoc loaded-md :link (str "/" (name lang-cd) (first (:link loaded-md)))
                       :template (first (:template loaded-md))))))
(defn- internationalize
  [filenames langs]
  (let [regex-of-lang-cds #"\.(en)\.[^\.]+$"
        default-files (remove (fn [filename] (re-seq regex-of-lang-cds filename)) filenames)
        i18-files (reduce (fn [acc lang]
                            (merge acc {(keyword lang)
                                        (filter
                                          (fn [filename]
                                            (re-seq (re-pattern (str "\\." (name lang) "\\.[^\\.]+$"))
                                                    filename))
                                          filenames)}))
                          {}
                          langs)]
    (into {:default default-files}
          (first
            (map (fn [lang files]
                   (let [multi-lang-files (map (fn [file] (string/replace  file #"\.\w{2}\.([^\.]+)$" ".$1")) files)
                         uni-lang-files (into () (set/difference (set default-files)  (set multi-lang-files)))]
                     {lang (into files uni-lang-files)}))
                 (keys i18-files)
                 (vals i18-files))))
    ))

(defn- compile-pages
  ([files i18n]
   (let [pages-per-lang (internationalize (map #(.getAbsolutePath %) files) (map #(keyword %) i18n))
         read-pages (reduce (fn [acc lang] (into acc (map (fn [page] (read-page page lang)) (get pages-per-lang lang))))
                            (map #(read-page %) (:default pages-per-lang))
                            (keys (dissoc pages-per-lang :default)))]
     (doseq [page read-pages] (write-page page))))
  ([]
   (compile-pages (.listFiles (io/file (:page-dir (config/read-config)))) (:i18n (config/read-config)))))

(defn load-markdown
  ([^String file]
   (letfn
     [(extract-date-from-filename [filename]
        (-> (re-matcher  #"^\d{4}-\d{1,2}-\d{1,2}" filename) re-find format-date))
      (format-date [date-str] (.parse date-formatter date-str))
      (build-dest [filename]
        (string/replace filename #"(\d{4})-(\d{1,2})-(\d{1,2})-(.+)\.(md|markdown)$" "$1/$2/$3/$4.html"))]

     (let [input    (new StringReader (slurp file))
           output   (new StringWriter)
           filename (.getName (io/file file))
           date     (extract-date-from-filename filename)
           metadata (md/md-to-html input output :parse-meta? true :heading-anchors true)
           html     (.toString output)
           url (if (nil? (-> metadata :link first))
                 (str (:journal-path (config/read-config)) "/" (build-dest filename))
                 (-> metadata :link first))
           dest-file (io/file  (string/replace url #"^/" ""))]
       (merge metadata {:body html :url url :dest-file dest-file :date date :src file})))))











(defn load-md-excerpt [^String md]
  (let [compiled (load-markdown md)
        excerpt  (-> (ehtml/select (ehtml/html-resource (StringReader. (:body compiled))) [:p]) first ehtml/text)]
    (dissoc (merge compiled {:excerpt excerpt}) :body)))

(defn load-recent-posts [num]
  (let [posts-dir (-> (config/read-config) :posts-dir)
        mds (take num (-> (.listFiles (io/file posts-dir)) reverse))]
    (map #(load-markdown (.getAbsolutePath %)) mds)))

(defn load-related-posts [filename num]
  (let [related (csv/parse-csv (slurp ".__related_posts"))
        posts-dir (-> (config/read-config) :posts-dir)]
    (map #(load-markdown (.getAbsolutePath (io/file posts-dir %)))
         (take num (rest (first (filter (fn [line] (re-find (re-pattern (str (first line) "$")) filename))
                                        related)))))))

(defn- compile-md [file]
  (let [file-path (.getAbsolutePath file)
        md (load-markdown file-path)
        dst (:dest-file md)
        fnc (-> md :template first)]
    (tfile/create-empty-file dst)
    (require (symbol (str/replace fnc  #"/.*"  "")))
    (spit dst ((var-get (resolve (symbol fnc))) md))))

(defn- create-neighbor-url [urls]
  "REFACTORING create current-page"
  (map (fn [p n] {:previous-page p :next-page n})
       (cons nil (drop-last urls))
       (concat (rest urls) '(nil))))

(defn- compile-serial-mds [dir]
  (let [files (reverse (.listFiles (io/file dir)))
        mds (map #(load-markdown (.getAbsolutePath %)) files)
        neighbor-urls (create-neighbor-url (map #(:url %) mds))
        mds-with-neighbors (map (fn [m n] (merge m n)) mds neighbor-urls)]
    (dorun
      (for [md mds-with-neighbors]
        (let [func (-> md :template first)]
          (tfile/create-empty-file (:dest-file md))
          (require (symbol (str/replace func  #"/.*"  "")))
          (spit (:dest-file md) ((var-get (resolve (symbol func))) md))
          )))))

(defn- gen-paginate-page [dir]
  ^{:doc "dir is the directory where posts are put."}
  (letfn [(create-paginate [total paginate-url-pattern]
            (cons "index.html"
                  (map #(str/replace paginate-url-pattern #":num" (str %)) (range 2 (+ 1 total)))))]
    (let [paginate (:paginate (config/read-config))
          ;;
          mds (map #(load-md-excerpt (.getAbsolutePath %)) (-> dir io/file .listFiles reverse))
          mdchunks (partition-all paginate mds)
          paginate (create-paginate (count mdchunks) (:paginate-path (config/read-config)))
          paginate2 (create-neighbor-url paginate)
          result (map (fn [md p pn] (merge {:posts md} {:current-page p}  pn)) mdchunks paginate paginate2)
          func (:paginate-template (config/read-config))]
      (require (symbol (str/replace func  #"/.*"  "")))
      (dorun
        (for [chunk result]
          (let [dst  ((:dest (config/read-config))  (:current-page chunk))]
            (tfile/create-empty-file dst)
            (spit dst ((var-get (resolve func)) chunk))))))))

(defn load-plugin []
  (gen-paginate-page (:posts-dir (config/read-config)))
  (dorun
    (for [file (.listFiles (io/file (:page-dir (config/read-config))))]
      (compile-md file)))
  (compile-serial-mds (:posts-dir (config/read-config))))
