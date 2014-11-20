package org.jboss.weld.environment.servlet.test.context.async;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "AsyncServlet", urlPatterns = { "/AsyncServlet" }, asyncSupported = true)
@SuppressWarnings("serial")
public class AsyncServlet extends HttpServlet {

    public static final String TEST_TIMEOUT = "timeout";
    public static final String TEST_COMPLETE = "complete";
    public static final String TEST_ERROR = "error";
    public static final String TEST_LOOP = "loop";
    private static final String[] VALID_TESTS = new String[] { TEST_TIMEOUT, TEST_COMPLETE, TEST_ERROR, TEST_LOOP };

    private static final long TIMEOUT = 200l;

    private static boolean inLoop = false;

    private ExecutorService executorService;

    @Override
    public void init() throws ServletException {
        // Note that executor thread does not use the same CL
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String test = req.getParameter("test");
        if (!Arrays.asList(VALID_TESTS).contains(test)) {
            resp.setStatus(404);
            return;
        }

        SimpleAsyncListener.reset();
        final AsyncContext actx = req.startAsync();
        actx.addListener(actx.createListener(SimpleAsyncListener.class));
        resp.setContentType("text/plain");

        if (TEST_TIMEOUT.equals(test)) {
            actx.setTimeout(TIMEOUT);
        } else if (TEST_COMPLETE.equals(test)) {
            executorService.execute(new AsyncRequestProcessor(actx, 50l, false, null));
        } else if (TEST_ERROR.equals(test)) {
            executorService.execute(new AsyncRequestProcessor(actx, 50l, true, "/FailingServlet"));
        } else if (TEST_LOOP.equals(test)) {
            if (inLoop) {
                executorService.execute(new AsyncRequestProcessor(actx, 50l, false, null));
            } else {
                executorService.execute(new AsyncRequestProcessor(actx, 50l, true, null));
                inLoop = true;
            }
        }
    }
}
