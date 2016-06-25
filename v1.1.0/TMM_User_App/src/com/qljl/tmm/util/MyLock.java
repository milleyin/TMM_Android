package com.qljl.tmm.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyLock {
	private static Lock lock = new ReentrantLock();

	private static Condition condition_pro = lock.newCondition();

	public static void getAwart() {
		lock.lock();
		try {
			condition_pro.await();
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			lock.unlock();
		}
	}

	public static void getSignal() {
		lock.lock();
		try {
			condition_pro.signalAll();
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			lock.unlock();
		}
	}

}
