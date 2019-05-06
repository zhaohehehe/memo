import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class CalculatePoolConfigHelperUtil extends AbstractPoolConfigCalculator {

	@Override
	public Object createTargetTaskInstance() {
		/*
		 * return new Callable<Object>() {
		 * 
		 * @Override public Object call() throws Exception { return null; } };
		 */
		return new Runnable() {

			@Override
			public void run() {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
	}

	@Override
	public BlockingQueue<Object> createBlockQueueInstance() {
		return new LinkedBlockingDeque<>();
	}

	public static void main(String[] args) {
		float coreThreadCount = AbstractPoolConfigCalculator.calculateOptimalThreadCount(10, 1000, 1);
		System.out.println("核心线程数量：" + coreThreadCount);
		CalculatePoolConfigHelperUtil util = new CalculatePoolConfigHelperUtil();
		float queueSize = util.calculateOptimalQueueCapacity(50_000);
		System.out.println("阻塞队列大小：" + queueSize);
	}

}
