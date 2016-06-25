package com.qljl.tmm_business.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.qljl.tmm_business.MainActivity;

public class MyLock {
	private static Lock lock = new ReentrantLock();

	private static Condition condition_pro = lock.newCondition();

	public static void getAwart() {
		lock.lock();
		MainActivity.bool = true;
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
		MainActivity.bool = false;
		try {
			condition_pro.signalAll();
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			lock.unlock();
		}
	}

}
