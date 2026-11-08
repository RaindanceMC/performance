package de.raindancer.modules.performance.model;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

/** A list of work done a little per call, within a time budget — so a big census is spread over ticks instead of one long one. */
public final class Sliced<T> {

    private final Deque<T> todo;
    private final Consumer<T> work;
    private final LongSupplier nanos;

    public Sliced(Collection<T> items, Consumer<T> work, LongSupplier nanos) {
        this.todo = new ArrayDeque<>(items);
        this.work = work;
        this.nanos = nanos;
    }

    /** Works until the budget is spent — at least one item, so it always advances. True when nothing is left. */
    public boolean step(long budgetNanos) {
        long until = nanos.getAsLong() + budgetNanos;
        do {
            T next = todo.poll();
            if (next == null) {
                return true;
            }
            work.accept(next);
        } while (nanos.getAsLong() < until);
        return todo.isEmpty();
    }

    public int left() {
        return todo.size();
    }
}
