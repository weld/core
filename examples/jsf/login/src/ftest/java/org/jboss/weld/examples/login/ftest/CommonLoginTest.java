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
package org.jboss.weld.examples.login.ftest;

import java.net.URL;

import org.jboss.arquillian.ajocado.framework.AjaxSelenium;
import org.jboss.arquillian.ajocado.locator.IdLocator;
import org.jboss.arquillian.ajocado.locator.XPathLocator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.jboss.arquillian.ajocado.locator.LocatorFactory.id;
import static org.jboss.arquillian.ajocado.locator.LocatorFactory.xp;
import static org.jboss.arquillian.ajocado.Ajocado.elementPresent;
import static org.jboss.arquillian.ajocado.Ajocado.waitForHttp;
import static org.jboss.arquillian.ajocado.Ajocado.waitModel;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    protected XPathLocator LOGGED_IN = xp("//li[contains(text(),'Welcome')]");
    protected XPathLocator LOGGED_OUT = xp("//li[contains(text(),'Goodbye')]");

    protected IdLocator USERNAME_FIELD = id("loginForm:username");
    protected IdLocator PASSWORD_FIELD = id("loginForm:password");

    protected IdLocator LOGIN_BUTTON = id("loginForm:login");
    protected IdLocator LOGOUT_BUTTON = id("loginForm:logout");

    @Drone
    AjaxSelenium selenium;
    
    @ArquillianResource
    private URL contextPath;
    
    @Deployment
    public static WebArchive createTestDeployment1() {
        return Deployments.createDeployment();
    }
    
    @Before
    public void openStartUrl() {
        selenium.deleteAllVisibleCookies();
        selenium.open(contextPath);
    }

    @Test
    public void loginTest() {
        waitModel.until(elementPresent.locator(USERNAME_FIELD));
        assertFalse("User should not be logged in!", selenium.isElementPresent(LOGGED_IN));
        selenium.type(USERNAME_FIELD, "demo");
        selenium.type(PASSWORD_FIELD, "demo");
        waitForHttp(selenium).click(LOGIN_BUTTON);
        assertTrue("User should be logged in!", selenium.isElementPresent(LOGGED_IN));
    }

    @Test
    public void logoutTest() {
        waitModel.until(elementPresent.locator(USERNAME_FIELD));
        assertFalse("User should not be logged in!", selenium.isElementPresent(LOGOUT_BUTTON));
        selenium.type(USERNAME_FIELD, "demo");
        selenium.type(PASSWORD_FIELD, "demo");
        waitForHttp(selenium).click(LOGIN_BUTTON);
        assertTrue("User should be logged in!", selenium.isElementPresent(LOGGED_IN));
        waitForHttp(selenium).click(LOGOUT_BUTTON);
        assertTrue("User should not be logged in!", selenium.isElementPresent(LOGGED_OUT));
    }

}
