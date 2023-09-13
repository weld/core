package org.jboss.weld.environment.servlet.test.context.async;

import java.io.IOException;

import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "StatusServlet", urlPatterns = { "/Status" })
public class StatusServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        writeInfo(resp);
    }

    private void writeInfo(ServletResponse response) throws IOException {
        response.getWriter().println("onComplete: " + SimpleAsyncListener.onCompleteCalled);
        response.getWriter().println("onTimeout: " + SimpleAsyncListener.onTimeoutCalled);
        response.getWriter().println("onError: " + SimpleAsyncListener.onErrorCalled);
        response.getWriter().println("onStartAsync: " + SimpleAsyncListener.onStartAsyncCalled);
        response.getWriter().flush();
    }

}