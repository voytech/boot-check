(ns tolitius.reporter.html
  (:require [tolitius.core.model :as m]
            [hiccup.core :refer :all]))


(defn build-file-tree [issues]
  (let [by-files (m/group-by-file issues)]
    ()))

(defn issue->html [issue]
  (let [{:keys [linter-tool message key severity coords]} issue]
    [:div {:class "card border-warning mb-3" :style "max-width: 40rem;"}
      [:div {:class "card-body text-warning"}
        [:h5 {:class "card-title"} message]
        [:p {:class "card-text"} key]]
      [:ul {:class "list-group list-group-flush"}
        [:li {:class "list-group-item"} (str (:file coords) " [ " (:line coords) ":" (:column coords) " ] ")]
        [:li {:class "list-group-item"} [:span "Severity"] [:b (name severity)]]]]))

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
      [:h1 "All found issues:"]
      [:div {:class "container-fluid"}
        (doall (map issue->html @issues))]]))

(defmethod m/report :html [issues options]
  (let [page (build issues options)]
    (println page)))
