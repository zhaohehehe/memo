package zhaohe.study.thread;

public class Thread1And2 {
	public static void main(String[] args) {

		Thread1.init();
		Thread2.init();
		/**
		 * 打印结果：
		 * <p>
		 * Thread1And2控制台只会执行一次InitClass初始化；
		 * <p>
		 * 分别打开Thread1和Thread2控制台，每个控制台都会执行一次InitClass初始化
		 * <p>
		 * <p>
		 * 结论：同一个类加载器下，一个类型只会初始化一次
		 */

	}
}
