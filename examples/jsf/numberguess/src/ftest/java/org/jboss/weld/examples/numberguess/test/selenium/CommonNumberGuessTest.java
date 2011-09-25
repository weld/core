/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.examples.numberguess.test.selenium;

import org.jboss.test.selenium.AbstractTestCase;
import org.jboss.test.selenium.guard.request.RequestTypeGuardFactory;
import org.jboss.test.selenium.locator.IdLocator;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.jboss.test.selenium.locator.LocatorFactory.id;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests numberguess examples in Weld
 *
 * @author Jozef Hartinger
 * @author kpiwko
 * @author plenyi
 */
public class CommonNumberGuessTest extends AbstractTestCase {

    protected String MAIN_PAGE = "/home.jsf";
    protected IdLocator GUESS_MESSAGES = id("numberGuess:messages");

    protected IdLocator GUESS_FIELD = id("numberGuess:inputGuess");
    protected IdLocator GUESS_SUBMIT = id("numberGuess:guessButton");
    protected IdLocator GUESS_RESET = id("numberGuess:resetButton");
    protected IdLocator GUESS_SMALLEST = id("numberGuess:smallest");
    protected IdLocator GUESS_BIGGEST = id("numberGuess:biggest");

    protected String WIN_MSG = "Correct!";
    protected String LOSE_MSG = "No guesses left!";

    @BeforeMethod
    public void openStartUrl() {
        selenium.open(contextPath);
        waitModel.until(elementPresent.locator(GUESS_FIELD));
    }

    @AfterMethod
    public void resetSession() {
        selenium.deleteAllVisibleCookies();
    }

    @Test
    public void smartTest() {

        int min;
        int max;
        int guess;
        int i = 0;

        while (isOnGuessPage()) {
            if (i > 10) {
                fail("Game should not be longer than 10 guesses");
            }

            assertTrue(selenium.isElementPresent(GUESS_SMALLEST), "Expected smallest number on page");
            assertTrue(selenium.isElementPresent(GUESS_BIGGEST), "Expected biggest number on page");

            min = Integer.parseInt(selenium.getText(GUESS_SMALLEST));
            max = Integer.parseInt(selenium.getText(GUESS_BIGGEST));
            guess = min + ((max - min) / 2);
            enterGuess(guess);
            i++;
        }
        assertTrue(isOnWinPage(), "Win page expected after playing smart.");
    }

    @Test
    public void linearTest() {
        int guess = 0;

        while (isOnGuessPage()) {
            enterGuess(++guess);
            assertTrue(guess <= 11, "Guess count exceeded.");
        }
        if (guess < 11) {
            assertTrue(isOnWinPage(), "Player should not lose before 10th guess.");
        } else {
            assertTrue(isOnLosePage() || isOnWinPage(), "After 10th guess player should lose or win.");
        }

    }

    protected void enterGuess(int guess) {
        selenium.type(GUESS_FIELD, String.valueOf(guess));
        RequestTypeGuardFactory.waitHttp(selenium).click(GUESS_SUBMIT);
    }

    protected boolean isOnGuessPage() {
        return !(isOnWinPage() || isOnLosePage());
    }

    protected boolean isOnWinPage() {
        String text = selenium.getText(GUESS_MESSAGES);
        return WIN_MSG.equals(text);
    }

    protected boolean isOnLosePage() {
        String text = selenium.getText(GUESS_MESSAGES);
        return LOSE_MSG.equals(text);
    }

}
