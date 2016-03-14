(ns tamaki.post.post
  (:require [tamaki-core.config :as config]
            [me.raynes.fs :as rfs]
            [clojure.java.io :as io]))


(defn post-seq
  "return the post files"
  ([post-dir]
   (filter #(rfs/file? %) (file-seq post-dir)))
  ([] (post-seq (io/file (:post-dir (config/read-config))))))
