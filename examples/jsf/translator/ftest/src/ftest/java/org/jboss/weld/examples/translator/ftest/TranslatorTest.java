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
package org.jboss.weld.examples.translator.ftest;

import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.jboss.arquillian.graphene.Graphene.waitModel;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
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
        waitModel(driver).until().element(INPUT_AREA).is().present();
    }

    @Test
    public void translateTest() {
        driver.findElement(INPUT_AREA).clear();
        driver.findElement(INPUT_AREA).sendKeys(ONE_SENTENCE);
        guardHttp(driver.findElement(TRANSLATE_BUTTON)).click();
        assertTrue("One sentence translated into latin expected.", driver.findElement(BODY).getText().contains(ONE_SENTENCE_TRANSLATED));
        driver.findElement(INPUT_AREA).clear();
        driver.findElement(INPUT_AREA).sendKeys(MORE_SENTENCES);
        guardHttp(driver.findElement(TRANSLATE_BUTTON)).click();
        assertTrue("More sentences translated into latin expected.", driver.findElement(BODY).getText().contains(MORE_SENTENCES_TRANSLATED));
    }

}
