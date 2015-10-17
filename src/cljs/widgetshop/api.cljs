(ns widgetshop.api
  "Defines the widgetshop server interface"
  (:require [ajax.core :as ajax]
            [cljs.core.async :refer [<! >! chan close!]]
            [cljs-time.format :as tf])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn GET [url & parameter-names-and-values]
  (let [response-ch (chan)]
    (ajax/GET url
              {:response-format :json
               :keywords? true
               :handler #(go (>! response-ch %))
               :error-handler (fn [{:keys [status status-text]}]
                                (.log js/console "Got " status ": " status-text)
                                (close! response-ch))
               :params (into {}
                             (map vec)
                             (partition 2 parameter-names-and-values))})
    response-ch))

(defn POST [url payload]
  (let [response-ch (chan)]
    (ajax/POST url
              {:response-format :json
               :keywords? true
               :content-type :json
               :format :json
               :handler #(go (>! response-ch %))
               :error-handler (fn [{:keys [status status-text]}]
                                (.log js/console "Got " status ": " status-text)
                                (close! response-ch))
               :params payload})
    response-ch))


(defn list-products
  ([]
   (GET "/products"))
  ([category]
   (GET (str "/products/" (:id category)))))

(defn list-product-reviews [id]
  (GET (str "/product/" id "/reviews")))

(defn save-review [product-id review]
  (POST (str "/product/" product-id "/reviews")
        review))

(defn list-categories []
  (GET "/categories"))

(def json-date-format (tf/formatter "yyyy-MM-dd'T'HH:mm:ss'Z'"))

(defn parse-date-xf [field]
  (map #(assoc % field (tf/parse json-date-format (get % field)))))

(defn list-sales []
  (go (into []
            (parse-date-xf :purchase_date)
            (<! (GET "/sales")))))

(defn list-sales-by-category [category-id]
  (go (into []
            (parse-date-xf :purchase_date)
            (<! (GET (str "/sales/" category-id))))))
