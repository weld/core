/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.weld.examples.numberguess.ftest.cluster;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.ajocado.framework.AjaxSelenium;
import org.jboss.arquillian.ajocado.locator.IdLocator;
import org.jboss.arquillian.ajocado.locator.XPathLocator;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.examples.numberguess.ftest.Deployments;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.jboss.arquillian.ajocado.locator.LocatorFactory.id;
import static org.jboss.arquillian.ajocado.locator.LocatorFactory.xp;
import static org.jboss.arquillian.ajocado.Ajocado.waitForHttp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 
 * @author mgencur
 * @author kpiwko
 * @author maschmid
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
public class NumberGuessClusterTest {
   
    protected String MAIN_PAGE = "home.jsf";
    
    public static final long GRACE_TIME_TO_REPLICATE = 1000;
    public static final long GRACE_TIME_TO_MEMBERSHIP_CHANGE = 5000;
    
    private static final String CONTAINER1 = "container1"; 
    private static final String CONTAINER2 = "container2"; 
    private static final String DEPLOYMENT1 = "dep.container1";
    private static final String DEPLOYMENT2 = "dep.container2";
    
    protected IdLocator GUESS_MESSAGES = id("numberGuess:messages");
    protected XPathLocator GUESS_STATUS = xp("//div[contains(text(),'I'm thinking of ')]");
    
    protected IdLocator GUESS_FIELD = id("numberGuess:inputGuess");
    protected XPathLocator GUESS_FIELD_WITH_VALUE = xp("//input[@id='numberGuess:inputGuess'][@value=3]");

    protected IdLocator GUESS_SUBMIT = id("numberGuess:guessButton");
    protected IdLocator GUESS_RESTART = id("numberGuess:restartButton");
    protected IdLocator GUESS_SMALLEST = id("numberGuess:smallest");
    protected IdLocator GUESS_BIGGEST = id("numberGuess:biggest");

    protected String WIN_MSG = "Correct!";
    protected String LOSE_MSG = "No guesses left!";
    protected String HIGHER_MSG = "Higher!";
    protected String LOWER_MSG = "Lower!";
    
    protected Pattern guessesNumberPattern = Pattern.compile("You have (\\d+) guesses remaining."); 
    
    private GameState gameState;
   
    @ArquillianResource
    private ContainerController controller;
    
    @ArquillianResource
    private Deployer deployer;
    
    @Drone
    AjaxSelenium selenium;
    
    String contextPath1;
    String contextPath2;
    
    boolean browsersSwitched = false;
   
    @Deployment(name = DEPLOYMENT1, managed=false, testable=false)
    @TargetsContainer(CONTAINER1)
    public static WebArchive createTestDeployment1() {
        return Deployments.createDeployment();
    }
    
    @Deployment(name = DEPLOYMENT2, managed=false, testable=false)
    @TargetsContainer(CONTAINER2)
    public static WebArchive createTestDeployment2() {
        return Deployments.createDeployment()
            .addAsWebInfResource(EmptyAsset.INSTANCE, "force-hashcode-change.txt");
    }
    
    @Before
    public void before() throws MalformedURLException {
        // We can't use @ArquillianResource URL here as we are using unmanaged deployments
        contextPath1 = System.getProperty("node1.contextPath");
        contextPath2 = System.getProperty("node2.contextPath");
    }
    
    protected void resetForm() {
        waitForHttp(selenium).click(GUESS_RESTART);
        gameState = null;
    }

    protected void enterGuess(int guess) throws InterruptedException {
        gameState.setGuess(guess);
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
    
    public String getAddressForSecondInstance() {
        String loc = selenium.getLocation().toString();
        String[] parsedStrings = loc.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 4; i < parsedStrings.length; i++) {
            sb.append("/").append(parsedStrings[i]);
        }

        String newAddress = sb.toString();
        String firstPart = "";
        String sid = "";         
        
        //if (selenium.isCookiePresent("JSESSIONID")) {
        if (!newAddress.contains(";")) {
            sid = selenium.getCookieByName("JSESSIONID").getValue();
            firstPart = newAddress;
        } else {
            // get sessionid directly from browser URL if JSESSIONID cookie is not
            // present
            firstPart = newAddress.substring(0, newAddress.indexOf(";"));
            sid = loc.substring(loc.indexOf("jsessionid=") + "jsessionid=".length(), loc.length());
        }

        newAddress = firstPart + ";jsessionid=" + sid;
                
        selenium.deleteAllVisibleCookies();

        return newAddress;
    }
    
    private Integer getRemainingGuesses() {
        Matcher m = guessesNumberPattern.matcher(selenium.getBodyText());
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        else {
            return null;
        }
    }

