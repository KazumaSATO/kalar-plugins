(ns tamaki.post.post-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [tamaki.post.post :as post]))

(deftest loading-posts
  (testing "find the post files "
    (is (some? (post/post-seq (io/file "dev-resources/tamaki/post"))))))