(ns app.helpers.dropdowns
  (:require [reagent.core        :as reagent]
            [re-frame.core       :as rf]
            [re-com.core         :refer [h-box v-box box gap single-dropdown input-text checkbox label title hyperlink-href p]]
            [re-com.dropdown     :refer [filter-choices-by-keyword single-dropdown-args-desc]]
            [re-com.util         :refer [item-for-id]]
            [app.subs            :as subs]
            [app.helpers.utils   :refer [panel-title title2 args-table github-hyperlink status-text]]))

;(defn tax-hash
;  []
;  @(rf/subscribe [:firebase/on-value {:path [:tax-maps :cch :2016 :us-federal :1120S :semantic-entries-hash]}]))
;
;
;(defn extract [coll]
;  []
;  (map (fn [e] (let [v (val e)
;                       id (:id v)
;                       desc (:description v)]
;                    {:id id :label desc}))
;       (tax-hash)))
;
;(def dropdown
;  (into [] (extract [tax-hash])))


(def countries [{:id "au" :label "Australia"}
                {:id "us" :label "United States"}
                {:id "uk" :label "United Kingdom"}
                {:id "ca" :label "Canada"}
                {:id "nz" :label "New Zealand"}])

(def grouped-countries [{:id "AU" :label "Australia"                :group "EN Speakers"}
                        {:id "US" :label "United States"            :group "EN Speakers"}
                        {:id "GB" :label "United Kingdom"           :group "EN Speakers"}
                        {:id "E1" :label "Iraq"                     :group "Updated Axis Of Evil"}
                        {:id "E2" :label "New Zealand"              :group "Updated Axis Of Evil"}
                        {:id "E3" :label "Iran"                     :group "Updated Axis Of Evil"}
                        {:id "E4" :label "North Korea"              :group "Updated Axis Of Evil"}
                        {:id "03" :label "Afghanistan"              :group "'A' COUNTRIES"}
                        {:id "04" :label "Albania"                  :group "'A' COUNTRIES"}
                        {:id "16" :label "Austria"                  :group "'A' COUNTRIES"}
                        {:id "17" :label "Azerbaijan"               :group "'A' COUNTRIES"}
                        {:id "18" :label "Bahamas"                  :group "'B' COUNTRIES"}
                        {:id "19" :label "Bahrain"                  :group "'B' COUNTRIES"}
                        {:id "20" :label "Bangladesh"               :group "'B' COUNTRIES"}
                        {:id "21" :label "Barbados"                 :group "'B' COUNTRIES"}
                        {:id "22" :label "Belarus"                  :group "'B' COUNTRIES"}])

(def account-lines [{:id "au" :label "Cash"}
                    {:id "us" :label "Accounts Payable"}
                    {:id "uk" :label "Meals & Entertainment"}
                    {:id "ca" :label "Repairs"}
                    {:id "nz" :label "Internet Expense"}])

(def grouped-tax-lines [{:id "AU" :label "Cash"                :group "EN Speakers"}
                        {:id "US" :label "Income"            :group "EN Speakers"}
                        {:id "GB" :label "Meal & Entertainment"           :group "EN Speakers"}
                        {:id "E1" :label "Iraq"                     :group "Updated Axis Of Evil"}
                        {:id "E2" :label "New Zealand"              :group "Updated Axis Of Evil"}
                        {:id "E3" :label "Iran"                     :group "Updated Axis Of Evil"}
                        {:id "E4" :label "North Korea"              :group "Updated Axis Of Evil"}])

(defn simple-demo
  []
  [v-box
   :gap "10px"
   :children [[p "The dropdown above is the simple case."]
              [p "It presents a list of choices and allows one to be selected, via mouse or keyboard."]]])

(defn grouping-demo
  []
  (let [selected-country-id (reagent/atom nil)]
    (fn []
      [v-box
       :gap      "10px"
       :children [[p "The dropdown below shows how related choices can be displayed in groups. In this case, several country related groups. e.g. 'EN COUNTRIES'."]
                  [p "This feature is triggered if any choice has a " [:code ":group"] " attribute. Typically all choices will have a " [:code ":group"] " or none will. It's up to you to ensure that choices with the same " [:code ":group"] " are adjacent in the vector."]
                  [p "Because :model is initially nil, the " [:code ":placeholder"] " text is initially displayed."]
                  [p [:code ":max-width"] " is set here to make the dropdown taller."]
                  [h-box
                   :gap      "10px"
                   :align    :center
                   :children [[single-dropdown
                               :choices     grouped-countries
                               :model       selected-country-id
                               :title?      true
                               :placeholder "Choose a country"
                               :width       "300px"
                               :max-height  "400px"
                               :filter-box? false
                               :on-change   #(reset! selected-country-id %)]
                              [:div
                               [:strong "Selected country: "]
                               (if (nil? @selected-country-id)
                                 "None"
                                 (str (:label (item-for-id @selected-country-id grouped-countries)) " [" @selected-country-id "]"))]]]]])))

(def selected-country-id2 (reagent/atom "US"))

;Custom Filters Start
(defn filtering-demo-shared
  [id]
  (let [selected-country-id (reagent/atom "US")
        active-client? @(rf/subscribe [:active-client? id])]
    (fn [id]
        [h-box
         :gap      "10px"
         :align    :center
         :children [[single-dropdown
                     :choices     grouped-tax-lines
                     :model       selected-country-id2
                     :width       "300px"
                     :max-height  "400px"
                     :filter-box? true
                     :on-change   #(reset! selected-country-id2 %)]
                    [:div
                     (if (nil? @selected-country-id2)
                       "None"
                       (str (:label (item-for-id @selected-country-id2 grouped-tax-lines)) " [" @selected-country-id2 "]"))]]])))

(defn filtering-demo
  [id]
  (let [selected-country-id (reagent/atom "US")
        active-client? @(rf/subscribe [:active-client? id])]

    (fn [id]
        [h-box
         :gap      "10px"
         :align    :center
         :children [[single-dropdown
                     :choices     ;dropdown
                     :model       selected-country-id
                     :width       "300px"
                     :max-height  "400px"
                     :filter-box? true
                     :on-change   #(reset! selected-country-id %)]
                    [:div
                     (if (nil? @selected-country-id)
                       "None"
                       (str (:label (item-for-id @selected-country-id grouped-tax-lines)) " [" @selected-country-id "]"))]]])))

(defn filtering-je
  [id]
  (let [selected-country-id (reagent/atom "US")
        active-client? @(rf/subscribe [:active-client? id])]

    (fn [id]
        [h-box
         :gap      "10px"
         :align    :center
         :children [[single-dropdown
                     :choices     account-lines
                     :model       selected-country-id
                     :width       "300px"
                     :max-height  "400px"
                     :filter-box? true
                     :on-change   #(reset! selected-country-id %)]]])))

;; core holds a reference to panel, so need one level of indirection to get figwheel updates
(defn main-panel []
  (fn []
    [:div]))
