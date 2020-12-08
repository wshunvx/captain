package com.netflix.eureka.constid;

public abstract class SnowflakeIdWorker {
	/** ����id��ռ��λ�� */
	private static final long workerIdBits = 5L;

	/** ���ݱ�ʶid��ռ��λ�� */
	private static final long datacenterIdBits = 5L;

	/** ֧�ֵ�������id�������31 (�����λ�㷨���Ժܿ�ļ������λ�����������ܱ�ʾ�����ʮ������) */
	private static final long maxWorkerId = -1L ^ (-1L << workerIdBits);

	/** ֧�ֵ�������ݱ�ʶid�������31 */
	private static final long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

	/** ������id��ռ��λ�� */
	private static final long sequenceBits = 12L;

	/** ����ID������12λ */
	private static final long workerIdShift = sequenceBits;

	/** ���ݱ�ʶid������17λ(12+5) */
	private static final long datacenterIdShift = sequenceBits + workerIdBits;

	/** ʱ���������22λ(5+5+12) */
	private static final long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;

	/** �������е����룬����Ϊ4095 (0b111111111111=0xfff=4095) */
	private static final long sequenceMask = -1L ^ (-1L << sequenceBits);

	abstract public long nextId();
	/**
	 * ���캯��
	 * 
	 * @param workerId
	 *            ����ID (0~31)
	 * @param datacenterId
	 *            ��������ID (0~31)
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
		/** ��ʼʱ��� */
		private long twepoch;
		/** ��������ID(0~31) */
		private long workerId;
		/** ��������ID(0~31) */
		private long datacenterId;
		
		/** ����������(0~4095) */
		private long sequence = 0L;

		/** �ϴ�����ID��ʱ��� */
		private long lastTimestamp = -1L;
		
		public IDWorker(long twepoch, long workerId, long datacenterId) {
			this.twepoch = twepoch;
			this.workerId = workerId;
			this.datacenterId = datacenterId;
		}
		
		/**
		 * �����һ��ID (�÷������̰߳�ȫ��)
		 * @return SnowflakeId
		 */
		public synchronized long produce() {
			long timestamp = timeGen();
			// �����ǰʱ��С����һ��ID���ɵ�ʱ�����˵��ϵͳʱ�ӻ��˹����ʱ��Ӧ���׳��쳣
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
	 * ��������һ�����룬ֱ������µ�ʱ���
	 * 
	 * @param lastTimestamp
	 *            �ϴ�����ID��ʱ���
	 * @return ��ǰʱ���
	 */
	protected long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen();
		}
		return timestamp;
	}

	/**
	 * �����Ժ���Ϊ��λ�ĵ�ǰʱ��
	 * 
	 * @return ��ǰʱ��(����)
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
