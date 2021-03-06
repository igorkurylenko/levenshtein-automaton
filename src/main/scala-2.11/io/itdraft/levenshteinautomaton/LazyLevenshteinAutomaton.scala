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

import io.itdraft.levenshteinautomaton.description._
import io.itdraft.levenshteinautomaton.description.nonparametric.{ElementaryTransition, FailureState, NonparametricState}

/**
  * A class to represent the Levenshtein-automaton.
  *
  * @note Lazy because it computes next state on every transition.
  * @example {{{
  * val dictionaryWord: String = ...
  * val misspelledWord: String = ...
  *
  * val automaton = LazyLevenshteinAutomaton(
  *   misspelledWord,
  *   degree = 2,
  *   includeTransposition = true)
  * var state = automaton.initialState
  *
  * // traverse
  * var i, cp = 0
  * while(i < dictionaryWord.length) {
  *   cp = dictionaryWord.codePointAt(i)
  *   state = automaton.getNextStateId(state, cp)
  *   i += Character.charCount(cp)
  * }
  *
  * if(state.isFinalState) println("Misspelled word is accepted.")
  * else println("Misspelled word is rejected.")
  * }}}
  */
class LazyLevenshteinAutomaton private(automatonConfig: LevenshteinAutomatonConfig) {
  private implicit val _ = automatonConfig

  /**
    * The initial state to start an automaton traverse.
    */
  lazy val initialState = NonparametricState.initial

  private val elementaryTransition = ElementaryTransition()

  /**
    * Transits to a next state.
    *
    * @param curState a current state.
    * @param alphaCodePoint a code point of a next alpha from a word
    *                        is being recognized.
    * @return an instance of a next `State`.
    */
  def getNextState(curState: NonparametricState, alphaCodePoint: Int) =
    curState.imageSet.fold(FailureState: NonparametricState) { (state, position) =>
    val vector = DefaultCharacteristicVector(alphaCodePoint, automatonConfig.getWordCodePoints,
      position.i, position.i + position.relevantSubwordMaxLength)

    state |~ elementaryTransition(position, vector)
  }
}

object LazyLevenshteinAutomaton {

  /**
    * Creates an instance of `LevenshteinAutomaton`.
    *
    * @param word a word to build Levenshtein-automaton for.
    * @param degree automaton recognizes the set of all words
    *               where the Levenshtein-distance between a word from the set
    *               and a word automaton is built for does not exceed `degree`.
    * @param inclTransposition whether include transposition as a primitive
    *                          edit operation.
    * @return an instance of `LevenshteinAutomaton` which uses `EncodedParametricDescription`
    *         if it exists for the specified parameters or
    *         computes next states at run time otherwise.
    */
  def apply(word: String, degree: Int, inclTransposition: Boolean): LazyLevenshteinAutomaton =
    apply(createLevenshteinAutomatonConfig(word, degree, inclTransposition))

  /**
    * Creates an instance of `LevenshteinAutomaton`.
    */
  def apply(config: LevenshteinAutomatonConfig) =
    new LazyLevenshteinAutomaton(config)
}
