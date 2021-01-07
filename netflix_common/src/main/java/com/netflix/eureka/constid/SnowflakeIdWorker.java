package com.netflix.eureka.constid;

public abstract class SnowflakeIdWorker {
	/** 机器id所占的位数 */
	private static final long workerIdBits = 5L;

	/** 数据标识id所占的位数 */
	private static final long datacenterIdBits = 5L;

	/** 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数) */
	private static final long maxWorkerId = -1L ^ (-1L << workerIdBits);

	/** 支持的最大数据标识id，结果是31 */
	private static final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

	/** 序列在id中占的位数 */
	private static final long sequenceBits = 12L;

	/** 机器ID向左移12位 */
	private static final long workerIdShift = sequenceBits;

	/** 数据标识id向左移17位(12+5) */
	private static final long datacenterIdShift = sequenceBits + workerIdBits;

	/** 时间截向左移22位(5+5+12) */
	private static final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

	/** 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095) */
	private static final long sequenceMask = -1L ^ (-1L << sequenceBits);

	abstract public long nextId();
	/**
	 * 构造函数
	 * 
	 * @param workerId
	 *            工作ID (0~31)
	 * @param datacenterId
	 *            数据中心ID (0~31)
	 */
	public static SnowflakeIdWorker newID(long twepoch, long workerId, long datacenterId) {
		if (workerId > maxWorkerId || workerId < 0) {
			throw new IllegalArgumentException(
					String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
		}
		if (datacenterId > maxDatacenterId || datacenterId < 0) {
			throw new IllegalArgumentException(
					String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
		}
		return new IDWorker(twepoch, workerId, datacenterId);
	}

	public static final class IDWorker extends SnowflakeIdWorker {
		/** 开始时间截 */
		private long twepoch;
		/** 工作机器ID(0~31) */
		private long workerId;
		/** 数据中心ID(0~31) */
		private long datacenterId;
		
		/** 毫秒内序列(0~4095) */
		private long sequence = 0L;

		/** 上次生成ID的时间截 */
		private long lastTimestamp = -1L;
		
		public IDWorker(long twepoch, long workerId, long datacenterId) {
			this.twepoch = twepoch;
			this.workerId = workerId;
			this.datacenterId = datacenterId;
		}
		
		/**
		 * 获得下一个ID (该方法是线程安全的)
		 * @return SnowflakeId
		 */
		public synchronized long produce() {
			long timestamp = timeGen();
			// 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
			if (timestamp < lastTimestamp) {
				throw new RuntimeException(String.format(
						"Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
			}

			if (lastTimestamp == timestamp) {
				sequence = (sequence + 1) & sequenceMask;
				if (sequence == 0) {
					timestamp = tilNextMillis(lastTimestamp);
				}
			} else {
				sequence = 0L;
			}
			lastTimestamp = timestamp;
			return ((timestamp - twepoch) << timestampLeftShift)
					| (datacenterId << datacenterIdShift)
					| (workerId << workerIdShift)
					| sequence;
		}

		@Override
		public long nextId() {
			return produce();
		}
		
		public String toString() {
			return String.format("%d:%d:%d", twepoch, workerId, datacenterId);
		}

	}

	/**
	 * 阻塞到下一个毫秒，直到获得新的时间戳
	 * 
	 * @param lastTimestamp
	 *            上次生成ID的时间截
	 * @return 当前时间戳
	 */
	protected long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen();
		}
		return timestamp;
	}

	/**
	 * 返回以毫秒为单位的当前时间
	 * 
	 * @return 当前时间(毫秒)
	 */
	protected long timeGen() {
		return System.currentTimeMillis();
	}
	
	public static void main(String[] args) {
		Long id = ((1588059715920L - 1588059715918L) << timestampLeftShift)
		| (28L << datacenterIdShift)
		| (3L << workerIdShift)
		| 0L;
		System.out.println(id);
		System.out.println(String.format("%016d", id));
    }
}
