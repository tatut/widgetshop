(ns widgetshop.api
  (:require [compojure.core :refer [defroutes context GET POST]]
            [compojure.coercions :refer [as-int]]
            [yesql.core :refer [defqueries]]
            [cheshire.core :as json]
            [clojure.java.jdbc :as jdbc]))

(defonce ^:private api-db-connection (atom nil))

(defqueries "widgetshop/api/products.sql")
(defqueries "widgetshop/api/sales.sql")

(defn- json-response [body]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string body)})

(defn- query-as-json [query-fn & params-and-values]
  (json-response (query-fn (into {}
                                 (map vec)
                                 (partition 2 params-and-values))
                           {:connection @api-db-connection})))

(defn- posted-json [req]
  (-> req :body
      slurp
      (json/parse-string keyword)))

(defn save-review [product-id {:keys [customer_name customer_email stars review]}]
  (jdbc/with-db-transaction [db @api-db-connection]
    (let [customer (create-customer<! {:name customer_name
                                       :email customer_email}
                                      {:connection db})]
      (create-product-review<! {:customer (:id customer)
                                :product product-id
                                :stars stars
                                :review review}
                               {:connection db})
      ;; Return updated product 
      (json-response
       (first (product-by-id {:product product-id}
                             {:connection db}))))))

(defroutes api-routes
  ;; Routes for the shop
  (GET "/products/:category" [category :<< as-int]
       (query-as-json list-products-by-category :category category))
  (GET "/products" []
       (query-as-json list-products))
  (context "/product/:id" [id :<< as-int]
           (GET "/reviews" []
                (query-as-json list-product-reviews :p id))
           (POST "/reviews" request
                 (save-review id (posted-json request))))

  ;; Routes for the sales dashboard
  (GET "/categories" []
       (query-as-json list-categories))
  (GET "/sales" []
       (query-as-json list-sales))
  (GET "/sales/:category" [category :<< as-int]
       (query-as-json list-sales-by-category :category category)))


(defn start [db]
  (reset! api-db-connection db))
