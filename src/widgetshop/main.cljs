(ns widgetshop.main
  (:require [reagent.core :as reagent :refer [atom]]
            [widgetshop.bootstrap :as bs]
            [figwheel.client :as fw :include-macros true]))

;; Define some test data our app will be working with
;; in a real app you would load over the net.
(def products 
  (atom [{:id :acme-pills 
          :name "Acme earthquake pills"
          :img "pills.jpg"
          :price {:amount 3999 :currency :eur} 
          :description "Why wait? Make your own earthquakes! Loads of fun."
          :manufacturer "Acme Inc"
          :reviews []
          
          }
         
         {:id :fine-leather-jackets
          :name "Fine leather jacket"
          :img "leatherjacket.jpg"
          :manufacturer "Threepwood Pirate Clothing Inc"
          :price {:amount 245 :currency :pieces-of-eight}
          :description "I'm selling these fine leather jackets"
          :reviews [{:name "Guybrush Threepwood"
                     :email "guybrush@example.com"
                     :stars 5
                     :review "Totally makes me look like an actual pirate!"} 
                    {:name "Haggis McMutton"
                     :email "ilovehaggis@example.com"
                     :stars 1
                     :review "Doesn't work as expected."}]}
         
         {:id :log
          :name "Log from Blammo!"
          :img "log.jpg"
          :price {:amount 1495 :currency :eur}
          :description "It's log, log, It's big, it's heavy, it's wood. It's log, log, it's better than bad, it's good."
          :manufacturer "Blammo Toy Company"}
         
         {:id :q36
          :name "Illudium Q-36 explosive space modulator"
          :img "q36.jpg"
          :price {:amount 20000 :currency :space-bucks}
          :description "Planets obstructing your view of Venus? Destroy them with the new explosive space modulator!"
          :manufacturer "Transgalactic Tools Ltd"
          :reviews []}]))

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

(defmulti format-price :currency)
(defmethod format-price :eur [p]
  (let [amount (:amount p)
        eur (int (/ amount 100))
        cents (rem amount 100)]
    (str eur (if (> cents 0)
               (str "," cents))
         " \u20AC")))
(defmethod format-price :pieces-of-eight [p]
  (str (:amount p) " Pieces o' Eight"))
(defmethod format-price :space-bucks [p]
  (str "S$ " (:amount p)))

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
           (if (empty? stars)
             "No reviews"
             (repeat (Math/round avg)
                     [:span.glyphicon.glyphicon-star {:aria-hidden "true"}]))])]))])


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

(defn review-form [new-review]
  [:form {:role "form"}
   [:div.form-group
    [:label {:for "reviewName"} "Name"]
    [:input#reviewName.form-control
     {:type "text"
      :on-change #(swap! new-review assoc :name (-> % .-target .-value))}]
    [:label.control-label {:for "reviewEmail"} "Email"]
    [:input#reviewEmail.form-control
     {:type "email"
      :on-change #(swap! new-review assoc :email (-> % .-target .-value))}]
    [:label {:for "reviewStars"} "Stars"]
    [:div#reviewStars
     (let [stars (or (:stars @new-review) 0)
           rate #(swap! new-review assoc :stars %)]
       (for [i (range 5)]
         (if (> stars i)
           [:span.glyphicon.glyphicon-star
            {:aria-hidden "true" :on-click #(rate (+ i 1))}]
           [:span.glyphicon.glyphicon-star-empty
            {:aria-hidden "true" :on-click #(rate (+ i 1))}])))]

    [:label {:for "reviewText"} "Review"]
    [:textarea#reviewText.form-control
     {:on-change #(swap! new-review assoc :review (-> % .-target .-value))}]
    [:button.btn.btn-default
     {:type "button"
      :on-click #(do (update-product! (:id @selected-product)
                                      update-in [:reviews] conj @new-review)
                     (reset! new-review nil))}
     "Submit review"]
    ]])


(defn product-reviews-view []
  (let [new-review (atom nil)]
    (add-watch new-review ::log
               (fn [_ _ old new]
                 (.log js/console (pr-str old) " => " (pr-str new))))
    (fn []
      (let [reviews (:reviews @selected-product)]
        [:div.reviews
         (if (empty? reviews)
           [:div.noReviews "No reviews"]
           (for [r reviews]
             [:span.review
              [:dl
               [:dt "reviewer"]
               [:dd (:name r)]
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

(fw/watch-and-reload
  :jsload-callback (fn [] (start)))
