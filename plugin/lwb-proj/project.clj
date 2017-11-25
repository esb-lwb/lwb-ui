(defproject proto-no-proj "0.1.0-SNAPSHOT"
  :description "A leiningen project to use for lwb."
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [proto-repl-charts "0.3.1"]
                 [proto-repl "0.3.1"]]

  :resource-paths ["lib/lwb.jar"]
  :profiles
  {:dev {:source-paths ["dev" "src"]}})
