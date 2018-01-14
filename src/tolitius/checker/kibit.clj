(ns tolitius.checker.kibit
  (:require [tolitius.boot.helper :refer :all]
            [tolitius.core.model :refer :all]
            [boot.core :as core]
            [boot.pod  :as pod]))

(def kibit-deps
  '[[jonase/kibit "0.1.5"]
    [org.clojure/tools.cli "0.3.3"]])

;;Kibit does not report file :(  - it is a bug.
(defn normalise-issue [warning]
  (let [{:keys [expr alt line column]} warning
        msg (str "Replace [ " (pr-str expr) " ] with [ " (pr-str alt) " ]")
        file "-"
        linter "kibit"]
    (->> (coords file line column)
         (issue :kibit linter msg))))

(defn check [pod-pool fileset & args]
  (let [worker-pod (pod-pool :refresh)
        namespaces (pod/with-eval-in worker-pod
                     (all-ns* ~@(->> fileset
                                     core/input-dirs
                                     (map (memfn getPath)))))
        sources (fileset->paths fileset)]
    (pod/with-eval-in worker-pod
      (boot.util/dbug (str "kibit is about to look at: -- " '~sources " --"))
      (require '[kibit.driver :as kibit]
               '[tolitius.checker.kibit :as checker])
      (doseq [ns '~namespaces] (require ns))
      (let [problems# (apply kibit.driver/run '~sources nil '~args)]   ;; nil for "rules" which would expand to all-rules,
        (if-not (zero? (count problems#))
          (do
            (boot.util/warn (str "\nWARN: kibit found some problems: \n\n" {:problems (set problems#)} "\n"))
            {:warnings (mapv checker/normalise-issue (vec problems#))
             :errors problems#})
          (boot.util/info "\nlatest report from kibit.... [You Rock!]\n"))))))
