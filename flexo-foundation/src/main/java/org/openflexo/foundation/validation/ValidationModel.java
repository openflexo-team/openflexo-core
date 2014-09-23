/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.validation;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.antar.binding.TypeUtils;
import org.openflexo.foundation.utils.FlexoListModel;
import org.openflexo.foundation.validation.annotations.DefineValidationRule;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.model.ModelContext;
import org.openflexo.model.ModelEntity;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

/**
 * Used to store and manage a set of {@link ValidationRule} associated to some types<br>
 * {@link ValidationRule} discovering is based on PAMELA models annotated with {@link DefineValidationRule} annotations
 * 
 * @author sguerin
 * 
 */
@SuppressWarnings("serial")
public abstract class ValidationModel extends FlexoListModel {

	private static final Logger logger = Logger.getLogger(ValidationModel.class.getPackage().getName());

	public static final String VALIDATION_START = "validationStart";
	public static final String VALIDATION_END = "validationEnd";
	public static final String VALIDATION_OBJECT = "validateObject";
	public static final String OBJECT_VALIDATION_START = "objectValidation";
	public static final String VALIDATE_WITH_RULE = "validateWithRule";

	private final Map<Class<?>, ValidationRuleSet<?>> ruleSets;

	private ModelFactory validationModelFactory;

