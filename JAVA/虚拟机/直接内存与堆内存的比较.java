package com.bonc.dataplatform.demo;

import java.nio.ByteBuffer;

/**
 * 直接内存 与 堆内存的比较
 */
public class Test {

	public static void main(String[] args) {
		allocateCompare(); // 分配比较
		operateCompare(); // 读写比较
	}

	/**
	 * 直接内存 和 堆内存的 分配空间比较
	 * 
	 * 结论： 在数据量提升时，直接内存相比非直接内的申请，有很严重的性能问题
	 * 
	 */
	public static void allocateCompare() {
		int time = 10000000; // 操作次数
		long st = System.currentTimeMillis();
		for (int i = 0; i < time; i++) {
			// Allocates a new byte buffer
			ByteBuffer buffer = ByteBuffer.allocate(2);
		}
		long et = System.currentTimeMillis();
		System.out.println("在进行" + time + "次分配操作时，堆内存 分配耗时:" + (et - st) + "ms");

		long st_heap = System.currentTimeMillis();
		for (int i = 0; i < time; i++) {
			// Allocates a new direct byte buffer
			ByteBuffer buffer = ByteBuffer.allocateDirect(2);
		}
		long et_direct = System.currentTimeMillis();
		System.out.println("在进行" + time + "次分配操作时，直接内存 分配耗时:" + (et_direct - st_heap) + "ms");

	}

	/**
	 * 直接内存 和 堆内存的 读写性能比较
	 * 
	 * 结论：直接内存在直接的IO操作上，在频繁的读写时会有显著的性能提升
	 * 
	 */
	public static void operateCompare() {
		int time = 10000000;

		ByteBuffer buffer = ByteBuffer.allocate(2 * time);
		long st = System.currentTimeMillis();
		for (int i = 0; i < time; i++) {
			// char占2个字节
			buffer.putChar('a');
			// buffer.put(Byte.parseByte("1"));
		}
		buffer.flip();
		for (int i = 0; i < time; i++) {
			buffer.getChar();
			// buffer.get();
		}
		long et = System.currentTimeMillis();
		System.out.println("在进行" + time + "次读写操作时，非直接内存读写耗时：" + (et - st) + "ms");

		ByteBuffer buffer_d = ByteBuffer.allocateDirect(2 * time);
		long st_direct = System.currentTimeMillis();
		for (int i = 0; i < time; i++) {
			buffer_d.putChar('a');
		}
		buffer_d.flip();
		for (int i = 0; i < time; i++) {
			buffer_d.getChar();
		}
		long et_direct = System.currentTimeMillis();
		System.out.println("在进行" + time + "次读写操作时，直接内存读写耗时:" + (et_direct - st_direct) + "ms");
	}
}
