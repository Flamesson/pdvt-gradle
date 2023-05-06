package org.izumi.pdvt.gradle;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
public interface Item {
    void create();
    void createIfAbsent();
    void recreate();
    void delete();
    void deleteIfExists();
}
