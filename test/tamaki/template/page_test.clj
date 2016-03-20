(ns tamaki.template.page-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [tamaki.template.page :as page]
            [clojure.string :as string]
            [tamaki-core.config :as config]))


(def ^:private test-md (-> "tamaki/template/pages/test.md" io/resource io/file) )

(defn-  find-res [path]
  (str "dev-resources/tamaki/template/" path))

(defn page-template [md] (str md))

(deftest tests
  (testing "Load markdown."
    (let [loaded (#'page/load-md  (-> test-md  .getAbsolutePath))]
      (is (some? (-> loaded :metadata :title)))
      ))

  (testing "Write a html file about a map generated from a markdown file."
    (let [loaded (#'page/load-md (-> test-md  .getAbsolutePath))
          mod (#'page/read-page loaded)]
      (is (= nil (#'page/write-page mod)))))

  (testing "Complile the markdowns for the pages."
    (is (= nil (#'page/compile-mds (-> test-md
                                       .getParentFile
                                       .getAbsolutePath))))))


(deftest post-tests
  (testing "Load a mardkwon file for a post."
    (let [post (#'page/read-postmd  (find-res "posts/2011-12-01-post.md"))]
      (is (= (:link post) "/2011/12/01/post.html"))
      (is (= "dev-resources/tamaki/template/posts/2011-12-01-post.md" (:src post)))
      ))

  (testing "Reads posts with the neighbor post links"
    (let [posts (map (fn [file] (let [path (-> file .getAbsolutePath)]
                                  (#'page/read-postmd path)))
                     (-> "posts" find-res io/file .listFiles))
          appended (#'page/append-neightbor-links posts)]
      (is (some? appended))))

  (testing "Load a post except."
    (is (some? (:excerpt (#'page/load-post-excerpt (find-res "posts/2011-12-01-post.md"))))))

  (testing "Load recent posts"
    (is (= 3 (-> (#'page/load-recent-posts 3 (find-res "posts"))  count))))

  (testing "Generate the paginate pages"
    (is (nil? (#'page/gen-paginate-page (find-res "posts")))))

   (testing "Generate the posts"
    (is (nil? (#'page/compile-postmds (find-res "posts")))))
  )

(deftest compling-post
  (testing "build the uri of a post"
    (let [uri (page/build-postlink "2016-03-19-foobar.md")]
      (is "2016/03/19/foobar.html" uri))))