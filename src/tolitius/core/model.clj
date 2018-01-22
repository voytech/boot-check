(ns tolitius.core.model)

(defrecord Coords [file line column line-end column-end])

(defrecord Issue [linter-tool
                  category
                  key
                  severity
                  message
                  coords
                  issue-form
                  hint-form
                  custom-attributes])
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
   (Issue. linter category key severity message coords issue-form hint-form nil)))

(defn group-by-category [issues])

(defn group-by-linter [issues])

(defn group-by-key [issues])

(defn group-by-severity [issues])

(defn group-by-file [issues]
  (group-by #(->> % :coords :file) issues))
