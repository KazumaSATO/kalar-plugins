(ns tamaki.post.post-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [tamaki.post.post :as tpost]))

(deftest loading-posts
  (testing "find the post files "
    (let [filenames (map #(fs/base-name %)  (tpost/post-seq (io/file "dev-resources/tamaki/post/recursive")))]
      (is (= '("2016-05-9-post.md" "2016-04-08-childpost.md")  filenames)))))

(def ^:private post-dir "dev-resources/tamaki/post/similarity")
(def ^:private post-dest "dev-resources/_site")

(defn-  find-res [path]
  (str "dev-resources/tamaki/template/" path))

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
    (is '({:post "dev-resources/tamaki/post/similarity/2015-01-29-lorem-ipsum.md", :score 0.02195880056341621}
          {:post "dev-resources/tamaki/post/similarity/2015-05-28-lorem-ipsum.md", :score 0.02153783977434482})
        (tpost/read-similar-post "dev-resources/_report/similarity.edn"
                                 "dev-resources/tamaki/post/similarity/2015-01-19-lorem-ipsum.md"))))

(deftest post-tests
  (testing "Load a mardkwon file for a post."
    (let [post (#'tpost/read-postmd  (find-res "posts/2011-12-01-post.md"))]
      (is (= (:link post) "/2011/12/01/post.html"))
      (is (= "dev-resources/tamaki/template/posts/2011-12-01-post.md" (:src post))))))

(deftest compling-post
  (testing "build the uri of a post"
    (let [uri (tpost/build-postlink "2016-03-19-foobar.md")]
      (is "2016/03/19/foobar.html" uri))))

(deftest compile-test
  (testing "create map"
    (let [compiled (tpost/compile-posts "http://localhost"
                                        "posts"
                                        "build"
                                        "dev-resources/tamaki/post/compile"
                                        {:md "tamaki.lwml.markdown/read-md"})]
      (is (some? compiled))))
  (testing "pagenation"
    (let [posts (tpost/compile-posts "http://localhost"
                                        "posts"
                                        "build"
                                        "dev-resources/tamaki/post/compile"
                                        {:md "tamaki.lwml.markdown/read-md"})]
      (is (some? (#'tpost/gen-pagenate posts 1 "page:num.html" "build" "http://localhost:8080"))))))