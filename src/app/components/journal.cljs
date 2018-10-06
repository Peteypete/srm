(ns app.components.journal
     (:require [reagent.core :as r]
               [reagent.ratom :refer-macros [reaction]]
               [reagent.ratom :as ratom :include-macros true]
               [re-frame.core :as rf]
               [soda-ash.core :as sa]
               [re-com.core :as rc]
               [re-com.core :as rc]
               [re-com.core       :refer [h-box v-box box gap single-dropdown datepicker datepicker-dropdown checkbox label title p md-icon-button]]
               [re-com.datepicker :refer [iso8601->date datepicker-dropdown-args-desc]]
               [re-com.misc   :refer [radio-button-args-desc]]
               [app.state :as state]
               [app.routes :as routes]
               [goog.date.Date]
               [cljs-time.core    :refer [now days minus plus day-of-week before?]]
               [cljs-time.coerce  :refer [to-local-date]]
               [cljs-time.format  :refer [formatter unparse]]
               [app.helpers.datepicker :refer [datepicker-dd]]
               [app.helpers.utils :refer [panel-title title2 title3 args-table github-hyperlink status-text]]
               [app.helpers.dropdowns :refer [simple-demo filtering-je filtering-demo filtering-demo-shared]]))


;Simple JE using plain Reagent - start
(defonce value (r/atom nil))
(defonce creditvalue (r/atom nil))
(defonce notevalue (r/atom nil))

