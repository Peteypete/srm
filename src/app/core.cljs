(ns app.core
  (:require [reagent.core :as r]
            [app.views :as views]
            [com.degel.re-frame-firebase :as re-fire]))

(defn ^:dev/after-load start
  []
  (r/render-component [views/app]
                      (.getElementById js/document "app")))

(defn ^:export main
  []
  (start))
