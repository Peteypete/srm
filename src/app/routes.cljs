(ns app.routes
  (:require [re-frame.core :as rf]
            [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [app.events :as events]))

(def routes ["/" {""            :home
                  "team"        :team
                  "tb-adjust"   :tb-adjust
                  "returns"     :returns
                  true          :not-found}])

(defn- parse-url [url]
  (bidi/match-route routes url))

(defn- dispatch-route [matched-route]
  (let [panel-name (keyword (str (name (:handler matched-route)) "-panel"))]
    (rf/dispatch [::events/set-active-panel panel-name])))

(defn app-routes[]
  (pushy/start! (pushy/pushy dispatch-route parse-url)))

(def url-for (partial bidi/path-for routes))
