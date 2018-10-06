(ns app.components.utils (:require [reagent.core :as r]
                                   [re-frame.core :as rf]
                                   [soda-ash.core :as sa]
                                   [app.state :as state]
                                   [app.routes :as routes]
                                   [app.subs :as subs]
                                   [clojure.string :as str]))

;; TODO move this to a better place

(rf/reg-event-db
 :write-local-debit
 (fn [db [_ newval]]
   (js/console.log ":write-local-debit: " newval)
   (assoc db :debit newval)))

(rf/reg-event-fx
  :read-debit
  (fn [{db :db} [_ status]]
    {:firebase/read-once {:path [:write-test :debit]
                          :on-success [:write-local-debit]
                          :on-failure [:my-empty "read-debit on-failure"]}}))

(rf/reg-sub
 :debit
 (fn [db]
   (get-in db [:debit])))

;; (rf/reg-sub
;;  :double-local-debit
;;  (fn [db]
;;    (get-in db [:double-local-debit])))

;; Cribbed from re-frame todoMVC example

(defn text-input [{:keys [title on-save on-stop]}]
  ;; the rf/subscribe atom is read-only, so we copy it to another atom
  ;; CHECKME is this a race condition? putting the rf/dispatch at the end is flaky
  (let [
        val (r/atom title)
        stop #(do (when on-stop (on-stop %)))
        save #(let [v (-> @val str str/trim)]
                (on-save v)
                (stop %)) 
        _ (js/console.log "val: " @val)
        ]
    (fn [props]
      [:input (merge (dissoc props :on-save :on-stop :title)
                     {:type        "text"
                      :value       @val
                      :on-blur     save
                      :auto-focus  true
                      :on-change   #(do (reset! val (-> % .-target .-value))) 
                      :on-key-down #(case (.-which %)
                                      13 (save %)
                                      27 (stop %)
                                      nil)})])))

