(ns tamaki.css.css-test
  (:require [clojure.test :refer :all]
            [me.raynes.fs :as fs]
            [clojure.string :as string]
            [tamaki.css.css :as css]
            [clojure.java.io :as io]))

(deftest test-css
  (testing "Compiles a sass file."
    (is (= '("dest/dev-resources/tamaki/css/css/dir/css.css")
           (#'css/resolve-sty "dev-resources/tamaki/css/css/dir" "dest"))
    (is (= '("dest/dev-resources/tamaki/css/css/css.css")
           (#'css/resolve-sty "dev-resources/tamaki/css/css/css.css" "dest"))))))
