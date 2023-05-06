package org.izumi.pdvt.gradle;

import java.util.concurrent.Callable;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
public final class Utils {
    private Utils() {}

    public static <T> T silently(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void silently(Action action) {
        try {
            action.execute();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public interface Action {
        void execute() throws Exception;
    }
}
