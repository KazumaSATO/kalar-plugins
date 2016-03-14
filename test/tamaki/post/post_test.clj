(ns tamaki.post.post-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [tamaki.post.post :as tpost]
            [tamaki.post.post :as post]))

(deftest loading-posts
  (testing "find the post files "
    (let [filenames (map #(fs/base-name %)  (post/post-seq (io/file "dev-resources/tamaki/post/recursive")))]
      (is (= #{"post.md" "childpost.md"} (set filenames))))))

(def ^:private post-dir "dev-resources/tamaki/post/similarity")
(def ^:private post-dest "dev-resources/_site")

(deftest test-calc-post-similarity

  (testing "calculation"
    (let [result  (tpost/calc-post-similarity post-dir post-dest)]
      (doseq [entity result]
        (is (fs/exists? (:post entity))))))

  (testing "write similarity"
    (let [dir "dev-resources/_report/"
          written (str dir "similarity.edn")]
      (fs/mkdirs dir)
      (tpost/report-post-similarity written post-dir post-dest)
      (let [from-txt (clojure.edn/read-string (slurp written))]
        (is (= 3 (count from-txt)))))))