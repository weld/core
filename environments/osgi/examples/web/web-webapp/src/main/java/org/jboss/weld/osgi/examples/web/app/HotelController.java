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

package org.jboss.weld.osgi.examples.web.app;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.jboss.weld.environment.osgi.api.Service;
import org.jboss.weld.environment.osgi.api.annotation.Required;
import org.jboss.weld.osgi.examples.web.api.Hotel;
import org.jboss.weld.osgi.examples.web.api.HotelProvider;
import org.jboss.weld.osgi.examples.web.fwk.api.Controller;
import org.jboss.weld.osgi.examples.web.fwk.view.Render;

@Path("hotels")
public class HotelController implements Controller {

    @Inject @Required Service<HotelProvider> providers;

    @Inject App app;

    @GET
    @Path("all")
    public Response all() {
        List<Hotel> hotels = new ArrayList<Hotel>();
        for (HotelProvider provider : providers) {
            hotels.addAll(provider.hotels());
        }
        if (app.isValid()) {
            return Render.view("hotel/all.xhtml", getClass())
                    .param("hotels", hotels)
                    .param("providers", providers)
                    .render();
        } else {
            return Render.view("hotel/none.xhtml", getClass())
                    .param("hotels", hotels)
                    .param("providers", providers)
                    .render();
        }
    }

    @GET
    @Path("index")
    public Response index() {
        return Render.view("index.xhtml", getClass())
                .render();
    }
}
