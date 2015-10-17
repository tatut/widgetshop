(ns widgetshop.sales
  "Widgetshop sales data."
  (:require [reagent.core :refer [atom] :as r]
            [cljs.core.async :refer [<! >! chan]]
            [widgetshop.format :as fmt]
            [cljs-time.core :as t]
            [cljs-time.coerce :as c]
            [cljs-time.format :as tf]
            [widgetshop.api :as api]
            [widgetshop.visualization :as vis]
            [clojure.string :as str])
  (:require-macros [reagent.ratom :refer [reaction run!]]
                   [cljs.core.async.macros :refer [go]]))


;; Distinct categories of all products
(defonce categories (let [categories (atom nil)]
                      (go (reset! categories
                                  (into {}
                                        (map (juxt :id identity))
                                        (<! (api/list-categories)))))
                      categories))

(defonce products
  (let [products (atom nil)]
    (go
      (reset! products
              (into {}
                    (map (juxt :id identity))
                    (<! (api/list-products)))))
    products))

(defonce selected-category-id (atom nil))

(defn product-name [id]
  (:name (get @products id)))

(defn category-name [id]
  (:name (get @categories id)))

(def sales-data
  (let [sales (atom nil)]
    (run! (let [category-id @selected-category-id]
            (go 
              (reset! sales
                      (<! (if (str/blank? category-id)
                            (api/list-sales)
                            (api/list-sales-by-category category-id)))))))
    sales))

(def sales-by-month
  (reaction (let [sales (group-by (fn [s]
                                    (fmt/year-and-month (:purchase_date s)))
                                  @sales-data)]
              (into []
                    (map (juxt first
                               #(reduce + (map :price (second %)))))
                    (sort-by first (seq sales)))))) 
              


;; Define the sales data columns from left to right,
;; each item has keys :label, :get  and optional :fmt to
;; specify custom formatting
(def sales-columns
  [
   ;; The date of this sale
   {:label "Date" :width "12%" :get :purchase_date :fmt fmt/date}

   ;; Product is shown as the name of the product
   {:label "Product" :width "25%" :get :product :fmt product-name} 

   ;; The quantity of the products sold
   {:label "Quantity" :width "9%" :get :quantity}

   ;; Unit price (formatted as euros)
   {:label "Unit price" :width "11%" :get #(/ (:price %) (:quantity %)) :fmt fmt/euros}

   ;; Total price of sale
   {:label "Total" :width "11%" :get :price :fmt fmt/euros}

   ])

   

(defn sales-row
  "Component for a single sales row."
  [columns item]
  ^{:key (hash item)}
  [:tr
   (doall
    (for [{:keys [label get fmt]} columns]
      ^{:key label}
      [:td ((or fmt str) (get item))]))])

  
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
          (doall 
           (for [item data] ;; generate this for each item
             (sales-row columns item)))]))]]])

(defn category-selection []
  [:select {:value @selected-category-id
            :on-change #(reset! selected-category-id (-> % .-target .-value))}
   [:option {:value ""} "All categories"]
   (for [{:keys [id name]} (vals @categories)]
     ^{:key id}
     [:option {:value id} name])])

(defn sales
  "Main component of our sales page"
  []
  [:span.salesPage
   [category-selection]
   [sales-listing sales-columns @sales-data]
   [:div.salesCount
    (count @sales-data)
    " sales worth "
    (fmt/euros (reduce + (map :price @sales-data)))
    ]
   
   [vis/bars {:width 500 :height 150
              :ticks [["75k" 75000]
                      ["50k" 50000]
                      ["25k" 10000]
                      ["10k" 10000]
                      ["1k" 1000]
                      ["500" 500]]
                       
              :color-fn (fn [[_ value]]
                          (cond
                           (> value 50000) "green"
                           (> value 25000) "#FFCC00"
                           :default "red"))}
    @sales-by-month]
   (when (str/blank? @selected-category-id)
     [vis/pie {:width 200 :height 200 :radius 70}
      (into []
            (map (fn [[category-id sales]]
                   [(category-name category-id)
                    (reduce + (map :price sales))]))
            (seq (group-by :category @sales-data)))])
   ])


(defn render []
  (r/render [sales] (.getElementById js/document "widgetshop-app")))

(defn ^:export start []
  (render))

