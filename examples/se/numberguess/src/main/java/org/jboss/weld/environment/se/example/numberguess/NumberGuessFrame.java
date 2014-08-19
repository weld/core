/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.environment.se.example.numberguess;

import java.awt.CardLayout;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.weld.environment.se.events.ContainerInitialized;

/**
 * Swing-based number guess example, main application frame.
 *
 * @author Peter Royle
 */
@ApplicationScoped
public class NumberGuessFrame extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;

    @Inject
    private Game game;

    @Inject
    private MessageGenerator msgGenerator;

    public void start(@Observes ContainerInitialized event) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                initComponents();
                setVisible(true);
            }
        });
    }

    /**
     * This method is called to initialize the form.
     */
    private void initComponents() {

        borderPanel = new javax.swing.JPanel();
        gamePanel = new javax.swing.JPanel();
        inputsPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        guessButton = new javax.swing.JButton();
        replayBtn = new javax.swing.JButton();
        guessText = new javax.swing.JTextField();
        remainingGuessesPanel = new javax.swing.JPanel();
        guessremainLabel = new javax.swing.JLabel();
        guessesLeftBar = new javax.swing.JProgressBar();
        mainMsgPanel = new javax.swing.JPanel();
        mainLabel = new javax.swing.JLabel();
        messageLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.CardLayout());

        borderPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        borderPanel.setLayout(new java.awt.CardLayout());

        gamePanel.setLayout(new java.awt.BorderLayout(6, 6));

        inputsPanel.setLayout(new java.awt.BorderLayout(8, 8));

        buttonPanel.setLayout(new java.awt.CardLayout());

        guessButton.setText("Guess");
        guessButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guessButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(guessButton, "card2");

        replayBtn.setText("Replay!");
        replayBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replayBtnActionPerformed(evt);
            }
        });
        buttonPanel.add(replayBtn, "card3");

        inputsPanel.add(buttonPanel, java.awt.BorderLayout.LINE_END);
        inputsPanel.add(guessText, java.awt.BorderLayout.CENTER);

        gamePanel.add(inputsPanel, java.awt.BorderLayout.CENTER);

        remainingGuessesPanel.setLayout(new java.awt.BorderLayout(8, 8));

        guessremainLabel.setText("Guesses remaining:  ");
        remainingGuessesPanel.add(guessremainLabel, java.awt.BorderLayout.LINE_START);

        guessesLeftBar.setMaximum(Game.MAX_NUM_GUESSES);
        guessesLeftBar.setValue(Game.MAX_NUM_GUESSES);
        guessesLeftBar.setPreferredSize(new java.awt.Dimension(10, 14));
        remainingGuessesPanel.add(guessesLeftBar, java.awt.BorderLayout.CENTER);

        gamePanel.add(remainingGuessesPanel, java.awt.BorderLayout.PAGE_END);

        mainMsgPanel.setLayout(new java.awt.GridLayout(2, 1, 6, 6));

        mainLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mainLabel.setText(msgGenerator.getChallengeMessage());
        mainMsgPanel.add(mainLabel);

        messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        messageLabel.setText(msgGenerator.getResultMessage());
        mainMsgPanel.add(messageLabel);

        gamePanel.add(mainMsgPanel, java.awt.BorderLayout.PAGE_START);

        borderPanel.add(gamePanel, "card2");

        getContentPane().add(borderPanel, "card2");

        pack();
    }

    private void guessButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int guess = -1;

        try {
            guess = Integer.parseInt(guessText.getText());
        } catch (NumberFormatException nfe) {
            // noop
        }

        game.setGuess(guess);
        game.check();
        refreshUI();

        if (game.isGameWon() || game.isGameLost()) {
            switchButtons();
        }
    }

    private void replayBtnActionPerformed(java.awt.event.ActionEvent evt) {
        game.reset();
        refreshUI();
        switchButtons();
    }

    private void switchButtons() {
        CardLayout buttonLyt = (CardLayout) buttonPanel.getLayout();
        buttonLyt.next(buttonPanel);
    }

    private void refreshUI() {
        mainLabel.setText(msgGenerator.getChallengeMessage());
        messageLabel.setText(msgGenerator.getResultMessage());
        guessText.setText("");
        guessesLeftBar.setValue(game.getRemainingGuesses());
        guessText.requestFocus();
    }

    // swing components
    private javax.swing.JPanel borderPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel gamePanel;
    private javax.swing.JButton guessButton;
    private javax.swing.JTextField guessText;
    private javax.swing.JProgressBar guessesLeftBar;
    private javax.swing.JLabel guessremainLabel;
    private javax.swing.JPanel inputsPanel;
    private javax.swing.JLabel mainLabel;
    private javax.swing.JPanel mainMsgPanel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JPanel remainingGuessesPanel;
    private javax.swing.JButton replayBtn;

}
