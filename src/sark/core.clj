0;; -*- coding: utf-8 -*-
;;
;; © 2013 Ian Eure.
;; Author: Ian Eure <ieure@simple.com>
;;
(ns sark.core
  (:require [clj-http.client :as client]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clucy.core :as clucy]
            [sark.analyzer :as anal])
  (:import [org.apache.lucene.queryparser.classic ParseException]))

(def index (atom nil))

(def ^:const arcarc-base "http://arcarc.xmission.com/")
(def ^:const arcarc-index (format "%s/index.txt" arcarc-base))

#_(defn fetch-index
  "Fetch the ArcArc index, returning a seq of filenames."
  []
  (-> (client/get arcarc-index {:as :stream})
      (:body)
      (io/reader)
      (line-seq)))

(defn fetch-index []
  (-> (io/resource "index.txt")
      (io/reader)
      (line-seq)))

(def ^:constant shitpost-re
  "A regexp of files which won't be indexed."
  #"(^(arcade by title|pictures|utils|icons fonts etc|kits and hacks|magazines and books|pinball/pictures|videos and sounds|web archives).*|.*thumbs.db$|.*index.txt.*|)")

(defn shitpost? [name]
  "Is this file a shitpost?"
  (re-matches shitpost-re (str/lower-case name)))

(defn clean-strip-punc [text]
  "Strip underscores."
  (str/replace text #"_" " "))

(defn clean-strip-extension [text]
  "Strip file extensions."
  (let [li (.lastIndexOf text ".")]
    (subs text 0 (if  (> li 0) li (count text)))))

(defn clean-fix-typos [text]
  "Fix typos in ArcArc."
  (str/replace text #"[Cc]ab[ea]rat" "Cabaret"))

(defn clean-strip-prefix [text]
  "Strip the prefix for some types."
  (cond
   (.startsWith text "PDF") (let [li (.lastIndexOf text "/")]
                              (subs text (if (> li 0) (+ li 1) 0)))
   (.startsWith text "Tech") (subs text 5)
   true text))

(defn clean-name [name]
  "Clean filename."
  (-> (clean-strip-punc name)
      (clean-strip-extension)
      (clean-fix-typos)
      (clean-strip-prefix)
      (str/trim)))

(defn boost [name]
  "How much should we boost this document?"
  (cond (.startsWith name "PDF") 1.0
        true 0.0))

(defn make-doc [name]
  "Turn a filename into a document to index."
  (with-meta {:name (clean-name name), :url (str arcarc-base name)}
              {:url {:indexed false
                     :stored true}
               :name {:boost (boost name)}}))

(defn build-index
  "Create an index of filenames."
  ([names] (build-index (clucy/memory-index) names))

  ([i names]
     (anal/with-standard-analyzer
       (apply clucy/add i (map make-doc (remove shitpost? names))))
     i))

(defn init []
  "Initialize the index."
  (reset! index (build-index (fetch-index))))

(defn search [index terms & [limit]]
  (anal/with-standard-analyzer
    (clucy/search index terms (or limit 100)
                  :default-operator :and)))

(defn explain [index terms & [limit]]
  (anal/with-standard-analyzer
    (clucy/search index terms (or limit 100)
                  :default-operator :and :explain true)))
