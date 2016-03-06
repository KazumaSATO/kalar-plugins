(ns tamaki.post.post-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [me.raynes.fs :as rfs]
            [tamaki.post.post :as post]))

(deftest loading-posts
  (testing "find the post files "
    (let [filenames (map #(rfs/base-name %)  (post/post-seq (io/file "dev-resources/tamaki/post")))]
      (is (= #{"post.md" "childpost.md"} (set filenames))))))