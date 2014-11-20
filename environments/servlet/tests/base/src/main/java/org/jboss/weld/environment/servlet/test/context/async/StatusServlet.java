package org.jboss.weld.environment.servlet.test.context.async;

import java.io.IOException;

import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "StatusServlet", urlPatterns = { "/Status" })
public class StatusServlet extends HttpServlet {


    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        writeInfo(resp);
    }

    private void writeInfo(ServletResponse response) throws IOException {
        response.getWriter().println("onComplete: "+SimpleAsyncListener.onCompleteCalled);
        response.getWriter().println("onTimeout: "+SimpleAsyncListener.onTimeoutCalled);
        response.getWriter().println("onError: "+SimpleAsyncListener.onErrorCalled);
        response.getWriter().println("onStartAsync: "+SimpleAsyncListener.onStartAsyncCalled);
        response.getWriter().flush();
    }

}