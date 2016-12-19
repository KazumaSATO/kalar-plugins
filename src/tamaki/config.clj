(ns tamaki.config)


(def config
  (letfn [(cat [tail] (symbol "tamaki.hook" tail))]
    {:renderers {:md ""}
     :build "build"
     :pages "pages"
     :hooks {:clean [(cat "clean")]
             :validate [(cat "validate")]
             :initialize [(cat "initialize")]
             :process-assets [(cat "process-assets")]
             :render [(cat "render")]}}))
