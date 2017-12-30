(ns tolitius.reporter.html
  (:require [tolitius.core.model :refer :all]
            [tolitius.reporter.abstract :as r]
            [hiccup.core :refer :all]))

(defonce dom (atom {}))

(defmethod r/init-report :html [options]
  (println (html
              [:body
                [:h1 "Ass found issues:"]])))

(defn issue-html [issue]
  [:div [:span (:message issue)]])

(defmethod reporter-aware-issue-handler :html [options]
   (fn [issue]
     (let [issue-component (issue-html issue)]
      (println "==================================")
      (println issue-component)
      (println "=================================="))))
