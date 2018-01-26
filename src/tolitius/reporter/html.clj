(ns tolitius.reporter.html
  (:require [tolitius.core.model :as m]
            [tolitius.core.reporting :as r]
            [hiccup.core :refer :all]))



(defn- severity-style [severity]
  (cond
    (= severity "normal") "table-warning"
    (= severity "severe") "table-danger"
    :else ""))

(defn- report-header []
  [:nav {:class "navbar navbar-inverse bg-faded"}
    [:a {:class "navbar-brand" :href "#"}
     [:img {:src "logo.svg"  :width "50px" :class "rounded img-thumbnail"}]
     "oot Check Report"]])

(defn- render-line [nr content warning-line]
  [:code {:class "row" :style (str (if (= warning-line nr) "background-color:red;color:white;" "background-color:white;color:black;") "white-space:pre-wrap;word-wrap:break-word;")}
      (str nr "." content)])

(defn- code-snippet [issue]
  (let [{:keys [snippet coords]} issue]
    (reduce (fn [val nxt] (conj val (render-line (first nxt) (last nxt) (:line coords)))) [:div {:class "grid"}] snippet)))

(defn- issue-details [issue]
  (let [{:keys [linter-tool message key severity coords]} issue]
    [:div {:class "card border-warning mb-3" :style "max-width: 40rem;"}
      [:div {:class "card-body text-warning"}
        [:h5 {:class "card-title"} message]
        [:p {:class "card-text"} key]]
      [:ul {:class "list-group list-group-flush"}
        [:li {:class "list-group-item"} (str (:file coords) " [ " (:line coords) ":" (:column coords) " ] ")]
        [:li {:class "list-group-item"} [:span "Severity"] [:b (name severity)]]]]))

(defn- issue-table-cell [issue]
  (let [{:keys [id linter-tool message key severity coords]} issue]
    [:tr {:class (severity-style severity)}
      [:td linter-tool]
      [:td key]
      [:td message]
      [:td (:file coords)]
      [:td (str "[ " (:line coords) ":" (:column coords) " ]")]
      [:td severity]
      [:td [:a {:href "#" :data-toggle "modal" :data-target (str "#" id) } [:span "more"]]]]))

(defn- insert-rows [aggr issue]
  (conj aggr (issue-table-cell issue)))

(defn- issues-with-snippet [issues]
  (filterv #(not (nil? (:snippet %))) issues))

(defn- snippet-modal [issue]
  [:div {:class "modal fade bd-example-modal-lg" :id (:id issue) :tabindex -1 :role "dialog" :aria-hidden true}
    [:div {:class "modal-dialog modal-lg" :role "document"}
      [:div {:class "modal-content"}
        [:div {:class "modal-body"}
          (code-snippet issue)]]]])

(defn- snippets [issues]
  (reduce (fn [val nxt] (conj val (snippet-modal nxt))) [:div {:id "snippets-container"}] (issues-with-snippet issues)))

(defn- build [issues options]
  (html
    [:head
      [:link {:rel "stylesheet" :href "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css"}]
      [:link {:rel "stylesheet" :href "https://cdn.datatables.net/1.10.16/css/dataTables.bootstrap4.min.css"}]
      [:script {:src "https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"}]
      [:script {:src "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js"}]
      [:script {:src "https://cdn.datatables.net/1.10.16/js/jquery.dataTables.min.js"}]
      [:script {:src "https://cdn.datatables.net/1.10.16/js/dataTables.bootstrap4.min.js"}]
      [:script  "$(document).ready(function() {$('#issuestable').DataTable();});"]]

    [:body
      (report-header)
      [:h5 {:class "p-3"} "All reported warnings:"]
      [:div {:class "container-fluid"}
        (snippets issues)
        [:div {:id "responsive-wrapper" :class "table-responsive"}
          [:table {:id "issuestable" :role "grid" :class "table table-sm table-striped table-hover table-bordered"}
            [:thead
              [:th "Tool"]
              [:th "Type"]
              [:th "Message"]
              [:th "File"]
              [:th "Location"]
              [:th "Severity"]
              [:th "See Details"]]
            (reduce insert-rows [:tbody] issues)]]]]))

(defmethod r/report :html [issues options]
  (boot.util/dbug "reporting to html")
  (let [page (build issues options)]
    (spit "issues.html" page)
    page))
