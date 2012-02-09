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
package org.jboss.weld.examples.numberguess.ftest;

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.ajocado.framework.AjaxSelenium;
import org.jboss.arquillian.ajocado.locator.IdLocator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.jboss.arquillian.ajocado.locator.LocatorFactory.id;
import static org.jboss.arquillian.ajocado.Ajocado.elementPresent;
import static org.jboss.arquillian.ajocado.Ajocado.waitForHttp;
import static org.jboss.arquillian.ajocado.Ajocado.waitModel;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests numberguess examples in Weld
 *
 * @author maschmid
 * @author Jozef Hartinger
 * @author kpiwko
 * @author plenyi
 */
@RunWith(Arquillian.class)
@RunAsClient
public class CommonNumberGuessTest {

    protected String MAIN_PAGE = "/home.jsf";
    protected IdLocator GUESS_MESSAGES = id("numberGuess:messages");

    protected IdLocator GUESS_FIELD = id("numberGuess:inputGuess");
    protected IdLocator GUESS_SUBMIT = id("numberGuess:guessButton");
    protected IdLocator GUESS_RESET = id("numberGuess:resetButton");
    protected IdLocator GUESS_SMALLEST = id("numberGuess:smallest");
    protected IdLocator GUESS_BIGGEST = id("numberGuess:biggest");

    protected String WIN_MSG = "Correct!";
    protected String LOSE_MSG = "No guesses left!";
    
    @Drone
    AjaxSelenium selenium;
    
    @ArquillianResource
    private URL contextPath;
    
    @Deployment(testable = false)
    public static WebArchive createTestDeployment1() {
        return Deployments.createDeployment();
    }

    @Before
    public void openStartUrl() throws MalformedURLException {
        selenium.open(new URL(contextPath.toString() + "home.jsf"));
        waitModel.until(elementPresent.locator(GUESS_FIELD));
    }

    @After
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

            assertTrue("Expected smallest number on page", selenium.isElementPresent(GUESS_SMALLEST));
            assertTrue("Expected biggest number on page", selenium.isElementPresent(GUESS_BIGGEST));

            min = Integer.parseInt(selenium.getText(GUESS_SMALLEST));
            max = Integer.parseInt(selenium.getText(GUESS_BIGGEST));
            guess = min + ((max - min) / 2);
            enterGuess(guess);
            i++;
        }
        assertTrue("Win page expected after playing smart.", isOnWinPage());
    }

    @Test
    public void linearTest() {
        int guess = 0;

        while (isOnGuessPage()) {
            enterGuess(++guess);
            assertTrue("Guess count exceeded.", guess <= 11);
        }
        if (guess < 11) {
            assertTrue("Player should not lose before 10th guess.", isOnWinPage());
        } else {
            assertTrue("After 10th guess player should lose or win.", isOnLosePage() || isOnWinPage());
        }

    }

    protected void enterGuess(int guess) {
        selenium.type(GUESS_FIELD, String.valueOf(guess));
        waitForHttp(selenium).click(GUESS_SUBMIT);
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
