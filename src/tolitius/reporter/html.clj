(ns tolitius.reporter.html
  (:require [tolitius.core.model :as m]
            [hiccup.core :refer :all]))



(defn build-file-tree [issues]
  (let [by-files (m/group-by-file issues)]
    ()))

(defn issue->html [issue]
  (let [{:keys [linter-tool message key severity coords]} issue]
    [:div [:span key]
      [:div [:span (:file coords)]]
      [:div [:span linter-tool]]
      [:div [:span message]]
      [:div [:span severity]]]))

(defmethod m/report :html [issues options]
  (let [dom (atom [:body [:h1 "All found issues:"]])]
    (println @dom)
    (doseq [issue @issues]
      (println issue))))
