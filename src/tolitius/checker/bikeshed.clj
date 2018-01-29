(ns tolitius.checker.bikeshed
  (:require [tolitius.boot.helper :refer :all]
            [tolitius.core.check :as ch]
            [boot.pod :as pod]))

(defmethod ch/checker-deps :bikeshed [checker]
  '[[org.clojure/clojure "1.8.0"]
    [lein-bikeshed "0.5.1" :exclusions [org.clojure/tools.cli
                                        org.clojure/tools.namespace]]])

(defmethod ch/check :bikeshed [checker pod-pool fileset & args]
  (let [worker-pod (pod-pool :refresh)]
    (pod/with-eval-in worker-pod
      (require '[bikeshed.core])
      (let [sources# ~(tmp-dir-paths fileset)
            _ (boot.util/info (str "bikeshed is about to look at: -- " sources# " --"))
            args# (update ~@args :check? #(merge % {}))
            problems# (bikeshed.core/bikeshed {:source-paths sources#} args#)]
        (if problems#
          (boot.util/warn (str "\nWARN: bikeshed found some problems ^^^ \n"))
          (boot.util/info "\nlatest report from bikeshed.... [You Rock!]\n"))
        {:warnings (or problems# [])}))))
