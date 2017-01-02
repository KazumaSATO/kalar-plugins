(ns tamaki.config)


(def config
  (letfn [(cat [tail] (symbol "tamaki.hook" tail))]
    {:renderers {:md "tamaki.lwml.markdown/read-md"}
     :build "build"
     :context ""
     :pages "resources/pages"
     :hooks {:clean [(cat "clean")]
             :validate [(cat "validate")]
             :initialize [(cat "initialize")]
             :process-assets [(cat "process-assets")]
             :render [(cat "render")]}}))
