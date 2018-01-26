(ns tolitius.checker.bikeshed
  (:require [tolitius.boot.helper :refer :all]
            [tolitius.core.check :as ch]
            [boot.pod :as pod]))

(defmethod ch/checker-deps :bikeshed [checker]
  '[[lein-bikeshed "0.4.1" :exclusions [org.clojure/tools.cli
                                        org.clojure/tools.namespace]]])

(defmethod ch/check :bikeshed [checker pod-pool fileset & args]
  (let [worker-pod (pod-pool :refresh)]
    (pod/with-eval-in worker-pod
      (require '[bikeshed.core])
      (let [sources# ~(tmp-dir-paths fileset)
            _ (boot.util/dbug (str "bikeshed is about to look at: -- " sources# " --"))
            problems# (apply bikeshed.core/bikeshed {:source-paths sources#} [~@args])]
        (if problems#
          (do
            (boot.util/warn (str "\nWARN: bikeshed found some problems ^^^ \n"))
            {:errors problems#})
          (boot.util/info "\nlatest report from bikeshed.... [You Rock!]\n"))))))
