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
package org.jboss.weld.examples.numberguess.clustertest.selenium;

import org.jboss.test.selenium.AbstractTestCase;
import org.jboss.test.selenium.encapsulated.JavaScript;
import org.jboss.test.selenium.framework.AjaxSeleniumImpl;
import org.jboss.test.selenium.framework.AjaxSeleniumProxy;
import org.jboss.test.selenium.guard.request.RequestTypeGuardFactory;
import org.jboss.test.selenium.locator.ElementLocationStrategy;
import org.jboss.test.selenium.locator.IdLocator;
import org.jboss.test.selenium.locator.XpathLocator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.jboss.test.selenium.SystemProperties.getSeleniumHost;
import static org.jboss.test.selenium.SystemProperties.getSeleniumPort;
import static org.jboss.test.selenium.SystemProperties.getSeleniumSpeed;
import static org.jboss.test.selenium.SystemProperties.isSeleniumMaximize;
import static org.jboss.test.selenium.encapsulated.JavaScript.fromResource;
import static org.jboss.test.selenium.locator.LocatorFactory.id;
import static org.jboss.test.selenium.locator.LocatorFactory.xp;
import static org.jboss.test.selenium.utils.URLUtils.buildUrl;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * <p>
 * This class tests Weld numberguess example in a cluster. Two instances of
 * JBoss AS are being used. First part of test is executed at first (master)
 * instance. Then the first instance is killed and a second (slave) instance
 * takes over executing of the application. This behaviour simulates recovery
 * from breakdown and session replication.
 * </p>
 * <p/>
 * <p/>
 * See <code>examples/readme.txt</code> for instructions to run the test.
 *
 * @author mgencur
 * @author kpiwko
 */
public class NumberGuessClusteringTest extends AbstractTestCase {
    protected String MAIN_PAGE = "home.jsf";

    protected IdLocator GUESS_MESSAGES = id("numberGuess:messages");
    protected XpathLocator GUESS_STATUS = xp("//div[contains(text(),'I'm thinking of ')]");

    protected IdLocator GUESS_FIELD = id("numberGuess:inputGuess");
    protected XpathLocator GUESS_FIELD_WITH_VALUE = xp("//input[@id='numberGuess:inputGuess'][@value=3]");

    protected IdLocator GUESS_SUBMIT = id("numberGuess:guessButton");
    protected IdLocator GUESS_RESTART = id("numberGuess:restartButton");
    protected IdLocator GUESS_SMALLEST = id("numberGuess:smallest");
    protected IdLocator GUESS_BIGGEST = id("numberGuess:biggest");

    protected String WIN_MSG = "Correct!";
    protected String LOSE_MSG = "No guesses left!";
    protected String HIGHER_MSG = "Higher!";

    private final String SECOND_INSTANCE_BROWSER_URL = "http://localhost:8180";
    private final long JBOSS_SHUTDOWN_TIMEOUT = 20000;
    String jbossConfig = System.getProperty("jboss.config");
    private String localContextPath = "";

    @BeforeMethod
    public void openStartURL() throws MalformedURLException {
        selenium.open(new URL(contextPath.toString() + MAIN_PAGE));
    }

    @Test
    public void guessingWithFailoverTest() throws MalformedURLException {
        preFailurePart();

        String newAddress = getAddressForSecondInstance();

        /*
        * stop and start browser -> simulate different web browser with different
        * session
        */
        super.finalizeBrowser();
        shutdownMasterJBossInstance();
        initializeSecondBrowser();
        super.initializeWaitTimeouts();
        try {
            super.initializeExtensions();
        } catch (IOException e) {
            new RuntimeException(e.getCause());
        }

        selenium.open(new URL(SECOND_INSTANCE_BROWSER_URL + newAddress));

        assertTrue(selenium.isTextPresent(HIGHER_MSG), "Page should contain message Higher!");
        assertEquals(Integer.parseInt(selenium.getText(GUESS_SMALLEST)), 4, "Page should contain smallest number equal to 4");
        assertEquals(Integer.parseInt(selenium.getText(GUESS_BIGGEST)), 100, "Page should contain biggest number equal to 100");
        assertTrue(selenium.isElementPresent(GUESS_FIELD_WITH_VALUE), "Page should contain input field with value of 3");

        postFailurePart();

        assertTrue(isOnWinPage(), "Win page expected after playing smart.");
    }

