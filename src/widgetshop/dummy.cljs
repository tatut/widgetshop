(ns widgetshop.dummy
  "Generate dummy sales data."
  (:require [cljs-time.core :as t]
            [cljs-time.coerce :as c]))

(def products
  [{:name "Acme earthquake pills" :category "Drugs" :price 14.99}
   {:name "Fine leather jacket" :category "Clothing" :price 150}
   {:name "Log from Blammo!" :category "Toys" :price 24.99}
   {:name "Illudium Q-36 modulator" :category "Toys" :price 49.99}
   {:name "Blue pants" :category "Clothing" :price 70}
   {:name "Powerthirst!" :category "Drugs" :price 19.95}
   {:name "Tornado kit" :category "Toys" :price 17.50}
   {:name "Boots of escaping" :category "Clothing" :price 999.95}])

(def tax [10 16 20])

(defn generate
  "Generate a random product sale"
  []
  (let [alku (c/to-long (t/date-time 2013 1 1))
        loppu (c/to-long (t/now))
        vali (- loppu alku)]
    {:product (rand-nth products)
     :date (c/from-long (+ alku (rand-int vali)))
     :qty (inc (rand-int 12))
     :tax (rand-nth tax)}))
   
                      
  
  
