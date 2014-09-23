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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.utils.FlexoListModel;
import org.openflexo.foundation.validation.annotations.DefineValidationRule;
import org.openflexo.toolbox.ChainedCollection;

/**
 * This is the set of rules beeing applicable to any instance of V class<br>
 * Inheritance is managed here
 * 
 * @author sylvain
 * 
 * @param <V>
 *            type of Validable
 */
@SuppressWarnings("serial")
public class ValidationRuleSet<V extends Validable> extends FlexoListModel {

	private static final Logger logger = Logger.getLogger(ValidationRuleSet.class.getPackage().getName());

	public static final String DELETED_PROPERTY = "deleted";

	private final Class<V> declaredType;

	/**
	 * This is the list of explicitely declared ValidationRule associated to declared type (not inherited one).
	 */
	private final List<ValidationRule<?, V>> declaredRules;

	private final List<ValidationRuleSet<? super V>> parentRuleSets;

	private ChainedCollection<ValidationRule<?, ? super V>> allRules;

	public ValidationRuleSet(Class<V> type) {
		super();
		declaredType = type;
		declaredRules = new ArrayList<ValidationRule<?, V>>();
		parentRuleSets = new ArrayList<ValidationRuleSet<? super V>>();

		for (Class<?> c : type.getDeclaredClasses()) {
			DefineValidationRule annotation = c.getAnnotation(DefineValidationRule.class);
			if (annotation != null && ValidationRule.class.isAssignableFrom(c)) {
				try {
					@SuppressWarnings("unchecked")
					Class<? extends ValidationRule<?, V>> validationRuleClass = (Class<? extends ValidationRule<?, V>>) c;
					// System.out.println("Found validation rule: " + validationRuleClass);
					Constructor<? extends ValidationRule<?, V>> constructor;
					constructor = validationRuleClass.getConstructor();
					ValidationRule<?, V> rule = constructor.newInstance();
					declaredRules.add(rule);
				} catch (Exception e) {
					e.printStackTrace();
					logger.warning("Unexpected exception: " + e.getMessage() + " Cannot add rule " + c);
				}
			}
		}
	}

	public List<ValidationRuleSet<? super V>> getParentRuleSets() {
		return parentRuleSets;
	}

	protected void addParentRuleSet(ValidationRuleSet<? super V> parentRuleSet) {
		parentRuleSets.add(parentRuleSet);
		getPropertyChangeSupport().firePropertyChange("size", getSize() - parentRuleSet.getSize(), getSize());
	}

	@Override
	public String getDeletedProperty() {
		return DELETED_PROPERTY;
	}

	public void delete() {
		declaredRules.clear();
		parentRuleSets.clear();
		if (getPropertyChangeSupport() != null) {
			getPropertyChangeSupport().firePropertyChange(DELETED_PROPERTY, this, null);
		}
	}

	/**
	 * Return declared rules for specified type<br>
	 * Does not return inherited rules
	 * 
	 * @return
	 */
	public List<ValidationRule<?, V>> getDeclaredRules() {
		return declaredRules;
	}

	/**
	 * Build and return a collection of all rules for specified type<br>
	 * Does return inherited rules (This method is really costfull and should not be called in a performance context, use
	 * getSize()/getElementAt(int) instead)
	 * 
	 * @return
	 */
	public List<ValidationRule<?, ? super V>> getRules() {
		List<ValidationRule<?, ? super V>> returned = new ArrayList<ValidationRule<?, ? super V>>();
		for (int i = 0; i < getSize(); i++) {
			returned.add(getElementAt(i));
		}
		return returned;
	}

	public boolean containsRuleClass(Class<? extends ValidationRule<?, ?>> ruleClass) {
		for (int i = 0; i < getSize(); i++) {
			if (getElementAt(i).getClass().equals(ruleClass)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Implements
	 * 
	 * @see javax.swing.ListModel#getSize()
	 */
	@Override
	public int getSize() {
		int returned = getDeclaredRules().size();
		for (ValidationRuleSet<?> parentRuleSet : parentRuleSets) {
			returned += parentRuleSet.getSize();
		}
		return returned;
	}

	/**
	 * Implements
	 * 
	 * @see javax.swing.ListModel#getElementAt(int)
	 * @see javax.swing.ListModel#getElementAt(int)
	 */
	@Override
	public ValidationRule<?, ? super V> getElementAt(int index) {
		if (index < 0) {
			return null;
		}
		if (index < getDeclaredRules().size()) {
			return getDeclaredRules().get(index);
		}
		int localIndex = index - getDeclaredRules().size();
		for (ValidationRuleSet<? super V> parentRuleSet : parentRuleSets) {
			if (localIndex < parentRuleSet.getSize()) {
				return parentRuleSet.getElementAt(localIndex);
			}
			localIndex = localIndex - parentRuleSet.getSize();
		}

		logger.warning("Could not find ValidationRule at index " + index);
		return null;
	}

	public Class<V> getDeclaredType() {
		return declaredType;
	}

	public String getTypeName() {
		if (getDeclaredType() != null) {
			return getDeclaredType().getSimpleName();
		}
		return null;
	}

}
