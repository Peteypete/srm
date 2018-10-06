(ns app.views
  (:require [app.state :refer [app-state]]
            [app.events :refer [increment decrement]]))

(defn header
  []
  [:div
   [:h1 "A template for reagent app. Do as u please for the second time"]])

(defn counter
  []
  [:div
   [:button.btn {:on-click #(decrement %)} "-"]
   [:button {:disabled true} (get @app-state :count)]
   [:button.btn {:on-click #(increment %)} "+"]])

(defn app []
  [:div
   [header]
   [counter]])
