/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.examples.pastecode.session;

import org.jboss.weld.examples.pastecode.model.CodeFragment;

import javax.ejb.EJBException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * PasteWindow holds the code fragment and other selections when a code fragment
 * is viewed and entered
 *
 * @author Pete Muir
 * @author Martin Gencur
 */
@Named
@RequestScoped
public class PasteWindow {
    private CodeFragment codeFragment;

    private String codeFragmentId;

    private Theme theme;

    private boolean privateFragment;

    @Inject
    private CodeFragmentManager codeFragmentManager;

    public PasteWindow() {
        this.codeFragment = new CodeFragment();
        this.theme = Theme.DEFAULT;
    }

    // The send method is called when we hit the Send button
    public String send() {
        codeFragmentId = codeFragmentManager.addCodeFragment(codeFragment, privateFragment);
        return "success";
    }

    // loadCodeFragment is a view action called to load the code fragment from
    // the database when requested for viewing
    public void loadCodeFragment() {
        this.codeFragment = codeFragmentManager.getCodeFragment(codeFragmentId);

        if (this.codeFragment == null) {
            throw new EJBException("Could not read entity with given id value");
        }
    }

    public CodeFragment getCodeFragment() {
        return codeFragment;
    }

    public String getCodeFragmentId() {
        return codeFragmentId;
    }

    public void setCodeFragmentId(String codeFragmentId) {
        this.codeFragmentId = codeFragmentId;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public boolean isPrivateFragment() {
        return privateFragment;
    }

    public void setPrivateFragment(boolean privateFragment) {
        this.privateFragment = privateFragment;
    }
}
