package org.jboss.weld.environment.osgi.samples.ee;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.jboss.weld.environment.osgi.api.Service;
import org.jboss.weld.environment.osgi.api.annotation.Required;
import org.jboss.weld.osgi.examples.web.api.Hotel;
import org.jboss.weld.osgi.examples.web.api.HotelProvider;

@Path("ctrl")
@Stateless
public class HotelController {

    @Inject
    @Required
    Service<HotelProvider> providers;
    @Inject
    App app;

    @GET
    @Path("hotels")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Hotel> hotels() {
        List<Hotel> hotels = new ArrayList<Hotel>();
        if (app.isValid()) {
            for (HotelProvider provider : providers) {
                hotels.addAll(provider.hotels());
            }
        }
        return hotels;
    }

    @GET
    @Path("hotels/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Hotel hotelForAnId(@PathParam("id") String id) {
        Hotel hotel = null;
        if (app.isValid()) {
            for (HotelProvider provider : providers) {
                for (Hotel h : provider.hotels()) {
                    if (h.getId().equals(id)) {
                        hotel = h;
                    }
                }
            }
        }
        return hotel;
    }

    @GET
    @Path("book")
    @Produces(MediaType.TEXT_PLAIN)
    public String bookHotel(@QueryParam("hotel.id") String id,
            @QueryParam("booking.checkinDate") Date checkin,
            @QueryParam("booking.checkoutDate") Date checkout,
            @QueryParam("booking.beds") Integer beds,
            @QueryParam("booking.smoking") Boolean smocking,
            @QueryParam("booking.creditCard") String cardNumber,
            @QueryParam("booking.creditCardName") String cardName,
            @QueryParam("booking.creditCardExpiryMonth") String cardMonth,
            @QueryParam("booking.creditCardExpiryYear") String cardYear) {
        if (app.isValid()) {
            for (HotelProvider provider : providers) {
                for (Hotel h : provider.hotels()) {
                    if (h.getId().equals(id)) {
                        boolean success = provider.book(id, checkin, checkout, beds, smocking, cardNumber, cardName, cardMonth, cardYear);
                        if (success) {
                            return "success";
                        } else {
                            return "failure";
                        }
                    }
                }
            }
        }
        return "failure";
    }

    @GET
    @Path("countries")
    @Produces(MediaType.TEXT_PLAIN)
    public String countries() {
        StringBuilder hotels = new StringBuilder();
        if (app.isValid()) {
            for (HotelProvider provider : providers) {
                hotels.append(provider.getCountry());
                hotels.append(",");
            }
        }
        return hotels.toString();
    }

    @GET
    @Path("countries/{country}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Hotel> hotelForACountry(@PathParam("country") String country) {
        List<Hotel> hotels = new ArrayList<Hotel>();
        if (app.isValid()) {
            for (HotelProvider provider : providers) {
                if (provider.getCountry().equals(country)) {
                    hotels.addAll(provider.hotels());
                    break;
                }
            }
        }
        return hotels;
    }

    @GET
    @Path("none")
    @Produces(MediaType.TEXT_PLAIN)
    public String none() {
        if (app.isValid()) {
            return "false";
        }
        return "true";
    }
}
