(ns tolitius.core.model
  (:require [boot.core :as c]
            [clojure.java.io :as io]))

(defrecord Coords [file line column line-end column-end])

(defrecord Issue [linter-tool
                  category
                  key
                  severity
                  message
                  coords
                  issue-form
                  hint-form])
(defn coords
  ([file line column]
   (coords file line column nil nil))
  ([file line column line-end column-end]
   (Coords. file line column line-end column-end)))

(defn issue
  ([linter message coords]
   (issue linter nil message coords))
  ([linter key message coords]
   (issue linter nil key message coords))
  ([linter category key message coords]
   (issue linter category key message coords :normal))
  ([linter category key message coords severity]
   (issue linter category key message coords severity nil))
  ([linter category key message coords severity issue-form]
   (issue linter category key message coords severity issue-form nil))
  ([linter category key message coords severity issue-form hint-form]
   (Issue. linter category key severity message coords issue-form hint-form)))

(defn group-by-category [issues])

(defn group-by-linter [issues])

(defn group-by-key [issues])

(defn group-by-severity [issues])

(defn group-by-file [issues]
  (group-by #(->> % :coords :file) issues))

(defn load-issues [fileset]
  (if-let [issues (->> fileset c/input-files (c/by-name ["issues.edn"]) first)]
    (read-string (-> issues c/tmp-file slurp))
    []))

(defn append-issues [fileset tmpdir issues]
  (c/empty-dir! tmpdir)
  (let [content (concat (load-issues fileset) issues)
        str-content (pr-str content)
        issues-file (io/file tmpdir "issues.edn")]
     (doto issues-file
        io/make-parents
        (spit str-content))
     (let [new (-> fileset (c/add-source tmpdir))]
       (c/commit! new))))

(defn- make-issue-handler [issues options]
  (fn [issue]
    (swap! issues conj issue)))

(defn issues-handling [options]
  (let [issues (atom [])]
    {:on-issue (make-issue-handler issues options)
     :issues issues}))
