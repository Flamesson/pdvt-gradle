package org.izumi.pdvt.gradle;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class Mapping {
    private static final Context context = new Context();

    /**
     * <p>Source dependency (or it' alias).</p>
     */
    private final String source;

    /**
     * <p>Target dependency (or it' alias).</p>
     */
    private final String target;

    public static Mapping parseLexeme(String lexeme) {
        final int signIndex = lexeme.indexOf(context.getMappingSign());
        final String source = lexeme.substring(0, signIndex);
        final String target = lexeme.substring(signIndex + context.getMappingSign().length());
        return new Mapping(source, target);
    }

    @Override
    public String toString() {
        return source + context.getMappingSign() + target + context.getLexemeEnd();
    }
}
