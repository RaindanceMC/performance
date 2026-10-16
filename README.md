# RaindanceMC/performance

- **`performance-module`** — Rain's Flexible Modules — Performance Optimizer: Watches the tick, finds what makes it slow — a farm, a pile of items, a plugin — proposes a fix staff can apply with a click, and reports it. Also keeps animal farms from growing past a limit.
- **`performance-standalone`** — Rain's Performance Optimizer: Rain's Performance Optimizer, as a plugin of its own: the performance module and the standard wrapper, shaded together.

Part of [RaindanceMC](https://github.com/RaindanceMC); built on [RainsCore](https://github.com/RaindanceMC/RainsCore) and the shared parent in [framework](https://github.com/RaindanceMC/framework). Split out of `rains-flexible-modules` on 16.10.2026, history kept.

## Build

```
mvn install
```

Needs Java 25 and read access to `packages.tstieh.de` (server ids `packages-releases` / `packages-snapshots` in `~/.m2/settings.xml`).

## Plugins

- `performance-standalone/target/RainsPerformanceOptimizer-0.2.0.jar` — also published as `de.raindancer.modules:performance-standalone:0.2.0`

## Versions

A module's version is a property in this repository's root `pom.xml`, a plugin's is `plugin.version` in its own pom. CI publishes every version that is not in the registry yet on the next push to master.
