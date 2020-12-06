;; -*- coding: utf-8 -*-
;;
;; eeeee eeeee eeeee  e   e
;; 8   " 8   8 8   8  8   8
;; 8eeee 8eee8 8eee8e 8eee8e
;;    88 88  8 88   8 88   8
;; 8ee88 88  8 88   8 88   8
;;
;; © 2013, 2014, 2020 Ian Eure.
;; Author: Ian Eure <ieure@simple.com>
;;
(ns sark.core
  (:require [clojure.string :as str]
            [taoensso.timbre :as log]
            [clucy.core :as clucy]
            [sark.arcarc :as arcarc]
            [sark.analyzer :as anal]
            [sark.meters :as meters])
  (:import [org.apache.lucene.queryparser.classic ParseException]
           [org.apache.lucene.index DirectoryReader]
           [org.apache.lucene.store Directory]))

(def ^:const update-interval (* 60 60 24 1000)) ; 24hb

(def index (atom nil))

(def ^:constant shitpost-re
  "A regexp of files which won't be indexed."
  #"(^(arcade by title|pictures|utils|icons fonts etc|kits and hacks|magazines and books|pinball/pictures|videos and sounds|web archives).*|.*thumbs.db$|.*index.txt.*|)")

(defn shitpost? [name] "Is this file a shitpost?"
  (re-matches shitpost-re (str/lower-case name)))

(defn clean-strip-punc [text] "Strip underscores."
  (str/replace text #"_" " "))

(defn clean-strip-extension [text] "Strip file extensions."
  (let [li (.lastIndexOf text ".")]
    (subs text 0 (if  (> li 0) li (count text)))))

(defn clean-fix-typos [text] "Fix typos in ArcArc."
  (str/replace text #"[Cc]ab[ea]rat" "Cabaret"))

(defn clean-strip-prefix [text] "Strip the prefix for some types."
  (cond
   (.startsWith text "PDF") (let [li (.lastIndexOf text "/")]
                              (subs text (if (> li 0) (+ li 1) 0)))
   (.startsWith text "Tech") (subs text 5)
   true text))

(defn clean-name [name] "Clean filename."
  (-> (clean-strip-punc name)
      (clean-strip-extension)
      (clean-fix-typos)
      (clean-strip-prefix)
      (str/trim)))

(defn boost [name] "How much should we boost this document?"
  (cond (.startsWith name "PDF") 1.0
        true 0.0))

(defn make-doc [name] "Turn a filename into a document to index."
  (with-meta {:name (clean-name name), :url (str arcarc/base name)}
              {:url {:indexed false
                     :stored true}
               :name {:boost (boost name)}}))

(defn build-index "Create an index of filenames."
  ([names] (build-index (clucy/memory-index) names))

  ([i names]
     (anal/with-analyzer
       (apply clucy/add i (map make-doc (remove shitpost? names))))
     i))

(defn update! []
  (when-let [updated-state (arcarc/fetch-update)]
    (log/info "Reindexing")
    (reset! arcarc/state updated-state)
    (reset! index (build-index (:files updated-state)))))

(defn update-periodically! []
  (log/infof "Refreshing index every %dms" update-interval)
  (doto (Thread. update!)
    (.setDaemon true)
    (.start)))

(defn init-cache! []
  (reset! index (build-index (:files (arcarc/load-cache!)))))

(defn init [] "Initialize Sark."
  (init-cache!)
  (update!))

(defn search [index terms & [limit]]
  (anal/with-analyzer
    (let [res (clucy/search index terms (or limit 100)
                            :default-operator :and)]
      (meters/searched! res))))

(defn explain [index terms & [limit]]
  (anal/with-analyzer
    (clucy/search index terms (or limit 100)
                  :default-operator :and :explain true)))

(defn stats [] "Return statistics about Sark."
  (with-open [r (DirectoryReader/open ^Directory @index)]
    {:disk {:index-last-updated (:lmd @arcarc/state)
            :documents (.numDocs r)}
     :mem (arcarc/status)
     :searches (meters/stats)
     :clicks @meters/clicked}))
