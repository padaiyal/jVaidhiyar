package org.padaiyal.utilities.vaidhiyar.abstractions;

import com.sun.management.OperatingSystemMXBean;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.padaiyal.utilities.I18nUtility;
import org.padaiyal.utilities.PropertyUtility;

/**
 * Generates Load on the CPU by keeping it busy for the given load percentage.
 */
public final class CpuLoadGenerator extends Thread {

  /**
   * Logger object used to log information and errors.
   */
  private static final Logger logger = LogManager.getLogger(CpuLoadGenerator.class);
  /**
   * Operating system bean.
   */
  private static final OperatingSystemMXBean osMxBean
      = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
  /**
   * Flag to check if property file has been added.
   */
  private static boolean propertyFilesAdded = false;

  /**
   * CPU load to generate.
   */
  private double load;
  /**
   * Duration to generate CPU load.
   */
  private long durationInMilliSeconds;

  /**
   * Default constructor used to initialize dependant values.
   *
   * @throws IOException If there is an issue adding the property file.
   */
  public CpuLoadGenerator() throws IOException {
    if (!propertyFilesAdded) {
      PropertyUtility.addPropertyFile(
          CpuLoadGenerator.class,
          CpuLoadGenerator.class.getSimpleName() + ".properties"
      );
      I18nUtility.addResourceBundle(
          CpuLoadGenerator.class,
          CpuLoadGenerator.class.getSimpleName(),
          Locale.US
      );
      propertyFilesAdded = true;
    }
  }

  /**
   * Start the CPU load generation.
   *
   * @param load CPU load to generate. A valid value is between 0 and 1 (inclusive).
   * @param durationInMilliSeconds Duration to generate CPU load.
   */
  public synchronized void start(double load, long durationInMilliSeconds) {
    if (load < 0.0 || load > 1.0) {
      throw new IllegalArgumentException(
          I18nUtility.getFormattedString(
              "CpuLoadGenerator.error.invalidLoadValue",
              load
          )
      );
    }
    if (durationInMilliSeconds < 0) {
      throw new IllegalArgumentException(
          I18nUtility.getFormattedString(
              "CpuLoadGenerator.error.invalidDuration",
              durationInMilliSeconds,
              "ms"
          )
      );
    }
    this.load = load;
    this.durationInMilliSeconds = durationInMilliSeconds;
    start();
  }

  /**
   * Triggers CPU load generation threads.
   */
  @Override
  public synchronized void run() {

    int logicalCoresCount = osMxBean.getAvailableProcessors();

    // Initialize all the threads (One per logical core)
    List<CpuLoadThread> cpuLoadThreads = IntStream.range(0, logicalCoresCount)
        .mapToObj(logicalCore
            -> new CpuLoadThread(
                "CPU load thread",
                PropertyUtility.getTypedProperty(
                    Integer.class,
                    "CpuLoadGenerator.thread.priority"
                ),
                load,
                durationInMilliSeconds
            )
        )
        .collect(Collectors.toList());

    // Invoke all the initialized threads
    for (CpuLoadThread cpuLoadThread : cpuLoadThreads) {
      logger.info(
          I18nUtility.getString("CpuLoadGenerator.info.startingCpuLoad"),
          cpuLoadThread.getId()
      );
      cpuLoadThread.start();
    }

    logger.info(I18nUtility.getString("CpuLoadGenerator.info.waitingForCpuLoad"));

    long threadsNotAlive = 0;
    while (threadsNotAlive < logicalCoresCount) {
      threadsNotAlive = cpuLoadThreads.stream()
          .map(CpuLoadThread::isAlive)
          .filter(isAlive -> !isAlive)
          .count();
    }
  }

}
