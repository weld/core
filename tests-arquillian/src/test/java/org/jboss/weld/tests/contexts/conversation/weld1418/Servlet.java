package org.jboss.weld.tests.contexts.conversation.weld1418;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import jakarta.enterprise.context.Conversation;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 *
 */
@SuppressWarnings("serial")
@WebServlet(value = "/servlet/*")
public class Servlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(Servlet.class.getName());

    @Inject
    private Conversation conversation;

    @Inject
    private SomeBean someBean;

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logInfo("Request started: " + req.getPathInfo());
        try {
            PrintWriter out = resp.getWriter();
            HttpSession session = req.getSession(true);
            logInfo("session.getId() = " + session.getId());
            if (req.getPathInfo().startsWith("/startConversation")) {
                conversation.begin();
                logInfo("conversation.getId() = " + conversation.getId());
                someBean.setValue(req.getParameter("value"));
                out.println(conversation.getId());
                logInfo("sending cid to client: cid=" + conversation.getId());
                out.flush();
                sleep(req.getParameter("sleep") == null ? 3000 : Integer.parseInt(req.getParameter("sleep"))); // keep request open, so another request can be made while this one is still open

            } else if (req.getPathInfo().startsWith("/getValue")) {
                logInfo("conversation.getId() = " + conversation.getId());
                out.println((Object) someBean.getValue());
            } else {
                out.println("");
            }
        } finally {
            logInfo("Request finished: " + req.getPathInfo());
        }
    }

    private void logInfo(String msg) {
        synchronized (log) {
            log.info(msg);
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
