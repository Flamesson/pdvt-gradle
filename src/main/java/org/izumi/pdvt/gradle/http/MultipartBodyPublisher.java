package org.izumi.pdvt.gradle.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Supplier;

public class MultipartBodyPublisher {
    private static final String LINE_SEPARATOR = "\r\n";
    private final List<PartsSpecification> partsSpecificationList = new ArrayList<>();
    private final String boundary = UUID.randomUUID().toString();

    public HttpRequest.BodyPublisher build() {
        if (partsSpecificationList.isEmpty()) {
            throw new IllegalStateException("Must have at least one part to build multipart message.");
        }

        addFinalBoundaryPart();

        return HttpRequest.BodyPublishers.ofByteArrays(PartsIterator::new);
    }

    public String getBoundary() {
        return boundary;
    }

    public MultipartBodyPublisher addPart(String name, String value) {
        PartsSpecification newPart = new PartsSpecification();
        newPart.type = PartsSpecification.TYPE.STRING;
        newPart.name = name;
        newPart.value = value;
        partsSpecificationList.add(newPart);

        return this;
    }

    public MultipartBodyPublisher addPart(String name, Path value) {
        PartsSpecification newPart = new PartsSpecification();
        newPart.type = PartsSpecification.TYPE.FILE;
        newPart.name = name;
        newPart.path = value;
        partsSpecificationList.add(newPart);

        return this;
    }

    public MultipartBodyPublisher addPart(String name, Supplier<InputStream> value, String filename, String contentType) {
        PartsSpecification newPart = new PartsSpecification();
        newPart.type = PartsSpecification.TYPE.STREAM;
        newPart.name = name;
        newPart.stream = value;
        newPart.filename = filename;
        newPart.contentType = contentType;
        partsSpecificationList.add(newPart);

        return this;
    }

    private void addFinalBoundaryPart() {
        PartsSpecification newPart = new PartsSpecification();
        newPart.type = PartsSpecification.TYPE.FINAL_BOUNDARY;
        newPart.value = "--" + boundary + "--";
        partsSpecificationList.add(newPart);
    }

    static class PartsSpecification {
        public enum TYPE {
            STRING, FILE, STREAM, FINAL_BOUNDARY
        }

        PartsSpecification.TYPE type;
        String name;
        String value;
        Path path;
        Supplier<InputStream> stream;
        String filename;
        String contentType;
    }

    class PartsIterator implements Iterator<byte[]> {
        private final Iterator<PartsSpecification> delegate;
        private InputStream currentFileInput;

        private boolean done;
        private byte[] next;

        PartsIterator() {
            delegate = partsSpecificationList.iterator();
        }

        @Override
        public boolean hasNext() {
            if (done)  {
                return false;
            }
            if (next != null) {
                return true;
            }

            try {
                next = computeNext();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            if (next == null) {
                done = true;
                return false;
            }

            return true;
        }

        @Override
        public byte[] next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            byte[] res = next;
            next = null;
            return res;
        }

        private byte[] computeNext() throws IOException {
            if (currentFileInput == null) {
                if (!delegate.hasNext()) {
                    return null;
                }

                PartsSpecification nextPart = delegate.next();
                if (PartsSpecification.TYPE.STRING.equals(nextPart.type)) {
                    String part =
                            "--" + boundary + LINE_SEPARATOR +
                                    "Content-Disposition: form-data; name=" + nextPart.name + LINE_SEPARATOR +
                                    "Content-Type: text/plain; charset=UTF-8" + LINE_SEPARATOR.repeat(2) +
                                    nextPart.value + LINE_SEPARATOR;
                    return part.getBytes(StandardCharsets.UTF_8);
                }

                if (PartsSpecification.TYPE.FINAL_BOUNDARY.equals(nextPart.type)) {
                    return nextPart.value.getBytes(StandardCharsets.UTF_8);
                }

                String filename;
                String contentType;
                if (PartsSpecification.TYPE.FILE.equals(nextPart.type)) {
                    Path path = nextPart.path;
                    filename = path.getFileName().toString();
                    contentType = Files.probeContentType(path);
                    if (contentType == null) contentType = "application/octet-stream";
                    currentFileInput = Files.newInputStream(path);
                } else {
                    filename = nextPart.filename;
                    contentType = nextPart.contentType;
                    if (contentType == null) contentType = "application/octet-stream";
                    currentFileInput = nextPart.stream.get();
                }

                String partHeader =
                        "--" + boundary + LINE_SEPARATOR +
                                "Content-Disposition: form-data; name=" + nextPart.name + "; filename=" + filename + LINE_SEPARATOR +
                                "Content-Type: " + contentType + LINE_SEPARATOR.repeat(2);

                return partHeader.getBytes(StandardCharsets.UTF_8);
            } else {
                byte[] buf = new byte[8192];
                int r = currentFileInput.read(buf);
                if (r > 0) {
                    byte[] actualBytes = new byte[r];
                    System.arraycopy(buf, 0, actualBytes, 0, r);
                    return actualBytes;
                } else {
                    currentFileInput.close();
                    currentFileInput = null;
                    return LINE_SEPARATOR.getBytes(StandardCharsets.UTF_8);
                }
            }
        }
    }
}
