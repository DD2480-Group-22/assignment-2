package org.group22.server;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.group22.ci.ProjectTester;
import org.group22.utilities.Configuration;
import org.group22.utilities.Helpers;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ContinuousIntegrationServer extends AbstractHandler {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ContinuousIntegrationServer.class);

    /**
     * Sets up and starts the CI server.
     *
     * @param args Command line arguments
     * @throws Exception If something goes wrong with the server
     */
    public void runServer(String[] args) throws Exception {
        Helpers.setUpConfiguration(args);
        Server server = new Server(Configuration.SERVER_PORT);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }

    /**
     * Handles HTTP requests sent to the server. Calls appropriate helper function based on the typ of request.
     *
     * @param target      The target
     * @param baseRequest The baseRequest
     * @param request     The request
     * @param response    The response
     * @throws IOException If something goes wring while parsing the response
     */
    @Override
    public void handle(String target, @NotNull Request baseRequest, @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        try {
            response.setContentType("text/html;charset=utf-8");
            baseRequest.setHandled(true);

            if ("GET".equalsIgnoreCase(request.getMethod())) {
                handleGetRequest(request, response, target);
            } else if ("POST".equalsIgnoreCase(request.getMethod())) {
                handlePostRequest(request, response);
            }
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong while handling request");
            logger.error("Something went wrong while handling request", e);
        }

    }

    /**
     * Handles {@code GET} requests that are sent to the server. If the URL query string contains a id of a generated report the
     * client is redirected to the report. If the URL does not contain a valid id the client is sent a HTML document
     * in the format of a {@code String} that contains a list to all the generated reports stored on AWS.
     *
     * @param request  The request
     * @param response The response
     * @throws IOException If something goes wrong while parsing the request or sending the response
     */
    private void handleGetRequest(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull final String target) throws IOException {
        try {
            if (target.matches("/reports/[a-z0-9_-]+")) {
                final String reportId = request.getRequestURI().replace("/reports/", "");
                if (Configuration.PREVIOUS_BUILDS.contains(reportId)) {
                    final String reportURL = Helpers.reportAddressHTML(reportId);
                    response.sendRedirect(reportURL);
                    logger.info("Served build report for build with id: {}", reportId);
                } else {
                    logger.warn("URL for non-existing report was requested, id: {}", reportId);
                }
            } else {
                logger.warn("GET request with no matching target was received, target: {}", target);
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(Helpers.generateIndex());
            response.getWriter().flush();
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong while handling GET request");
            logger.error("Server failed to write response to GET request", e);
        }
    }

    /**
     * Handles {@code POST} requests that are sent to the server. Starts the CI process for the repository specified by
     * the request.
     *
     * @param request  The request
     * @param response The response
     * @throws IOException If something goes wrong while parsing the request or sending the response
     */
    private void handlePostRequest(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws IOException {
        String payload = IOUtils.toString(request.getReader());
        try {
            JSONObject jsonObject = new JSONObject(payload);
            if (Helpers.isPushEvent(jsonObject)) {
                ProjectTester projectTester = new ProjectTester(jsonObject);
                projectTester.processPush();
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().println("CI job running");
                logger.info("Successfully handled POST request");
            } else {
                logger.info("Received POST request with invalid JSON object");
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Post request most contain a head commit");
            }
        } catch (JSONException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong while handling POST request");
            logger.error("Server failed while parsing received payload", e);
        }
    }
}