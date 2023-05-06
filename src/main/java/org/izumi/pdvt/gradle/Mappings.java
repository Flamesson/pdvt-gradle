package org.izumi.pdvt.gradle;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class Mappings extends File {
    private static final Context context = new Context();

    public Mappings(Path path) {
        super(path);
    }

    public Mappings(java.io.File file) {
        super(file);
    }

    public Mappings(String path) {
        super(path);
    }

    public void addIfAbsent(Mapping mapping) {
        if (isPresent(mapping)) {
            return;
        }

        appendln(mapping);
    }

    public boolean isPresent(Mapping mapping) {
        try (final BufferedReader reader = newBufferedReader()) {
            while (reader.ready()) {
                final String line = reader.readLine();
                final String[] lexemes = line.split(context.getLexemeEnd());
                for (String lexeme : lexemes) {
                    final Mapping parsed = Mapping.parseLexeme(lexeme);
                    if (Objects.equals(mapping, parsed)) {
                        return true;
                    }
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        return false;
    }

    private void appendln(Mapping mapping) {
        appendln(mapping.toString());
    }
}
