package de.raindancer.modules.performance.rules;

/** How often the chunks may be counted: a count costs the server time, so never back to back — least of all while it lags. */
public final class CensusPace implements IPerformanceRule {

    public static final long NEVER = Long.MIN_VALUE;

    private final long gapMillis;

    public CensusPace(long gapMillis) {
        this.gapMillis = gapMillis;
    }

    public boolean mayStart(long nowMillis, long lastStartMillis) {
        return lastStartMillis == NEVER || nowMillis - lastStartMillis >= gapMillis;
    }

    @Override
    public String describe() {
        return "counts the chunks at most once every " + gapMillis / 1000 + " s";
    }
}
