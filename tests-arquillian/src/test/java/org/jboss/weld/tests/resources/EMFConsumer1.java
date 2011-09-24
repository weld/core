package org.jboss.weld.tests.resources;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

@WebServlet("/emfconsumer1")
public class EMFConsumer1 extends HttpServlet {

    @Inject
    @ProducedViaStaticFieldOnEJB
    private EntityManagerFactory emf;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (emf == null || emf.createEntityManager() == null) {
            resp.sendError(SC_INTERNAL_SERVER_ERROR);
        }
    }

}
