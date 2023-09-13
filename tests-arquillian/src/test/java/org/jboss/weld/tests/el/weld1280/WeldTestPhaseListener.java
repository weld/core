package org.jboss.weld.tests.el.weld1280;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;

public class WeldTestPhaseListener implements PhaseListener {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void afterPhase(PhaseEvent event) {
        testELResolver("helloBean");
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        testELResolver("helloBean");
    }

    private void testELResolver(String name) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELResolver resolver = facesContext.getApplication().getELResolver();
        ELContext elContext = facesContext.getELContext();
        Object object = resolver.getValue(elContext, null, name);
        if (object == null)
            throw new NullPointerException("ELResolver returned null");
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

}
