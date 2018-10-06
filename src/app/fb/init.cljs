(ns app.fb.init
  (:require [com.degel.re-frame-firebase :as re-fire]
            [app.fb.auth :as fb-auth]
            [re-frame.core :as rf]))

;; TODO nominally security, move these keys and urls separate from source

(defonce firebase-app-info
  {:apiKey "AIzaSyBUINzFEw1yGhtxXuXs-5cPbK-2LXegv8I",
   :authDomain "shadow-taxnode.firebaseapp.com",
   :databaseURL "https://shadow-taxnode.firebaseio.com",
   :storageBucket "gs://shadow-taxnode.appspot.com"})

(defonce firebase-app-info-staging
  {:apiKey "AIzaSyC8abNR8GgCcBc2E14uR4f5Y83ihFHuSSc",
   :authDomain "taxhub-staging.firebaseapp.com",
   :databaseURL "https://taxhub-staging.firebaseio.com",
   :storageBucket "taxhub-staging.appspot.com",})

;; == firebase-init ===========================================================
;; Initialize default app. Retrieve your own options values by adding a web app
;; on https://console.firebase.google.com
;;
;; usage: (firebase-init))
;;

(rf/reg-event-fx
 :my-empty (fn my-empty [c e other] (js/console.log (str "my-empty keys c = " (keys c) ".  e = " e))))

(defonce inited? (atom false))
(defn firebase-init
  "Initialize re-frame-firebase (implicitly firebase.js)"
  []
  (when-not @inited?
    (js/console.log "init re-frame-firebase (aka re-fire)")
    (re-fire/init :firebase-app-info      firebase-app-info-staging
                  :firestore-settings     {:timestampsInSnapshots true}
                  :get-user-sub           [:user]
                  :set-user-event         [:set-user]
                  :default-error-handler  [:my-empty :default-error-handler])
    (reset! inited? true)))
