(ns app.components.tb-adjust
     (:require [reagent.core :as r]
               [re-frame.core :as rf]
               [soda-ash.core :as sa]
               [app.state :as state]
               [app.routes :as routes]
               [app.subs :as subs]
               [app.components.journal :refer [ui2 je-panel radios-demo re-com-textarea re-com-text]]
               [app.components.trial-balance :refer [panel]]
               [cljs.core.async :refer [<! timeout go]]))

;;; TODO move these to a wq (work-queue) file
(rf/reg-event-fx
 :wq-write-xero
 (fn [{db :db} [_ value]]
      {:firebase/write {:path [:work-queue :xero]
                        :value value
                        :on-success #(js/console.log "Wrote to :work-queue :xero value: " value)
                        :on-failure [:my-empty]}}))
(rf/reg-event-fx
 :wq-write-cch
 (fn [{db :db} [_ value]]
      {:firebase/write {:path [:work-queue :cch]
                        :value value
                        :on-success #(js/console.log "Wrote to :work-queue :cch value: " value)
                        :on-failure [:my-empty]}}))

(defn connect-xero-button
  []
  [:div [:div.ui.primary.button
         {:on-click #(let [tb-date (->
                                    (.getElementById js/document "tb-date")
                                    .-value)
                           basis (-> (.getElementById js/document "basis") .-value)
                           return-id (-> (.getElementById js/document "return-id") .-value)]
                       (rf/dispatch [:wq-write-xero {:tb-date tb-date
                                                     :basis basis
                                                     :return-id return-id}])
                       (go
                         (<! (timeout 1000))
                         (set! (.-location js/window)  "http://localhost:3000/xero/vip-oauth-init")))}
         "Connect Xero Client"
         [:i.icon-xero-logo {:style {:font-size "1.4em"
                                     :color "white"
                                     :float "left"
                                     :margin-right "20px"}}]]
   [:br] [:label "tb-date: "] [:input {:id "tb-date" :default-value "2018-09-26"}] 
   [:br] [:label "accrual or cash: "][:input {:id "basis" :default-value "accrual"}]
   [:br] [:label "return-id: "][:input {:id "return-id" :default-value "2017S:TaxHubTest:V5"}]])

(defn connect-qbo-button
  []
  [:div.ui.green.button
   "Connect QBO Client"
   [:i.icon-qbo-logo {:style {:font-size "1.4em"
                               :color "white"
                               :float "left"
                              :margin-right "20px"}}]])

(defn send-to-cch-button
  []
  [:div [:div.ui.green.button
         {:on-click #(let [cch-return-id (->
                                   (.getElementById js/document "cch-return-id")
                                   .-value)] (rf/dispatch [:wq-write-cch {:cch-return-id cch-return-id}]))}
         "Send to CCH Axcess"
         [:i {:style {:font-size "1.4em"
                      :color "white"
                      :float "left"
                      :margin-right "20px"}}]]
   [:br] [:label "cch-return-id: "] [:input {:id "cch-return-id" :default-value "2017S:TaxHubTest:V1"}]] )

(defn send-to-cch-button-2
  []
  [:div [:div.ui.green.button
         {:on-click #(let [cch-return-id "2017S:TaxHubTest:V2"] (rf/dispatch [:wq-write-cch {:cch-return-id cch-return-id}]))}
         "Send to CCH Axcess V2"
         [:i {:style {:font-size "1.4em"
                      :color "white"
                      :float "left"
                      :margin-right "20px"}}]]] )

(defn active-return-component [id return]
 (let [active-return? @(rf/subscribe [:active-return? id])
       favorite-return? @(rf/subscribe [:favorite-return? id])]
   [:div
    [:div (if active-return?
            [:div.ui.card
               [:div.content
                [:div.header (:Name return)]
                [:div.meta (:ReturnId return)]
                [:div.description
                 (:ReturnId return)]]
               [:div.extra.content
                [:span.right.floated.like [:a {:on-click (fn [e]
                                                             (.preventDefault e)
                                                             (if favorite-return?
                                                               (rf/dispatch [:unfavorite-return id])
                                                               (rf/dispatch [:favorite-return id])))
                                                 :href "#"
                                                 :style {:color (if favorite-return?
                                                                  :orange
                                                                  :grey)
                                                         :text-decoration :none}}
                                             [:i.star.icon]] "Favorite"]
                [:span.left.floated.icon-1120S.icon][:br][:i.icon-xero-logo.icon.blue.large]]])]]))

(defn steps-example3
  []
  [:div.ui.steps
    [:div.step
     [:i.plug.icon]
     [:div.content
      [:div.title "Connect"]
      [:div.description "your accounting system"]
      [connect-xero-button]]]

    [:div.step
     [:i.share.alternate.icon]
     [:div.content
      [:div.title "Map"]
      [:div.description "your tax lines"]]]
    [:div.active.step
     [:i.sliders.horizontal.icon]
     [:div.content
      [:div.title "Adjust"]
      [:div.description "your trial balance"]]]
    [:div.step
     [:i.share.icon]
     [:div.content
      [:div.title "File"]
      [:div.description "your return"]
      [send-to-cch-button]
      [send-to-cch-button-2]]]])

(defn active-return-panel [id return]
  (let [active-return? @(rf/subscribe [:active-return? id])]
     [:div
      [:div
       (doall
         (for [[id return] @(rf/subscribe [:firebase/on-value {:path [:returns :2017 :1120S]}])]
           [:span {:key id}
             [active-return-component id return]]))]]))


(defn tb-adjust
  []
  [:div.ui
    [:div.ui.grid
     [:div.four.wide.column [active-return-panel]]
     [:div.twelve.wide.column [steps-example3]]
     [:br]
     [:div.ten.wide.column [ui2]]
     [:div.sixteen.wide.column [panel]]]])
