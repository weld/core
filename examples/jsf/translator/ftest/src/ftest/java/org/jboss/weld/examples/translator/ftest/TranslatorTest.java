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
package org.jboss.weld.examples.translator.ftest;

import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import static org.jboss.arquillian.graphene.Graphene.element;
import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.jboss.arquillian.graphene.Graphene.waitModel;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Tests translator example in Weld
 *
 * @author maschmid
 * @author mgencur
 * @author plenyi
 */
@RunWith(Arquillian.class)
@RunAsClient
public class TranslatorTest {

    protected String MAIN_PAGE = "/home.jsf";
    protected By INPUT_AREA = By.id("TranslatorMain:text");
    protected By TRANSLATE_BUTTON = By.id("TranslatorMain:button");
    protected By BODY = By.tagName("body");
    protected String ONE_SENTENCE = "This is only one sentence.";
    protected String MORE_SENTENCES = "First sentence. Second and last sentence.";
    protected String ONE_SENTENCE_TRANSLATED = "Lorem ipsum dolor sit amet.";
    protected String MORE_SENTENCES_TRANSLATED = "Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet.";
    
    @Drone
    WebDriver driver;
    
    @ArquillianResource
    private URL contextPath;

    @Deployment(testable = false)
    public static EnterpriseArchive createTestDeployment1() {
        return Deployments.createDeployment();
    }

    @Before
    public void openStartUrl() {
        driver.navigate().to(contextPath);
        waitModel(driver).until(element(INPUT_AREA).isPresent());
    }

    @Test
    public void translateTest() {
        driver.findElement(INPUT_AREA).clear();
        driver.findElement(INPUT_AREA).sendKeys(ONE_SENTENCE);
        guardHttp(driver.findElement(TRANSLATE_BUTTON)).click();
        assertTrue("One sentence translated into latin expected.", element(BODY).textContains(ONE_SENTENCE_TRANSLATED).apply(driver));
        driver.findElement(INPUT_AREA).clear();
        driver.findElement(INPUT_AREA).sendKeys(MORE_SENTENCES);
        guardHttp(driver.findElement(TRANSLATE_BUTTON)).click();
        assertTrue("More sentences translated into latin expected.", element(BODY).textContains(MORE_SENTENCES_TRANSLATED).apply(driver));
    }
    
}
