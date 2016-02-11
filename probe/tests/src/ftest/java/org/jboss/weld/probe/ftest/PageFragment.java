/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.probe.ftest;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
public class PageFragment {
    
    @FindBy(partialLinkText = "Dashboard")
    private WebElement dashboardTab;
    
    @FindBy(partialLinkText = "Bean Archives")
    private WebElement beanArchivesTab;
    
    @FindBy(partialLinkText = "Beans")
    private WebElement beansTab;
    
    @FindBy(partialLinkText = "Observers")
    private WebElement observersTab;
    
    @FindBy(partialLinkText = "Monitoring")
    private WebElement monitoringTab;
    
    @FindBy(partialLinkText = "Weld Configuration")
    private WebElement weldConfigurationTab;

    @FindBy(partialLinkText = "SessionScoped")
    private WebElement sessionScopedContext;
    
    @FindBy(partialLinkText = "ApplicationScoped")
    private WebElement applicationScopedContext;
    
    @FindBy(partialLinkText = "ConversationScoped")
    private WebElement conversationScopedContext;
    
    @FindBy(partialLinkText = "Invocation Trees")
    private WebElement invocationTrees;
    
    @FindBy(partialLinkText = "Fired Events")
    private WebElement firedEvents;
    
    public WebElement getBeanArchivesTab() {
        return beanArchivesTab;
    }

    public WebElement getBeansTab() {
        return beansTab;
    }

    public WebElement getSessionScopedContext() {
        return sessionScopedContext;
    }

    public WebElement getFiredEvents() {
        return firedEvents;
    }

    public WebElement getApplicationScopedContext() {
        return applicationScopedContext;
    }

    public WebElement getConversationScopedContext() {
        return conversationScopedContext;
    }

    public WebElement getInvocationTrees() {
        return invocationTrees;
    }

    public WebElement getObserversTab() {
        return observersTab;
    }

    public WebElement getMonitoringTab() {
        return monitoringTab;
    }

    public WebElement getWeldConfigurationTab() {
        return weldConfigurationTab;
    }
    
    public WebElement getDashboardTab() {
        return dashboardTab;
    }
}
