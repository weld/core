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

import java.util.Date;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Dan Allen
 */
@Named
@RequestScoped
public class Comment {

    private static final String SEMICOLON = ";";

    private Long id;

    private BlogEntry entry;

    private Date postDate;

    private String author;

    private boolean remember;

    private String body;

    private Users users;

    public Comment() {
    }

    public Comment(Long id, BlogEntry entry, String author, Date postDate, String body) {
        this.id = id;
        this.entry = entry;
        this.author = author;
        this.postDate = postDate;
        this.body = body;
    }

    public Comment(Comment other) {
        this.id = other.getId();
        this.entry = other.getEntry();
        this.author = other.getAuthor();
        this.postDate = other.getPostDate();
        this.body = other.getBody();
    }

    public void checkAuthor() {
        if (users != null && isRemember()) {
            users.setUsername(author);
        }
    }

    public BlogEntry getEntry() {
        return entry;
    }

    public void setEntry(BlogEntry entry) {
        this.entry = entry;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public String getAuthor() {
        if (users != null) {
            String username = users.getUsername();
            if (username != null) {
                return username;
            }
        }
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isRemember() {
        return remember;
    }

    public void setRemember(boolean remember) {
        this.remember = remember;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Inject
    public void setUsers(Users users) {
        this.users = users;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Comment@").append(hashCode()).append("{");
        sb.append("id=").append(id).append(SEMICOLON);
        sb.append("author=").append(author).append(SEMICOLON);
        sb.append("body=").append(body);
        sb.append("}");
        return sb.toString();
    }
}
