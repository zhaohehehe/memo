package **;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledExecutors {

	/**
	 * scheduleAtFixedRate()并不考虑任务的实际用时，预测定时调度；
	 * <p>
	 * 如果指定了一个period为1分钟而任务需要执行2分钟，那么下一次执行时开始时间会有延迟，而不会同步执行。
	 */
	public static void testScheduleAtFixedRate() {
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		Runnable task = runnable();
		// 每秒执行一次，若上一次没有执行完，下一次的开始时间会有延迟(注意：不会同步执行！！)
		int initialDelay = 0;
		int period = 1;
		executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);
		// executor.shutdown();
	}

	/**
	 * scheduleWithFixedDelay()上次任务结束和下次任务开始时间间隔
	 */
	public static void testScheduleWithFixedDelay() {
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		Runnable task = runnable();
		// 下一次开始时间 - 上一次结束时间 = 1秒，即每次执行间隔1秒
		executor.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS);
		// executor.shutdown();
	}

	public static void testDelay() throws InterruptedException {
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

		Runnable task = runnable();
		// 延迟3秒执行
		ScheduledFuture<?> future = executor.schedule(task, 3, TimeUnit.SECONDS);

		TimeUnit.MILLISECONDS.sleep(1000);
		long remainingDelay = future.getDelay(TimeUnit.MILLISECONDS);
		System.out.printf("\nRemaining Delay: %sms", remainingDelay);

		TimeUnit.MILLISECONDS.sleep(1000);
		remainingDelay = future.getDelay(TimeUnit.MILLISECONDS);
		System.out.printf("\nRemaining Delay: %sms", remainingDelay);

		TimeUnit.MILLISECONDS.sleep(1000);
		remainingDelay = future.getDelay(TimeUnit.MILLISECONDS);
		System.out.printf("\nRemaining Delay: %sms", remainingDelay);
		executor.shutdown();
	}

	public static Runnable runnable() {
		return new Runnable() {

			@Override
			public void run() {
				try {
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Scheduling: " + System.nanoTime());

			}
		};
	}

	public static void main(String[] args) throws InterruptedException {
		testScheduleWithFixedDelay();
	}
}
