package org.jboss.weld.environment.osgi.samples.ee;

import org.jboss.weld.environment.osgi.samples.ee.annotation.Presenter;
import org.jboss.weld.environment.osgi.samples.ee.service.HotelsBean;
import javax.ejb.EJB;
import javax.inject.Inject;

@Presenter
public class HotelPresenter {

    @Inject
    HotelView view;
    @EJB
    HotelsBean helloBean;

    public void searchHotels() {
        view.setMessage("Hotels for " + view.getCountry());
        view.setHotels(helloBean.getHotels());
    }
}
