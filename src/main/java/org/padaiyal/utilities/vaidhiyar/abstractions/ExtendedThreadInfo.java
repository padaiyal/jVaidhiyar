package org.padaiyal.utilities.vaidhiyar.abstractions;

import java.lang.management.ThreadInfo;

/**
 * Abstraction to store the ThreadInfo object along with additional information.
 */
public class ExtendedThreadInfo {

  /**
   * ThreadInfo object for a specific thread.
   */
  private final ThreadInfo threadInfo;
  /**
   * CPU usage for a specific thread.
   */
  private final double cpuUsage;
  /**
   * Memory allocated by this specific thread.
   */
  private final long memoryAllocatedInBytes;

  /**
   * Constructor used to abstract thread information.
   *
   * @param threadInfo ThreadInfo object of the thread.
   * @param cpuUsage CPU usage of the thread.
   * @param memoryAllocatedInBytes Memory allocated by the thread in bytes
   */
  public ExtendedThreadInfo(ThreadInfo threadInfo, double cpuUsage, long memoryAllocatedInBytes) {
    this.threadInfo = threadInfo;
    this.cpuUsage = cpuUsage;
    this.memoryAllocatedInBytes = memoryAllocatedInBytes;
  }

  /**
   * Returns the ThreadInfo object for this thread.
   *
   * @return The ThreadInfo object for this thread.
   */
  public ThreadInfo getThreadInfo() {
    return threadInfo;
  }

  /**
   * Returns the CPU usage of this thread.
   *
   * @return The CPU usage of this thread.
   */
  public double getCpuUsage() {
    return cpuUsage;
  }

  /**
   * Returns the memory allocated in bytes by this thread.
   *
   * @return The memory allocated in bytes by this thread.
   */
  public long getMemoryAllocatedInBytes() {
    return memoryAllocatedInBytes;
  }
}