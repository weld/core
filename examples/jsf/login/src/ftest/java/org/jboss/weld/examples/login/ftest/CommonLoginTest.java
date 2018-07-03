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
package org.jboss.weld.examples.login.ftest;

import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.jboss.arquillian.graphene.Graphene.waitModel;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.condition.element.WebElementConditionFactory;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Tests login examples in Weld
 *
 * @author maschmid
 * @author kpiwko
 * @author plenyi
 */
@RunWith(Arquillian.class)
@RunAsClient
public class CommonLoginTest {

    protected String MAIN_PAGE = "/home.jsf";
    private static final By LOGGED_IN = By.xpath("//li[contains(text(),'Welcome')]");
    private static final By LOGGED_OUT = By.xpath("//li[contains(text(),'Goodbye')]");
    private static final By USERNAME_FIELD = By.id("loginForm:username");
    private static final By PASSWORD_FIELD = By.id("loginForm:password");
    private static final By LOGIN_BUTTON = By.id("loginForm:login");
    private static final By LOGOUT_BUTTON = By.id("loginForm:logout");

    @Drone
    WebDriver driver;

    @FindBy(id = "loginForm:logout")
    WebElement logoutElement;

    @ArquillianResource
    private URL contextPath;

    @Deployment(testable = false)
    public static WebArchive createTestDeployment1() {
        return Deployments.createDeployment();
    }

    @Before
    public void openStartUrl() {
        driver.manage().deleteAllCookies();
        driver.navigate().to(contextPath);
    }

    @Test
    public void loginTest() {
        waitModel(driver).until().element(USERNAME_FIELD).is().present();
        assertFalse("User should not be logged in!", new WebElementConditionFactory(logoutElement).isPresent().apply(driver));

        driver.findElement(USERNAME_FIELD).clear();
        driver.findElement(USERNAME_FIELD).sendKeys("demo");

        driver.findElement(PASSWORD_FIELD).clear();
        driver.findElement(PASSWORD_FIELD).sendKeys("demo");

        guardHttp(driver.findElement(LOGIN_BUTTON)).click();
        assertTrue("User should be logged in!", driver.findElement(LOGGED_IN).isDisplayed());
    }

    @Test
    public void logoutTest() {
        waitModel(driver).until().element(USERNAME_FIELD).is().present();
        assertFalse("User should not be logged in!", new WebElementConditionFactory(logoutElement).isPresent().apply(driver));

        driver.findElement(USERNAME_FIELD).clear();
        driver.findElement(USERNAME_FIELD).sendKeys("demo");

        driver.findElement(PASSWORD_FIELD).clear();
        driver.findElement(PASSWORD_FIELD).sendKeys("demo");

        guardHttp(driver.findElement(LOGIN_BUTTON)).click();
        assertTrue("User should be logged in!", driver.findElement(LOGGED_IN).isDisplayed());

        guardHttp(driver.findElement(LOGOUT_BUTTON)).click();
        assertTrue("User should not be logged in!", driver.findElement(LOGGED_OUT).isDisplayed());
    }
}
