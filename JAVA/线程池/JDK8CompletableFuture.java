package com.bonc.dataplatform.workflowmonitor.dao.impl;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Java 8提出了强大的CompletableFuture,请尽可能的使用它。 CompletableFuture继承了Future及其所有功能
 * 
 * @author ZH
 *
 */
public class JDK8CompletableFuture {
	private <V> Callable<V> calculate() {
		return new Callable<V>() {

			@Override
			public V call() throws Exception {
				return null;
			}
		};
	}

	private final ExecutorService executorService;
	final CompletableFuture<BigDecimal> future1 = CompletableFuture.supplyAsync(this::calculate, executorService);
	// 代替：
	final Future<BigDecimal> future2 = executorService.submit(this::calculate);

}
