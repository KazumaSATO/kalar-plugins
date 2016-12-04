(ns tamaki.page.page-test
 (:require [clojure.test :refer :all]
           [me.raynes.fs :as fs]
           [tamaki.page.page :as p]))

(deftest compile-page
  (testing "compile pages"
    (let [page-dir "dev-resources/tamaki/page"
          pages (#'p/compile-pages page-dir "build" {:md "tamaki.lwml.markdown/read-md"})
          page (first pages)]
      (is (contains? page :body))
      (is (= (-> page :metadata :link) "/about/index.html"))
      (is (= (page :output fs/absolute) (-> (str "build/about/index.html") fs/file fs/absolute))))))

(defn page-template [args] (str args))
