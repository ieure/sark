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

(def ^:constant shitposts
  #{"arcade by title"
    "pictures"
    "utils"
    "icons fonts etc"
    "kits and hacks"
    "magazines and books"
    "pinball/pictures"
    "videos and sounds"
    "web archives"})

(def ^:constant shitpost-re
  #"(^(arcade by title|pictures|utils|icons fonts etc|kits and hacks|magazines and books|pinball/pictures|videos and sounds|web archives).*|.*thumbs.db$|.*index.txt.*|)")

(defn shitpost? [name]
  (re-matches shitpost-re (str/lower-case name)))

(defn clean-strip-punc [text]
  (str/replace text #"_" " "))

(defn clean-strip-extension [text]
  (let [li (.lastIndexOf text ".")]
    (subs text 0 (if  (> li 0) li (count text)))))

(defn clean-fix-typos [text]
  (str/replace text #"[Cc]ab[ea]rat" "Cabaret"))

(defn clean-strip-prefix [text]
  (cond
   (.startsWith text "PDF") (let [li (.lastIndexOf text "/")]
                              (subs text (if (> li 0) (+ li 1) 0)))
   (.startsWith text "Tech") (subs text 5)
   true text))

(defn clean-name [name]
  (-> (clean-strip-punc name)
      (clean-strip-extension)
      (clean-fix-typos)
      (clean-strip-prefix)
      (str/trim)))

(defn boost [name]
  (cond (.startsWith name "PDF") 1.0
        true 0.0))

(defn make-doc [name]
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
  (reset! index (build-index (fetch-index))))

(defn search [index terms & [limit]]
  (clucy/search index terms (or limit 100)
                :default-operator :and))

(defn explain [index terms & [limit]]
  (clucy/search index terms (or limit 100)
                :default-operator :and :explain true))
