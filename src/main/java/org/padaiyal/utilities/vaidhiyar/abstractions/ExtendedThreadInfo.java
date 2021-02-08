package org.padaiyal.utilities.vaidhiyar.abstractions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.lang.management.ThreadInfo;
import java.util.Arrays;

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
   * @param threadInfo             ThreadInfo object of the thread.
   * @param cpuUsage               CPU usage of the thread.
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

  /**
   * Gets a JSON representation of an instance of this class.
   *
   * @return JSON representation of an instance of this class.
   */
  public JsonObject toJsonObject() {
    JsonObject extendedThreadInfoJsonObject = new JsonObject();
    ThreadInfo threadInfo = getThreadInfo();
    extendedThreadInfoJsonObject.addProperty("name", threadInfo.getThreadName());
    extendedThreadInfoJsonObject.addProperty("id", threadInfo.getThreadId());
    extendedThreadInfoJsonObject.addProperty("state", threadInfo.getThreadState().toString());
    extendedThreadInfoJsonObject.addProperty("priority", threadInfo.getPriority());
    extendedThreadInfoJsonObject.addProperty("blockedCount", threadInfo.getBlockedCount());
    extendedThreadInfoJsonObject
        .addProperty("blockedTimeInMilliSeconds", threadInfo.getBlockedTime());
    extendedThreadInfoJsonObject.addProperty("lockName", threadInfo.getLockName());
    extendedThreadInfoJsonObject.addProperty("lockOwnerId", threadInfo.getLockOwnerId());
    extendedThreadInfoJsonObject.addProperty("lockOwnerName", threadInfo.getLockOwnerName());
    extendedThreadInfoJsonObject.addProperty("waitedCount", threadInfo.getWaitedCount());
    extendedThreadInfoJsonObject
        .addProperty("waitedTimeInMilliSeconds", threadInfo.getWaitedTime());
    extendedThreadInfoJsonObject.addProperty("isDaemon", threadInfo.isDaemon());
    extendedThreadInfoJsonObject.addProperty("isInNative", threadInfo.isInNative());
    extendedThreadInfoJsonObject.addProperty("isSuspended", threadInfo.isSuspended());
    JsonArray stackTrace = new JsonArray();
    Arrays.stream(threadInfo.getStackTrace())
        .map(StackTraceElement::toString)
        .forEach(stackTrace::add);
    extendedThreadInfoJsonObject.add("stackTrace", stackTrace);

    return extendedThreadInfoJsonObject;
  }
}