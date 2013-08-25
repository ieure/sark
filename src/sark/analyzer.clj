;; -*- coding: utf-8 -*-
;;
;; © 2013 Buster Marx, Inc All rights reserved.
;; Author: Ian Eure <ian.eure@gmail.com>
;;
(ns sark.analyzer
  (:require [clojure.string :as str]
            [clucy.core :as clucy])
  (:import [org.apache.lucene.analysis Analyzer$TokenStreamComponents]
           [org.apache.lucene.analysis.util StopwordAnalyzerBase]
           [org.apache.lucene.analysis.core LowerCaseFilter]
           [org.apache.lucene.analysis.standard StandardTokenizer StandardFilter]
           [org.apache.lucene.analysis.synonym SynonymFilter SynonymMap$Builder SynonymMap]
           [org.apache.lucene.util CharsRef]))

(def ^:constant nyms
  {"dp" "schematic"
   "sp" "schematic"
   "tm" "manual"
   "dk" "donky kong"
   "donkey kong" "dk"
   "dkjr" "donkey kong jr"
   "donkey kong jr" "dkjr"
   "jr" "junior"
   "junior" "jr"
   "jnr" "junior"})

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
                    (SynonymFilter. (or synonyms default-synonyms) true))]
        (Analyzer$TokenStreamComponents. src tok)))))

(def analyzer (make-analyzer))

(defmacro with-analyzer [analyzer & body]
  `(binding [clucy/*analyzer* ~analyzer]
     ~@body))

(defmacro with-synonyms [nyms & body]
  `(with-analyzer (make-analyzer (make-syn-map ~nyms))
     ~@body))

(defmacro with-standard-analyzer [& body]
  `(with-analyzer analyzer
     ~@body))
