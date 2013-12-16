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
package org.jboss.weld.environment.se.example.groovy.numberguess;

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.inject.Inject

/**
 * This class contains the pure logic of the Number Guess game. Redone to groovy from java version
 *
 * @author Matej Briskar
 */

@ApplicationScoped
class Game {

    static final int MAX_NUM_GUESSES = 10;
    int number;
    int guess = 0;
    int smallest = 0;
    @Inject
    @MaxNumber
    def maxNumber;
    int biggest;
    int remainingGuesses = MAX_NUM_GUESSES;
    boolean validNumberRange = true;



    @Inject
    Generator rndGenerator;

    boolean isGameWon() {
        return guess == number;
    }
    boolean isGameLost() {
        return guess != number && remainingGuesses <= 0;
    }

    def check() {
        def result = false;
        if (checkNewNumberRangeIsValid()) {
            if (guess > number) {
                biggest = guess - 1;
            }
            if (guess < number) {
                smallest = guess + 1;
            }
            if (guess == number) {
                result = true;
            }
            remainingGuesses--;
        }
        return result;
    }

    def observeGuess(@Observes Guess event) {
        guess=event.guessNumber
        check()
    }

    def checkNewNumberRangeIsValid() {
        return validNumberRange = ((guess >= smallest) && (guess <= biggest));
    }

    @PostConstruct
    def reset() {
        this.smallest = 0;
        this.guess = 0;
        this.remainingGuesses = 10;
        this.biggest = maxNumber;
        this.number = rndGenerator.next();
        println "psst! the number is $number"
    }

    @PreDestroy
    def end() {
        println "game over!"
    }
}
