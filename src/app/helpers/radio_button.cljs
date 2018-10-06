;not currently used
(ns app.components.radio-button
  (:require [re-com.core   :refer [h-box v-box box gap line radio-button p]]
            [re-com.misc   :refer [radio-button-args-desc]]
            [app.components.utils :refer [panel-title title2 title3 args-table github-hyperlink status-text]]
            [reagent.core  :as    reagent]))


(defn radios-demo
  []
  (let [color (reagent/atom "green")]
    (fn
      []
      [v-box
       :gap      "10px"
       :children [[title3 "JE Type"]
                  [v-box
                   :children [(doall (for [c ["Book JE" "Tax JE"]]    ;; Notice the ugly "doall"
                                       ^{:key c}                        ;; key should be unique among siblings
                                       [radio-button
                                        :label       c
                                        :value       c
                                        :model       color
                                        :label-style (if (= c @color) {:color       c
                                                                        :font-weight "bold"})
                                        :on-change   #(reset! color %)]))]]]])))


;; core holds a reference to panel, so need one level of indirection to get figwheel updates
(defn panel
  []
  [radios-demo])
