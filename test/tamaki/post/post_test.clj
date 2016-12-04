(ns tamaki.post.post-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [tamaki.post.post :as tpost]))

(deftest loading-posts
  (testing "find the post files "
    (let [filenames (map #(fs/base-name %)  (#'tpost/post-seq (io/file "dev-resources/tamaki/post/recursive")))]
      (is (= '("2016-05-9-post.md" "2016-04-08-childpost.md")  filenames)))))

(deftest compile-test
  (testing "create map"
    (let [compiled (#'tpost/compile-posts "http://localhost"
                                        "posts"
                                        "build"
                                        "dev-resources/tamaki/post/compile"
                                        {:md "tamaki.lwml.markdown/read-md"})]
      (is (some? compiled))))
  (testing "pagenation"
    (let [posts (#'tpost/compile-posts "http://localhost"
                                        "posts"
                                        "build"
                                        "dev-resources/tamaki/post/compile"
                                        {:md "tamaki.lwml.markdown/read-md"})]
      (is (some? (#'tpost/gen-pagenate posts 1 "page:num.html" "build" "http://localhost:8080"))))))