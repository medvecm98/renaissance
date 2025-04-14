package org.renaissance.plugins.jmxthreads;

import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;

import org.renaissance.Plugin;

public class Main implements Plugin,
    Plugin.AfterOperationSetUpListener,
    Plugin.BeforeOperationTearDownListener,
    Plugin.MeasurementResultPublisher {

      ThreadMXBean __threadMXBean;

  long __compilationTimeBefore;
  long __compilationTimeAfter;

  int __threadCount;
  long cpuTimeBefore, cpuTimeAfter, userTimeBefore, userTimeAfter;
  long deadlockedCount, waitingAfter, waitingBefore, waitingMax;

  public Main() {
    __threadMXBean = ManagementFactory.getThreadMXBean();
    __threadMXBean.setThreadContentionMonitoringEnabled(true);
    __threadMXBean.setThreadCpuTimeEnabled(true);
    waitingMax = Long.MIN_VALUE;
  }

  private long iterativeMeanCpu(long[] threadIds) {
    long avg = 0;
    int t = 1;
    for (long thread : threadIds) {
      avg += (__threadMXBean.getThreadCpuTime(thread) - avg) / t;
      ++t;
    }
    return avg;
  }

  private long iterativeMeanUser(long[] threadIds) {
    long avg = 0;
    int t = 1;
    for (long thread : threadIds) {
      avg += (__threadMXBean.getThreadUserTime(thread) - avg) / t;
      ++t;
    }
    return avg;
  }

  private long iterativeMeanWaiting(long[] threadIds) {
    long avg = 0;
    int t = 1;
    for (long thread : threadIds) {
      avg += (__threadMXBean.getThreadInfo(thread).getWaitedTime() - avg) / t;
      ++t;
    }
    return avg;
  }

  private void iterativeMaxWaiting(long[] threadIds) {
    long waitTime = 0;

    for (long thread : threadIds) {
      waitTime = __threadMXBean.getThreadInfo(thread).getWaitedTime();
      if (waitTime > waitingMax) {
        waitingMax = waitTime;
      }
    }
  }

  @Override
  public void afterOperationSetUp(String benchmark, int opIndex, boolean isLastOp) {
    // cpu time
    cpuTimeBefore = iterativeMeanCpu(__threadMXBean.getAllThreadIds());
    userTimeBefore = iterativeMeanUser(__threadMXBean.getAllThreadIds());

    // deadlocks
    long[] deadlockedThreads = __threadMXBean.findDeadlockedThreads();
    deadlockedCount = deadlockedThreads != null ? deadlockedThreads.length : 0;

    // waiting average
    waitingBefore = iterativeMeanWaiting(__threadMXBean.getAllThreadIds());
  }

  @Override
  public void beforeOperationTearDown(String benchmark, int opIndex, long harnessDuration) {
    //thread count
    __threadCount = __threadMXBean.getThreadCount();

    // cpu time
    cpuTimeAfter = iterativeMeanCpu(__threadMXBean.getAllThreadIds());
    userTimeAfter = iterativeMeanUser(__threadMXBean.getAllThreadIds());

    // waiting average
    waitingAfter = iterativeMeanWaiting(__threadMXBean.getAllThreadIds());

    // waiting max
    iterativeMaxWaiting(__threadMXBean.getAllThreadIds());
  }

  @Override
  public void onMeasurementResultsRequested(String benchmark, int opIndex, Plugin.MeasurementResultListener dispatcher) {
    dispatcher.onMeasurementResult(benchmark, "jmx_threads_thread_count", __threadCount);
    dispatcher.onMeasurementResult(benchmark, "jmx_threads_thread_deadlcoked", deadlockedCount);

    dispatcher.onMeasurementResult(benchmark, "jmx_threads_thread_kernel_time_ns", (cpuTimeAfter - userTimeAfter) - (cpuTimeBefore - userTimeBefore));
    dispatcher.onMeasurementResult(benchmark, "jmx_threads_thread_kernel_time_ns_total", (cpuTimeAfter - userTimeAfter));

    dispatcher.onMeasurementResult(benchmark, "jmx_threads_thread_user_time_ns", userTimeAfter - userTimeBefore);
    dispatcher.onMeasurementResult(benchmark, "jmx_threads_thread_user_time_ns_total", userTimeAfter);
    
    dispatcher.onMeasurementResult(benchmark, "jmx_threads_thread_waiting_ms", waitingAfter - waitingBefore);
    dispatcher.onMeasurementResult(benchmark, "jmx_threads_thread_waiting_ms_total", waitingAfter);
    dispatcher.onMeasurementResult(benchmark, "jmx_threads_thread_waiting_ms_max_total", waitingMax);
  }
}
