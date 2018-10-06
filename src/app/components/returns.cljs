(ns app.components.returns
     (:require [reagent.core :as r]
               [re-frame.core :as rf]
               [soda-ash.core :as sa]
               [clojure.string :as str]
               [re-com.buttons :refer [button-args-desc button]]
               [re-com.core     :refer [h-box v-box box gap single-dropdown input-text checkbox label title hyperlink-href p]]
               [re-com.dropdown :refer [filter-choices-by-keyword single-dropdown-args-desc]]
               [re-com.util     :refer [item-for-id]]
               [app.state :as state]
               [app.routes :as routes]
               [app.subs :as subs]))
        

(def accounting-icon [:i.icon-xero-logo.icon.blue.large :i.icon-qbo-logo.icon.green.large :i])

;re-com utiltarian
(def year-type [{:id "2016" :label "2016"}
                {:id "2017" :label "2017"}
                {:id "2018" :label "2018"}])

(def form-type [{:id "1120" :label "1120"}
                {:id "1120S" :label "1120S"}
                {:id "1065" :label "1065"}])

(def yearform-type [{:id "2016-1120" :label "2016-1120"}
                    {:id "2016-1120S" :label "2016-1120S"}
                    {:id "2017-1120" :label "2017-1120"}
                    {:id "2017-1120S" :label "2017-1120S"}])



