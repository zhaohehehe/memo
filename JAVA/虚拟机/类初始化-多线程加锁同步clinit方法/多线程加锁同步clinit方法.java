package zhaohe.study.thread;

/**
 * 虚拟机会保证一个类的<clinit>()方法在多线程环境中被正确的加锁、同步，如果多个线程同时初始化一个类，那么只会有一个线程
 * 去执行该类的init方法，其他线程阻塞等待，直到init结束。如果init非常耗时，可能造成多个线程阻塞。
 * <p>
 * 注意：同一个类加载器(带有main函数的类，执行入口)下，一个类型只会初始化一次。参见Thread1And2 main函数打印结果.
 * <p>
 * 
 * @author ZH
 *
 */
class ClinitTest2 {
	static {
		if (true) {
			System.out.println(Thread.currentThread() + " init ClinitTest2");
			// 模拟一直在init
			while (true) {

			}
		}
	}
}

public class 多线程加锁同步clinit方法 {
	static {
		System.out.println(Thread.currentThread() + " init 多线程加锁同步clinit方法");
	}

	static class ClinitTest1 {
		static {
			if (true) {
				System.out.println(Thread.currentThread() + " init ClinitTest1");
				// 模拟一直在init
				while (true) {

				}
			}
		}
	}

	public static void main(String[] args) {
		多线程加锁同步clinit方法.test1();
		while (Thread.currentThread().isAlive()) {
			System.out.println(Thread.currentThread() + "主线程未结束");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 打印结果： Thread[main,5,main] init 多线程加锁同步clinit方法【main函数触发类初始化】
		 * <p>
		 * Thread[main,5,main]主线程未结束
		 * <p>
		 * Thread[Thread-1,5,main] start
		 * <p>
		 * Thread[Thread-0,5,main] start
		 * <p>
		 * Thread[main,5,main]主线程未结束
		 * <p>
		 * Thread[Thread-0,5,main] init
		 * ClinitTest1/ClinitTest2【Thread-0初始化类init方法未结束，线程阻塞】
		 * <p>
		 * Thread[main,5,main]主线程未结束
		 * <p>
		 * Thread[main,5,main]主线程未结束
		 * <p>
		 * Thread[main,5,main]主线程未结束
		 * <p>
		 * Thread[main,5,main]主线程未结束
		 * <p>
		 * Thread[main,5,main]主线程未结束
		 * <p>
		 * .....
		 */

	}

	public static void test2() {
		Runnable run = new Runnable() {

			@Override
			public void run() {
				System.out.println(Thread.currentThread() + " start ");
				// new 指令触发类初始化
				ClinitTest2 test = new ClinitTest2();
				System.out.println(Thread.currentThread() + " run over ");
			}
		};
		Thread thread1 = new Thread(run);
		Thread thread2 = new Thread(run);
		thread1.start();
		thread2.start();
	}

	public static void test1() {
		Runnable run = new Runnable() {

			@Override
			public void run() {
				System.out.println(Thread.currentThread() + " start ");
				// new 指令触发类初始化
				ClinitTest1 test = new ClinitTest1();
				System.out.println(Thread.currentThread() + " run over ");
			}
		};
		Thread thread1 = new Thread(run);
		Thread thread2 = new Thread(run);
		thread1.start();
		thread2.start();
	}
}
