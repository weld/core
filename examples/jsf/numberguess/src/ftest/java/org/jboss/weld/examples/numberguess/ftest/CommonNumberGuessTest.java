/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.weld.examples.numberguess.ftest;

import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.jboss.arquillian.graphene.Graphene.waitModel;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.Graphene;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
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
        waitModel().until().element(GUESS_FIELD).is().present();
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
            assertTrue("Expected smallest number on page", driver.findElement(GUESS_SMALLEST).isDisplayed());
            assertTrue("Expected biggest number on page", driver.findElement(GUESS_BIGGEST).isDisplayed());

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
