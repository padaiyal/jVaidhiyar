package org.padaiyal.utilities.vaidhiyar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.ThreadMXBean;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
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
import org.padaiyal.utilities.vaidhiyar.abstractions.ExtendedMemoryUsage;
import org.padaiyal.utilities.vaidhiyar.abstractions.ExtendedThreadInfo;
import org.padaiyal.utilities.vaidhiyar.abstractions.GarbageCollectionInfo;

/**
 * Utility for retrieving JVM specific information.
 */
public final class JvmUtility {

  /**
   * Logger object used to log information and errors.
   */
  private static final Logger logger = LogManager.getLogger(JvmUtility.class);
  /**
   * GarbageCollectorMXBean objects used to get garbage collection information.
   */
  private static final List<GarbageCollectorMXBean> garbageCollectorMxBeans
      = ManagementFactory.getGarbageCollectorMXBeans();
  /**
   * MemoryMXBean object used to get heap memory information.
   */
  private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
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
   * HotSpotDiagnosticMXBean object used to retrieve heap dump and VM options.
   */
  private static HotSpotDiagnosticMXBean hotSpotDiagnosticMXBean;
  /**
   * Switch to enable/disable thread CPU usage collection.
   */
  private static boolean runThreadCpuUsageCollectorSwitch;
  /**
   * Sampling interval to use to measure the thread CPU usages.
   */
  private static long cpuSamplingIntervalInMilliSeconds;
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