    protected void preFailurePart() {
        int numberOfGuesses = 3;
        int guess = 0;

        // enter several guesses (3)
        while (true) {
            while (isOnGuessPage() && guess < numberOfGuesses) {
                enterGuess(++guess);
            }

            // we always want to enter at least 3 guesses so that we can continue
            // in the other browser window with expected results
            if (guess < numberOfGuesses) {
                resetForm();
                guess = 0;
            } else {
                break;
            }
        }
    }

    protected void postFailurePart() {
        int min, max, guess;
        int i = 0;

        selenium.deleteAllVisibleCookies();

        while (isOnGuessPage()) {
            selenium.deleteAllVisibleCookies();
            /*
            * 3+8 = 11 -> even though we have 10 attempts, it is possible to enter
            * value 11 times, but the 11th time it is actually not guessing but
            * only validating that 10 times has gone and the game is finished (no
            * 11th guessing)
            */
            if (i >= 8) {
                fail("Game should not be longer than 7 guesses in the second selenium after failover");
            }

            assertTrue(selenium.isElementPresent(GUESS_SMALLEST), "Expected smallest number on page");
            assertTrue(selenium.isElementPresent(GUESS_BIGGEST), "Expected biggest number on page");

            min = Integer.parseInt(selenium.getText(GUESS_SMALLEST));
            max = Integer.parseInt(selenium.getText(GUESS_BIGGEST));
            guess = min + ((max - min) / 2);
            enterGuess(guess);
            i++;
        }
    }

    protected void resetForm() {
        RequestTypeGuardFactory.waitHttp(selenium).click(GUESS_RESTART);
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

    public String getAddressForSecondInstance() {
        String loc = selenium.getLocation().toString();
        String[] parsedStrings = loc.split("/");
        localContextPath = "/" + parsedStrings[3] + "/";
        StringBuilder sb = new StringBuilder();
        for (int i = 3; i < parsedStrings.length; i++) {
            sb.append("/").append(parsedStrings[i]);
        }

        String newAddress = sb.toString();
        String firstPart = "";
        String sid = "";

        if (selenium.isCookiePresent("JSESSIONID")) {
            sid = selenium.getCookieByName("JSESSIONID").getValue();
            firstPart = newAddress;
        } else {
            // get sessionid directly from browser URL if JSESSIONID cookie is not
            // present
            firstPart = newAddress.substring(0, newAddress.indexOf(";"));
            sid = loc.substring(loc.indexOf("jsessionid=") + "jsessionid=".length(), loc.length());
        }

        newAddress = firstPart + ";jsessionid=" + sid;

        return newAddress;
    }

    public void shutdownMasterJBossInstance() {
        String command = jbossConfig + "/../../bin/shutdown.sh -s service:jmx:rmi:///jndi/rmi://localhost:1090/jmxrmi -S";
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            Thread.sleep(JBOSS_SHUTDOWN_TIMEOUT);
        } catch (IOException e) {
            throw new RuntimeException(e.getCause());
        } catch (InterruptedException e) {
        }
    }

    public void initializeSecondBrowser() {
        selenium = new AjaxSeleniumImpl(getSeleniumHost(), getSeleniumPort(), browser, buildUrl(SECOND_INSTANCE_BROWSER_URL, localContextPath));
        AjaxSeleniumProxy.setCurrentContext(selenium);

        selenium.start();

        selenium.deleteAllVisibleCookies();
        loadCustomLocationStrategies();

        selenium.setSpeed(getSeleniumSpeed());

        if (isSeleniumMaximize()) {
            // focus and maximaze tested window
            selenium.windowFocus();
            selenium.windowMaximize();
        }
    }

    private void loadCustomLocationStrategies() {
        JavaScript strategySource = fromResource("javascript/selenium-location-strategies/jquery-location-strategy.js");
        selenium.addLocationStrategy(ElementLocationStrategy.JQUERY, strategySource);
    }
}