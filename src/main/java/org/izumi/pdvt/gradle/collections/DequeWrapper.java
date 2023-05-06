package org.izumi.pdvt.gradle.collections;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import lombok.RequiredArgsConstructor;

/**
 * @author Aiden Izumi (aka Flamesson).
 */
@RequiredArgsConstructor
public class DequeWrapper<T> {
    private final Deque<T> delegate;

    public void addFirst(Collection<? extends T> toAdd) {
        final Deque<T> temp = new LinkedList<>(toAdd);
        while (!temp.isEmpty()) {
            delegate.addFirst(temp.pollLast());
        }
    }
}
