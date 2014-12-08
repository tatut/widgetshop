(ns widgetshop.bootstrap
  "Common utilities for components using Bootstrap CSS and markup."
  (:require [reagent.core :refer [atom]]))

(defn tabs [config & alternating-title-and-component]
  (let [active (or (:active config) (atom 0))
        style-class (case (:style config)
                      :pills "nav-pills"
                      :tabs "nav-tabs"
                      "nav-tabs")
        tabs (partition 2 alternating-title-and-component)]
    (fn []
      (let [[active-tab-title active-component] (nth tabs @active)]
        [:span 
         [:ul.nav {:class style-class}
          (map-indexed 
           (fn [i [title]]
             [:li {:role "presentation" 
                   :class (when (= active-tab-title title)
                            "active")}
              [:a {:href "#" :on-click #(reset! active i)}
               title]])
           tabs)]
         active-component])))) 


    
