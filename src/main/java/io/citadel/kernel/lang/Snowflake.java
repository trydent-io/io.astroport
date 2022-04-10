package io.citadel.kernel.lang;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * Distributed Sequence Generator.
 * Inspired by Twitter snowflake: https://github.com/twitter/snowflake/tree/snowflake-2010
 * <p>
 * This class should be used as a Singleton.
 * Make sure that you create and reuse a Single instance of Snowflake per node in your distributed system cluster.
 */
public enum Snowflake {
  Default;

  private static final int UNUSED_BITS = 1; // Sign bit, Unused (always set to 0)
  private static final int EPOCH_BITS = 41;
  private static final int NODE_ID_BITS = 10;
  private static final int SEQUENCE_BITS = 12;

  private static final long MAX_NODE_ID = (1L << NODE_ID_BITS) - 1;
  private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

  // Custom Epoch (January 1, 2015 Midnight UTC = 2015-01-01T00:00:00Z)
  private static final long DEFAULT_CUSTOM_EPOCH = 1420070400000L;

  private final long nodeId;
  private final long customEpoch;

  private volatile long lastTimestamp = -1L;
  private volatile long sequence = 0L;

  // Create Snowflake with a nodeId and custom epoch
  Snowflake(long nodeId, long customEpoch) {
    if (nodeId < 0 || nodeId > MAX_NODE_ID) {
      throw new IllegalArgumentException(String.format("NodeId must be between %d and %d", 0, MAX_NODE_ID));
    }
    this.nodeId = nodeId;
    this.customEpoch = customEpoch;
  }

  // Create Snowflake with a nodeId
  Snowflake(long nodeId) {
    this(nodeId, DEFAULT_CUSTOM_EPOCH);
  }

  // Let Snowflake generate a nodeId
  Snowflake() {
    this(newNodeId());
  }

  public synchronized long next() {
    var currentTimestamp = timestamp();

    if (currentTimestamp < lastTimestamp) {
      throw new IllegalStateException("Invalid System Clock!");
    }

    if (currentTimestamp == lastTimestamp) {
      sequence = (sequence + 1) & MAX_SEQUENCE;
      if (sequence == 0) {
        // Sequence Exhausted, wait till next millisecond.
        currentTimestamp = waitNextMillis(currentTimestamp);
      }
    } else {
      // reset sequence to start with zero for the next millisecond
      sequence = 0;
    }

    lastTimestamp = currentTimestamp;

    var id = currentTimestamp << (NODE_ID_BITS + SEQUENCE_BITS)
      | (nodeId << SEQUENCE_BITS)
      | sequence;

    return id;
  }

  public String nextAsString() { return Long.toString(next()); }


  // Get current timestamp in milliseconds, adjust for the custom epoch.
  private long timestamp() {
    return Instant.now().toEpochMilli() - customEpoch;
  }

  // Block and wait till next millisecond
  private long waitNextMillis(long currentTimestamp) {
    while (currentTimestamp == lastTimestamp) {
      currentTimestamp = timestamp();
    }
    return currentTimestamp;
  }

  private static long newNodeId() {
    try {
      final var snapshot = new StringBuilder();
      final var networkInterfaces = NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
        final var mac = networkInterfaces.nextElement().getHardwareAddress();
        if (mac != null) {
          for (var macPort : mac) {
            snapshot.append(String.format("%02X", macPort));
          }
        }
      }
      return snapshot.toString().hashCode() & MAX_NODE_ID;
    } catch (SocketException e) {
      return new SecureRandom().nextInt() & MAX_NODE_ID;
    }
  }

  public long[] parse(long id) {
    var maskNodeId = ((1L << NODE_ID_BITS) - 1) << SEQUENCE_BITS;
    var maskSequence = (1L << SEQUENCE_BITS) - 1;

    var timestamp = (id >> (NODE_ID_BITS + SEQUENCE_BITS)) + customEpoch;
    var nodeId = (id & maskNodeId) >> SEQUENCE_BITS;
    var sequence = id & maskSequence;

    return new long[]{timestamp, nodeId, sequence};
  }

  @Override
  public String toString() {
    return "Snowflake Settings [EPOCH_BITS=" + EPOCH_BITS + ", NODE_ID_BITS=" + NODE_ID_BITS
      + ", SEQUENCE_BITS=" + SEQUENCE_BITS + ", CUSTOM_EPOCH=" + customEpoch
      + ", NodeId=" + nodeId + "]";
  }
}
