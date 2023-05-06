package org.izumi.pdvt.gradle.generator;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.ImmutableMap;

public class AliasGenerator {
    private static final Map<Character, Character> DIGIT_TO_NEXT_DIGIT = new ImmutableMap.Builder<Character, Character>()
            .put('0', '1')
            .put('1', '2')
            .put('2', '3')
            .put('3', '4')
            .put('4', '5')
            .put('5', '6')
            .put('6', '7')
            .put('7', '8')
            .put('8', '9')
            .put('9', '0')
            .build();
    private static final char INITIAL_LETTER = 'a';
    private static final char LATEST_LETTER = 'z';
    private static final char INITIAL_NUMBER = '0';
    private static final char LATEST_NUMBER = '9';
    private final Lock lock = new ReentrantLock();
    private String latestAlias;

    /**
     * <p>a0 a1 a2 ... b0 b1 b2 ... aa0 aa1 ... etc.</p>
     *
     * @return Next generated string.
     */
    public String generate() {
        lock.lock();
        try {
            if (Objects.isNull(latestAlias)) {
                latestAlias = Character.toString(INITIAL_LETTER) + INITIAL_NUMBER;
                return latestAlias;
            } else {
                final char letter = latestAlias.charAt(latestAlias.length() - 1);
                if (Character.isLetter(letter)) {
                    latestAlias = latestAlias + INITIAL_NUMBER;
                } else {
                    if (letter != LATEST_NUMBER) {
                        latestAlias = latestAlias.substring(0, latestAlias.length() - 1) + nextDigit(letter);
                    } else {
                        final char alphabetic = latestAlias.charAt(latestAlias.length() - 2);
                        if (alphabetic == LATEST_LETTER) {
                            final String alphabeticsPart = Character.toString(INITIAL_LETTER)
                                    .repeat(latestAlias.length());
                            latestAlias = alphabeticsPart + INITIAL_NUMBER;
                        } else {
                            final String withoutThisAlphabetic = latestAlias.substring(0, latestAlias.length() - 2);
                            final char nextAlphabetic = (char) (alphabetic + 1);
                            latestAlias = withoutThisAlphabetic + nextAlphabetic + INITIAL_NUMBER;
                        }
                    }
                }

                return latestAlias;
            }
        } finally {
            lock.unlock();
        }
    }

    private char nextDigit(char digit) {
        return DIGIT_TO_NEXT_DIGIT.get(digit);
    }
}
