package org.jboss.weld.environment.osgi.samples.ee;

import java.util.ArrayList;
import java.util.List;
import org.jboss.weld.environment.osgi.samples.ee.annotation.View;
import org.jboss.weld.osgi.examples.web.api.Hotel;

@View
public class HotelView {

    private String country;
    private String message = "Please enter a country ...";
    private List<Hotel> hotels;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Hotel> getHotels() {
        if (hotels == null) {
            hotels = new ArrayList<Hotel>();
        }
        return hotels;
    }

    public void setHotels(List<Hotel> hotels) {
        this.hotels = hotels;
    }
}
