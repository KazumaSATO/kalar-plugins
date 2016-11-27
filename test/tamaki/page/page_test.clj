(ns tamaki.page.page-test
 (:require [clojure.test :refer :all]
           [me.raynes.fs :as fs]
           [tamaki.page.page :as p]))

(deftest compile-page
  (testing "Copy."
    (let [build-dir (-> "foo" fs/temp-dir fs/absolute)
          page-dir "dev-resources/tamaki/page"]
      (p/compile-pages page-dir build-dir)
      (is (fs/file? (fs/file build-dir "about/index.html"))))))

(defn page-template [args] (str args))
