(ns app.components.team
     (:require [reagent.core :as r]
               [re-frame.core :as rf]
               [soda-ash.core :as sa]
               [app.state :as state]
               [app.routes :as routes]
               [app.subs :as subs]))


(defn user-component [id user]
      [:tr
       [:td {:key "Name"}  (get-in user [:profile :display-name])]
       [:td {:key "Email"} (get-in user [:profile :email])]
       [:td {:key "Photo"} [:figure.avatar [:img {:src (get-in user [:profile :photo-url])}]]]
       [:td {:key "Id"}    (get-in user [:profile :uid])]])
       ;[:td (:status client)]
       ;[:td (:action client)]])

(defn users-panel
  []
  [:div
   [:h1 "Team"]
   [:div
    [:div.container
     [:table.ui.selectable.table
       [:thead [:tr
                [:th {:key "Name"}  "Name"]
                [:th {:key "Email"} "Email"]
                [:th {:key "Photo"} "Photo"]
                [:th {:key "Id"}    "Id"]]]
       [:tbody
        (doall
          (for [[id user] @(rf/subscribe [:firebase/on-value {:path [:users]}])]
               ^{:key id}
                [user-component id user]))]]]]])

(defn team-table
  []
  [:div [users-panel]])
