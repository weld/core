/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.weld.osgi.examples.web.fwk.view;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 *
 * @author Mathieu ANCELIN
 */
public class Render {

    public static Response redirect(String url) {
        ResponseBuilder builder;
        try {
            builder = Response.seeOther(new URI(url));
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        return builder.build();
    }

    public static Response text(final String text) {
        return Response.ok(text, MediaType.TEXT_PLAIN).build();
    }

    public static Response binary(String file) {
        return Response.ok(new File(file), MediaType.APPLICATION_OCTET_STREAM).build();
    }

    public static Response binary(File file) {
        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM).build();
    }

    public static Response json(Object json) {
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    public static Response xml(Object xml) {
        return Response.ok(xml, MediaType.APPLICATION_XML).build();
    }

    public static Response notFound() {
        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.TEXT_HTML)
            .entity("<html><head><title>Page not found</title></head>"
                + "<body><h1>Page not found</h1></body></html>").build();
    }

    public static Response badRequest() {
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.TEXT_HTML)
            .entity("<html><head><title>Bad request</title></head>"
                        + "<body><h1>Bad request</h1></body></html>").build();
    }

    public static Response ok() {
        return Response.ok().build();
    }

    public static Response error() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.TEXT_HTML)
            .entity("<html><head><title>Error</title></head>"
                        + "<body><h1>Error</h1></body></html>").build();
    }

    public static Response unavailable() {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
            .type(MediaType.TEXT_HTML)
            .entity("<html><head><title>Error</title></head>"
                        + "<body><h1>Service unavailable</h1></body></html>").build();
    }

    public static Response accesDenied() {
        return Response.status(Response.Status.FORBIDDEN)
            .type(MediaType.TEXT_HTML)
            .entity("<html><head><title>Acces denied</title></head>"
                        + "<body><h1>Access Denied</h1></body></html>").build();
    }

    public static Response todo() {
        return Response.status(501)
            .type(MediaType.TEXT_HTML)
            .entity("<html><head><title>TODO</title></head>"
                        + "<body><h1>Page not yet implemented</h1></body></html>").build();
    }
}
