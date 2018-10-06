(ns app.helpers.acctcalc
  (:require 
            [clojure.string :as string]
            [goog.string :as gstring]
            [goog.string.format]))


;; -- Debugging aids ----------------------------------------------------------
;; (devtools/install!)       ;; we love https://github.com/binaryage/cljs-devtools
;; (enable-console-print!)   ;; so that println writes to `console.log`

;; -- Entry Point -------------------------------------------------------------
;; Within ../../resources/public/index.html you'll see this code
;;    window.onload = function () {
;;      todomvc.core.main();
;;    }
;; So this is the entry function that kicks off the app once the HTML is loaded.
;;

;;;
;;; A primer in accounting math.
;;; The terms "credit" and "debit" are used in a very antiquated
;;; sense, so forget what you think you know about them.
;;;
;;; Except for the Total column, which we treat separately...  If the
;;; YTD Debit/Credit field that is non-zero tells us if this is a
;;; Debit or Credit primary row.
;;;
;;; Matching Tax Adj column to the primary (ie. Credit & Credit or
;;; Debit & Debit) determines if that adjustment column is additive
;;; or subtractive.  If they are the same, add the adjustment.  If
;;; they are different, subtract.
;;;
;;; If it's the total column, ignore?  Flash red?
;;;

;;; TODO make this more robust wrt to hard coded id's, etc
;;; TODO Also should calculate total at bottom of column

;;;
;;; Note this is copied from taxnode.trial_balance
;;;

(defn to-number [f]
  (if-not (empty? f)
    (js/parseFloat f)
    0.0))

(defn calc-adj
  "Calculates simple adjusting journal entry. _type_ is :debit, :credit,
  (future types will include :total-debit, :total-credit)."
  [type ytd-debit-value ytd-credit-value tax-adj-debit tax-adj-credit]
  (let [is-total? (and (not= 0.0 ytd-debit-value)
                       (not= 0.0 ytd-credit-value))
        is-weird? (and (= 0.0 ytd-credit-value)
                       (= 0.0 ytd-debit-value))
        is-ytd-debit? (and (not= 0.0 ytd-debit-value)
                           (= 0.0 ytd-credit-value))
        is-ytd-credit?  (and (= 0.0 ytd-debit-value)
                             (not= 0.0 ytd-credit-value))
        retval (cond
                 is-weird?
                 (do
                   ;; TODO use conditionals #?(:clj foo :cljs bar)
                   (js/console.log "Something unexpected: both YTD Debit and YTD Credit are 0.0.  Can't guess what arithmatic to perform.  Ignoring adjustment.")
                   nil)
                 is-total?
                 (do                            ; TODO handle this
                   (js/console.log "TODO handle the state where both YTD Debit and YTD Credit are nonzero.  Likely this is a summary column.  Ignoring adjustment.")
                   0.0)
                 (and is-ytd-debit? (= :debit type))
                 (- (+ ytd-debit-value tax-adj-debit) tax-adj-credit)
                 (and is-ytd-credit? (= :credit type))
                 (- (+ ytd-credit-value tax-adj-credit) tax-adj-debit)
                 :else
                 nil)]
    (js/console.log "ytd-debit-value: " ytd-debit-value
                    " ytd-credit-value: " ytd-credit-value
                    " tax-adj-debit: " tax-adj-debit
                    " tax-adj-credit: " tax-adj-credit
                    " retval: " retval)
    retval))