	public ValidationModel(ModelContext modelContext) {
		super();

		ruleSets = new HashMap<Class<?>, ValidationRuleSet<?>>();

		try {
			searchAndRegisterValidationRules(modelContext);
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void searchAndRegisterValidationRules(ModelContext modelContext) throws ModelDefinitionException {

		validationModelFactory = new ModelFactory(modelContext);

		Iterator<ModelEntity> it = modelContext.getEntities();

		while (it.hasNext()) {
			ModelEntity e = it.next();
			// System.out.println("assertTrue(validationModel.getValidationModelFactory().getModelContext().getModelEntity("
			// + e.getImplementedInterface().toString().substring(10) + ".class) != null);");
			Class i = e.getImplementedInterface();
			ruleSets.put(i, new ValidationRuleSet<Validable>(i));
		}

		// Now manage inheritance
		it = modelContext.getEntities();
		while (it.hasNext()) {
			ModelEntity e = it.next();
			// System.out.println("assertTrue(validationModel.getValidationModelFactory().getModelContext().getModelEntity("
			// + e.getImplementedInterface().toString().substring(10) + ".class) != null);");
			Class i = e.getImplementedInterface();
			manageInheritanceFor(i, ruleSets.get(i));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void manageInheritanceFor(Class<?> cl, ValidationRuleSet<?> originRuleSet) {
		for (Class<?> superInterface : cl.getInterfaces()) {
			if (ruleSets.get(superInterface) != null && originRuleSet.getDeclaredType() != superInterface) {
				// System.out.println("Found " + originRuleSet.getDeclaredType() + " inherits from " + superInterface);
				originRuleSet.addParentRuleSet((ValidationRuleSet) ruleSets.get(superInterface));

			} else {
				manageInheritanceFor(superInterface, originRuleSet);
			}
		}
	}

	public ModelFactory getValidationModelFactory() {
		return validationModelFactory;
	}

	@Override
	public String getDeletedProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Validate supplied Validable object by returning boolean indicating if validation throw errors (warnings are not considered as invalid
	 * model).
	 * 
	 * @param object
	 * @return
	 */
	public boolean isValid(Validable object) {
		return validate(object).getErrorNb() == 0;
	}

	/**
	 * Validate supplied Validable object by building and returning a new ValidationReport object.
	 * 
	 * @param object
	 * @return a newly created ValidationReport object
	 */
	public ValidationReport validate(Validable object) {
		ValidationReport returned = new ValidationReport(this, object);
		if (validate(object, returned)) {
			returned.addToValidationIssues(new InformationIssue(object, "consistency_check_ok"));
		}
		return returned;
	}

	public Collection<Validable> retrieveAllEmbeddedValidableObjects(Validable o) {
		List<Validable> returned = new ArrayList<Validable>();
		appendAllEmbeddedValidableObjects(o, returned);
		return returned;
	}

	private void appendAllEmbeddedValidableObjects(Validable o, Collection<Validable> c) {
		if (o != null) {
			c.add(o);
			Collection<? extends Validable> embeddedObjects = o.getEmbeddedValidableObjects();
			if (embeddedObjects != null) {
				for (Validable o2 : embeddedObjects) {
					appendAllEmbeddedValidableObjects(o2, c);
				}
			}
		}
	}

	/**
	 * Validate supplied Validable object by appending ValidationIssues object to supplied ValidationReport. Return true if no validation
	 * issues were found, false otherwise
	 * 
	 * @param object
	 * @param report
	 */
	public final <V extends Validable> boolean validate(V object, ValidationReport report) {
		int addedIssues = 0;

		// Get all the objects to validate
		Collection<Validable> allEmbeddedValidableObjects = retrieveAllEmbeddedValidableObjects(object);

		// logger.info("For object " + object + " objects to validate are: " + allEmbeddedValidableObjects);

		// Remove duplicated objects
		Vector<Validable> objectsToValidate = new Vector<Validable>();
		for (Validable next : allEmbeddedValidableObjects) {
			if (!objectsToValidate.contains(next)) {
				objectsToValidate.add(next);
			}
		}

		// Compute validation steps and notify validation initialization
		int validationStepToNotify = 0;
		for (Enumeration<Validable> en = objectsToValidate.elements(); en.hasMoreElements();) {
			Validable next = en.nextElement();
			if (shouldNotifyValidation(next)) {
				validationStepToNotify++;
			}
		}

		getPropertyChangeSupport().firePropertyChange(VALIDATION_START, object, validationStepToNotify);

		// Perform the validation
		for (Enumeration<Validable> en = objectsToValidate.elements(); en.hasMoreElements();) {
			Validable next = en.nextElement();
			if (shouldNotifyValidation(next)) {
				getPropertyChangeSupport().firePropertyChange(VALIDATION_OBJECT, null, next);
			}

			if (!next.isDeleted()) {
				addedIssues += performValidation(next, report);
			}

		}

		// Notify validation is finished
		getPropertyChangeSupport().firePropertyChange(VALIDATION_END, null, object);

		return addedIssues == 0;
	}

	public <V extends Validable> ValidationRuleSet<? super V> getRuleSet(V validable) {
		return (ValidationRuleSet<? super V>) getRuleSet(validable.getClass());
	}

	public <V extends Validable> ValidationRuleSet<? super V> getRuleSet(Class<V> validableClass) {
		return (ValidationRuleSet<? super V>) TypeUtils.objectForClass(validableClass, ruleSets, false);
	}

	private <V extends Validable> int performValidation(V validable, ValidationReport report) {
		int addedIssues = 0;

		ValidationRuleSet<? super V> ruleSet = getRuleSet(validable);

		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Validating " + validable.toString() + " " + validable.toString());
		}

		if (shouldNotifyValidationRules()) {
			getPropertyChangeSupport().firePropertyChange(OBJECT_VALIDATION_START, 0, ruleSet.getSize());
		}

		for (int i = 0; i < ruleSet.getSize(); i++) {
			ValidationRule<?, ? super V> rule = ruleSet.getElementAt(i);
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Applying rule " + rule.getLocalizedName());
			}

			// System.out.println("--> Applying rule " + rule.getLocalizedName() + " for " + validable);

			if (shouldNotifyValidationRules()) {
				getPropertyChangeSupport().firePropertyChange(VALIDATE_WITH_RULE, null, rule);
			}

			if (performRuleValidation((ValidationRule) rule, validable, report)) {
				addedIssues++;
			}
		}

		return addedIssues;
	}

	private <R extends ValidationRule<R, V>, V extends Validable> boolean performRuleValidation(R rule, V next, ValidationReport report) {
		ValidationIssue<R, V> issue = null;
		try {
			issue = rule.getIsEnabled() ? rule.applyValidation(next) : null;
		} catch (Exception e) {
			logger.warning("Exception occured during validation: " + e.getMessage() + " object was " + next + " deleted="
					+ next.isDeleted());
			e.printStackTrace();
		}
		if (issue != null) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Adding issue " + issue);
			}
			issue.setCause(rule);
			report.addToValidationIssues(issue);
			if (fixAutomaticallyIfOneFixProposal()) {
				if (issue instanceof ProblemIssue && ((ProblemIssue<R, V>) issue).getSize() == 1) {
					if (logger.isLoggable(Level.INFO)) {
						logger.info("Fixing automatically...");
					}
					((ProblemIssue<R, V>) issue).getElementAt(0).apply(false);
					report.addToValidationIssues(new InformationIssue<R, V>(next, FlexoLocalization.localizedForKey("fixed_automatically:")
							+ " " + issue.getLocalizedMessage() + " : "
							+ ((ProblemIssue<R, V>) issue).getElementAt(0).getLocalizedMessage(), false));
				} else if (issue instanceof CompoundIssue) {
					for (ValidationIssue<R, V> containedIssue : ((CompoundIssue<R, V>) issue).getContainedIssues()) {
						if (containedIssue instanceof ProblemIssue && containedIssue.getSize() == 1) {
							report.addToValidationIssues(containedIssue);
							if (logger.isLoggable(Level.INFO)) {
								logger.info("Fixing automatically...");
							}
							((ProblemIssue<R, V>) containedIssue).getElementAt(0).apply(false);
							report.addToValidationIssues(new InformationIssue<R, V>(containedIssue.getObject(), FlexoLocalization
									.localizedForKey("fixed_automatically:")
									+ " "
									+ containedIssue.getLocalizedMessage()
									+ " : "
									+ ((ProblemIssue<R, V>) containedIssue).getElementAt(0).getLocalizedMessage(), false));
						}
					}
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Return a boolean indicating if validation of supplied object must be notified
	 * 
	 * @param next
	 * @return a boolean
	 */
	protected abstract boolean shouldNotifyValidation(Validable next);

	/**
	 * Return a boolean indicating if validation of each rule must be notified
	 * 
	 * @param next
	 * @return a boolean
	 */
	protected boolean shouldNotifyValidationRules() {
		return false;
	}

	public abstract boolean fixAutomaticallyIfOneFixProposal();

	private List<Class<?>> sortedClasses;

	public List<Class<?>> getSortedClasses() {
		if (sortedClasses == null) {
			sortedClasses = new ArrayList<Class<?>>();
			sortedClasses.addAll(ruleSets.keySet());
			Collections.sort(sortedClasses, new ClassComparator());
		}
		return sortedClasses;
	}

	private ValidationRuleFilter _filter = null;

	public void update(ValidationRuleFilter filter) {
		_filter = filter;
	}

	/**
	 * Implements
	 * 
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		return ruleSets.size();
	}

	/**
	 * Implements
	 * 
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public ValidationRuleSet<?> getElementAt(int index) {
		if (index >= 0 && index < getSortedClasses().size()) {
			return ruleSets.get(getSortedClasses().get(index));
		}
		return null;
	}

	private class ClassComparator implements Comparator<Class> {
		private final Collator collator;

		ClassComparator() {
			collator = Collator.getInstance();
		}

		@Override
		public int compare(Class o1, Class o2) {
			String className1 = null;
			String className2 = null;
			StringTokenizer st1 = new StringTokenizer(o1.getName(), ".");
			while (st1.hasMoreTokens()) {
				className1 = st1.nextToken();
			}
			StringTokenizer st2 = new StringTokenizer(o2.getName(), ".");
			while (st2.hasMoreTokens()) {
				className2 = st2.nextToken();
			}
			if (className1 != null && className2 != null) {
				return collator.compare(className1, className2);
			}
			return 0;
		}

	}

}
