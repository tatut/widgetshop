(ns widgetshop.main
  (:require [reagent.core :as reagent :refer [atom]]
            [widgetshop.bootstrap :as bs]
            [widgetshop.api :as api]
            [cljs.core.async :refer [<! >!]])
  (:require-macros [reagent.ratom :refer [reaction run!]]
                   [cljs.core.async.macros :refer [go]]))

(defonce products (let [p (atom nil)]
                    (go (reset! p (<! (api/list-products))))
                    p))


(defonce selected-product (atom nil))
(defonce selected-product-tab (atom 0))

(defn select-product!
  "Set the selected product and reset product view tab to details."
  [product]
  (reset! selected-product product)
  (reset! selected-product-tab 0))

(defn update-product! 
  "Update the product with the given id." 
  [product-id update-fn & args]
  (let [product-index (first (keep-indexed (fn [i p]
                                             (when (= product-id (:id p))
                                               i))
                                           @products))
        updated-product (nth (swap! products 
                                    update-in [product-index]
                                    (fn [p] (apply update-fn p args)))
                             product-index)]
    (when (= product-id (:id @selected-product))
      ;; If this product was selected, reset that too
      (reset! selected-product updated-product))))

(comment 
  ;; example of updating a product
  (js/setTimeout (fn [] (update-product! :q36 
                                         assoc :manufacturer "FOO"))
                 2000))


(defn format-price [amount]
  (if amount
    (str (.toFixed amount 2) " \u20AC")
    ""))

(defn products-list [products]
  [:table.table
   [:thead
    [:tr
     [:th "Name"]
     [:th "Price"]
     [:th "Avg. rating"]]]
   (let [selected @selected-product]
     (for [product @products]
       ^{:key (:id product)}
       [:tr {:on-click #(select-product! product)
             :class (when (= product selected)
                      "active")}
        [:td (:name product)]
        [:td (format-price (:price product))]
        (let [stars (map :stars (:reviews product))
              avg (/ (reduce + 0 stars) (count stars))]
          [:td 
           (if-let [avg (:average_rating product)]
             (for [s (range 0 (Math/round (:average_rating product)))]
               ^{:key s}
               [:span.glyphicon.glyphicon-star {:aria-hidden "true"}])
             "No reviews")])]))])


(defn product-details-view []
  (let [{:keys [name description price manufacturer]} @selected-product]
    [:dl
     [:dt "Name"]
     [:dd name]
     [:dt "Description"]
     [:dd description]
     [:dt "Price"]
     [:dd (format-price price)]
     [:dt "Manufacturer"]
     [:dd manufacturer]]))



(defn product-gallery-view []
  [:img {:src (str "img/" (:img @selected-product))}])


;; Holds the reviews of the currently selected product
;; whenever product is changed, the data is re-fetched from the server
(defonce reviews (let [reviews (atom nil)]
                   (run!
                    (let [product @selected-product]
                      (reset! reviews nil)
                      (go (reset! reviews
                                  (<! (api/list-product-reviews (:id product)))))))
                   reviews))

(defn review-form [new-review]
  (let [saving? (atom false)]
    (fn [new-review]
      [:form {:role "form"}
       [:div.form-group
        [:label {:for "reviewName"} "Name"]
        [:input#reviewName.form-control
         {:type "text"
          :on-change #(swap! new-review assoc :customer_name (-> % .-target .-value))}]
        [:label.control-label {:for "reviewEmail"} "Email"]
        [:input#reviewEmail.form-control
         {:type "email"
          :on-change #(swap! new-review assoc :customer_email (-> % .-target .-value))}]
        [:label {:for "reviewStars"} "Stars"]
        [:div#reviewStars
         (let [stars (or (:stars @new-review) 0)
               rate #(swap! new-review assoc :stars %)]
           (for [i (range 5)]
             (if (> stars i)
               ^{:key i}
               [:span.glyphicon.glyphicon-star
                {:aria-hidden "true" :on-click #(rate (+ i 1))}]
               ^{:key i}
               [:span.glyphicon.glyphicon-star-empty
                {:aria-hidden "true" :on-click #(rate (+ i 1))}])))]

        [:label {:for "reviewText"} "Review"]
        [:textarea#reviewText.form-control
         {:on-change #(swap! new-review assoc :review (-> % .-target .-value))}]
        [:button.btn.btn-default
         {:type "button"
          :disabled @saving?
          :on-click #(do (reset! saving? true)
                         (go (when (<! (api/save-review (:id @selected-product)
                                                        @new-review))
                               ;; save success, add new review to local view
                               (swap! reviews conj @new-review)
                               (reset! new-review nil))
                             (reset! saving? false)))}
         "Submit review"]
        ]])))



(defn product-reviews-view []
  (let [new-review (atom nil)]
    (add-watch new-review ::log
               (fn [_ _ old new]
                 (.log js/console (pr-str old) " => " (pr-str new))))
    
    (fn []
      (let [reviews @reviews]
        [:div.reviews
         (if (empty? reviews)
           [:div.noReviews "No reviews"]
           (for [r reviews]
             ^{:key (hash r)}
             [:span.review
              [:dl
               [:dt "Reviewer"]
               [:dd (:customer_name r)]
               [:dt "Stars"]
               [:dd (:stars r)]
               [:dt "Review"]
               [:dd (:review r)]]
              [:hr]]))
         (if (nil? @new-review)
           [:button {:on-click #(reset! new-review {})}
            "Add review"]
           [review-form new-review])
         ]))))




(defn product-view 
  "Render a tabbed product view with detailed description, image gallery and reviews"
  []
  [bs/tabs 
   {:active selected-product-tab :style :tabs}
   "Details" [product-details-view]
   "Gallery" [product-gallery-view]
   "Reviews" [product-reviews-view]
   ])     

(defn widgetshop []
  [:div
   [products-list products]
   (when @selected-product
     [product-view])])

(defn ^:export start []
  (reagent/render-component [widgetshop] (.getElementById js/document "widgetshop-app")))


