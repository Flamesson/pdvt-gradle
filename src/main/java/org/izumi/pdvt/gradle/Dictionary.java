package org.izumi.pdvt.gradle;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class Dictionary extends File {
    private static final Context context = new Context();

    public Dictionary(Path path) {
        super(path);
    }

    public Dictionary(java.io.File file) {
        super(file);
    }

    public Dictionary(String path) {
        super(path);
    }

    public Alias getAlias(String dependency, Supplier<String> defaultAlias) {
        return findAlias(dependency).orElseGet(() -> {
            final Alias alias = new Alias(dependency, defaultAlias.get());
            appendln(alias);
            return alias;
        });
    }

    public Optional<Alias> findAlias(String aliased) {
        try (final BufferedReader reader = newBufferedReader()) {
            while (reader.ready()) {
                final String line = reader.readLine();
                final String[] lexemes = line.split(context.getLexemeEnd());
                for (String lexeme : lexemes) {
                    final Alias alias = Alias.parseLexeme(lexeme);
                    if (Objects.equals(alias.getRenamed(), aliased)) {
                        return Optional.of(alias);
                    }
                }
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        return Optional.empty();
    }

    private void appendln(Alias alias) {
        appendln(alias.toString());
    }
}
