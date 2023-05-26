package org.izumi.pdvt.gradle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
public class File implements Item {
    private final Charset charset = StandardCharsets.UTF_8;
    private final Path path;

    public File(Path path) {
        this.path = path;
    }

    public File(java.io.File file) {
        this.path = file.toPath();
    }

    public File(String path) {
        this.path = Paths.get(path);
    }

    @Override
    public void create() {
        Utils.silently(() -> Files.createFile(path));
    }

    @Override
    public void createIfAbsent() {
        Utils.silently(() -> {
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        });
    }

    @Override
    public void recreate() {
        deleteIfExists();
        create();
    }

    @Override
    public void delete() {
        Utils.silently(() -> Files.delete(path));
    }

    @Override
    public void deleteIfExists() {
        Utils.silently(() -> Files.deleteIfExists(path));
    }

    public boolean isEmpty() {
        return path.toFile().length() == 0;
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public void append(Object toWrite) {
        final OpenOption option = StandardOpenOption.APPEND;
        Utils.silently(() -> Files.writeString(path, toWrite.toString(), charset, option));
    }

    public void appendln(Object toWrite) {
        final OpenOption option = StandardOpenOption.APPEND;
        Utils.silently(() -> Files.writeString(path, toWrite.toString() + System.lineSeparator(), charset, option));
    }

    public void appendln(File file) {
        try (final BufferedReader reader = file.newBufferedReader();
             final BufferedWriter writer = newBufferedWriter(StandardOpenOption.APPEND)) {
            final char[] buffer = new char[1024 * 4];
            while (true) {
                final int read = reader.read(buffer);
                if (read == -1) {
                    break;
                }

                writer.write(buffer, 0, read);
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public boolean containsLine(String line) {
        return Utils.silently(() -> {
            if (!Files.exists(path)) {
                return false;
            }

            final BufferedReader reader = Files.newBufferedReader(path);
            while (reader.ready()) {
                final String readLine = reader.readLine();
                if (Objects.equals(readLine, line)) {
                    return true;
                }
            }

            reader.close();
            return false;
        });
    }

    public boolean doesNotContainLine(String line) {
        return !containsLine(line);
    }

    public BufferedReader newBufferedReader() {
        return Utils.silently(() -> Files.newBufferedReader(path));
    }

    public BufferedWriter newBufferedWriter() {
        return Utils.silently(() -> Files.newBufferedWriter(path));
    }

    public BufferedWriter newBufferedWriter(OpenOption... options) {
        return Utils.silently(() -> Files.newBufferedWriter(path, options));
    }

    public OutputStream newOutputStream() {
        return Utils.silently(() -> Files.newOutputStream(path));
    }

    public Path asPath() {
        return path;
    }

    public java.io.File asFile() {
        return path.toFile();
    }
}
