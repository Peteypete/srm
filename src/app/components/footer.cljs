(ns app.components.footer
      [:require [soda-ash.core :as sa]])


(defn footer
  []
  [:footer
    [sa/Grid
     [sa/GridColumn {:floated "left" :width 3}
      [:a.item [:img
                {:src "/img/h-logo.png"
                 :alt "TaxHub logo"
                 :height "35px"}]]]
     [sa/GridColumn {:floated "right" :width 4}
                    [:p
                     "Made with "
                     [:img.emoji
                      {:src "https://twemoji.maxcdn.com/2/svg/2764.svg"
                       :width "10rem"
                       :alt "❤"
                       :title ":heart:"
                       :draggable "false"}]
                     " in San Francisco and "
                     [:img.emoji
                      {:src "https://twemoji.maxcdn.com/2/svg/2615.svg"
                       :width "10rem"
                       :alt "☕"
                       :title ":coffee:"
                       :draggable "false"}]
                     " in Boston"]]]])
