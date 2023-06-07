package org.izumi.pdvt.gradle.task;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Function;

import com.github.mizosoft.methanol.Methanol;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import com.github.mizosoft.methanol.MutableRequest;
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
    private final Function<Code, String> uriSupplier;

    public void send() {
        final Code code = new Code(parameters.getClientCode().orElseThrow());

        final Methanol client = Methanol.create();
        final MutableRequest createUserRequest = MutableRequest.POST(
                URI.create(code.getServer() + "/client/" + code.getCodeword() + "/create-if-absent"),
                HttpRequest.BodyPublishers.ofString(code.getRaw())
        );
        final HttpResponse<InputStream> clientCodeCheckResponse = Utils.silently(
                () -> client.send(createUserRequest, BODY_HANDLER)
        );
        if (clientCodeCheckResponse.statusCode() != 200) {
            logger.lifecycle("Failed to check client-code");
            return;
        }

        final MultipartBodyPublisher publisher = Utils.silently(() ->
                MultipartBodyPublisher.newBuilder()
                        .textPart("code", code.getRaw())
                        .filePart("file", file.asPath())
                        .build()
        );

        final MutableRequest request = MutableRequest.POST(URI.create(uriSupplier.apply(code)), publisher);
        Utils.silently(() -> {
            final HttpResponse<InputStream> response = client.send(request, BODY_HANDLER);
            if (response.statusCode() != 200) {
                logger.lifecycle("Failed to send file to server");
            }
        });
    }
}
