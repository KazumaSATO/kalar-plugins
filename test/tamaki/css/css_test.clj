(ns tamaki.css.css-test
  (:require [clojure.test :refer :all]
            [me.raynes.fs :as fs]
            [clojure.string :as string]
            [tamaki.css.css :as css]
            [clojure.java.io :as io]))

(deftest test-css
  (testing "Compiles a sass file."
    (is (= '({:src "dev-resources/tamaki/css/css/dir/sass.sass"
              :dest "dest/tamaki/css/css/dir/sass.css"})
           (#'css/resolve-sty "dev-resources/tamaki/css/css/dir" "dest")))
    (is (= '({:src "dev-resources/tamaki/css/css/css.css"
              :dest "dest/tamaki/css/css/css.css"})
           (#'css/resolve-sty "dev-resources/tamaki/css/css/css.css" "dest"))))
  (testing "Compiles stylesheets including sass and css."
    (let [dest (str (fs/temp-dir ""))]
      (#'css/compile-styles #{"dev-resources/tamaki/css/css/dir"
                               "dev-resources/tamaki/css/css/css.css"} dest)
      (is (= 2
             (count (map #(.getPath %)
                         (filter #(and (fs/file? %) (re-seq  #"\.css$" (str %)))  (file-seq (io/file dest))))))))))
