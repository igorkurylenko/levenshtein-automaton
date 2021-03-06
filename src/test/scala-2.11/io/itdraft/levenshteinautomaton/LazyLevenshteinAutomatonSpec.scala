package io.itdraft.levenshteinautomaton

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.specs2.matcher.Matcher
import org.specs2.mutable.Specification
import org.specs2.specification.Tables

class LazyLevenshteinAutomatonSpec extends Specification with Tables {

  import MisspelledWordsUtil._

  "Lazy Levenshtein automaton" should {
    "accept acceptable misspelled words" in {
      val rows = generateRowsForAcceptableMisspelledWords

      Table4("correct" :: "misspelled" :: "degree" :: "inclTranspositions" :: Nil, rows) |> {
        (correct, misspelled, degree, inclTransp) =>
            LazyLevenshteinAutomaton(correct, degree, inclTransp) must accept(misspelled)
      }
    }

    "reject not acceptable misspelled words" in {
      "correct" | "misspelled" | "degree" | "inclTranspositions" |>
        "abcdefg" ! "abcdefgx" ! 0 ! true |
        "abcdefg" ! "abcdefgx" ! 0 ! false |
        "abcdefg" ! "abcdef" ! 0 ! true |
        "abcdefg" ! "abcdef" ! 0 ! false |
        "abcdefg" ! "abcdefgxx" ! 1 ! true |
        "abcdefg" ! "abcdefgxx" ! 1 ! false |
        "abcdefg" ! "abcde" ! 1 ! true |
        "abcdefg" ! "abcde" ! 1 ! false |
        "abcdefg" ! "abcdefgxxx" ! 2 ! true |
        "abcdefg" ! "abcdefgxxx" ! 2 ! false |
        "abcdefg" ! "abcd" ! 2 ! true |
        "abcdefg" ! "abcd" ! 2 ! false |
        "ab" * 15 ! ("ab" * 15).takeRight(14) ! 15 ! true |
        "ab" * 15 ! ("ab" * 15) + ("d" * 16) ! 15 ! true |
        "ab" * 15 ! ("ac" * 14) + "cc" ! 15 ! true |
        "ab" * 15 ! ("ab" * 15).takeRight(14) ! 15 ! false |
        "ab" * 15 ! ("ab" * 15) + ("d" * 16) ! 15 ! false |
        "ab" * 15 ! ("ac" * 14) + "cc" ! 15 ! false | {
        (correct, misspelled, degree, inclTransp) =>
            LazyLevenshteinAutomaton(correct, degree, inclTransp) must notAccept(misspelled)
      }
    }
  }

  def accept(misspelled: String): Matcher[LazyLevenshteinAutomaton] = {
    automaton: LazyLevenshteinAutomaton =>
      val state = process(automaton, misspelled)
      (state.isFinal, s"Levenshtein automaton must accept a misspelled word")
  }

  def notAccept(misspelled: String): Matcher[LazyLevenshteinAutomaton] = {
    automaton: LazyLevenshteinAutomaton =>
      val state = process(automaton, misspelled)
      (!state.isFinal, s"Levenshtein automaton must not accept a misspelled word")
  }

  def process(automaton: LazyLevenshteinAutomaton, misspelled: String) = {
    var state = automaton.initialState
    for (x <- misspelled) state = automaton.getNextState(state, x)
    state
  }

  def generateRowsForAcceptableMisspelledWords = {
    var rows: List[DataRow4[String, String, Int, Boolean]] = Nil

    // if degree is 0
    for (inclTranspositions <- List(false, true)) {
      val degree = 0
      val correct = "abcdefg"
      val misspelled = correct + ("x" * degree)
      rows = DataRow4(correct, misspelled, degree, inclTranspositions) :: rows
    }

    // if degree is 1 or 2
    for (degree <- 1 to 2) {
      val correct = "abcdefg"
      var misspelledWords =
        generateMisspelledWords(correct, degree)(forInsertionEditOp) :::
          generateMisspelledWords(correct, degree)(forDeletionEditOp) :::
          generateMisspelledWords(correct, degree)(forSubstitutionEditOp)

      if (degree == 2) {
        misspelledWords = misspelledWords :::
          generateMisspelledWords(correct, 1)(forInsertionEditOp).flatMap {
            generateMisspelledWords(_, 1)(forDeletionEditOp)
          }

        misspelledWords = misspelledWords :::
          generateMisspelledWords(correct, 1)(forInsertionEditOp).flatMap {
            generateMisspelledWords(_, 1)(forSubstitutionEditOp)
          }

        misspelledWords = misspelledWords :::
          generateMisspelledWords(correct, 1)(forDeletionEditOp).flatMap {
            generateMisspelledWords(_, 1)(forSubstitutionEditOp)
          }
      }

      for (inclTranspositions <- List(false, true)) {
        if (inclTranspositions) {
          misspelledWords = misspelledWords :::
            generateMisspelledWords(correct, degree)(forTranspositionEditOp)
        }

        misspelledWords.foreach { misspelled =>
          rows = DataRow4(correct, misspelled, degree, inclTranspositions) :: rows
        }

        rows = DataRow4("", "x" * degree, degree, inclTranspositions) :: rows
      }
    }

    // if degree is 15
    for (inclTranspositions <- List(false, true)) {
      val degree = 15
      val correct = "ab" * degree
      var misspelledWords =
        correct.takeRight(degree) :: // for insertion edit op
          correct + ("d" * degree) :: // for deletion edit op
          "ac" * degree :: // for substitution edit op
          Nil
      // for transposition edit op
      if (inclTranspositions) misspelledWords = "ba" * degree :: misspelledWords

      misspelledWords.foreach { misspelled =>
        rows = DataRow4(correct, misspelled, degree, inclTranspositions) :: rows
      }
    }
    rows
  }
}