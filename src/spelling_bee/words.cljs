(ns spelling-bee.words
  (:require
   [clojure.string :as s]
   ["fs" :as fs]
   ["highland" :as stream]))

(def read-file (.wrapCallback stream (.-readFile fs)))

(defn in-charset?
  "
  Determines if every character of a word is within an allowed charset
  Takes a word string and a set of characters
  Returns true if word is within charset
  "
  [word charset]
  (every? #(s/includes? charset %) word))

(defn word-checker
  "
  Determines if a word fits our charset and additional criteria
  - Word must be more than 3 characters
  - Assumes first letter of charset is required char
  - Word must contain the required-char
  - Word must only contain letters in the charset
  Takes a word string and a set of chars
  Returns true if all criteria is met
  "
  [required-letter charset]
  (fn accept-word?
    [word]
    (and
     (s/trim word)
     (> (count word) 3)
     (s/includes? word required-letter)
     (in-charset? word charset))))

(defn search
  "
  Given a string of allowed characters returns a list of words read from a
  dictionary list that match our word-fits? predicate
  Prints each word that matches our criteria
  Takes a function to call on each result and a string of characters.
  Assumes first char is required.
  Returns nil
  "
  [charset]
  (-> (read-file "./resources/words.txt")
      (.split)
      (.filter (word-checker
                 (first charset)
                 charset))))

(defn create-search
  [{:keys [charset] :as state}]
  (assoc state :search (search charset)))

(defn -main
  [charset]
  (-> charset
      (search)
      (.each println)))

(comment
  (first "ohtcunm")
  (s/includes? "abcd" "c")
  (word-fits? "coconut" (set "ohtcunm"))
  (find-words "ohtcunm"))
