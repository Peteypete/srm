(ns app.subs
  (:require [re-frame.core :as rf]
            [app.state :as state]))
        


(rf/reg-sub
  :clients
  (fn [db]
    (:clients db)))

(rf/reg-sub
  :active-client
  (fn [db]
   (get-in db [:user :active-client] #{})))

;there can only be one active client
(rf/reg-sub
  :active-client?
  (fn []
    (rf/subscribe [:active-client]))
  (fn [active-client [_ id]]
    (= active-client id)))

(rf/reg-sub
  :favorite-client
  (fn [db]
   (get-in db [:user :favorite-client] #{})))

;there can be many favorited cleints
(rf/reg-sub
  :favorite-client?
  (fn []
    (rf/subscribe [:favorite-client]))
  (fn [favorite-client [_ id]]
    (contains? favorite-client id)))

(rf/reg-sub
  :users
  (fn [db]
    (:users db)))
