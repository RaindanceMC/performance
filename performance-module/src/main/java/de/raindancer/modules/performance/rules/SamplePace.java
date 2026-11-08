package de.raindancer.modules.performance.rules;

/**
 * When the server thread is looked at. Every look stops it for a moment, so while a tick is quick
 * nothing is sampled but a slow trickle; a tick that runs long is sampled at the full rate — that is what a report explains.
 */
public final class SamplePace implements IPerformanceRule {

    /** A tick this far in is on its way to being slow. */
    public static final long LATE_MS = 25;
    /** The picture of an ordinary minute needs only this many looks per second. */
    public static final long TRICKLE_MS = 500;

    /** @param busyMillis how long the current tick has run; negative between ticks */
    public boolean due(long busyMillis, long sinceLastSampleMillis) {
        return busyMillis >= LATE_MS || sinceLastSampleMillis >= TRICKLE_MS;
    }

    @Override
    public String describe() {
        return "samples a tick only once it runs long, plus a slow trickle otherwise";
    }
}
