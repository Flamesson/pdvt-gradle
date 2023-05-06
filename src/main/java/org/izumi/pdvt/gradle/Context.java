package org.izumi.pdvt.gradle;

public class Context {
    private static final String LEXEME = ";";
    private static final String ALIAS_SIGN = "=";
    private static final String MAPPING_SIGN = "->";

    public String getLexemeEnd() {
        return LEXEME;
    }

    public String getAliasSign() {
        return ALIAS_SIGN;
    }

    public String getMappingSign() {
        return MAPPING_SIGN;
    }
}
