(ns tolitius.core.model)

(defrecord Coords [file line column line-end column-end])

(defrecord Issue [id
                  linter-tool
                  category
                  key
                  severity
                  message
                  coords
                  snippet
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
   (issue linter nil message coords nil))
  ([linter message coords snippet]
   (issue linter nil message coords snippet))
  ([linter key message coords snippet]
   (issue linter nil key message coords snippet))
  ([linter category key message coords snippet]
   (issue linter category key message coords :normal snippet))
  ([linter category key message coords severity snippet]
   (issue linter category key message coords severity snippet nil))
  ([linter category key message coords severity snippet issue-form]
   (issue linter category key message coords severity snippet issue-form nil))
  ([linter category key message coords severity snippet issue-form hint-form]
   (Issue. (str (java.util.UUID/randomUUID)) linter category key severity message coords snippet issue-form hint-form nil)))

(defn group-by-category [issues])

(defn group-by-linter [issues])

(defn group-by-key [issues])

(defn group-by-severity [issues])

(defn group-by-file [issues]
  (group-by #(->> % :coords :file) issues))