(defn year-dropdown
  [id]
  (let [selected-year-id (r/atom "2018")]
    (fn [id]
        [h-box
         :gap      "10px"
         :align    :center
         :children [[sa/Label {:horizontal true :color "brown"} "Year"]
                    [single-dropdown
                     :choices     year-type
                     :model       selected-year-id
                     :width       "100px"
                     :max-height  "400px"
                     :filter-box? false
                     :on-change   #(reset! selected-year-id %)]]])))
                    ;[:div
                    ; (if (nil? @selected-year-id)
                    ;   "None"
                    ;   (str (:label (item-for-id @selected-year-id year-type)) " [" @selected-year-id "]"))]]])))

(defn form-dropdown
  [id]
  (let [selected-form-id (r/atom "1120")]
    (fn [id]
        [h-box
         :gap      "10px"
         :align    :center
         :children [[sa/Label {:horizontal true :color "purple"} "Form"]
                    [single-dropdown
                     :choices     form-type
                     :model       selected-form-id
                     :width       "100px"
                     :max-height  "400px"
                     :filter-box? false
                     :on-change   #(reset! selected-form-id %)]]])))
                    ;[:div
                    ; (if (nil? @selected-form-type-id)
                    ;   "None"
                    ;   (str (:label (item-for-id @selected-form-type-id form-type)) " [" @selected-form-type-id "]"))]]])))

(defn yearform-dropdown
  [id]
  (let [selected-yearform-id (r/atom "2017-1120")]
    (fn [id]
        [h-box
         :gap      "10px"
         :align    :center
         :children [[sa/Label {:horizontal true :color "purple"} "Year/Form"]
                    [single-dropdown
                     :choices     yearform-type
                     :model       selected-yearform-id
                     :width       "130px"
                     :max-height  "400px"
                     :filter-box? false
                     :on-change   #(reset! selected-yearform-id %)]]])))
                    ;[:div
                    ; (if (nil? @selected-form-type-id)
                    ;   "None"
                    ;   (str (:label (item-for-id @selected-form-type-id form-type)) " [" @selected-form-type-id "]"))]]])))




(def state2 (r/atom
             {:outcome-index 0
              :see-throbber  false}))

(defn button-demo
 []
 (let [hover? (r/atom false)]
   (fn
     []
     [:div
      [button
                  :label            "2017"
                  :tooltip          "Filter 2017 Returns"
                  :tooltip-position :below-center
                  :on-click          #(swap! state2 update-in [:outcome-index] inc)
                  :class             "btn-primary"]])))

(rf/reg-sub
  :active-return
  (fn [db]
   (get-in db [:user :active-return] #{})))

;there can only be one active return
(rf/reg-sub
  :active-return?
  (fn []
    (rf/subscribe [:active-return]))
  (fn [active-return [_ id]]
    (= active-return id)))

(rf/reg-sub
  :favorite-return
  (fn [db]
   (get-in db [:user :favorite-return] #{})))

;there can be many favorited cleints
(rf/reg-sub
  :favorite-return?
  (fn []
    (rf/subscribe [:favorite-return]))
  (fn [favorite-return [_ id]]
    (contains? favorite-return id)))

;there can only be one active return
(rf/reg-event-db
  :active-return
  (fn [db [_ id]]
      (assoc-in db [:user :active-return] id)))

(rf/reg-event-db
  :unactive-return
  (fn [db [_ id]]
      (update-in db [:user :active-return] disj id)))

;there can be many favorited returns
(rf/reg-event-db
 :favorite-return
 (fn [db [_ id]]
     (update-in db [:user :favorite-return] (fnil conj #{}) id)))

(rf/reg-event-db
  :unfavorite-return
  (fn [db [_ id]]
      (update-in db [:user :favorite-return] disj id)))


;code based on Eric Normand "Understanding R-frame Lesson 27 and 28"
(defn return-component [id return]
  (let [active-return? @(rf/subscribe [:active-return? return])
        favorite-return? @(rf/subscribe [:favorite-return? return])]
      [:tr {:key return}
       [:td {:key "fav"} [:a {:on-click (fn [e]
                                         (.preventDefault e)
                                         (if favorite-return?
                                           (rf/dispatch [:unfavorite-return return])
                                           (rf/dispatch [:favorite-return return])))
                              :href "#"
                              :style {:color (if favorite-return?
                                               :orange
                                               :grey)
                                      :text-decoration :none}}
                          [:i.star.icon]]]
       [:td {:key "nam"} [:a {:on-click (fn [e]
                                         (.preventDefault e)
                                         (if active-return?
                                           (rf/dispatch [:unactive-return id])
                                           (rf/dispatch [:active-return id])))
                              :href (routes/url-for :tb-adjust)}(:Name return)]]
       [:td {:key "rid"} (:ReturnId return)]
       [:td {:key "typ"} (:ReturnType return)]
       [:td {:key "yer"} (:TaxYear return)]
       [:td {:key "acc"} [(rand-nth accounting-icon)]]]))

(defn ->regex [data]
  (re-pattern (apply str "(?i)" (interpose ".*" data))))

(defn search-box [data]
  (fn [data]
    [:div.ui.fluid.icon.input
     [:input {:type "text"
              :placeholder "Search"
              :size "300px"
              :value (:filter-string @data)
              :on-change #(swap! data assoc :filter-string (-> % .-target .-value))}]
     [:i.search.link.icon]]))

(defn returns-panel [fltr]
  (let [data (r/atom {:filter-string fltr})]
    (fn
      []
      [:div
       [:h1 "Returns"]
       [:div
        [:div.item
         [:div.ui.grid
          [:div.four.wide.column [search-box data]]
          [:div.three.wide.column [year-dropdown]]
          [:div.three.wide.column [form-dropdown]]
          [:div.three.wide.column [yearform-dropdown]]
          ;[:div.two.wide.column [dropdown-year]]
          ;[:div.four.wide.column [dropdown-form-type]]
          [:table.ui.selectable.table
            [:thead
                [:tr

                     [:th {:key "fav"} "Favorite"]
                     [:th {:key "nam"} "Name"]
                     [:th {:key "rid"} "Return ID"]
                     [:th {:key "typ"} "Return Type"]
                     [:th {:key "yer"} "Tax Year"]
                     [:th {:key "acc"} "Accounting"]]]

            [:tbody
             (doall
              (for [[id return] @(rf/subscribe [:firebase/on-value
                                                {:path [:returns :2017 :1120S]}])
                     :when (re-find (->regex (:filter-string @data)) (str (:Name return) " " (:ReturnId return) " "(:TaxYear return)))]
                 ^{:key (str (:ReturnId return) "_")}
                  [return-component id return]))]]]]]])))
          ;[:div
         ;  [:h4 "State of :active-return subscription"]
         ;  (pr-str @(rf/subscribe [:active-return]))]
          ;[:div
         ;  [:h4 "State of :favorite-return subscription"]
         ;  (pr-str @(rf/subscribe [:favorite-return]))]]]])))
;end re-frame


(defn returns-table
 []
 (fn []
   [:div
    [:div [returns-panel]]]))
