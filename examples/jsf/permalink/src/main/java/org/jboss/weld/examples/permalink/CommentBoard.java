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
package org.jboss.weld.examples.permalink;

import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 * @author Dan Allen
 */
@Model
public class CommentBoard {
    @Inject
    Repository repository;

    @Inject
    Comment comment;

    @Inject
    Blog blog;

    public Boolean post() {
        if (comment == null || blog == null) {
            return null;
        }

        BlogEntry entry = repository.getEntry(blog.getEntryId());
        if (entry == null) {
            return null;
        }

        comment.checkAuthor();

        repository.addComment(comment, entry);
        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.addMessage(null, new FacesMessage("Thanks for leaving a comment!"));
        // FIXME doesn't seem to be working; must investigate
        ctx.getExternalContext().getFlash().setKeepMessages(true);
        return true;
    }
}
