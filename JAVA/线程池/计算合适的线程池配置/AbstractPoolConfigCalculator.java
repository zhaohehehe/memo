import java.util.concurrent.BlockingQueue;

public abstract class AbstractPoolConfigCalculator {
	private static final int SAMPLE_QUEUE_SIZE = 1000;

	/**
	 * 粗略计算线城池核心线程数量
	 * 
	 * @param cpuTimeMs
	 *            线程CPU耗时
	 * @param blockTimeMs
	 *            线程阻塞耗时
	 * @param targetUtilization
	 *            CPU利用率
	 * @return
	 */
	public static float calculateOptimalThreadCount(long cpuTimeMs, long blockTimeMs, float targetUtilization) {
		float blockCoefficient = (float) blockTimeMs / cpuTimeMs;
		return Runtime.getRuntime().availableProcessors() * targetUtilization * (1 + blockCoefficient);
	}

	/**
	 * 粗略计算线程池队列大小
	 * 
	 * @param targetQueueSizeBytes
	 *            期望线城池任务所占内存大小(单位bytes)
	 * @return
	 */
	public float calculateOptimalQueueCapacity(long targetQueueSizeBytes) {
		return targetQueueSizeBytes / calculateSampleQueueCapacity();
	}

	/**
	 * 根据样本SAMPLE_QUEUE_SIZE计算每个任务占用内存大小(bytes)
	 * 
	 * @return 单位是bytes
	 */
	private float calculateSampleQueueCapacity() {
		BlockingQueue<Object> blockQueue = createBlockQueueInstance();

		runGC();
		long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		for (int i = 0; i < SAMPLE_QUEUE_SIZE; i++) {
			blockQueue.add(createTargetTaskInstance());
		}

		runGC();
		long after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		blockQueue = null;
		runGC();
		return (float) (after - before) / SAMPLE_QUEUE_SIZE;
	}

	private static void runGC() {
		long usedMem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long usedMem2 = Long.MAX_VALUE;
		for (int i = 0; (usedMem1 < usedMem2) && (i < 500); ++i) {
			Runtime.getRuntime().runFinalization();
			Runtime.getRuntime().gc();
			Thread.yield();
			usedMem2 = usedMem1;
			usedMem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		}
	}

	/**
	 * 创建一个目标任务
	 * 
	 * @return
	 */
	public abstract Object createTargetTaskInstance();

	/**
	 * 创建线城池阻塞队列
	 * 
	 * @return
	 */
	public abstract BlockingQueue<Object> createBlockQueueInstance();

}
