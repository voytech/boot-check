(ns tolitius.checker.yagni
  (:require [boot.pod :as pod]
            [tolitius.core.model :refer :all]
            [tolitius.boot.helper :refer :all]
            [clojure.string :as s]))

(def yagni-deps
  '[[venantius/yagni "0.1.4" :exclusions [org.clojure/clojure]]])

;; yagni implementation (07/26/2016) is coupled with a lein specific file
(defonce entry-points-file ".lein-yagni")

(defn- pp [s]
  (s/join "\n" s))

(defn- namespace->file [namespace]
  namespace)

(defn- family-desc [family]
  (cond
    (= family :no-refs) "Could not find any references to this function"
    (= family :no-parent-refs) "The following have references to them, but their parents do not"))

(defn to-issue [namespace family]
  (->> (coords (namespace->file namespace) 0 0)
       (issue :yagni family (family-desc family))))

(defn check-graph [find-family g]
  (let [{:keys [children parents]} (find-family @g)]
    (concat (mapv #(to-issue % :no-parent-refs) (vec children))
            (mapv #(to-issue % :no-refs) (vec parents)))))
    ;(cond-> {}
    ;  (seq parents) (assoc :no-refs (set parents))
    ;  (seq children) (assoc :no-parent-refs (set children)))))

;(defn report [{:keys [no-refs no-parent-refs]}]
;  (when no-refs
;    (boot.util/warn (str "\nWARN: could not find any references to the following:\n\n" (pp no-refs) "\n")))
;  (when no-parent-refs
;    (boot.util/warn (str "\nWARN: the following have references to them, but their parents do not:\n\n" (pp no-parent-refs) "\n"))))

(defn create-entry-points [entry-points]
  (when entry-points
    (->> (interpose "\n" entry-points)
         (apply str)
         (spit entry-points-file))))

(defn check [pod-pool fileset {:keys [entry-points]}]
  (let [worker-pod (pod-pool :refresh)
        sources (fileset->paths fileset)]
    (pod/with-eval-in worker-pod
      (boot.util/dbug (str "yagni is about to look at: -- " '~sources " --"
                        (if '~entry-points
                          (str "\nwith entry points -- " '~entry-points " --")
                          "\nwith no entry points")))
      (require '[yagni.core :as yagni]
               '[yagni.graph :refer [find-children-and-parents]]
               '[tolitius.checker.yagni :refer [check-graph create-entry-points]])
      (let [graph# (binding [*ns* (the-ns *ns*)]
                     (create-entry-points '~entry-points)
                     (yagni/construct-reference-graph '~sources))
            problems# (check-graph find-children-and-parents graph#)]
        (if problems#
          {:warnings problems#
           :errors problems#}
          (boot.util/info "\nlatest report from yagni.... [You Rock!]\n"))))))
