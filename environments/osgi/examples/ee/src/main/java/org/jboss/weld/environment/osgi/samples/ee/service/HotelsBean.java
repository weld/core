package org.jboss.weld.environment.osgi.samples.ee.service;

import org.jboss.weld.environment.osgi.samples.ee.HotelView;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jboss.weld.environment.osgi.api.Service;
import org.jboss.weld.osgi.examples.web.api.Hotel;
import org.jboss.weld.osgi.examples.web.api.HotelProvider;

@Stateless
public class HotelsBean {

    @Inject HotelView view;
    @Inject Service<HotelProvider> providers;

    public List<Hotel> getHotels() {
        List<Hotel> hotels = new ArrayList<Hotel>();
        for (HotelProvider provider : providers) {
            if (view.getCountry() == null || view.getCountry().equals("")) {
                hotels.addAll(provider.hotels());
            } else {
                if (view.getCountry().equals(provider.getCountry())) {
                    hotels.addAll(provider.hotels());
                }
            }
        }
        return hotels;
    }
}
