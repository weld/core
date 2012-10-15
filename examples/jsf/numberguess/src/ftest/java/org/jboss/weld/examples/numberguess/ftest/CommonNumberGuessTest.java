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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import static org.jboss.arquillian.graphene.Graphene.waitModel;
import static org.jboss.arquillian.graphene.Graphene.element;
import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

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
    protected By GUESS_MESSAGES = By.id("numberGuess:messages");
    protected By GUESS_FIELD = By.id("numberGuess:inputGuess");
    protected By GUESS_SUBMIT = By.id("numberGuess:guessButton");
    protected By GUESS_RESET = By.id("numberGuess:resetButton");
    protected By GUESS_SMALLEST = By.id("numberGuess:smallest");
    protected By GUESS_BIGGEST = By.id("numberGuess:biggest");
    protected String WIN_MSG = "Correct!";
    protected String LOSE_MSG = "No guesses left!";
    
    @Drone
    WebDriver driver;
    
    @ArquillianResource
    private URL contextPath;

    @Deployment(testable = false)
    public static WebArchive createTestDeployment1() {
        return Deployments.createDeployment();
    }

    @Before
    public void openStartUrl() throws MalformedURLException {
        driver.navigate().to(new URL(contextPath.toString() + "home.jsf"));
        waitModel(driver).until(element(GUESS_FIELD).isPresent());
    }

    @After
    public void resetSession() {
        driver.manage().deleteAllCookies();
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

            assertTrue("Expected smallest number on page", element(GUESS_SMALLEST).isPresent().apply(driver));
            assertTrue("Expected biggest number on page", element(GUESS_BIGGEST).isPresent().apply(driver));

            min = Integer.parseInt(driver.findElement(GUESS_SMALLEST).getText());
            max = Integer.parseInt(driver.findElement(GUESS_BIGGEST).getText());

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
        driver.findElement(GUESS_FIELD).clear();
        driver.findElement(GUESS_FIELD).sendKeys(String.valueOf(guess));
        guardHttp(driver.findElement(GUESS_SUBMIT)).click();
    }

    protected boolean isOnGuessPage() {
        return !(isOnWinPage() || isOnLosePage());
    }

    protected boolean isOnWinPage() {
        String text = driver.findElement(GUESS_MESSAGES).getText();
        return WIN_MSG.equals(text);
    }

    protected boolean isOnLosePage() {
        String text = driver.findElement(GUESS_MESSAGES).getText();
        return LOSE_MSG.equals(text);
    }
}