(defn debit-input [value]
  [sa/Input {:placeholder "Debit"
             :type "text"
             :value (or @value "")
             :on-change #(reset! value (-> % .-target .-value))}])

(defn credit-input [creditvalue]
  [sa/Input {:placeholder "Credit"
             :type "text"
             :value (or @creditvalue "")
             :on-change #(reset! creditvalue (-> % .-target .-value))}])

(defn je-note-input [notevalue]
  [sa/TextArea {:placeholder "JE Note"
                              :auto-height true
                              :style {:min-height 50
                                      :width 400}
                :value (or @notevalue "")
                :on-change #(reset! notevalue (-> % .-target .-value))}])
(defn je-note-display
  []
  [:div
     [:p "JE Notes: " [je-note-input notevalue]]])

(defn je-debit-display
  []
  [:div
     [:span "Debit " [debit-input value]]])
(defn je-credit-display
  []
  [:div
     [:span "Credit " [credit-input creditvalue]]])

(defn textarea-example
  []
  [sa/Form
    [:br]
    [je-note-display]
    [je-debit-display]
    [je-credit-display]
    [sa/Button {:primary true}
     "Save"]])

(defn je-segment
  []
  [:div
   [sa/Segment {:color "blue"}
    "Old Beta JE Form. Wired to reagent atom value, creditvalue, notevalue"
    [textarea-example]]])

;Simple JE using plain Reagent - end


(def journals {:journal-01 {:id :journal-01
                            :name "TaxHubTest"
                            :return-id "2016:S:S453454160:V2"
                            :desc "Tax Hub Test"
                            :form "1120"
                            :lead false
                            :accounting "xero"
                            :status "Ready to File"
                            :action  "File"}
               :journal-02 {:id :journal-02
                            :name "Brandly"
                            :return-id "2016:S:S32854160:V2"
                            :desc "A Corporation"
                            :form "1120"
                            :lead false
                            :accounting "xero"
                            :status "Ready to Map"
                            :action  "Map"}
               :journal-03 {:id :journal-03
                            :name "Newaya, LLC"
                            :return-id "2016:S:S454754160:V2"
                            :desc "Real journals"
                            :form "1120S"
                            :lead "Peter Wen"
                            :accounting "qbo"
                            :status "Ready For Review"
                            :action  "Review"}
               :journal-04 {:id :journal-04
                            :name "PartnershipTest"
                            :return-id "2016:S:S434564160:V2"
                            :desc "101 Main St"
                            :form "1065"
                            :lead "Ben Wen"
                            :accounting "xero"
                            :status "Ready to File"
                            :action  "File"}
               :journal-05 {:id :journal-05
                            :name "Newaya, LLC"
                            :return-id "2016:S:S454754160:V2"
                            :desc "Real journals"
                            :form "1120S"
                            :lead "Peter Wen"
                            :accounting "qbo"
                            :status "Ready For Review"
                            :action  "Review"}
               :journal-06 {:id :journal-06
                            :name "PartnershipTest"
                            :return-id "2016:S:S434564160:V2"
                            :desc "101 Main St"
                            :form "1065"
                            :lead "Ben Wen"
                            :accounting "xero"
                            :status "Ready to File"
                            :action  "File"}})

(rf/reg-event-db
 :initialize-journals
 (js/console.log "Registered :initialize-journal")
 (fn [db]
   (js/console.log ":initialize-journal handler invoked")
   (assoc db :journals journals)))


(rf/reg-sub
 :journals
 (fn [db]
   (:journals db)))

;start form examples
(rf/reg-sub-raw
  :re-frame-plain
  (fn [db]
    (ratom/reaction (:re-frame-plain @db))))

(rf/reg-event-db
  :update-re-frame-plain
  (fn [db [_ keys value :as event]]
    (-> db
        (assoc-in (cons :re-frame-plain keys) value)
        (update :event-log #(conj % event)))))


;Larger JE using Re-com UI - start
(defn radios-demo
  []
  (let [color (r/atom "green")]
    (fn
      []
      [v-box
       :gap      "10px"
       :children [[title3 "JE Type"]
                  [v-box
                   :children [(doall (for [c ["Book JE" "Tax JE"]]    ;; Notice the ugly "doall"
                                       ^{:key c}                        ;; key should be unique among siblings
                                       [rc/radio-button
                                        :label       c
                                        :value       c
                                        :model       color
                                        :label-style (if (= c @color) {:color       c
                                                                        :font-weight "bold"})
                                        :on-change   #(reset! color %)]))]]]])))

(defn re-com-textarea
  [{:keys [on-save]}]
  (let [val (r/atom "")
        stop #(reset! val "")
        save #(let [v (-> @val str clojure.string/trim)]
                (when (seq v) (on-save v))
                (stop))]
    (fn []
      [rc/box
       :size "auto"
       :child [rc/input-textarea
               :model val
               :placeholder "Enter a JE Memo"
               :on-change #(do (reset! val %)
                               (save))
               :change-on-blur? true]])))

(defn re-com-text
  [{:keys [on-save]}]
  (let [val (r/atom "")
        stop #(reset! val "")
        save #(let [v (-> @val str clojure.string/trim)]
                (when (seq v) (on-save v))
                (stop))]
    (fn []
      [rc/box
       :size "auto"
       :child [rc/input-text
               :model val
               :placeholder "Enter"
               :on-change #(do (reset! val %)
                               (save))
               :change-on-blur? true]])))

(defn je-component [id journal]
    [:tr {:key (str id "-row") :bgcolor :white}
     [:td {:key (str id "-fje")} [filtering-je id]]
     [:td {:key (str id "-rct1")} [re-com-text]]
     [:td {:key (str id "-rct2")} [re-com-text]]
     [:td {:key (str id "-rct3")} [re-com-text]]])

(defn je-panel []
  (let [data (rf/subscribe [:re-frame-plain])]
    (fn []
      [:div.plain-form
       [:br]
       [:form
        [:div {:style {:float "left" :width "400px"}} [re-com-textarea]]
        [:div {:style {:float "left" :width "200px"}} [datepicker-dd]]
        [:div {:style {:float "left" :width "200px"}}] [radios-demo]
        [:br {:style {:clear "left"}}]
        [:table.ui.selectable.table
          [:thead [:tr {:key "head"} [:th {:key "tba"} "TB Account"][:th {:key "tbd"} "Debit"] [:th {:key "tbc"} "Credit"] [:th {:key "tbn"} "Note"]]]
          [:tbody
            (doall
              (for [[id journal] @(rf/subscribe [:journals])]
                  ^{:key (str id "-")} [je-component id journal]))]]]

       [:br]
       [textarea-example]])))

;Larger JE using Re-com UI - start

;Collapsible JE panel (from Eric Normand code)  - start
(rf/reg-event-db
  :teacher/initialize
  (fn [_ _]
    {}))

(rf/reg-event-db
  :teacher/toggle-panel
  (fn [db [_ id]]
    (update-in db [:panels id] not)))

(rf/reg-sub
  :teacher/panel-state
  (fn [db [_ id]]
    (get-in db [:panels id])))

(defn example-component []
  [je-panel])

(defn panel [id title & children]
  (let [s (r/atom {:open false})]
    (fn [id title & children]
      (let [open? @(rf/subscribe [:teacher/panel-state id])
            child-height (:child-height @s)]
        [:div
         [:div {:on-click #(rf/dispatch [:teacher/toggle-panel id])
                :style {:background-color "#ddd"
                        :padding "0 1em"
                        :font-size "1.5em"}}
          [:div {:style {:float "left"}}
           (if open? [:i.chevron.down.icon] [:i.chevron.right.icon])]
          title]
         [:div {:style  {:overflow "hidden"
                         :transition "max-height 0.8s"
                         :max-height (if open? child-height 0)}}
          [:div {:ref #(when %
                         (swap! s assoc :child-height (.-clientHeight %)))
                 :style {:background-color "#eee"
                         :padding "0 1em"}}
           children]]]))))

(defn ui2 []
  [:div
   ;; Maybe bug? Need metadata :key to supress reagent key warning
   [panel :ex-1 "Click to Make a Journal Entry" ^{:key "ec"} [example-component]]])
