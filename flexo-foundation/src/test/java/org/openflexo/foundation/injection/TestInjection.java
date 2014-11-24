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
