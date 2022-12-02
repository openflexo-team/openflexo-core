/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
 * developed at Openflexo.
 * 
 * 
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either 
 * version 1.1 of the License, or any later version ), which is available at 
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any 
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 * 
 * You can redistribute it and/or modify under the terms of either of these licenses
 * 
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *          Additional permission under GNU GPL version 3 section 7
 *
 *          If you modify this Program, or any covered work, by linking or 
 *          combining it with software containing parts covered by the terms 
 *          of EPL 1.0, the licensors of this Program grant you additional permission
 *          to convey the resulting work. * 
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 *
 * See http://www.openflexo.org/license.html for details.
 * 
 * 
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 * 
 */

package org.openflexo.foundation.test.task;

import java.math.BigInteger;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.openflexo.foundation.task.FlexoTask;
import org.openflexo.foundation.task.Progress;

public class InfiniteTask extends FlexoTask {

	private final BlockingQueue<BigInteger> queue;

	public InfiniteTask(String name) {
		super("INFINITE_TASK", name);
		queue = new ArrayBlockingQueue<>(1000);
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
