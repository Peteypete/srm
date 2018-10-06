(ns app.helpers.datepicker
  (:require [reagent.core           :as    reagent]
            [reagent.ratom          :refer-macros [reaction]]
            [re-com.core            :refer [h-box v-box box gap single-dropdown datepicker datepicker-dropdown checkbox label title p md-icon-button]]
            [re-com.datepicker      :refer [iso8601->date datepicker-dropdown-args-desc]]
            [goog.date.Date]
            [cljs-time.core         :refer [now days minus plus day-of-week before?]]
            [cljs-time.coerce       :refer [to-local-date]]
            [cljs-time.format       :refer [formatter unparse]]
            [app.helpers.utils      :refer [panel-title title2 args-table github-hyperlink status-text]]))

(def ^:private days-map
     {:Su "S" :Mo "M" :Tu "T" :We "W" :Th "T" :Fr "F" :Sa "S"})

(defn datepicker-dd
  []
  (let [model1          (reagent/atom (now))
        model2          (reagent/atom (plus  (now) (days 120)))
        disabled?       (reagent/atom false)
        show-today?     (reagent/atom true)
        show-weeks?     (reagent/atom false)
        enabled-days    (reagent/atom (-> days-map keys set))
        as-days         (reaction (-> (map #(% {:Su 7 :Sa 6 :Fr 5 :Th 4 :We 3 :Tu 2 :Mo 1}) @enabled-days) set))
        selectable-pred (fn [date] (@as-days (day-of-week date))) ; Simply allow selection based on day of week.
        label-style     {:font-style "italic" :font-size "smaller" :color "#777"}]
    (fn examples-fn []
      [v-box
       :size     "auto"
       :align    :start
       :children [[gap :size "10px"]
                  [datepicker-dropdown
                   :model         model1
                   :show-today?   @show-today?
                   :show-weeks?   @show-weeks?
                   :selectable-fn selectable-pred
                   :format        "dd MMM, yyyy"
                   :disabled?     disabled?
                   :on-change     #(reset! model1 %)]]])))

;; core holds a reference to panel, so need one level of indirection to get figwheel updates
(defn datepicker-panel
  [])
