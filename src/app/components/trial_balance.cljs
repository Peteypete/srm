(ns app.components.trial-balance
     (:require [reagent.core :as r]
               [re-frame.core :as rf]
               [soda-ash.core :as sa]
               [re-com.core     :refer [h-box v-box box gap single-dropdown input-text checkbox label title hyperlink-href p]]
               [re-com.util     :refer [item-for-id]]
               [app.state :as state]
               [app.routes :as routes]
               [app.subs :as subs]
               [app.components.journal :refer [ui2 je-segment value creditvalue notevalue je-panel radios-demo re-com-textarea re-com-text]]
               [app.helpers.dropdowns :refer [simple-demo filtering-je filtering-demo filtering-demo-shared]]
               [app.helpers.acctcalc :as acctcalc]
               [app.components.utils :as thu]
               [cljs.pprint :as pp]
               [goog.string :as gstring]
               [goog.string.format]))

;re-com checkbox
(defonce ticked? (r/atom false))
(defn simple-checkbox
      []
      [v-box
       :gap "15px"
       :children [[h-box
                   :gap      "10px"
                   :height   "20px"
                   :children [[checkbox
                               :label     "tick me  "
                               :model     ticked?
                               :on-change #(reset! ticked? %)]
                              (when @ticked? [label :label " is ticked"])]]]])

;start Trial Balance custom form
(def rows {:row-01 {:id :row-01
                         :name "Cash"
                         :return-id "2016:S:S453454160:V2"
                         :desc "Tax Hub Test"
                         :form "1120"
                         :lead false
                         :accounting "xero"
                         :status "Ready to File"
                         :action  "File"}
             :row-02 {:id :row-02
                         :name "Accounts Payable"
                         :return-id "2016:S:S32854160:V2"
                         :desc "A Corporation"
                         :form "1120"
                         :lead false
                         :accounting "xero"
                         :status "Ready to Map"
                         :action  "Map"}
             :row-03 {:id :row-03
                         :name "Meals and Ent"
                         :return-id "2016:S:S454754160:V2"
                         :desc "Real rows"
                         :form "1120S"
                         :lead "Peter Wen"
                         :accounting "qbo"
                         :status "Ready For Review"
                         :action  "Review"}
             :row-04 {:id :row-04
                         :name "Repairs"
                         :return-id "2016:S:S434564160:V2"
                         :desc "101 Main St"
                         :form "1065"
                         :lead "Ben Wen"
                         :accounting "xero"
                         :status "Ready to File"
                         :action  "File"}})

(rf/reg-sub
  :rows
  (fn [db]
    (:rows db)))

(rf/reg-sub
  :active-row
  (fn [db]
   (get-in db [:user :active-row] #{})))



(rf/reg-sub
  :active-row?
  (fn []
    (rf/subscribe [:active-row]))
  (fn [active-row [_ id]]
    (contains? active-row id)))

(rf/reg-event-db
  :active-row
  (fn [db [_ id]]
      (update-in db [:user :active-row] (fnil conj #{}) id)))

(rf/reg-event-db
  :unactive-row
  (fn [db [_ id]]
     (update-in db [:user :active-row] disj id)))

(defn trial-balance-row [id row]
 (let [active-row? @(rf/subscribe [:active-row? id])]
   [:tr {:bgcolor (if active-row?
                            :#cce5ff
                            :white)}

     [:td [:a {:on-click (fn [e]
                             (.preventDefault e)
                             (if active-row?
                               (rf/dispatch [:unactive-row id])
                               (rf/dispatch [:active-row id])))
                 :href "#"
                 :style {:color (if active-row?
                                  :blue
                                  :grey)
                         :text-decoration :none}}
           (if active-row?
               [:i.check.square.icon]
               [:i.square.outline.icon])]]
     [:td @value]
     [:td @creditvalue]
     [:td]
     [:td]
     [:td]
     [:td]
     [:td (:name row)]
     [:td (if active-row?
            [filtering-demo-shared id]
            [filtering-demo id])]]))

(defn row-header [row]
  [:tr
   [:th {:key "Icon-H"} "Icon"]
   (for [cell (drop 1 (:cells row))]
     [:th {:key (keyword (str (:value cell) "-H"))} (:value cell)])])

(defn row-section [item]
  [:tr
   (let [title (if (not= "" (:title item))
                (:title item)
                "TOTAL")]
     [:td {:key (keyword title) :colSpan "9"}
      [:b title]])])

(rf/reg-event-fx
  :write-test
  (fn [{db :db} [_ value & k]]
    {:firebase/write {:path (into [:write-test] k)
                      :value value
                      :on-success #(js/console.log "Wrote " k " value: " value)
                      :on-failure [:my-empty]}}))

(rf/reg-sub
 :categories
 ;; Signal fn
 (fn [_ _] (rf/subscribe [:firebase/on-value
                          {:path
                           [:tax-maps :cch :2017 :us-federal :1120S :semantic-entries-hash]}]))
 ;; Computation fn
 (fn [options _]
   (into (sorted-map)
         (map
          (fn [[front-id option]]
            (js/console.log "_: " front-id "  option: " option)
            {(keyword (str (gstring/format "%04d" (:sort-order option)) "-" (:id option)))
             option})
          options))))

(defn category-picker [key-base]
  (js/console.log "category-picker")
  (fn [key-base]
    (let [remote-value @(rf/subscribe [:firebase/on-value {:path [:write-test :category key-base]}])
          options-hash @(rf/subscribe [:categories])
          selected-value (if (empty? remote-value)
                           "None"
                           remote-value)]
      [:td
       [:select {:value selected-value
                 :on-change (fn [e]
                              (rf/dispatch [:write-test (-> e .-target .-value) :category key-base]))}
        [:option {:value "None"} "None (Do Not Import)"]
        (for [[_ option] @(rf/subscribe [:categories])]
          ^{:key (:id option)}
          [:option {:value (:id option)} (str (:description option))])]])))

(defn editable-td-ytd-hacky-adj
  "key-base is the account name
  c-or-d is a keyword. Either :credit or :debit"
  [key-base c-or-d]
  (let [editing (r/atom false)]
    (fn [key-base c-or-d]
      ;; TODO This calc for label feels like it should be a Stage 3 calculation
      (let [remote-value @(rf/subscribe [:firebase/on-value {:path [:write-test c-or-d key-base]}])
            remote-label (str (if (empty? remote-value) "0.00" remote-value))]
        [:td {:width "100"
              :key (keyword (str key-base "-hacky-" (name c-or-d) "-adj"))
              :class (str (when @editing "editing"))}
         [:div (if @editing [:i.chevron.down.icon] [:i.chevron.right.icon])
          [:div.view
           [:label {:on-click #(do (reset! editing true))}
            remote-label]]
          (when @editing
            (js/console.log "editing")
            [thu/text-input
             {:class "edit"
              :id (keyword (str key-base "-hacky-" (name c-or-d) "-adj"))
              :title remote-label
              :on-save #(if (seq %1)
                          (rf/dispatch [:write-test %1 c-or-d key-base]))
              :on-stop #(reset! editing false)}])]]))))



(defn editable-td-ytd-hacky-adj-memo
  "key-base is the account name
  memo is a keyword. Either :memo"
  [key-base memo]
  (let [editing (r/atom false)]
    (fn [key-base memo]
      ;; TODO This calc for label feels like it should be a Stage 3 calculation
      (let [remote-value @(rf/subscribe [:firebase/on-value {:path [:write-test memo key-base]}])
            remote-label (str (if (empty? remote-value) "0.00" remote-value))]
        [:td {:width "200"
              :key (keyword (str key-base "-hacky-" (name memo) "-adj"))
              :class (str (when @editing "editing"))}
         [:div.view
          [:label {:on-click #(do (reset! editing true))}
           remote-label]]
         (when @editing
           (js/console.log "editing")
           [thu/text-input
            {:class "edit"
             :id (keyword (str key-base "-hacky-" (name memo) "-adj"))
             :title remote-label
             :on-save #(if (seq %1)
                         (rf/dispatch [:write-test %1 memo key-base]))
             :on-stop #(reset! editing false)}])]))))





(defn row-details [cells]
    (fn [cells]
      (let [key-base (-> cells
                         (get 0)
                         :value)
            ytd-debit (-> cells
                                         (get 2)
                                         :value) ;; A string
            ytd-credit (-> cells
                                         (get 1)
                                         :value)
            ytd-debit-value (acctcalc/to-number ytd-debit)
            ytd-credit-value (acctcalc/to-number ytd-credit)
            ;; TODO make this fit in the re-frame 3rd stage better
            tax-adj-debit-maybe @(rf/subscribe [:firebase/on-value {:path [:write-test :debit key-base]}])
            tax-adj-credit-maybe @(rf/subscribe [:firebase/on-value {:path [:write-test :credit key-base]}])
            tax-adj-debit-value (acctcalc/to-number (if (empty? tax-adj-debit-maybe) "0.0" tax-adj-debit-maybe))
            tax-adj-credit-value (acctcalc/to-number (if (empty? tax-adj-credit-maybe) "0.0" tax-adj-credit-maybe))
            ytd-debit-adjusted (acctcalc/calc-adj :debit ytd-debit-value ytd-credit-value tax-adj-debit-value tax-adj-credit-value)
            ytd-credit-adjusted (acctcalc/calc-adj :credit ytd-debit-value ytd-credit-value tax-adj-debit-value tax-adj-credit-value)
            _ (js/console.log "row-detail tax-adj-debit")]

        [:tr {:class "tb-row"}
         (list
          [:td {:key :empty}]
          [:td {:id (keyword (str key-base "-ytd-debit"))
                :key "-ytd-debit"}
           ytd-debit]
          [:td {:id (keyword (str key-base "-ytd-credit"))
                :key "-ytd-credit"}
           ytd-credit]
          ^{:key "debit-adj"} [editable-td-ytd-hacky-adj key-base :debit]
          ^{:key "credit-adj"} [editable-td-ytd-hacky-adj key-base :credit]
          ^{:key "memo-adj"} [editable-td-ytd-hacky-adj-memo key-base :memo]
          [:td {:key (keyword (str key-base "-ytd-debit-adjusted"))
                :id (keyword (str key-base "-ytd-debit-adjusted"))}
           ytd-debit-adjusted]
          [:td {:key (keyword (str key-base "-ytd-credit-adjusted"))
                :id (keyword (str key-base "-ytd-credit-adjusted"))}
           ytd-credit-adjusted]
          [:td {:key (keyword (str key-base "-cat"))} key-base]
          ^{:key "picker"} [category-picker key-base])])))


(defn trial-balance-view []
  (fn []
    (js/console.log (str "trial-balance-view start"))
    [:div
      [:table.ui.celled.selectable.table
       [:thead [:tr [:th "Icon"]
                [:th {:key :yt} "YTD Debit"] [:th {:key :yc} "YTD Credit"]
                [:th {:key :tad} "Tax Adj Debit"] [:th {:key :tac} "Tax Adj Credit"]
                [:th {:key :tam} "Tax Adj Memo"]
                [:th {:key :ad} "Adj Debit"] [:th {:key :ac} "Adj Credit"]
                [:th {:key :tba} "TB Account"][:th {:key :tc} "Tax Category"]]]
       [:tbody
        ;; TODO make this generic
        (let [{rows :rows} (:2017S:TaxHubTest:V1 @(rf/subscribe [:firebase/on-value
                                                                 {:path
                                                                  [:trial-balances]}]))]
          (for [row rows
                :let [row-type (:row-type row)]]
            (if (= "Header" row-type)
              ^{:key "_header"} [row-header row] ; Maybe reagent bug? :key
              (if (= "Section" row-type)
                (list
                 ^{:key (str "_sec_" (get-in row [:cells :value]))} [row-section row] ; Maybe reagent bug? :key
                 (for [innerrow (:rows row)
                       :let [cells (:cells innerrow)]] ;; nested rows
                   ^{:key (str "_r_" (-> cells (get 0) :value))} [row-details cells]))

                                        ; Maybe reagent bug? :key
                [:div "something went wrong"]))))]]]))


(defn panel
  []
  [:div [trial-balance-view]])
