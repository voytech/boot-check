(ns tolitius.checker.eastwood
  (:require [tolitius.boot.helper :refer :all]
            [tolitius.core.model :refer :all]
            [tolitius.core.check :as ch]
            [boot.pod  :as pod]))

(defmethod ch/checker-deps :eastwood [checker]
  '[[jonase/eastwood "0.2.5" :exclusions [org.clojure/clojure]]])

(defn eastwood-linting-callback [files handle-issue options]
  (fn [{:keys [warn-data kind] :as data}]
    (when (= :lint-warning kind)
      (let [{:keys [file line column linter msg form]} warn-data
            issue (issue :eastwood linter msg (coords file line column) nil)]
        (if-let [warn-contents (load-issue-related-file-part files issue 5)]
          (handle-issue (assoc issue :snippet warn-contents))
          (handle-issue issue))))))

(defmethod ch/check :eastwood [checker pod-pool fileset options & args]
  (let [worker-pod (pod-pool :refresh)
        inputs (fileset->paths fileset)
        exclude-linters (:exclude-linters options)]
    (pod/with-eval-in worker-pod
      (require '[eastwood.lint :as eastwood]
               '[tolitius.checker.eastwood :as checker]
               '[tolitius.core.model :as m]
               '[hiccup.core :refer :all])

      (let [sources# #{~@(tmp-dir-paths fileset)}
            _ (boot.util/dbug (str "eastwood is about to look at: -- " sources# " --"))
            {:keys [some-warnings] :as checks} (eastwood/eastwood {:source-paths sources#
                                                                   :exclude-linters ~exclude-linters
                                                                   :debug #{:compare-forms}})

            issues# (atom #{})]
        (if some-warnings
          (do
            (boot.util/warn (str "\nWARN: eastwood found some problems ^^^ \n\n"))
            (eastwood/eastwood-core (eastwood/last-options-map-adjustments  ;; TODO rerun to get the actual errors, but otherwise need to rewrite eastwood/eastwood
                                        {:source-paths sources#
                                         :callback (checker/eastwood-linting-callback ~inputs #(swap! issues# conj %) ~options)}))
            {:warnings (vec @issues#)})
          (do
            (boot.util/info "\nlatest report from eastwood.... [You Rock!]\n")
            {:warnings []}))))))    
