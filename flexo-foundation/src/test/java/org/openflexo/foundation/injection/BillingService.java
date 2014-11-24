package org.openflexo.foundation.injection;

import com.google.inject.Inject;

public class BillingService {
	private final CreditCardProcessor processor;
	private final TransactionLog transactionLog;

	@Inject
	private Logger logger;

	@Inject
	BillingService(CreditCardProcessor processor, TransactionLog transactionLog) {
		this.processor = processor;
		this.transactionLog = transactionLog;
	}

	public CreditCardProcessor getProcessor() {
		return processor;
	}

	public TransactionLog getTransactionLog() {
		return transactionLog;
	}

	public Logger getLogger() {
		return logger;
	}

	public Receipt chargeOrder(PizzaOrder order, CreditCard creditCard) {
		return null;
	}

	public static class Receipt {

	}

	public static class PizzaOrder {

	}

	public static class CreditCard {

	}

}
