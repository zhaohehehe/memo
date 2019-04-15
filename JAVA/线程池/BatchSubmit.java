package **;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BatchSubmit {
	public static Callable<String> callable(final String result, final long sleepSeconds) {
		return new Callable<String>() {

			@Override
			public String call() throws Exception {
				TimeUnit.SECONDS.sleep(sleepSeconds);
				return result;
			}
		};
	}

	/**
	 * 
	 * 只要有执行结束的就返回
	 * <p>
	 * 这个方法将会阻塞直到第一个callable中止然后返回这一个callable的结果
	 * 
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void invokeAny() throws InterruptedException, ExecutionException {
		// jdk8,new a ForkJoinPool,ForkJoinPools使用一个并行因子数来创建，默认值为主机CPU的可用核心数
		ExecutorService executor = Executors.newWorkStealingPool();

		List<Callable<String>> callables = Arrays.asList(callable("task1", 2), callable("task2", 1),
				callable("task3", 3));

		String result = executor.invokeAny(callables);
		System.out.println(result);

	}

	/**
	 * 所有都执行结束再返回
	 * 
	 * @throws InterruptedException
	 */
	public static void invokeAll() throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		List<Callable<String>> callables = Arrays.asList(callable("task1", 0), callable("task1", 0),
				callable("task1", 0));
		List<Future<String>> futures = executor.invokeAll(callables);
		for (Future<?> future : futures) {
			try {
				System.out.println(future.get());
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		executor.shutdownNow();
	}

	public static void main(String[] args) {
		try {
			invokeAny();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
