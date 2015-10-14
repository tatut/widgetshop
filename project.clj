(defproject widgetshop "0.1.0-SNAPSHOT"
  :description "Reagent demo of a simple widget shop"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [figwheel "0.4.0"]
                 [reagent "0.5.1"]
                 [com.andrewmcveigh/cljs-time "0.3.14"]]
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
                       ;; FIXME: add production build
                       ]}

  
  )


