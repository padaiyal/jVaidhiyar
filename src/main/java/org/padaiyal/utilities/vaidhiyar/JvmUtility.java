package org.padaiyal.utilities.vaidhiyar;

import com.sun.management.ThreadMXBean;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.padaiyal.utilities.I18nUtility;
import org.padaiyal.utilities.PropertyUtility;
import org.padaiyal.utilities.vaidhiyar.abstractions.ExtendedThreadInfo;

/**
 * Utility for retrieving JVM specific information.
 */
public final class JvmUtility {

  /**
   * Logger object used to log information and errors.
   */
  private static final Logger logger = LogManager.getLogger(JvmUtility.class);
  /**
   * ThreadMXBean object used to get thread CPU times.
   */
  private static final ThreadMXBean threadMxBean
      = (ThreadMXBean) ManagementFactory.getThreadMXBean();
  /**
   * RuntimeMxBean object used to get CPU core count.
   */
  private static final RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();

  /**
   * Map to store the thread CPU usages.
   */
  private static final Map<Long, Double> threadCpuUsages
      = new ConcurrentHashMap<>();
  /**
   * ExecutorService object used to manage the thread CPU usage collector.
   */
  private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
  /**
   * Callable implementation to execute when the thread CPU usage is to be collected.
   */
  static final Callable<Void> cpuUsageCollectorThread = () -> {
    try {
      JvmUtility.runCpuUsageCollector();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return null;
  };

  /**
   * Initialize dependant values.
   */
  static Runnable dependantValuesInitializer = () -> {
    I18nUtility.addResourceBundle(
        JvmUtility.class,
        JvmUtility.class.getSimpleName(),
        Locale.US
    );

    try {
      PropertyUtility.addPropertyFile(
          JvmUtility.class,
          JvmUtility.class.getSimpleName() + ".properties"
      );

      runThreadCpuUsageCollectorSwitch = PropertyUtility.getTypedProperty(
          Boolean.class,
          "JvmUtility.threadCpuUsageCollector.switch"
      );
      cpuSamplingIntervalInMilliSeconds = PropertyUtility.getTypedProperty(
          Long.class,
          "JvmUtility.cpuSamplingInterval.milliseconds"
      );
    } catch (IOException e) {
      logger.error(e);
    }


  };

  /**
   * Switch to enable/disable thread CPU usage collection.
   */
  private static boolean runThreadCpuUsageCollectorSwitch;
  /**
   * Sampling interval to use to measure the thread CPU usages.
   */
  private static long cpuSamplingIntervalInMilliSeconds;
  /**
   * Stores the Future object returned when the thread CPU usage collector is triggered.
   */
  private static Future<Void> cpuUsageCollectorFuture;

  static {
    dependantValuesInitializer.run();

    cpuUsageCollectorFuture = executorService.submit(cpuUsageCollectorThread);
  }

  /**
   * Private constructor to prevent instantiation.
   */
  private JvmUtility() {

  }

  /**
   * Returns the thread CPU usage collector future.
   *
   * @return The thread CPU usage collector future.
   */
  static Future<Void> getCpuUsageCollectorFuture() {
    return cpuUsageCollectorFuture;
  }

  /**
   * Checks if the thread CPU usage collector is running.
   *
   * @return true if the thread CPU usage collector is running. Else false.
   */
  public static boolean isThreadCpuUsageCollectorRunning() {
    return !cpuUsageCollectorFuture.isDone();
  }

  /**
   * Returns the switch used to toggle the state of the thread CPU usage collector.
   *
   * @return State of the thread CPU usage collector.
   *         If true, it means that the thread CPU usage collector will keep running,
   *         else it means that the thread CPU usage collector has terminated or will terminate
   *         soon.
   */
  public static boolean getRunThreadCpuUsageCollectorSwitch() {
    return runThreadCpuUsageCollectorSwitch;
  }

  /**
   * Sets the switch used to toggle the state of the thread CPU usage collector.
   *
   * @param runThreadCpuUsageCollectorFlag If true, the thread CPU usage collector is started again,
   *                                       else it will be terminated.
   */
  public static void setRunThreadCpuUsageCollectorSwitch(boolean runThreadCpuUsageCollectorFlag) {
    runThreadCpuUsageCollectorSwitch = runThreadCpuUsageCollectorFlag;
    // If the thread is enabled, try starting it.
    if (!isThreadCpuUsageCollectorRunning() && JvmUtility.runThreadCpuUsageCollectorSwitch) {
      cpuUsageCollectorFuture = executorService.submit(cpuUsageCollectorThread);
    }
  }

  /**
   * Gets the thread IDs of all running JVM threads.
   *
   * @return The thread IDs of all running JVM threads.
   */
  public static long[] getAllThreadsId() {
    return threadMxBean.getAllThreadIds();
  }

  /**
   * Gets the extended thread info for all JVM threads.
   *
   * @return Extended thread info for all JVM threads.
   */
  public static ExtendedThreadInfo[] getAllExtendedThreadInfo() {
    return Arrays.stream(
        threadMxBean.getThreadInfo(
            threadMxBean.getAllThreadIds(),
            PropertyUtility.getTypedProperty(
                Integer.class,
                "JvmUtility.thread.stackDepth"
            )
        )
    ).map(threadInfo -> new ExtendedThreadInfo(
            threadInfo,
            getCpuUsage(threadInfo.getThreadId()),
            getAllocatedMemoryInBytes(threadInfo.getThreadId())
        )
    ).toArray(ExtendedThreadInfo[]::new);
  }

  /**
   * Gets the amount of memory allocated by a specific thread.
   *
   * @param threadId  Thread ID for thread whose allocated memory is to be retrieved.
   * @return          Memory allocated for specified thread.
   */
  public static long getAllocatedMemoryInBytes(long threadId) {
    // Input validation.
    if (threadId <= 0) {
      throw new IllegalArgumentException(
          I18nUtility.getFormattedString(
              "JvmUtility.error.invalidThreadId",
              threadId
          )
      );
    }

    return threadMxBean.getThreadAllocatedBytes(threadId);
  }

  /**
   * Gets the CPU usage for a specific thread.
   *
   * @param threadId  Thread ID for thread whose CPU usage is to be retrieved.
   * @return          CPU usage of requested thread.
   */
  public static double getCpuUsage(long threadId) {
    // Input validation.
    if (threadId <= 0) {
      throw new IllegalArgumentException(
          I18nUtility.getFormattedString(
              "JvmUtility.error.invalidThreadId",
              threadId
          )
      );
    }

    double threadCpuUsage = threadCpuUsages.getOrDefault(threadId, -1.0);
    if (threadCpuUsage == -1) {
      logger.warn(
          I18nUtility.getString("JvmUtility.error.unableToGetThreadCpuUsage"),
          threadId
      );
    }
    return threadCpuUsage;
  }

  /**
   * Computes the thread CPU usages given the necessary inputs.
   *
   * @param initialThreadCpuTimesInNanoSeconds  Initial thread CPU times in nano seconds.
   * @param currentThreadCpuTimesInNanoSeconds  Thread CPU times at the end of the sampling
   *                                            duration in nano seconds.
   * @param samplingDurationInMilliSeconds      Sampling duration in milli seconds.
   * @return                                    A map, mapping the thread ID to it's CPU usage.
   */
  static Map<Long, Double> getThreadCpuUsages(
      Map<Long, Long> initialThreadCpuTimesInNanoSeconds,
      Map<Long, Long> currentThreadCpuTimesInNanoSeconds,
      Long samplingDurationInMilliSeconds
  ) {
    // Input validation.
    Objects.requireNonNull(initialThreadCpuTimesInNanoSeconds);
    Objects.requireNonNull(currentThreadCpuTimesInNanoSeconds);
    Objects.requireNonNull(samplingDurationInMilliSeconds);

    Map<Long, Double> threadCpuUsages = new ConcurrentHashMap<>();
    for (long threadId : currentThreadCpuTimesInNanoSeconds.keySet()) {
      if (initialThreadCpuTimesInNanoSeconds.containsKey(threadId)) {
        Long initialThreadCpuTimeInNanoSeconds = initialThreadCpuTimesInNanoSeconds.get(threadId);
        Long currentThreadCpuTimeInNanoSeconds = currentThreadCpuTimesInNanoSeconds.get(threadId);

        // Input validation.
        Objects.requireNonNull(initialThreadCpuTimeInNanoSeconds);
        Objects.requireNonNull(currentThreadCpuTimeInNanoSeconds);
        String illegalArgumentExceptionMessage = null;
        if (currentThreadCpuTimeInNanoSeconds < 0) {
          illegalArgumentExceptionMessage = I18nUtility.getFormattedString(
              "JvmUtility.error.negativeThreadCpuTime",
              "currentThreadCpuTimeInNanoSeconds",
              currentThreadCpuTimeInNanoSeconds,
              "ns",
              threadId
          );
        } else if (initialThreadCpuTimeInNanoSeconds < 0) {
          illegalArgumentExceptionMessage = I18nUtility.getFormattedString(
              "JvmUtility.error.negativeThreadCpuTime",
              "initialThreadCpuTimeInNanoSeconds",
              initialThreadCpuTimeInNanoSeconds,
              "ns",
              threadId
          );
        } else if (currentThreadCpuTimeInNanoSeconds < initialThreadCpuTimeInNanoSeconds) {
          illegalArgumentExceptionMessage = I18nUtility.getFormattedString(
              "JvmUtility.error.currentThreadCpuTimeLesserThanInitialThreadCpuTime",
              currentThreadCpuTimeInNanoSeconds,
              "ns",
              initialThreadCpuTimeInNanoSeconds,
              "ns"
          );
        }
        if (illegalArgumentExceptionMessage != null) {
          throw new IllegalArgumentException(illegalArgumentExceptionMessage);
        }

        double cpuThreadTimeInMilliSeconds = (
                currentThreadCpuTimesInNanoSeconds.get(threadId)
                    - initialThreadCpuTimesInNanoSeconds.get(threadId)
        ) / (1000.0 * 1000.0);
        double threadCpuUsage
            = cpuThreadTimeInMilliSeconds * 100.0 / samplingDurationInMilliSeconds;
        threadCpuUsages.put(threadId, threadCpuUsage);
      }
    }
    return threadCpuUsages;
  }

  /**
   * Runs the steps to collect CPU usage for all threads in the JVM.
   *
   * @throws InterruptedException If the thread sleep is interrupted.
   */
  static void runCpuUsageCollector() throws InterruptedException {
    logger.info(
        I18nUtility.getString("JvmUtility.threadCpuUsageCollector.started")
    );
    synchronized (Thread.currentThread()) {
      while (runThreadCpuUsageCollectorSwitch) {
        long[] threadIds = getAllThreadsId();
        long initialUpTimeInMilliSeconds;
        long finalUpTimeInMilliSeconds;

        initialUpTimeInMilliSeconds = runtimeMxBean.getUptime();
        final Map<Long, Long> initialThreadCpuTimes = Arrays.stream(threadIds)
            .boxed()
            .collect(
                Collectors.toMap(
                    threadId -> threadId,
                    threadMxBean::getThreadCpuTime
                )
            );

        //noinspection BusyWait
        Thread.sleep(cpuSamplingIntervalInMilliSeconds);

        final Map<Long, Long> currentThreadCpuTimes = Arrays.stream(threadIds)
            .boxed()
            .collect(
                Collectors.toMap(
                    threadId -> threadId,
                    threadMxBean::getThreadCpuTime
                )
            );

        finalUpTimeInMilliSeconds = runtimeMxBean.getUptime();

        threadCpuUsages.clear();
        threadCpuUsages.putAll(getThreadCpuUsages(initialThreadCpuTimes, currentThreadCpuTimes,
            finalUpTimeInMilliSeconds - initialUpTimeInMilliSeconds));
      }
    }
    logger.info(
        I18nUtility.getString("JvmUtility.threadCpuUsageCollector.stopping")
    );
  }

}
