package com.malt.multilaunch.launcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.malt.multilaunch.launcher.launchers.SunriseJpUltiLauncherModule;
import com.malt.multilaunch.login.SunriseApiResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class Sunrise2004GameLoginClient implements GameLoginClient<SunriseApiResponse> {
    @Override
    public SunriseApiResponse login(String username, String password, Map<String, String> additionalArgs) {
        try (var client = HttpClient.newHttpClient()) {

            var formData = SunriseJpUltiLauncherModule.generateFormData(username, password, additionalArgs);

            var request = HttpRequest.newBuilder(getLoginApiUri())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            var objectReader = new ObjectMapper().readerFor(responseType());
            return objectReader.readValue(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URI getLoginApiUri() {
        return URI.create("https://sunrise.games/api/login/alt/");
    }

    @Override
    public Class<SunriseApiResponse> responseType() {
        return SunriseApiResponse.class;
    }
}
