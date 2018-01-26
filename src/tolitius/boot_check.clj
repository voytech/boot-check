(ns tolitius.boot-check
  {:boot/export-tasks true}
  (:require [tolitius.checker.yagni :as yagni :refer [yagni-deps]]
            [tolitius.checker.kibit :as kibit :refer [kibit-deps]]
            [tolitius.checker.eastwood :as eastwood :refer [eastwood-deps]]
            [tolitius.checker.bikeshed :as bikeshed :refer [bikeshed-deps]]
            [tolitius.boot.helper :refer :all]
            [tolitius.core.model :as m]
            [tolitius.core.reporting :as r]
            [tolitius.reporter.html :refer :all]
            [boot.core :as core :refer [deftask]]
            [clojure.java.io :as io]
            [boot.pod  :as pod]))

(defn store-tmp-file [fileset tmpdir content filename]
  (core/empty-dir! tmpdir)
  (let [content-file (io/file tmpdir filename)]
    (doto content-file
      io/make-parents
      (spit content))
    (let [new (-> fileset (core/add-source tmpdir))]
      (core/commit! new))))

(defn load-issues [fileset]
  (if-let [issues (->> fileset core/input-files (core/by-name ["issues.edn"]) first)]
    (read-string (-> issues core/tmp-file slurp))
    []))

(defn append-issues [fileset tmpdir issues]
  (let [content (concat (load-issues fileset) issues)
        str-content (pr-str content)]
    (store-tmp-file fileset tmpdir str-content "issues.edn")))

(defn write-report [fileset tmpdir report]
  (store-tmp-file fileset tmpdir report "report.html"))

(def pod-deps
  '[[org.clojure/tools.namespace "0.2.11" :exclusions [org.clojure/clojure]]])

(defn bootstrap [fresh-pod]
  (doto fresh-pod
    (pod/with-eval-in
     (require '[clojure.java.io :as io]
              '[clojure.tools.namespace.find :refer [find-namespaces-in-dir]]
              '[tolitius.boot.helper :refer :all])

     (defn all-ns* [& dirs]
       (distinct (mapcat #(find-namespaces-in-dir (io/file %)) dirs))))))

(defn with-result [fileset tmpdir f]
  (let [{:keys [warnings]} (f)]
    (when warnings
      (append-issues fileset tmpdir warnings))))

(deftask with-kibit
  "Static code analyzer for Clojure, ClojureScript, cljx and other Clojure variants.

  This task will run all the kibit checks within a pod.

  At the moment it takes no arguments, but behold..! it will. (files, rules, reporters, etc..)"
  ;; [f files FILE #{sym} "the set of files to check."]      ;; TODO: convert these to "tmp-dir/file"
  []
  (let [pod-pool (make-pod-pool (concat pod-deps kibit-deps) bootstrap)
        tmpdir (core/tmp-dir!)]
    (core/with-pre-wrap fileset
      (with-result fileset tmpdir #(kibit/check pod-pool fileset)))))          ;; TODO with args

(deftask with-yagni
  "Static code analyzer for Clojure that helps you find unused code in your applications and libraries.

  This task will run all the yagni checks within a pod."
  [o options OPTIONS edn "yagni options EDN map"]
  (let [pod-pool (make-pod-pool (concat pod-deps yagni-deps) bootstrap)
        tmpdir (core/tmp-dir!)]
    (core/with-pre-wrap fileset
      (with-result fileset tmpdir #(yagni/check pod-pool fileset options)))))  ;; TODO with args

(deftask with-eastwood
  "Clojure lint tool that uses the tools.analyzer and tools.analyzer.jvm libraries to inspect namespaces and report possible problems

  This task will run all the eastwood checks within a pod.

  At the moment it takes no arguments, but behold..! it will. (linters, namespaces, etc.)"
  ;; [f files FILE #{sym} "the set of files to check."]      ;; TODO: convert these to "tmp-dir/file"
  [o options OPTIONS edn "eastwood options EDN map"]
  (let [pod-pool (make-pod-pool (concat pod-deps eastwood-deps) bootstrap)
        tmpdir (core/tmp-dir!)]
    (core/with-pre-wrap fileset
      (with-result fileset tmpdir #(eastwood/check pod-pool fileset options)))))

(deftask with-bikeshed
  "This task is backed by 'lein-bikeshed' which is designed to tell you your code is bad, and that you should feel bad

  This task will run all the bikeshed checks within a pod.

  At the moment it takes no arguments, but behold..! it will. ('-m, --max-line-length', etc.)"
  ;; [f files FILE #{sym} "the set of files to check."]       ;; TODO: convert these to "tmp-dir/file"
  [o options OPTIONS edn "bikeshed options EDN map"]
  (let [pod-pool (make-pod-pool (concat pod-deps bikeshed-deps) bootstrap)
        tmpdir (core/tmp-dir!)]
    (core/with-pre-wrap fileset
      (with-result fileset tmpdir #(bikeshed/check pod-pool fileset options)))))  ;; TODO with args

(deftask with-checker
  "This task currently does nothing but it will allow for extending standard set of code checkers with new ones by implementing check multimethod."
  []
  (throw (ex-info "Not YET implemented.")))

(deftask report
  "This task dispatches reporting into specific report multimethod which is choosen depending on options :reporter key.

  Currently only hiccup html reporter is implemented,  but you can register reporter of your choice by implementing core.reporting/report multimethod.

  Custom reporter implementations needs to consume vector of tolitius.core.model/Issue records as this is the persistent model of all warnings reported by checkers

  backed by boot filesets during pipline execution."

  [o options OPTIONS edn "Reporting options"]
  (let [tmpdir (core/tmp-dir!)]
    (core/with-pre-wrap fileset
      (when-let [issues (load-issues fileset)]
        (let [report-content (r/report issues options)]
          (write-report fileset tmpdir report-content))))))

(deftask throw-exception
  "This task provides caller with exception when some of code checkers reports warnings.

  Using this task makes sense when You want to skip later tasks within the pipline as your

  rigorous policy assumes every line of code to be perfect ;-)

  When using this task You decide when to throw an exception. You may want to throw exception after

  particular checker or after all checkers has completed"

  []
  (core/with-pre-wrap fileset
    (when-let [issues (load-issues fileset)]
      (throw (ex-info "Some of code checkers have failed." {:causes issues})))))
