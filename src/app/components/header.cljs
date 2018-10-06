(ns app.components.header
  (:require [reagent.core :as reagent]
            [soda-ash.core :as sa]
            [app.state :as state]
            [app.routes :as routes]
            [app.fb.auth :as auth]
            [re-frame.core :as rf]))


(defn user-preferences
  []
  [:div.userpreferences
   [:a#userPreferences
    {:href "",
     :data-tracking "click"}]
   [:div.user-info
    [:div.user-info-name (:display-name @(rf/subscribe [:user]))]
    [:div.user-info-email (:email @(rf/subscribe [:user]))]]])

(defn account-details
  []
  [:div.account-extras
   [:div "Account & Billing"]
   [:div "Integrations"]
   [:div "Profile settings"]])


(defn account-bottom
  []
  [:div.navAccountMenu-bottom
   [:div.signout
    [:a#signout]
    [:div.ui.small.teal.button
     {:on-click #(rf/dispatch [:sign-out])}
     "Sign out"]]])


(defn popup-example-user-wide
  []
  [sa/Popup {:trigger (reagent/as-element [:figure.avatar
                                            [:img {:src (:photo-url @(rf/subscribe [:user]))}]])
                      :hoverable true
                      :wide true}
            [user-preferences]
            [:hr]
            [account-details]
            [:hr]
            [account-bottom]])

(defn login-widget
  []
  [:div.right.menu
   [:a.item
    {:href "https://taxhub.freshdesk.com/support/home"
     :target "_blank"
     :data-tracking "click"}
    "Learning Center"]

   [:a.ui.item  (if @(rf/subscribe [:user])
                 [:div
                    [popup-example-user-wide]]
                 [:div.ui.teal.button
                  {:on-click #(rf/dispatch [:sign-in])}
                  "Login"])]])

(defn nav-tabs [menu-class tabs-def]
  (let [active-tab (reagent/atom (:key (first tabs-def)))]
    (fn []
      [:div
       [:div.ui.fixed.menu
        {:class menu-class}
        (doall (map (fn [{:keys [key label]}]
                      [:a.item
                       {:class    (when (= @active-tab key)
                                    "active")
                        :key key
                        :on-click #(reset! active-tab key)
                        :href (routes/url-for key)}
                       label])
                    tabs-def))
        [login-widget]]

       ^{:key @active-tab}
       [:div (->> tabs-def
                  (filter #(= @active-tab (:key %)))
                  (first))]])))

(defn nav-tabs-usage []
  [nav-tabs
   "blue inverted"
   (list {:key       :home
          :label     [:img {:src "/img/logo.png" :alt "TaxHub logo"}]}
         ;{:key       :client-list
          ;:label     "Clients"}
         ;{:key       :client-adjust
        ;:label     "Client"}
         {:key       :returns
          :label     "Returns"}
         {:key       :team
          :label     "Team"})])


(defn header
  []
  [:header
   [:div.pusher
    [:div.ui.vertical.center.aligned.segment
     [:div.ui]
     [nav-tabs-usage]]]])
