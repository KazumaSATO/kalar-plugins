(ns tamaki.post.post-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [tamaki.post.post :as tpost])
  (:import (java.text SimpleDateFormat)))

(deftest loading-posts
  (testing "find the post files "
    (let [filenames (map #(fs/base-name %)  (#'tpost/post-seq (io/file "dev-resources/tamaki/post/recursive")))]
      (is (= '("2016-05-9-post.md" "2016-04-08-childpost.md")  filenames)))))

(deftest compile-test
  (let [build (-> "build" fs/temp-dir fs/absolute)
        site-root "/"
        post-prefix "posts"
        post-dir "dev-resources/tamaki/post/compile"
        renderers {:md "tamaki.lwml.markdown/read-md"}
        posts (#'tpost/compile-posts
                site-root
                post-prefix
                build
                post-dir
                renderers)
        post (first posts)]
    (testing "create map"
        (is (= (.parse (new SimpleDateFormat "yyyy-MM-dd") "2015-05-28") (:date post)))
        (is (= "/posts/2015/05/28/foobar3.html" (:current post)))
        (is (= "/posts/2015/01/29/foobar2.html" (:next post)))
        (is (= (str build "/posts/2015/05/28/foobar3.html") (:output post))))

    (testing "pagenation"
      (let [pages (#'tpost/gen-pagenate posts 1 "page:num.html" build "/")
            page2 (nth pages 1)]
        (is (= "/index.html" (:previous page2)))
        (is (= "/page2.html" (:current page2)))
        (is (= (str build "/page2.html") (:output page2)))))
    (testing "write"
      (tpost/write-posts {:context site-root
                          :post-context post-prefix
                          :build build
                          :posts post-dir
                          :renderers renderers
                          :pagenate-url "page:num.html"
                          :postnum-per-page 1
                          :pagenate-template "tamaki.post.post-test/write-doc"})
      (is (fs/exists? (fs/file build "index.html")))
      (is (fs/exists? (fs/file build "page2.html")))
      (is (fs/exists? (fs/file build "page3.html")))
      (is (fs/exists? (fs/file build "posts"))))))

(defn write-doc [doc config] (str doc))
