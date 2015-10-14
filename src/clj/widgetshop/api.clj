(ns widgetshop.api
  (:require [compojure.core :refer [defroutes GET POST]]
            [yesql.core :refer [defqueries]]
            [cheshire.core :as json]))

(def ^:private api-db-connection (atom nil))

(defqueries "widgetshop/api/products.sql")

(defn- query-as-json [query-fn & params-and-values]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string (query-fn (into {}
                                               (map vec)
                                               (partition 2 params-and-values))
                                         {:connection @api-db-connection}))})

(defroutes api-routes
  (GET "/products/:category" [c] (query-as-json list-products-by-category :category c)))


(defn start [db]
  (reset! api-db-connection db)
  api-routes)
