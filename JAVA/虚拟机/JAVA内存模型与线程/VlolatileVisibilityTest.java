package zhaohe.study.design;

import java.util.concurrent.atomic.AtomicInteger;

public class VlolatileVisibilityTest {
	private volatile static int race;
	private static AtomicInteger race2 = new AtomicInteger();

	public static void increase() {
		race++;
		race2.incrementAndGet();
	}

	private static final int threshold = 20;

	public static void main(String[] args) {
		for (int i = 0; i < threshold; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < 1000; i++) {
						VlolatileVisibilityTest.increase();
					}
				}
			});
			thread.start();
		}
		while (Thread.activeCount() > 1) {
			/*
			 * 线程让步。使用了这个方法之后，线程会让出CPU执行权，让自己或者其它的线程运行。也就是说，当前线程调用yield()之后，
			 * 并不能保证：其它具有相同优先级的线程一定能获得执行权，也有可能是当前线程又进入到“运行状态”继续运行
			 */
			Thread.yield();
		}
		System.out.println(race);
		System.out.println(race2);
		/*
		 * 多执行几次，发现打印race结果并不一定是20000,race2为20000
		 * <p>因为volatile不能保证原子性，如果想保证同步，一个是使用synchronized同步increase()方法，
		 * 另一个比较高效的反法是使用 AtomicInteger.incrementAndGet()方法，该方法可以保证原子性。
		 */
	}
}

/**
 * 适合使用volatile的场景，必须保证：
 * <p>
 * 1.运算结果不依赖当前变量的值；
 * <p>
 * 2.变量不需要与其他状态变量共同参与不变约束
 */
class VlolatileVisibilityTest2 {
	private volatile boolean shutdownRequested;

	public void shutdown() {
		shutdownRequested = true;
	}

	public void doWork() {
		while (!shutdownRequested) {
			// do something
		}
	}
}

/**
 * volatile禁止指令重排序： 指令重排序？例如以下doWork方法中①处的代码可能在它处于位置的前面执行，这样并发时可能会导致逻辑错误，而
 * volatile可以禁止这种指令优化重排，它添加了内存屏障PP371
 */
class VlolatileVisibilityTest3 {
	private volatile boolean isDone;

	public void doWork() {
		if (!isDone) {
			this.doSomething();
		}
		this.doAnotherthing();
		isDone = true;// ①
	}

	private void doSomething() {

	}

	private void doAnotherthing() {

	}
}
