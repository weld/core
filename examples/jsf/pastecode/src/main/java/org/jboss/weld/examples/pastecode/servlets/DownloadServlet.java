/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.examples.pastecode.servlets;

import org.jboss.weld.examples.pastecode.model.CodeFragment;
import org.jboss.weld.examples.pastecode.session.CodeFragmentManager;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet that offers the code fragment for download as a file
 *
 * @author Martin Gencur
 */
@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Inject
    private Logger log;

    @Inject
    private CodeFragmentManager codeFragmentManager;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        CodeFragment c = codeFragmentManager.getCodeFragment(id);
        String fileName = c.getUser() + "." + c.getLanguage();
        String txt = c.getText();

        response.setContentType("text/plain");
        response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setContentLength(txt.length());

        ServletOutputStream out = response.getOutputStream();
        try {
            out.print(txt);
        } catch (Exception e) {
            log.log(Level.WARNING, "Error processing file for download", e);
        } finally {
            out.close();
        }

    }
}
