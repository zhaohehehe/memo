package com.bonc.dataplatform.demo;

public class VlolatileVisibilityTest {
	private static int race;

	public static void increase() {
		race++;
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
		/*
		 * 多执行几次，发现打印结果并不一定是20000
		 */
	}
}
