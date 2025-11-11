package com.malt.multilaunch.multicontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DefaultMulticontrollerService implements MultiControllerService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMulticontrollerService.class);
    private static final String MULTICONTROLLER_BASE_ENDPOINT = "http://127.0.0.1:12525/";
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    DefaultMulticontrollerService() {}

    @Override
    public void unassignAllToons() {
        try (var httpClient = HttpClient.newHttpClient()) {
            var unassignEndpoint = new URI(MULTICONTROLLER_BASE_ENDPOINT + "unassign");
            var request = HttpRequest.newBuilder(unassignEndpoint)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            LOG.debug("Received response {} from unassign request to {}", response, unassignEndpoint);
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendAssignRequestsToController(List<WindowAssignRequest> requests) {
        LOG.debug("Sending requests to controller {}", requests);
        try (var httpClient = HttpClient.newHttpClient()) {
            var assign = new URI(MULTICONTROLLER_BASE_ENDPOINT + "assign");
            var windowRequestsAsString = OBJECT_MAPPER.writeValueAsString(requests);
            LOG.debug("Window request as string is {}", windowRequestsAsString);
            var request = HttpRequest.newBuilder(assign)
                    .POST(HttpRequest.BodyPublishers.ofString(windowRequestsAsString))
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setMode(ControllerMode mode, String substate) {}

    @Override
    public void swapHandles(long hwnd1, long hwnd2) {
        LOG.debug("Sending swap request to controller for hwnds {} and {}", hwnd1, hwnd2);
        try (var httpClient = HttpClient.newHttpClient()) {
            var swapEndpoint = new URI(MULTICONTROLLER_BASE_ENDPOINT + "swap");
            var swapRequest = new SwapRequest(hwnd1, hwnd2);
            var request = HttpRequest.newBuilder(swapEndpoint)
                    .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(swapRequest)))
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            LOG.debug("Got response for swap request {}", response.body());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
