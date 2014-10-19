package org.openflexo.foundation.task;

import java.math.BigInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class InfiniteTask extends FlexoTask {

	private final BlockingQueue<BigInteger> queue;

	public InfiniteTask(String name) {
		super(name);
		queue = new ArrayBlockingQueue<BigInteger>(1000);
	}

	@Override
	public void performTask() {

		Progress.setExpectedProgressSteps(1000);

		try {
			BigInteger p = BigInteger.ONE;
			while (!Thread.currentThread().isInterrupted()) {
				p = p.nextProbablePrime();
				queue.put(p = p.nextProbablePrime());
				System.out.println("Prime number: " + p);
				Progress.progress();
			}
		} catch (InterruptedException consumed) {
			/* Allow thread to exit */
		}

		/*System.out.println(Thread.currentThread().getName() + " Start. Thread: " + Thread.currentThread());
		try {
			Thread.sleep((new Random(System.currentTimeMillis())).nextInt(2000));
			while (true) {
				Progress.progress();
				Thread.sleep(500);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/

		/*try {
			synchronized (this) {

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/

		/*try {
			Thread.currentThread().join();
			while (true) {
				System.out.println("prout");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/

		// System.out.println(Thread.currentThread().getName() + " End.");
	}

	/*public class PrimeProducer extends Thread {
		private final BlockingQueue<BigInteger> queue;

		PrimeProducer(BlockingQueue<BigInteger> queue) {
			this.queue = queue;
		}

		@Override
		public void run() {
			try {
				BigInteger p = BigInteger.ONE;
				while (!Thread.currentThread().isInterrupted()) {
					queue.put(p = p.nextProbablePrime());
					System.out.println("Prime number: " + p);
				}
			} catch (InterruptedException consumed) {
				// Allow thread to exit
			}
		}

		//public void cancel() {
		//	interrupt();
		//}
	}*/

	@Override
	public boolean isCancellable() {
		return true;
	}

}