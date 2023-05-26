package org.izumi.pdvt.gradle.task;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import com.github.mizosoft.methanol.MultipartBodyPublisher;
import lombok.RequiredArgsConstructor;
import org.gradle.api.logging.Logger;
import org.izumi.pdvt.gradle.File;
import org.izumi.pdvt.gradle.Utils;
import org.izumi.pdvt.gradle.parameters.Parameters;

@RequiredArgsConstructor
public class SendableReport {
    private static final HttpResponse.BodyHandler<InputStream> BODY_HANDLER = HttpResponse.BodyHandlers.ofInputStream();
    private final File file;
    private final Parameters parameters;
    private final Logger logger;

    public void send() {
        final String clientCode = parameters.getClientCode().orElseThrow();
        final String decoded = new String(Base64.getDecoder().decode(clientCode));
        final int firstColon = decoded.indexOf(':');
        if (firstColon == -1) {
            throw new IllegalArgumentException("Invalid client code in params detected");
        }

        final String code = decoded.substring(0, firstColon);
        final String server = decoded.substring(firstColon + 1);

        final HttpClient client = HttpClient.newHttpClient();
        final HttpRequest createUserRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://" + server + "/client/" + code + "/create-if-absent"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        final HttpResponse<InputStream> clientCodeCheckResponse = Utils.silently(
                () -> client.send(createUserRequest, BODY_HANDLER)
        );
        if (clientCodeCheckResponse.statusCode() != 200) {
            logger.lifecycle("Failed to check client-code");
            return;
        }

        final MultipartBodyPublisher publisher = Utils.silently(() ->
                MultipartBodyPublisher.newBuilder().filePart("file", file.asPath()).build()
        );
        final HttpRequest uploadFileRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://" + server + "/client/" + code + "/files/add"))
                .POST(publisher)
                .header("Content-Type", "multipart/form-data; boundary=" + publisher.boundary())
                .build();

        Utils.silently(() -> {
            final HttpResponse<InputStream> response = client.send(uploadFileRequest, BODY_HANDLER);
            if (response.statusCode() != 200) {
                logger.lifecycle("Failed to send file to server");
            }
        });
    }
}
