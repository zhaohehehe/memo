package **;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GracefulShutdown {
	public static void main(String[] args) {
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		executorService.submit(new Runnable() {

			@Override
			public void run() {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		});
		/**
		 * shutdwon()会等待正在执行的任务执行完
		 * <p>
		 * shutdownNow()会终止所有正在执行的任务并立即关闭executor
		 */
		/**
		 * <pre>
		 * executorService.shutdown();
		 * try {
		 * 	if (!executorService.awaitTermination(5000, TimeUnit.SECONDS)) {
		 * 		executorService.shutdownNow();
		 * 	}
		 * } catch (InterruptedException e) {
		 * 	executorService.shutdownNow();
		 * }
		 * </pre>
		 */

		try {
			System.out.println("attempt to shutdown executor");
			executorService.shutdown();
			executorService.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.err.println("tasks interrupted");
		} finally {
			if (!executorService.isTerminated()) {
				System.err.println("cancel non-finished tasks");
			}
			executorService.shutdownNow();
			System.out.println("shutdown finished");
		}

	}

}
