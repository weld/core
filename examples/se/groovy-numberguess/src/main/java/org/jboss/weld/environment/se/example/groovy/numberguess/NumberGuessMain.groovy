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

import groovy.beans.Bindable
import groovy.swing.SwingBuilder

import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.GridLayout

import jakarta.enterprise.context.Dependent
import jakarta.enterprise.event.Event
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import javax.swing.WindowConstants as WC

import org.jboss.weld.environment.se.events.ContainerInitialized

/**
 * Class containing the Strings that are binded in the swing application.
 *
 * @author Matej Briskar
 */
class Model {
    @Bindable def challengeMessage
    @Bindable def resultMessage
    @Bindable def guessText=''
    @Bindable def remainingGueses
}

/**
 * Swing-based groovy numberGuess example.
 *
 * @author Matej Briskar
 */

@Dependent
class NumberGuessMain {
    @Inject
    Game game;
    @Inject
    Event<Guess> guessEvent;
    @Inject
    MessageGenerator msgGenerator;

    def swing = new SwingBuilder()
    def buttonPanel
    def model

    def start(@Observes ContainerInitialized event) {
        java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        runSwing();
                    }
                });
    }

    private runSwing() {
        model = new Model(challengeMessage: msgGenerator.getChallengeMessage(),
        resultMessage: msgGenerator.getResultMessage(),
        remainingGueses: game.getRemainingGuesses())

        def frame = swing.frame(title: "Groovy-numberGuess", defaultCloseOperation: WC.EXIT_ON_CLOSE, pack: true, show: true) {
            panel() {
                // root
                panel(layout: new CardLayout()) {
                    // borderPanel
                    panel() {
                        //game panel
                        borderLayout()
                        panel(constraints: java.awt.BorderLayout.CENTER,name: 'msgGenerator',layout: new BorderLayout(8,8)) {
                            //inputs panel
                            buttonPanel= panel(constraints: BorderLayout.LINE_END) {
                                //buttonpanel
                                cardLayout()
                                button(name: "guessbutton", constraints: '2', text: 'Guess', actionPerformed: {guessButtonActionPerformed()})
                                button(name: "replaybutton", constraints: '3', text: 'Replay!', actionPerformed: {replayButton()})
                            }
                            textField(id: 'guessfield', constraints: BorderLayout.CENTER, text: bind('guessText', source: model, mutual: true))

                        }
                        panel(constraints: java.awt.BorderLayout.PAGE_END, name: 'remainingGuessesPanel',layout: new BorderLayout(8, 8)) {
                            //remainingGuessesPanel
                            label(text: "Guesses remaining:  ", constraints: java.awt.BorderLayout.LINE_START)
                            progressBar(maximum: Game.MAX_NUM_GUESSES,
                            value: bind{model.remainingGueses},
                            preferredSize: new java.awt.Dimension(10, 14));
                        }
                        panel(constraints: java.awt.BorderLayout.PAGE_START,name: 'mainmsgpanel',layout: new GridLayout(2, 1, 6, 6)) {
                            //mainmsgpanel
                            challengeMessageLabel= label(id:'challengeMessageLabel', text: bind{model.challengeMessage })
                            resultMessageLabel= label(name:'resultMessageLabel', text: bind{model.resultMessage})
                        }
                    }
                }
            }
        }

    }

    def refreshUI() {
        model.challengeMessage=msgGenerator.getChallengeMessage()
        model.resultMessage=msgGenerator.getResultMessage()
        model.guessText=""
        model.remainingGueses=game.getRemainingGuesses()
    }
    def replayButton() {
        game.reset();
        refreshUI();
        switchButtons();
    }
    def guessButtonActionPerformed() {
        int parsed=-1
        try{
            parsed=model.guessText.toInteger()
        }catch(NumberFormatException ex) {
            //nothing
        }
        //fire the guess, game observes it
        guessEvent.fire(new Guess(guessNumber: parsed))
        refreshUI();
        if (game.isGameWon() || game.isGameLost()) {
            switchButtons();
        }
    }
    def switchButtons() {
        CardLayout buttonLyt = (CardLayout) buttonPanel.layout;
        buttonLyt.next(buttonPanel);
    }
}
