(ns kalar-plugins.templates.page-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [kalar-plugins.templates.page :as page]))

(deftest tests
  (testing "Internationalize pages."
    (is (= (#'page/internationalize  '("foo.en.md" "foo.md" "bar.md") '(:en))
           '{:default ("foo.md" "bar.md"), :en ("bar.md" "foo.en.md")})))

  (testing "Compile pages."
    (is (= (#'page/compile-pages  (.listFiles (io/file (io/resource "pages"))) #{"en"})
           nil))))



(defn page-template [md] (str md))
