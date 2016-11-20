(ns tamaki-core.hook)


(defn invoke [hook-name plugins]
  "TODO write test"
  (doseq [plugin plugins]
    (let [hook (str plugin ".hook.hook")]
      (require (symbol hook))
      (let [func (resolve (symbol (str  hook "/" hook-name)))]
        (if-not (nil? func) ((var-get func)))))))
