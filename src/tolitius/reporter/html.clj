(ns tolitius.reporter.html
  (:require [tolitius.core.model :as m]
            [tolitius.core.reporting :as r]
            [hiccup.core :refer :all]))


(defn build-file-tree [issues]
  (let [by-files (m/group-by-file issues)]
    ()))

(defn- severity-style [severity]
  (cond
    (= severity "normal") "table-warning"
    (= severity "severe") "table-danger"
    :else ""))

(defn issue-details [issue]
  (let [{:keys [linter-tool message key severity coords]} issue]
    [:div {:class "card border-warning mb-3" :style "max-width: 40rem;"}
      [:div {:class "card-body text-warning"}
        [:h5 {:class "card-title"} message]
        [:p {:class "card-text"} key]]
      [:ul {:class "list-group list-group-flush"}
        [:li {:class "list-group-item"} (str (:file coords) " [ " (:line coords) ":" (:column coords) " ] ")]
        [:li {:class "list-group-item"} [:span "Severity"] [:b (name severity)]]]]))


(defn issue-table-cell [issue]
  (let [{:keys [linter-tool message key severity coords]} issue]
    [:tr {:class (severity-style severity)}
      [:td linter-tool]
      [:td key]
      [:td message]
      [:td (:file coords)]
      [:td (str "[ " (:line coords) ":" (:column coords) " ]")]
      [:td severity]]))

(defn build [issues options]
  (html
    [:head
      [:link {:rel "stylesheet" :href "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.3/css/bootstrap.min.css"
              :integrity "sha384-Zug+QiDoJOrZ5t4lssLdxGhVrurbmBWopoEl+M6BdEfwnCJZtKxi1KgxUyJq13dy"
              :crossorigin "anonymous"}
       [:script {:src "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.3/js/bootstrap.min.js"
                 :integrity "sha384-a5N7Y/aK3qNeh15eJKGWxsqtnX/wWdSZSKp+81YjTmS15nvnvxKHuzaWwXHDli+4"
                 :crossorigin "anonymous"}]]]
    [:body
      [:h1 "Found issues:"]
      [:div {:class "container-fluid"}
        [:table {:class "table table-sm table-responsive table-striped"}
          [:thead
            [:th "Tool"]
            [:th "Type"]
            [:th "Message"]
            [:th "File"]
            [:th "Cursor"]
            [:th "Severity"]]
          [:tbody
           (doall (map issue-table-cell issues))]]]]))

(defmethod r/report :html [issues options]
  (println "reporting to html")
  (let [page (build issues options)]
    (spit "issues.html" page)))
