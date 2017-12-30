(ns tolitius.reporter.html
  (:require [tolitius.core.model :refer :all]
            [tolitius.reporter.abstract :as r]
            [hiccup.core :refer :all]))

(defonce dom (atom {}))

(defmethod r/init-report :html [options]
  (reset! dom [:body [:h1 "All found issues:"]]))

(defmethod r/finish-report :html [options]
  (println "=====report ready======")
  (println (html @dom)))


(defn issue-html [issue]
  (let [{:keys [linter-tool message key severity coords]} issue]
    [:div [:span key]
      [:div [:span (:file coords)]]
      [:div [:span linter-tool]]
      [:div [:span message]]
      [:div [:span severity]]]))


(defmethod reporter-aware-issue-handler :html [options]
   (fn [issue]
     (swap! dom conj (issue-html issue))))
