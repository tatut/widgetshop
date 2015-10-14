(ns widgetshop.main
  (:gen-class)
  (:require [org.httpkit.server :refer [run-server]]
            [widgetshop.api :as api]))

(defn read-settings-file [f]
  (binding [*read-eval* false]
    (read-string (slurp f))))


(defn start-server [{port :port} routes]
  (run-server routes
              {:port port}))

(defn -main [& args]
  (let [settings (read-settings-file
                  (or (first args) "settings.edn"))
        database (:database settings)
        api-routes (api/start database)
        server (start-server (:server settings) api-routes)]
    
    (println "Widgetshop Enterprise Edition started ;)")

    (.addShutdownHook (Runtime/getRuntime)
                      (Thread. #(do (println "Widgetshop Enterprise Edition shutting down")
                                    (server))))))

