package zhaohe.study.thread;

public class Thread2 {
	public static void init() {
		System.out.println(Thread.currentThread() + " start ");
		InitClass test = new InitClass();
		System.out.println(Thread.currentThread() + " run over ");
	}

	public static void main(String[] args) {
		Thread2.init();
		while (true) {

		}
	}
}
