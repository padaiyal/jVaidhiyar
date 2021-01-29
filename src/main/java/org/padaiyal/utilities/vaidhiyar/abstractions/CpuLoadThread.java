package org.padaiyal.utilities.vaidhiyar.abstractions;

import com.sun.management.ThreadMXBean;
import java.lang.management.ManagementFactory;

/**
 * Thread that actually generates the given load.
 */
public class CpuLoadThread extends Thread {

  /**
   * ThreadMxBean object used to get thread CPU times.
   */
  private static final ThreadMXBean threadMxBean
      = (ThreadMXBean) ManagementFactory.getThreadMXBean();

  /**
   * CPU load to generate.
   */
  private final double load;
  /**
   * Duration to generate CPU load.
   */
  private final long durationInMilliSeconds;

  /**
   * Constructor which creates the thread.
   *
   * @param name                   Name of the thread.
   * @param priority               Priority of the thread. A value between 1 (lowest priority)
   *                               and 10 (highest priority) inclusive.
   * @param load                   Load % that this thread should generate. A value between 0 and 1
   *                               inclusive.
   * @param durationInMilliSeconds Duration for which this thread should generate the CPU load.
   */
  public CpuLoadThread(String name, int priority, double load, long durationInMilliSeconds) {
    this.load = load;
    this.durationInMilliSeconds = durationInMilliSeconds;
    this.setPriority(priority);
    this.setName(name);
  }

  /**
   * Sleeps the current thread for the specified duration.
   *
   * @param sleepDurationInMilliSeconds Duration to sleep the current thread.
   * @throws InterruptedException If the thread sleep is interrupted.
   */
  public static void sleepCurrentThread(long sleepDurationInMilliSeconds)
      throws InterruptedException {
    Thread.sleep(sleepDurationInMilliSeconds);
  }

  /**
   * Generates the load when run.
   * NOTE: In some systems where the priority of this thread or the JVM process is low,
   *       the CPU load will be lower than desired.
   */
  @Override
  public void run() {
    long startTimeInMilliSeconds = threadMxBean.getCurrentThreadCpuTime() / 1_000_000;
    long previousSleepTimeInMilliSeconds = startTimeInMilliSeconds;
    long timeElapsedSincePreviousSleepInMilliSeconds;

    try {
      synchronized (this) {

        // Loop for the given duration
        while (threadMxBean.getCurrentThreadCpuTime() / 1_000_000 - startTimeInMilliSeconds
            < durationInMilliSeconds) {
          // Every 100ms, sleep for the percentage of unladen time
          if (
              (timeElapsedSincePreviousSleepInMilliSeconds
                  = (threadMxBean.getCurrentThreadCpuTime() / 1_000_000
                  - previousSleepTimeInMilliSeconds)) >= load * 100
          ) {
            sleepCurrentThread(
                (long) Math.floor((1 - load) * timeElapsedSincePreviousSleepInMilliSeconds
                    / (load))
            );
            previousSleepTimeInMilliSeconds = threadMxBean.getCurrentThreadCpuTime() / 1_000_000;
          }
        }
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
