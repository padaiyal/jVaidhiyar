package org.padaiyal.utilities.vaidhiyar.abstractions;

import java.time.Duration;

/**
 * Stores information on garbage collection.
 */
public class GarbageCollectionInfo {

  /**
   * Name of the GarbageCollectorMXBean.
   */
  private final String name;
  /**
   * Total number of collections occurred so far.
   */
  private final long collectionCount;
  /**
   * Time taken to perform garbage collection in milliseconds.
   */
  private final long collectionTimeInMilliSeconds;

  /**
   * Abstracts the information on the garbage collection performed so far.
   *
   * @param name                         Name of the memory pool associated with this information.
   * @param collectionCount              Total number of collections occurred so far.
   * @param collectionTimeInMilliSeconds Time taken to perform garbage collection in milliseconds.
   */
  public GarbageCollectionInfo(
      String name,
      long collectionCount,
      long collectionTimeInMilliSeconds
  ) {
    this.name = name;
    this.collectionCount = collectionCount;
    this.collectionTimeInMilliSeconds = collectionTimeInMilliSeconds;
  }

  /**
   * The name of the garbage collector.
   *
   * @return Name of the garbage collector.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the total number of collections occurred so far.
   *
   * @return Total number of collections occurred so far.
   */
  public long getCollectionCount() {
    return collectionCount;
  }

  /**
   * Gets the time taken to perform garbage collection.
   *
   * @return Time taken to perform garbage collection.
   */
  public Duration getCollectionTime() {
    return Duration.ofMillis(collectionTimeInMilliSeconds);
  }
}
