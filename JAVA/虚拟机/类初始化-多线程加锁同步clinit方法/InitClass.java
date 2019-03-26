package zhaohe.study.thread;

public class InitClass {
	static {
		System.out.println(Thread.currentThread() + " init InitClass");
	}
}
