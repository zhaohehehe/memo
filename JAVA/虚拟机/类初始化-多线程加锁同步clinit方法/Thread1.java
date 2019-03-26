package zhaohe.study.thread;

public class Thread1 {
	public static void init() {
		System.out.println(Thread.currentThread() + " start ");
		InitClass test = new InitClass();
		System.out.println(Thread.currentThread() + " run over ");
	}

	public static void main(String[] args) {
		Thread1.init();
		while (true) {

		}
	}
}
