(ns tolitius.checker.eastwood
  (:require [tolitius.boot.helper :refer :all]
            [tolitius.core.model :refer :all]
            [boot.pod  :as pod]))

(def eastwood-deps
  '[[jonase/eastwood "0.2.5" :exclusions [org.clojure/clojure]]])

(defn eastwood-linting-callback [handle-issue options]
  (fn [{:keys [warn-data kind]}]
    (when (= :lint-warning kind)
      (let [{:keys [file line column linter msg form]} warn-data
            issue (->> (coords file line column)
                       (issue :eastwood linter msg))]
        (handle-issue issue)))))

(defn check [pod-pool fileset options & args]
  (let [worker-pod (pod-pool :refresh)
        exclude-linters (:exclude-linters options)]
    (pod/with-eval-in worker-pod
      (require '[eastwood.lint :as eastwood]
               '[tolitius.checker.eastwood :as checker]
               '[tolitius.reporter.html :as h]
               '[tolitius.core.model :as m]
               '[hiccup.core :refer :all])

      (let [sources# #{~@(tmp-dir-paths fileset)}
            _ (boot.util/dbug (str "eastwood is about to look at: -- " sources# " --"))
            {:keys [some-warnings] :as checks} (eastwood/eastwood {:source-paths sources#
                                                                   :exclude-linters ~exclude-linters
                                                                   :debug #{:compare-forms}})
            {:keys [handle-issue handle-finished]} (m/init ~options)]
        (if some-warnings
          (do
            (boot.util/warn (str "\nWARN: eastwood found some problems ^^^ \n\n"))
            {:errors (eastwood/eastwood-core (eastwood/last-options-map-adjustments  ;; TODO rerun to get the actual errors, but otherwise need to rewrite eastwood/eastwood
                                               {:source-paths sources#
                                                :callback (checker/eastwood-linting-callback handle-issue ~options)}))}
            (handle-finished))
          (boot.util/info "\nlatest report from eastwood.... [You Rock!]\n"))))))
