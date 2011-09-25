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
package org.jboss.weld.examples.permalink.test.selenium;

import org.jboss.test.selenium.AbstractTestCase;
import org.jboss.test.selenium.guard.request.RequestTypeGuardFactory;
import org.jboss.test.selenium.locator.IdLocator;
import org.jboss.test.selenium.locator.XpathLocator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.jboss.test.selenium.locator.LocatorFactory.id;
import static org.jboss.test.selenium.locator.LocatorFactory.xp;
import static org.testng.Assert.assertTrue;

/**
 * Tests permalink example in Weld. The first test just adds comment on the first
 * topic and returns back. Second test tests permanent link from jsf2.
 *
 * @author mgencur
 * @author plenyi
 */
public class PermalinkTest extends AbstractTestCase {
    protected String MAIN_PAGE = "/home.jsf";
    protected String PAGE_TITLE = "Direct links to the news you crave";
    protected XpathLocator VIEW_ENTRY_LINK = xp("//a[contains(text(),'View Entry')][1]");
    protected String TOPIC_TITLE = "Mojarra == RI";
    protected XpathLocator PERMALINK_LINK = xp("//a[@title='A bookmarkable link for this entry.'][1]");
    protected IdLocator AUTHOR_INPUT = id("author");
    protected IdLocator COMMENT_INPUT = id("body");
    protected IdLocator SUBMIT_BUTTON = id("post");
    protected XpathLocator BACK_BUTTON = xp("//input[@type='button'][@value='Back to main page']");
    protected String COMMENT_TEXT = "This is my first comment on Mojarra project";
    protected String AUTHOR_NAME = "Martin";
    protected String TEXT_ON_HOME_PAGE = "Annotation nation";

    @BeforeMethod
    public void openStartUrl() {
        selenium.open(contextPath);
        waitModel.until(elementPresent.locator(VIEW_ENTRY_LINK));
    }

    @Test
    public void addCommentOnTopicTest() {
        RequestTypeGuardFactory.waitHttp(selenium).click(VIEW_ENTRY_LINK);
        assertTrue(selenium.isTextPresent(TOPIC_TITLE), "Topic title expected on the page");
        selenium.type(AUTHOR_INPUT, AUTHOR_NAME);
        selenium.type(COMMENT_INPUT, COMMENT_TEXT);
        RequestTypeGuardFactory.waitHttp(selenium).click(SUBMIT_BUTTON);
        assertTrue(selenium.isTextPresent(AUTHOR_NAME), "A name of comment's author expected");
        assertTrue(selenium.isTextPresent(COMMENT_TEXT), "A text of entered comment expected");
        RequestTypeGuardFactory.waitHttp(selenium).click(BACK_BUTTON);
        assertTrue(selenium.isTextPresent(TEXT_ON_HOME_PAGE), "Home page expected");
    }

    @Test(dependsOnMethods = "addCommentOnTopicTest")
    public void permanentLinkTest() {
        RequestTypeGuardFactory.waitHttp(selenium).click(PERMALINK_LINK);
        assertTrue(selenium.isTextPresent(AUTHOR_NAME), "A name of comment's author expected");
        assertTrue(selenium.isTextPresent(COMMENT_TEXT), "A text of entered comment expected");
    }
}
