package org.padaiyal.utilities.vaidhiyar.abstractions;

import java.lang.management.MemoryUsage;

/**
 * An extension on the MemoryUsage object.
 * https://docs.oracle.com/en/java/javase/14/docs/api/java.management/java/lang/management/
 * GarbageCollectorMXBean.html
 */
public class ExtendedMemoryUsage extends MemoryUsage {

  /**
   * Memory usage percentage.
   */
  private final double memoryUsagePercentage;

  /**
   * An extension on the MemoryUsage object.
   *
   * @param init      Represents the initial amount of memory (in bytes) that the Java virtual
   *                  machine requests from the operating system for memory management during
   *                  startup. The Java virtual machine may request additional memory from the
   *                  operating system and may also release memory to the system over time. The
   *                  value of init may be undefined.
   * @param used      Represents the amount of memory currently used (in bytes).
   * @param committed Represents the amount of memory (in bytes) that is guaranteed to be available
   *                  for use by the Java virtual machine. The amount of committed memory may change
   *                  over time (increase or decrease). The Java virtual machine may release memory
   *                  to the system and committed could be less than init. committed will always be
   *                  greater than or equal to used.
   * @param max       Represents the maximum amount of memory (in bytes) that can be used for memory
   *                  management. Its value may be undefined. The maximum amount of memory may
   *                  change over time if defined. The amount of used and committed memory will
   *                  always be less than or equal to max if max is defined. A memory allocation
   *                  may fail if it attempts to increase the used memory such that used > committed
   *                  even if used <= max would still be true (for example, when the system is low
   *                  on virtual memory).
   *      Below is a picture showing an example of a memory pool:
   *         +----------------------------------------------+
   *         +////////////////           |                  +
   *         +////////////////           |                  +
   *         +----------------------------------------------+
   *         |--------|
   *            init
   *         |---------------|
   *                used
   *         |---------------------------|
   *                   committed
   *         |----------------------------------------------|
   *                              max
   */
  public ExtendedMemoryUsage(long init, long used, long committed, long max) {
    super(init, used, committed, max);
    memoryUsagePercentage = (max > 0) ? (double) used / max : -1;
  }

  /**
   * An extension on the MemoryUsage object.
   *
   * @param memoryUsage MemoryUsage object to retrieve values from.
   */
  public ExtendedMemoryUsage(MemoryUsage memoryUsage) {
    this(
        memoryUsage.getInit(),
        memoryUsage.getUsed(),
        memoryUsage.getCommitted(),
        memoryUsage.getMax()
    );
  }

  /**
   * Gets the memory usage percentage which is computed using the used memory and max memory.
   *
   * @return The memory usage percentage.
   */
  public double getMemoryUsagePercentage() {
    return memoryUsagePercentage;
  }
}
