(ns tamaki.post.post-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [tamaki.post.post :as tpost]))

(deftest loading-posts
  (testing "find the post files "
    (let [filenames (map #(fs/base-name %)  (tpost/post-seq (io/file "dev-resources/tamaki/post/recursive")))]
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
        (is (= 3 (count from-txt))))))
  (testing "read similarity report"
    (is (= ({:post "dev-resources/tamaki/post/similarity/2015-01-29-lorem-ipsum.md"
             :score 0.02195880056341621}
             {:post "dev-resources/tamaki/post/similarity/2015-05-28-lorem-ipsum.md",
              :score 0.02153783977434482})
           (tpost/read-similar-post "dev-resources/_report/similarity.edn"
                                    "dev-resources/tamaki/post/similarity/2015-01-19-lorem-ipsum.md")))))