    /**
     * Asserts the game state matches what the page displays
     */
    private void updateGameState() {
        
        GameState nextState = new GameState();
        nextState.setRemainingGuesses(getRemainingGuesses());
        nextState.setLargest(Integer.parseInt(selenium.getText(GUESS_BIGGEST)));
        nextState.setSmallest(Integer.parseInt(selenium.getText(GUESS_SMALLEST)));              
      
        if (gameState == null) {
            // Initial state
            assertEquals("Remaining guesses dosn't match", 10, nextState.getRemainingGuesses());
            assertEquals("Smallest numbers dosn't match", 0, nextState.getSmallest());
            assertEquals("Largest numbers dosn't match", 100, nextState.getLargest());
        }
        else {
            nextState.setPreviousGuess(gameState.getGuess());
            assertEquals("Remaining guesses dosn't match", gameState.getRemainingGuesses() - 1, nextState.getRemainingGuesses());

            boolean higher = selenium.isTextPresent(HIGHER_MSG);
            boolean lower = selenium.isTextPresent(LOWER_MSG);
            
            assertEquals(lower, (nextState.getLargest() < gameState.getLargest()));
            if (gameState.getGuess() != 0) {
                // Bug in numberguess, doesn't display "higher" for zero guess.
                assertEquals(higher, (nextState.getSmallest() > gameState.getSmallest()));
            }
            assertTrue(!lower || !higher);
        }
 
        gameState = nextState;
    }
    
    private void naiveStep() throws InterruptedException {
        updateGameState();
        enterGuess(gameState.getSmallest());
    }
    
    private void smartStep() throws InterruptedException {
        updateGameState();       
        enterGuess(gameState.getSmallest() + ((gameState.getLargest() - gameState.getSmallest()) / 2));
    }
    
    private void switchBrowsers() throws MalformedURLException {
        String address = getAddressForSecondInstance();
        String contextPath = browsersSwitched ? contextPath1 : contextPath2;
        selenium.open(new URL(contextPath + "/" + address));
        
        browsersSwitched = !browsersSwitched;
    }
    
    @Test
    @InSequence(1)
    public void guessingWithFailoverTest() throws MalformedURLException, InterruptedException {
              
        controller.start(CONTAINER1);
        deployer.deploy(DEPLOYMENT1);
                
        controller.start(CONTAINER2);        
        deployer.deploy(DEPLOYMENT2);
        
        selenium.open(new URL(contextPath1 + "/" + MAIN_PAGE));
        
        // we always want to enter at least 3 guesses so that we can continue
        // in the other browser window with expected results
        do {
            resetForm();
            for (int i = 0; i < 3 && isOnGuessPage(); ++i) {
                naiveStep();
            }
        } while(!isOnGuessPage());
        
        deployer.undeploy(DEPLOYMENT1);
        controller.stop(CONTAINER1);
        
        Thread.sleep(GRACE_TIME_TO_REPLICATE);
        
        switchBrowsers();
        
        while(isOnGuessPage()) {
            smartStep();
        }
        
        assertTrue("Win page expected after playing smart.", isOnWinPage());      
        
        deployer.undeploy(DEPLOYMENT2);
        controller.stop(CONTAINER2);
    }
    
    @Test
    @InSequence(2)
    public void guessingWithInterleavingTest() throws MalformedURLException, InterruptedException {
        controller.start(CONTAINER1);
        deployer.deploy(DEPLOYMENT1);
        
        selenium.open(new URL(contextPath1 + "/" + MAIN_PAGE));      
         
        for(;;) {
            
            smartStep();
            
            if (!isOnGuessPage()) {
                break;
            }
            
            if (browsersSwitched) {
                controller.start(CONTAINER1);
                deployer.deploy(DEPLOYMENT1);
                
                Thread.sleep(GRACE_TIME_TO_MEMBERSHIP_CHANGE);                               
                
                deployer.undeploy(DEPLOYMENT2);
                controller.stop(CONTAINER2);
            }
            else {
                controller.start(CONTAINER2);
                deployer.deploy(DEPLOYMENT2);
                
                Thread.sleep(GRACE_TIME_TO_MEMBERSHIP_CHANGE);
                
                deployer.undeploy(DEPLOYMENT1);
                controller.stop(CONTAINER1);
            }                       
            
            Thread.sleep(GRACE_TIME_TO_REPLICATE);
            
            switchBrowsers();                       
        }
        
        assertTrue("Win page expected after playing smart.", isOnWinPage());      
        
        if (browsersSwitched) {           
            deployer.undeploy(DEPLOYMENT2);
            controller.stop(CONTAINER2);
        }
        else {
            deployer.undeploy(DEPLOYMENT1);
            controller.stop(CONTAINER1);
        }
    }
}
