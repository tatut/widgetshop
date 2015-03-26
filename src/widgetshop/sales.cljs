(ns widgetshop.sales
  "Widgetshop sales data."
  (:require [reagent.core :refer [atom] :as r]
            [widgetshop.format :as fmt]
            [figwheel.client :as fw]
            [cljs-time.core :as t]
            [cljs-time.coerce :as c]
            
            [widgetshop.visualization :as vis]
            [widgetshop.dummy])
  (:require-macros [reagent.ratom :refer [reaction run!]]))

;; This atom holds the complete sales data in vector form
(defonce sales-data (atom nil))

(defn sale-total [s]
  (* (:qty s) (:price (:product s))))

;; Define the sales data columns from left to right,
;; each item has keys :label, :get  and optional :fmt to
;; specify custom formatting
(def sales-columns
  [
   ;; The date of this sale
   {:label "Date" :width "12%" :get :date :fmt fmt/date}

   ;; Product is shown as the name of the product
   {:label "Product" :width "25%" :get :product :fmt :name} 

   ;; Category is inside the product data
   {:label "Category" :width "10%" :get :product :fmt :category}

   ;; The quantity of the products sold
   {:label "Quantity" :width "9%" :get :qty}

   ;; Unit price (formatted as euros)
   {:label "Unit price" :width "11%" :get #(:price (:product %)) :fmt fmt/euros}

   ;; Total price of sale
   {:label "Total" :width "11%" :get sale-total :fmt fmt/euros}

   ;; Tax%
   {:label "Tax%" :width "11%" :get :tax :fmt fmt/percent}
   
   ;; Tax amount
   {:label "Tax" :width "11%"
    :get #(* (/ (:tax %) 100.0)
             (* (:qty %) (:price (:product %)))) :fmt fmt/euros}
   ])


(defn sales-row
  "Component for a single sales row."
  [columns item]
  ^{:key (hash item)}
  [:tr
   (for [{:keys [label get fmt]} columns]
     ^{:key label}
     [:td ((or fmt str) (get item))])])
  
(defn sales-listing
  "Component that lists all sales data."
  [columns data]
  [:span

   [:table.table.sales-head
    ;; Header just has each sales column
    [:thead
     [:tr
      (for [{:keys [label width]} columns]
        ^{:key label}
        [:th {:width width} label])]]]
   
   ;; at the top level, the listing is a table
   [:div.sales-data-container
    [:table.table.table-striped.sales-data
     [:thead
      [:tr
       (for [{:keys [label width]} columns]
         ^{:key label}
         [:th {:height 0 :width width}])]]

     ;; table body contains all sales rows
     (if (nil? data)
       ;; If data is nil, it hasn't been loaded yet...
       [:tbody.loading
        [:tr [:td {:colSpan (count columns)}
              "Loading..."]]]
      
       (if (empty? data)
         ;; Otherwise if data is empty, there just aren't any sales
         [:tbody.noSales
          [:tr [:td {:colSpan (count columns)}
                "No sales."]]]

         ;; We have non-empty sales data, output body with rows for each
         [:tbody
          (for [item data] ;; generate this for each item
            (sales-row columns item))]))]]])

(defn sales
  "Main component of our sales page"
  []
  [:span.salesPage
   [sales-listing sales-columns @sales-data]
   [:div.salesCount
     (count @sales-data) " sales"]])

(defn generate-dummy-sales []
  (reset! sales-data
          (into []
                (sort-by #(c/to-long (:date %))
                         (repeatedly 500 widgetshop.dummy/generate)))))

(defn render []
  (r/render [sales] (.getElementById js/document "widgetshop-app")))

(defn ^:export start []
  (render)
  ;; Simulate a "slow" server, generate data after 2 seconds
  (js/setTimeout generate-dummy-sales 2000))



(fw/watch-and-reload
 :jsload-callback (fn [] (render)))
