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

import jakarta.annotation.Priority;
import org.jboss.weld.examples.pastecode.model.CodeFragment;

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;
import java.io.Serializable;

/**
 * Prohibit posting more than 2 fragments a minute
 *
 * @author Pete Muir
 */
@Decorator
@Priority(jakarta.interceptor.Interceptor.Priority.APPLICATION + 10)
public abstract class FloodingDecorator implements CodeFragmentManager, Serializable {

    private static final long serialVersionUID = -4615837206290420112L;

    @Inject
    @Delegate
    private CodeFragmentManager codeFragmentManager;

    @Inject
    private PostTracker postTracker;

    public String addCodeFragment(CodeFragment code, boolean privateFragment) {
        // Check if we are allowed to post
        if (postTracker.isNewPostAllowed()) {
            postTracker.addPost();
            return codeFragmentManager.addCodeFragment(code, privateFragment);
        } else {
            throw new IllegalStateException("You've posted more than 2 fragments in the last 20s. No flooding allowed!");
        }
    }

}
