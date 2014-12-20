;; -*- coding: utf-8 -*-
;;
;; eeeee eeeee eeeee  e   e
;; 8   " 8   8 8   8  8   8
;; 8eeee 8eee8 8eee8e 8eee8e
;;    88 88  8 88   8 88   8
;; 8ee88 88  8 88   8 88   8
;;
;; © 2013, 2014 Ian Eure.
;; Author: Ian Eure <ian.eure@gmail.com>
;;
(ns sark.analyzer
  (:require [clojure.string :as str]
            [clojure.java.io :as io]
            [clucy.core :as clucy]
            [clojure.edn :as edn])
  (:import [org.apache.lucene.analysis.core LowerCaseFilter]
           [org.apache.lucene.analysis.en PorterStemFilter]
           [org.apache.lucene.analysis.standard StandardTokenizer StandardFilter]
           [org.apache.lucene.analysis.synonym SynonymFilter SynonymMap$Builder SynonymMap]
           [org.apache.lucene.analysis.util StopwordAnalyzerBase]
           [org.apache.lucene.util CharsRef]
           [org.apache.lucene.analysis Analyzer$TokenStreamComponents]))

(def ^:constant nyms (edn/read-string (slurp (io/resource "nyms.clj"))))

(defn ^CharsRef make-ref [^String s]
  (CharsRef. (str/replace s " " "\u0000")))

(defn ^SynonymMap make-syn-map [nyms]
  (let [m (SynonymMap$Builder. true)]
    (doseq [[from to] nyms]
      (.add m (make-ref from) (make-ref to) true))
    (.build m)))

(def ^:constant ^SynonymMap default-synonyms (make-syn-map nyms))

(defn make-analyzer [& [^SynonymMap synonyms]]
  (proxy [StopwordAnalyzerBase] [clucy/*version*]
    (createComponents [fieldName reader]
      (let [src (StandardTokenizer. clucy/*version* reader)
            tok (-> (LowerCaseFilter. clucy/*version*
                                      (StandardFilter. clucy/*version* src))
                    (SynonymFilter. (or synonyms default-synonyms) true)
                    (PorterStemFilter.))]
        (Analyzer$TokenStreamComponents. src tok)))))

(def standard-analyzer (make-analyzer))

(defmacro with-any-analyzer [analyzer & body]
  `(binding [clucy/*analyzer* ~analyzer]
     ~@body))

(defmacro with-analyzer [& body]
  `(with-any-analyzer standard-analyzer
     ~@body))
