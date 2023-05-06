package org.izumi.pdvt.gradle;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class Alias {
    private static final Context context = new Context();

    /**
     * <p>F.e. - dependency (org.apache.commons:commons-exec:1.3 or org.apache.common:commons-exec).</p>
     */
    private final String renamed;

    /**
     * <p>Alias. F.e - a, a11, etc.</p>
     */
    private final String newName;

    public static Alias parseLexeme(String lexeme) {
        final int signIndex = lexeme.indexOf(context.getAliasSign());
        final String aliased = lexeme.substring(0, signIndex);
        final String alias = lexeme.substring(signIndex + context.getAliasSign().length());
        return new Alias(aliased, alias);
    }

    @Override
    public String toString() {
        return renamed + context.getAliasSign() + newName + context.getLexemeEnd();
    }
}