      if (hotSpotDiagnosticMXBean == null) {
        hotSpotDiagnosticMXBean = ManagementFactory.newPlatformMXBeanProxy(
            ManagementFactory.getPlatformMBeanServer(),
            "com.sun.management:type=HotSpotDiagnostic",
            HotSpotDiagnosticMXBean.class
        );
      }
    } catch (IOException e) {
      logger.error(e);
    }
  };
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
   * Get the heap memory usage information.
   *
   * @return The ExtendedMemoryUsage object representing the heap memory usage.
   */
  public static ExtendedMemoryUsage getHeapMemoryUsage() {
    return new ExtendedMemoryUsage(memoryMXBean.getHeapMemoryUsage());
  }

  /**
   * Get the non heap memory usage information.
   *
   * @return The ExtendedMemoryUsage object representing the non heap memory usage.
   */
  public static ExtendedMemoryUsage getNonHeapMemoryUsage() {
    return new ExtendedMemoryUsage(memoryMXBean.getNonHeapMemoryUsage());
  }

  /**
   * Runs the garbage collector.
   *
   * @return The duration taken for garbage collection.
   */
  public static Duration runGarbageCollector() {
    memoryMXBean.setVerbose(
        PropertyUtility.getTypedProperty(
            Boolean.class,
            "JvmUtility.memoryMxBean.verbose.switch"
        )
    );
    Duration garbageCollectionDuration;
    logger.info(
        I18nUtility.getString("JvmUtility.garbageCollection.starting")
    );
    Instant gcStartInstant = Instant.now();
    memoryMXBean.gc();
    garbageCollectionDuration = Duration.between(gcStartInstant, Instant.now());
    logger.info(
        I18nUtility.getString("JvmUtility.garbageCollection.completed"),
        garbageCollectionDuration.toMillis(),
        "ms"
    );
    return garbageCollectionDuration;
  }

  /**
   * Gets the following garbage collection info:
   *  - Collection count.
   *  - Collection time.
   *  - Memory pools in which garbage has been collected.
   *
   * @return The garbage collection info.
   */
  public static GarbageCollectionInfo[] getGarbageCollectionInfo() {
    return garbageCollectorMxBeans.stream()
        .map(garbageCollectorMXBean -> new GarbageCollectionInfo(
            garbageCollectorMXBean.getName(),
            garbageCollectorMXBean.getCollectionCount(),
            garbageCollectorMXBean.getCollectionTime()
        ))
        .toArray(GarbageCollectionInfo[]::new);
  }

  /**
   * Dump the heap memory contents onto a file.
   *
   * @param destinationDirectory  Destination directory
   * @param heapDumpFileName      Name of the heap dump file generated.
   * @param dumpOnlyLiveObjects   If true dump only live objects i.e. objects that are reachable
   *                              from others
   * @return                      Time taken to generate heap dump.
   * @throws IOException          When there is an issue generating a heap dump.
   */
  public static Duration generateHeapDump(
      Path destinationDirectory,
      String heapDumpFileName,
      boolean dumpOnlyLiveObjects
  ) throws IOException {
    // Input validation.
    Objects.requireNonNull(destinationDirectory);
    if (!Files.exists(destinationDirectory)) {
      throw new IllegalArgumentException(
          I18nUtility.getFormattedString(
              "JvmUtility.generateHeapDump.destinationDirectoryDoesNotExist",
              destinationDirectory
          )
      );
    }
    if (!Files.isDirectory(destinationDirectory)) {
      throw new IllegalArgumentException(
          I18nUtility.getFormattedString(
              "JvmUtility.generateHeapDump.invalidDestinationDirectory",
              destinationDirectory
          )
      );
    }
    Objects.requireNonNull(heapDumpFileName);

    String heapDumpFileExtension = ".hprof";
    String heapDumpFileNameWithExtension = heapDumpFileName;
    // Add file extension if missing.
    if (!heapDumpFileName.endsWith(heapDumpFileExtension)) {
      heapDumpFileNameWithExtension += heapDumpFileExtension;
    }

    Path destinationHeapDumpFilePath = destinationDirectory.resolve(heapDumpFileNameWithExtension);

    logger.info(
        I18nUtility.getString("JvmUtility.generateHeapDump.generating")
    );
    Instant heapDumpGenerationStartInstant = Instant.now();
    dependantValuesInitializer.run();
    hotSpotDiagnosticMXBean.dumpHeap(
        destinationHeapDumpFilePath.toAbsolutePath()
            .toString(),
        dumpOnlyLiveObjects
    );
    Duration heapDumpGenerationDuration = Duration.between(
        heapDumpGenerationStartInstant,
        Instant.now()
    );
    logger.info(
        I18nUtility.getString("JvmUtility.generateHeapDump.generated"),
        destinationHeapDumpFilePath.toAbsolutePath().toString()
    );

    return heapDumpGenerationDuration;
  }

  /**
   * Gets a list of available VM options.
   *
   * @return A list of available VM options.
   */
  public static JsonArray getAllVmOptions() {
    dependantValuesInitializer.run();
    JsonArray vmOptions = new JsonArray();
    hotSpotDiagnosticMXBean.getDiagnosticOptions()
        .forEach(vmOption -> {
          JsonObject vmOptionJsonObject = new JsonObject();
          vmOptionJsonObject.addProperty("name", vmOption.getName());
          vmOptionJsonObject.addProperty("value", vmOption.getValue());
          vmOptionJsonObject.addProperty("origin", vmOption.getOrigin().toString());
          vmOptionJsonObject.addProperty("isWriteable", vmOption.isWriteable());
          vmOptions.add(vmOptionJsonObject);
        });
    return vmOptions;
  }

  /**
   * Gets the thread CPU usage collector future.
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
   * Gets the switch used to toggle the state of the thread CPU usage collector.
   *
   * @return State of the thread CPU usage collector. If true, it means that the thread CPU usage
   *         collector will keep running, else it means that the thread CPU usage collector has
   *         terminated or will terminate soon.
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
   * @param threadStackDepth  Depth of thread stack to retrieve.
   * @return                  Extended thread info for all JVM threads.
   */
  public static ExtendedThreadInfo[] getAllExtendedThreadInfo(int threadStackDepth) {
    return Arrays.stream(
        threadMxBean.getThreadInfo(
            threadMxBean.getAllThreadIds(),
            threadStackDepth
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
   * @param currentThreadCpuTimesInNanoSeconds  Thread CPU times at the end of the sampling duration
   *                                            in nano seconds.
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

  /**
   * Dump the JVM information onto a JSON file in the specified directory.
   *
   * @param destinationDirectory  Directory in which to create the JSON file.
   * @param fileName              Name of the JSON file to create.
   * @param threadStackDepth      Depth of the thread stack to obtain.
   * @throws IOException          If there is an issue writing the JSON file.
   */
  public static void dumpJvmInformationToFile(
      Path destinationDirectory,
      String fileName,
      int threadStackDepth
  ) throws IOException {
    // Input validation
    Objects.requireNonNull(destinationDirectory);
    if (!Files.exists(destinationDirectory)) {
      throw new IllegalArgumentException(
          I18nUtility.getFormattedString(
              "JvmUtility.error.destinationPathDoesNotExist",
              destinationDirectory
          )
      );
    } else if (!Files.isDirectory(destinationDirectory)) {
      throw new IllegalArgumentException(
          I18nUtility.getFormattedString(
              "JvmUtility.error.destinationPathNotADirectory",
              destinationDirectory
          )
      );
    }
    Objects.requireNonNull(fileName);

    @SuppressWarnings("SpellCheckingInspection")
    Gson gsonObject = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    JsonObject jvmInformation = new JsonObject();
    jvmInformation.addProperty(
        "timestamp",
        Instant.now().toString()
    );
    jvmInformation.add(
        "heapUsage",
        getHeapMemoryUsage().toJsonObject()
    );
    jvmInformation.add(
        "nonHeapUsage",
        getNonHeapMemoryUsage().toJsonObject()
    );
    jvmInformation.add(
        "vmOptions",
        getAllVmOptions()
    );
    jvmInformation.add(
        "garbageCollectionInfos",
        gsonObject.toJsonTree(getGarbageCollectionInfo())
    );
    JsonArray extendedThreadInfos = new JsonArray();
    Arrays.stream(getAllExtendedThreadInfo(threadStackDepth))
        .map(ExtendedThreadInfo::toJsonObject)
        .forEach(extendedThreadInfos::add);
    jvmInformation.add("threadInfos", extendedThreadInfos);

    try (
        BufferedWriter bufferedWriter = Files.newBufferedWriter(
            destinationDirectory.resolve(fileName + ".json")
        )
    ) {
      gsonObject.toJson(
          jvmInformation,
          bufferedWriter
      );
    }
  }
}
