(ns widgetshop.format
  "Helpers for formatting data."
  (:require [cljs-time.format :as df]))

(defn euros [number]
  ;; \u20ac is unicode for the EURO SIGN
  (str (.toFixed number 2) " \u20ac"))

(defn percent [number]
  (str (.toFixed number 0) "%"))

(def date-formatter (df/formatter "dd.MM.yyyy"))
(defn date [d]
  (df/unparse date-formatter d))

(def year-and-month-formatter (df/formatter "yyyy/MM"))

(defn year-and-month [d]
  (df/unparse year-and-month-formatter d))
