package com.malt.multilaunch.multicontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.malt.multilaunch.model.Account;
import com.malt.multilaunch.model.Config;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface MultiControllerService {
    Logger LOG = LoggerFactory.getLogger(MultiControllerService.class);

    void unassignAllToons();

    void sendAssignRequestsToController(HashMap<Account, WindowAssignRequest> requests);

    Optional<WindowAssignRequest> lastAssignedForAccount(Account account);

    void setMode(ControllerMode mode, String substate);

    static MultiControllerService createDefault(Config config) {
        LOG.debug("Creating multicontroller service, enabled: {}", config.enableMultiControllerIntegration());
        return new ConfigAwareMulticontrollerService(config, new DefaultMulticontrollerService());
    }

    void swapHandles(Account acct1, long hwnd1, Account acct2, long hwnd2);

    class DefaultMulticontrollerService implements MultiControllerService {
        private static final Logger LOG = LoggerFactory.getLogger(DefaultMulticontrollerService.class);
        private static final String MULTICONTROLLER_BASE_ENDPOINT = "http://127.0.0.1:12525/";
        public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        private final Map<Account, WindowAssignRequest> lastRequests;

        private DefaultMulticontrollerService() {
            lastRequests = new HashMap<>();
        }

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
                logError(e);
            }
        }

        @Override
        public void sendAssignRequestsToController(HashMap<Account, WindowAssignRequest> mappings) {
            var requests = mappings.values();
            LOG.debug("Sending requests to controller {}", requests);
            try (var httpClient = HttpClient.newHttpClient()) {
                var assign = new URI(MULTICONTROLLER_BASE_ENDPOINT + "assign");
                var windowRequestsAsString = OBJECT_MAPPER.writeValueAsString(requests);
                LOG.debug("Window request as string is {}", windowRequestsAsString);
                var request = HttpRequest.newBuilder(assign)
                        .POST(HttpRequest.BodyPublishers.ofString(windowRequestsAsString))
                        .build();
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                lastRequests.putAll(mappings);
            } catch (URISyntaxException | IOException | InterruptedException e) {
                logError(e);
            }
        }

        @Override
        public Optional<WindowAssignRequest> lastAssignedForAccount(Account account) {
            return Optional.ofNullable(lastRequests.get(account));
        }

        @Override
        public void setMode(ControllerMode mode, String substate) {}

        @Override
        public void swapHandles(Account acct1, long hwnd1, Account acct2, long hwnd2) {
            LOG.debug("Sending swap request to controller for hwnds {} and {}", hwnd1, hwnd2);
            try (var httpClient = HttpClient.newHttpClient()) {
                var swapEndpoint = new URI(MULTICONTROLLER_BASE_ENDPOINT + "swap");
                var swapRequest = new SwapRequest(hwnd1, hwnd2);
                var request = HttpRequest.newBuilder(swapEndpoint)
                        .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(swapRequest)))
                        .build();
                var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                LOG.debug("Got response for swap request {}", response.body());
                if (response.statusCode() == 200) {
                    var prevAssignAcct1 = lastRequests.get(acct1);
                    var prevAssignAcct2 = lastRequests.get(acct2);
                    lastRequests.put(acct1, prevAssignAcct2);
                    lastRequests.put(acct2, prevAssignAcct1);
                }
            } catch (URISyntaxException | IOException | InterruptedException e) {
                logError(e);
            }
        }

        private static void logError(Exception e) {
            LOG.error(
                    "Could not perform multicontroller action, is endpoint {} down? {}",
                    MULTICONTROLLER_BASE_ENDPOINT,
                    e.getMessage());
        }
    }
}
