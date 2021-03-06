package io.itdraft.levenshteinautomaton.description.nonparametric

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

import io.itdraft.levenshteinautomaton.description.{DefaultCharacteristicVector, State}

/**
  * A class to represent the elementary transitions table 4.1 and 7.1 from the paper.
  */
protected[levenshteinautomaton] object ElementaryTransition {
  import io.itdraft.levenshteinautomaton._

  /**
    * A factory method to create the elementary transition function.
    *
    * @param automatonConfig a configuration for the nonparametrically described
    *                        Levenshtein-automaton.
    *
    * @return a function which represents the elementary transition.
    */
  def apply()(implicit automatonConfig: LevenshteinAutomatonConfig):
  (Position, DefaultCharacteristicVector) => NonparametricState = {
    if (automatonConfig.inclTransposition) inclTranspositionsTransition
    else standardTransition
  }

  /**
    * Represents elementary transition of the 4.1 table.
    */
  private def standardTransition(position: Position, v: DefaultCharacteristicVector)
                                (implicit automatonConfig: LevenshteinAutomatonConfig) = {
    val e = position.e
    val n = automatonConfig.n

    if (0 <= e && e <= n - 1) standardTransitionPart1(position, v)
    else if (e == n) standardTransitionPart2(position, v)
    else FailureState
  }

  /**
    * Represents elementary transition of the part 1 of the 4.1 table.
    */
  private def standardTransitionPart1(position: Position, v: DefaultCharacteristicVector)
                                     (implicit config: LevenshteinAutomatonConfig) = {
    val i = position.i
    val e = position.e
    val w = config.w

    if (i <= w - 2)
      v.j match {
        case -1 => State(i ^# (e + 1), (i + 1) ^# (e + 1))
        case 1 => State((i + 1) ^# e)
        case j => State(i ^# (e + 1), (i + 1) ^# (e + 1), (i + j) ^# (e + j - 1))
      }
    else if (i == w - 1)
      v.j match {
        case -1 => State(i ^# (e + 1), (i + 1) ^# (e + 1))
        case 1 => State((i + 1) ^# e)
      }
    else if (i == w)
      State(w ^# (e + 1))
    else FailureState
  }

  /**
    * Represents elementary transition of the part 2 of the 4.1 table.
    */
  private def standardTransitionPart2(position: Position, v: DefaultCharacteristicVector)
                                     (implicit config: LevenshteinAutomatonConfig) = {
    val i = position.i
    val w = config.w
    val n = config.n

    if (i <= w - 1)
      v.j match {
        case 1 => State((i + 1) ^# n)
        case _ => FailureState
      }
    else FailureState
  }

  /**
    * Represents elementary transition of the 7.1 table.
    */
  private def inclTranspositionsTransition(position: Position, v: DefaultCharacteristicVector)
                                          (implicit automatonConfig: LevenshteinAutomatonConfig) = {
    val e = position.e
    val n = automatonConfig.n

    if (e == 0 && e < n) inclTranspositionsTransitionPart1(position, v)
    else if (1 <= e && e <= n - 1) inclTranspositionsTransitionPart2(position, v)
    else if (e == n) inclTranspositionsTransitionPart3(position, v)
    else FailureState
  }

  /**
    * Represents elementary transition of the part 1 of the 7.1 table.
    */
  private def inclTranspositionsTransitionPart1(position: Position, v: DefaultCharacteristicVector)
                                               (implicit config: LevenshteinAutomatonConfig) = {
    val i = position.i
    val w = config.w

    if (i <= w - 2)
      v.j match {
        case -1 => State(i ^# 1, (i + 1) ^# 1)
        case 1 => State((i + 1) ^# 0)
        case 2 =>
          State(i ^# 1, i.t ^# 1, (i + 1) ^# 1, (i + 2) ^# 1)
        case j => State(i ^# 1, (i + 1) ^# 1, (i + j) ^# (j - 1))
      }
    else if (i == w - 1)
      v.j match {
        case 1 => State((i + 1) ^# 0)
        case -1 => State(i ^# 1, (i + 1) ^# 1)
      }
    else if (i == w) State(w ^# 1)
    else FailureState
  }

  /**
    * Represents elementary transition of the part 2 of the 7.1 table.
    */
  private def inclTranspositionsTransitionPart2(position: Position, v: DefaultCharacteristicVector)
                                               (implicit config: LevenshteinAutomatonConfig) = {
    val i = position.i
    val e = position.e
    val w = config.w

    if (i <= w - 2)
      position match {
        case StandardPosition(_, _, _) =>
          v.j match {
            case -1 => State(i ^# (e + 1), (i + 1) ^# (e + 1))
            case 1 => State((i + 1) ^# e)
            case 2 => State(i ^# (e + 1), i.t ^# (e + 1), (i + 1) ^# (e + 1),
              (i + 2) ^# (e + 1))
            case j => State(i ^# (e + 1), (i + 1) ^# (e + 1), (i + j) ^# (e + j - 1))
          }
        case TPosition(_, _, _) =>
          v.j match {
            case 1 => State((i + 2) ^# e)
            case _ => FailureState
          }
      }
    else if (i == w - 1)
      v.j match {
        case 1 => State((i + 1) ^# e)
        case -1 => State(i ^# (e + 1), (i + 1) ^# (e + 1))
      }
    else if (i == w) State(w ^# (e + 1))
    else FailureState
  }

  /**
    * Represents elementary transition of the part 3 of the 7.1 table.
    */
  private def inclTranspositionsTransitionPart3(position: Position, v: DefaultCharacteristicVector)
                                               (implicit config: LevenshteinAutomatonConfig) = {
    val i = position.i
    val w = config.w
    val n = config.n

    position match {
      case StandardPosition(_, _, _) =>
        if (i <= w - 1)
          v.j match {
            case 1 => State((i + 1) ^# n)
            case -1 => FailureState
          }
        else FailureState
      case TPosition(_, _, _) =>
        if (i <= w - 2)
          v.j match {
            case 1 => State((i + 2) ^# n)
            case -1 => FailureState
          }
        else FailureState
    }
  }
}