package **;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class GracefulShutdown {
	public static void sample01() {
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

	public static Map<Object, Object> sample02() {
		ExecutorService exec = Executors.newFixedThreadPool(3);
		Future<List<Map<String, Object>>> good = exec.submit(new Callable<List<Map<String, Object>>>() {
			@Override
			public List<Map<String, Object>> call() throws Exception {
				// business
				return null;
			}
		});
		Future<List<Map<String, Object>>> bad = exec.submit(new Callable<List<Map<String, Object>>>() {
			@Override
			public List<Map<String, Object>> call() throws Exception {
				// business
				return null;
			}
		});
		exec.shutdown();
		try {
			if (!exec.awaitTermination(5000, TimeUnit.SECONDS)) {
				exec.shutdownNow();
			}
		} catch (InterruptedException e) {
			exec.shutdownNow();
		}
		// 格式化返回信息
		try {
			return business(good.get(), bad.get());
		} catch (Exception e) {
			return new LinkedHashMap<>();
		}
	}

	private static Map<Object, Object> business(List<Map<String, Object>> list, List<Map<String, Object>> list2)
			throws Exception {
		return null;
	}

}
