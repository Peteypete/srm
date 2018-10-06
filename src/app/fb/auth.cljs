(ns app.fb.auth
  (:require [re-frame.core :as rf]
            [com.degel.re-frame-firebase :as re-fire]
            [cljs.core.async :refer [<! timeout go]]))


(rf/reg-event-fx
 :write-user-to-users
 ;; An authenticated user is memorialized into the :users key
 (fn [{db :db} [_ my-user]]
   (js/console.log ":write-user-to-users got my-user: " my-user)
   {:firebase/write {:path [:users (:uid my-user) :profile]
                     :value my-user
                     :on-success #(js/console.log "Wrote user: " my-user " into :users.")
                     :on-failure [:my-empty]}}))


;;; Simple sign-in event. Just trampoline down to the re-frame-firebase
;;; fx handler.
(rf/reg-event-fx
 :sign-in
 (fn [_ _] (js/console.log "firebase-sign-in received.  Starting...")
   (rf/dispatch [:events/set-active-panel :home-panel]) ; TODO FIXME (:active-panel db) gets munged on login/logouts
   {:firebase/google-sign-in {:sign-in-method :popup}}))


;;; Ditto for sign-out
(rf/reg-event-fx
 :sign-out
 (fn [db _] (js/console.log "firebase-sign-out received.  Invoking... db: " db " post assoc: " (assoc db :user nil))
   (go                                  ; TODO FIXME also related to (:active-panel db) munging
     (<! (timeout 500))
     (set! (.-location js/window)  "/"))
   {:db (assoc db :user nil)
    :firebase/sign-out nil
    }))

;;; Store the user object
(rf/reg-event-db
 :set-user
 (fn [db [_ user]]
   (let
       ;; [user (clj->js raw-user)]
       [
        uid (:uid user)
        display-name (:display-name user)
        photo-url (:photo-url user)
        email (:email user)
        ;; uid (.-uid user)
        ;; display-name (.-displayName user)
        ;; photo-url (.-photoURL user)
        ;; email (.-email user)
        my-user {:photo-url photo-url
                 :display-name display-name
                 :email email
                 :uid uid}]
     (js/console.log ":set-user event received.  user: " user " db: " db)
     (when uid
       (rf/dispatch [:write-user-to-users my-user])
       (assoc db :user my-user)))))

;;; A subscription to return the user to the library
(rf/reg-sub
  :user
  (fn [db _]
    (js/console.log ":user subscription invocation:... " (:user db)) (:user db)))
