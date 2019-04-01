package zhaohe.study.design;

public class ReentrantLockTest {
	ReentrantLock lock = new ReentrantLock();

	public void print() {
		try {
			lock.lock();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		doAdd();
		lock.unlock();
	}

	public void doAdd() {
		try {
			lock.lock();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// do something
		lock.unlock();
	}
}

/**
 * 可重入锁
 * 
 * @author ZH
 *
 */
class ReentrantLock {
	boolean isLocked = false;
	Thread lockedBy = null;
	int lockedCount = 0;

	public synchronized void lock() throws InterruptedException {
		Thread thread = Thread.currentThread();
		while (isLocked && lockedBy != thread) {
			wait();
		}
		isLocked = true;
		lockedCount++;
		lockedBy = thread;
	}

	public synchronized void unlock() {
		if (Thread.currentThread() == this.lockedBy) {
			lockedCount--;
			if (lockedCount == 0) {
				isLocked = false;
				notify();
			}
		}
	}
}

/**
 * 不可重入锁
 * 
 * @author ZH
 *
 */
class UnReentrantLock {
	private boolean isLocked = false;

	public synchronized void lock() throws InterruptedException {
		while (isLocked) {
			wait();
		}
		isLocked = true;
	}

	public synchronized void unlock() {
		isLocked = false;
		notify();
	}

}
