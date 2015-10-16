(defproject widgetshop "0.1.0-SNAPSHOT"
  :description "Reagent demo of a simple widget shop"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [figwheel "0.4.0"]
                 [reagent "0.5.1"]
                 [com.andrewmcveigh/cljs-time "0.3.14"]
                 [cljs-ajax "0.5.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 
                 [http-kit "2.1.18"]
                 [compojure "1.4.0"]
                 [org.postgresql/postgresql "9.4-1204-jdbc42"]
                 [yesql "0.5.1"]
                 [cheshire "5.5.0"]]
  
  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.4.0"]]

  :cljsbuild {:builds [{:id "dev"
                        :figwheel true
                        :source-paths ["src/cljs"]
                        :compiler {:optimizations :none
                                   :source-map true
                                   :preamble ["reagent/react.js"]
                                   :output-to "resources/public/js/compiled/widgetshop.js"
                                   :output-dir "resources/public/js/compiled/out"}}
                       {:id "prod"
                        :source-paths ["src/cljs"]
                        :compiler {:optimizations :advanced
                                   :output-to "resources/public/js/widgetshop.js"}}]}

  ;; Server side source 
  :source-paths ["src/clj"]
  :main widgetshop.main
  :repl-options {:port 4005}
  
  ;; Make alias "lein build" run the full cljs+clj compilation and produce an uberjar
  :aliases {"build" ["do" "clean," "deps," "compile," "cljsbuild" "once" "prod," "uberjar"]}
  
  )


