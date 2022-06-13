/**
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.environment.se.example.groovy.numberguess

import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject

@Dependent
class MessageGenerator {

    @Inject
    Game game;

    def getChallengeMessage() {
        return "I'm thinking of a number between ${game.getSmallest()} and ${game.getBiggest()}. Can you guess what it is?"
    }

    def getResultMessage() {
        if (game.isGameWon()) {
            return "You guessed it! The number was ${game.getNumber()}";
        } else if (game.isGameLost()) {
            return "You are fail! The number was ${game.getNumber()}";
        } else if (!game.isValidNumberRange()) {
            return "Invalid number range!";
        } else if (game.getRemainingGuesses() == Game.MAX_NUM_GUESSES) {
            return "What is your first guess?";
        } else {
            String direction = null;

            if (game.getGuess() < game.getNumber()) {
                direction = "Higher";
            } else {
                direction = "Lower";
            }

            return direction + "! You have ${game.getRemainingGuesses()} guesses left.";
        }
    }
}