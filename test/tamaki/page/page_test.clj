(ns tamaki.page.page-test
 (:require [clojure.test :refer :all]
           [me.raynes.fs :as fs]
           [tamaki.page.page :as p]))

(deftest compile-page
  (let [build-dir (fs/absolute (fs/temp-dir "build"))
        compilers {:md "tamaki.lwml.markdown/read-md"}
        page (#'p/compile-page
                "dev-resources/tamaki/page/test.md"
                "/blog"
                build-dir
               compilers)]
    (testing "compile pages"
      (is (contains? page :body))
      (is (= (-> page :metadata :link) "/blog/about/index.html"))
      (is (= (page :output fs/absolute) (-> (str build-dir "/about/index.html") fs/file fs/absolute))))
    (testing "write compiled pages"
      (let [build-dir (-> "build" fs/temp-dir fs/absolute)]
        (p/compile-pages {:pages "dev-resources/tamaki/page"
                          :context ""
                          :build build-dir
                          :renderers compilers})
        (is (fs/exists? (fs/file build-dir "about/index.html")))))))


(defn page-template [args config] (str args))
