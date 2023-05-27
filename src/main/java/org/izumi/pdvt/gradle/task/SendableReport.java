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
        final Code code = new Code(parameters.getClientCode().orElseThrow());

        final HttpClient client = HttpClient.newHttpClient();
        final HttpRequest createUserRequest = HttpRequest.newBuilder()
                .uri(URI.create(code.getServer() + "/client/" + code.getCodeword() + "/create-if-absent"))
                .POST(HttpRequest.BodyPublishers.ofString(code.getRaw()))
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
                .uri(URI.create(code.getServer() + "/client/" + code.getCodeword() + "/files/add"))
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

    @RequiredArgsConstructor
    private static final class Code {
        private static final String PROTOCOL = "http://";

        private final String raw;
        private boolean initialized = false;
        private String decoded;
        private String codeword;
        private String address;
        private String password;

        public String getRaw() {
            return raw;
        }

        public String getCodeword() {
            if (!initialized) {
                decode();
            }

            return codeword;
        }

        public String getAddress() {
            if (!initialized) {
                decode();
            }

            return address;
        }

        public String getServer() {
            if (!initialized) {
                decode();
            }

            if (!address.startsWith(PROTOCOL)) {
                return PROTOCOL + address;
            } else {
                return address;
            }
        }

        public String getPassword() {
            if (!initialized) {
                decode();
            }

            return password;
        }

        private void decode() {
            this.decoded = new String(Base64.getDecoder().decode(raw));

            final int firstLengthFrom = 0;
            final String lengthFirstString = decoded.substring(firstLengthFrom, decoded.indexOf('.', firstLengthFrom));
            final int secondLengthFrom = lengthFirstString.length() + 1;
            final String lengthSecondString = decoded.substring(secondLengthFrom, decoded.indexOf('.', secondLengthFrom));
            final int thirdLengthFrom = lengthFirstString.length() + 1 + lengthSecondString.length() + 1;
            final String lengthThirdString = decoded.substring(thirdLengthFrom, decoded.indexOf('.', thirdLengthFrom));

            final int encodedFrom = lengthFirstString.length() + lengthSecondString.length() + lengthThirdString.length() + 3;

            final int lengthFirst = Integer.parseInt(lengthFirstString);
            final int lengthSecond = Integer.parseInt(lengthSecondString);
            final int lengthThird = Integer.parseInt(lengthThirdString);

            final int firstFrom = encodedFrom;
            final int secondFrom = encodedFrom + lengthFirst;
            final int thirdFrom = encodedFrom + lengthFirst + lengthSecond;

            this.codeword = decoded.substring(firstFrom, firstFrom + lengthFirst);
            this.address = decoded.substring(secondFrom, secondFrom + lengthSecond);
            this.password = decoded.substring(thirdFrom, thirdFrom +  lengthThird);

            this.initialized = true;
        }
    }

}
