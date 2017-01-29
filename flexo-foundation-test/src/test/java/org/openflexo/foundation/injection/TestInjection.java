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

package org.openflexo.foundation.injection;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestInjection {

	public static void main(String[] args) {
		/*
		 * Guice.createInjector() takes your Modules, and returns a new Injector
		 * instance. Most applications will call this method exactly once, in their
		 * main() method.
		 */
		Injector injector = Guice.createInjector(new BillingModule());

		/*
		 * Now that we've got the injector, we can build objects.
		 */

		System.out.println("create billing service");

		// BillingService billingService = injector.getInstance(BillingService.class);

		BillingService billingService = new BillingService(new PaypalCreditCardProcessor(), new DatabaseTransactionLog());

		System.out.println("created billing service");

		System.out.println("processor = " + billingService.getProcessor());
		System.out.println("transaction log =" + billingService.getTransactionLog());
		System.out.println("logger = " + billingService.getLogger());

		injector.injectMembers(billingService);

		System.out.println("processor = " + billingService.getProcessor());
		System.out.println("transaction log =" + billingService.getTransactionLog());
		System.out.println("logger = " + billingService.getLogger());

	}
}
