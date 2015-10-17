(ns widgetshop.dummy
  "Generate dummy sales data")

(def products (into [] (range 1 9)))
(def tax [10 16 20])

(defn generate
  "Generate a random product sale"
  []
  (let [alku (.getTime #inst "2013-01-01")
        loppu (.getTime (java.util.Date.))
        vali (- loppu alku)
        fmt (java.text.SimpleDateFormat. "yyyy-MM-dd")
        qty (inc (rand-int 12))
        p (rand-nth products)]
    (str "INSERT INTO sale (purchase_date) VALUES ('"
         (.format fmt (java.util.Date. (long (+ alku (* (rand) vali)))))
         "');\n"

         "INSERT INTO saleitem (sale, product, quantity, price) VALUES("
         "(SELECT currval('sale_id_seq')), "
         p ","
         qty ","
         "(SELECT " qty " * price  FROM product WHERE id = " p "));"
         "\n")))

