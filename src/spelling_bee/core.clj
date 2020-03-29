(ns spelling-bee.core
  (:require [clojure.string :as s]))

(defn only-chars?
  "
  Determines if every character of a word is within an allowed charset
  Takes a word string and a set of characters
  Returns true if word is within charset
  "
  [word charset]
  (every? #(contains? charset %) word))

(defn word-fits?
  "
  Determines if a word fits our charset and additional criteria
  - Word must be more than 3 characters
  - Assumes first letter of charset is required char
  - Word must contain the required-char
  - Word must only contain letters in the charset
  Takes a word string and a set of chars
  Returns true if all criteria is met
  " 
  [word charset]
  (and
   (> (count word) 3)
   (s/includes? word (str (first charset)))
   (only-chars? word charset)))

(defn find-words
  "
  Given a string of allowed characters returns a list of words read from a
  dictionary list that match our word-fits? predicate
  Prints each word that matches our criteria
  Takes a string of characters. Assumes first char is required.
  Returns nil
  "
  [charstr]
  (let [charset (set charstr)]
    (with-open [rdr (clojure.java.io/reader "./resources/words.txt")]
      (doseq [word (->> (line-seq rdr)
                        (filter #(word-fits? % charset)))]
        (println word)))))

(defn -main
  "
  A CLI to find each word from a dictionary list of words to help solve
  nytimes.com/puzzles/speling-bee word finds.
  - Words must be more than 3 chars
  - Words must contain the required char (first char of charset)
  - Words must only contain the allowed charset characters

  Usage
  clj -m spelling-bee.core \"ohtcumn\"

  # =>
  choochoo
  choom
  chott
  chout
  cocco
  coch
  ...
  "
  [charstr & args]
  (find-words charstr))

(comment
  (first "ohtcunm")
  (s/includes? "abcd" "c")
  (word-fits? "coconut" (set "ohtcunm"))
  (find-words "ohtcunm"))
