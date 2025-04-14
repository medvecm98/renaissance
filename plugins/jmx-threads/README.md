# jmx-timers plugin for Renaissance suite

This plugin collects information about JIT compilation times via
[ThreadMXBean](https://docs.oracle.com/en/java/javase/21/docs/api/java.management/java/lang/management/ThreadMXBean.html).

## Building

To build the plugin run the following command:

```shell
../../tools/sbt/bin/sbt assembly
```

The plugin shall be available as `target/plugin-jmxthreads-assembly-VER.jar`.

## Using the plugin

To use the plugin, simply add it with the `--plugin` option when
starting the suite.
Note that we specify an output file as the counters are not visible on the
standard output.

```shell
java renaissance-gpl-0.17.1.4m.jar \
  --plugin plugin-jmxthreads-assembly-0.0.1.jar\
  --json results.json \
  ...
```

The results in the JSON file will have the following form.
Note that the `total` might be increasing even when the per-loop
time stays at `0` because some compilation may happen between loops
(i.e., outside of the measured operation).

```json
{
  ...
  "data": {
    "BENCHMARK": {
      "results": [
        {
          "jmx_threads_thread_waiting_ms_total": 817,
          "jmx_threads_thread_count": 19,
          "jmx_threads_thread_waiting_ms": 737,
          "duration_ns": 2405860180,
          "jmx_threads_thread_kernel_time_ns_total": 11004318,
          "jmx_threads_thread_kernel_time_ns": 10783609,
          "jmx_threads_thread_user_time_ns": 603867141,
          "uptime_ns": 849734068,
          "jmx_threads_thread_user_time_ns_total": 735618561,
          "jmx_threads_thread_deadlcoked": 0
        },
        ...
  ...
}
```
