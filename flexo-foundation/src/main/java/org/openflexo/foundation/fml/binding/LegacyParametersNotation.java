/**
 *
 * Copyright (c) 2014-2026, Openflexo
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

package org.openflexo.foundation.fml.binding;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParametersValuesType;

/**
 * The legacy notation of the arguments of a behaviour, <code>parameters.aValue</code>, and its rewriting in the current one,
 * <code>aValue</code>.<br>
 * The rewriting is decided binding by binding: a name is rewritten only when it designates an argument of the behaviour AND the short name,
 * resolved where the binding is, designates that same argument (no local variable hides it). Text of string literals is never touched.
 * 
 * @author sylvain
 */
public class LegacyParametersNotation {

	// A literal (skipped), or parameters.<name> not preceded by a name or a dot
	private static final Pattern PATTERN = Pattern
			.compile("\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|(?<![\\w.$])parameters\\.([A-Za-z_]\\w*)");

	private LegacyParametersNotation() {
	}

	/**
	 * Return the behaviour whose arguments are visible in supplied binding model through the <code>parameters</code> variable, or null
	 */
	public static FlexoBehaviour getBehaviour(BindingModel bindingModel) {
		if (bindingModel == null) {
			return null;
		}
		BindingVariable parameters = bindingModel.bindingVariableNamed(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY);
		if (parameters != null && parameters.getType() instanceof FlexoBehaviourParametersValuesType) {
			return ((FlexoBehaviourParametersValuesType) parameters.getType()).getFlexoBehaviour();
		}
		return null;
	}

	/**
	 * Return the text of supplied binding written in the current notation, or null when there is nothing to rewrite: the binding does not
	 * use the legacy notation, or every use of it must stay (the name is hidden by a local variable, or is not an argument)
	 */
	public static String migratedText(DataBinding<?> binding) {
		if (binding == null || !binding.isSet() || binding.getOwner() == null) {
			return null;
		}
		Bindable owner = binding.getOwner();
		BindingModel bindingModel = owner.getBindingModel();
		FlexoBehaviour behaviour = getBehaviour(bindingModel);
		if (behaviour == null) {
			return null;
		}
		String text = binding.getPrettyPrint();
		if (text == null || !text.contains(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY + ".")) {
			return null;
		}
		Matcher m = PATTERN.matcher(text);
		StringBuffer sb = new StringBuffer();
		boolean changed = false;
		while (m.find()) {
			String name = m.group(1);
			String replacement = m.group();
			if (name != null && behaviour.getParameter(name) != null) {
				BindingVariable shortName = bindingModel.bindingVariableNamed(name);
				// A name that is also a property of the concept is not rewritten: the behaviour may assign that property (label =
				// parameters.label), which the short name would turn into a self-assignment. The migration tool handles that case.
				boolean hidesProperty = behaviour.getFlexoConcept() != null && behaviour.getFlexoConcept().getAccessibleProperty(name) != null;
				if (!hidesProperty && shortName instanceof FlexoBehaviourArgumentBindingVariable
						&& ((FlexoBehaviourArgumentBindingVariable) shortName).getArgument() == behaviour.getParameter(name)) {
					replacement = name;
					changed = true;
				}
			}
			m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		m.appendTail(sb);
		return changed ? sb.toString() : null;
	}

	/**
	 * Tell if supplied text (a binding, or a whole FML text) uses the legacy notation outside of a string literal
	 */
	public static boolean containsLegacyNotation(String text) {
		if (text == null || !text.contains(FlexoBehaviourBindingModel.PARAMETERS_PROPERTY + ".")) {
			return false;
		}
		Matcher m = PATTERN.matcher(text);
		while (m.find()) {
			if (m.group(1) != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return true when supplied binding uses the legacy notation for an argument that can be written in the current one
	 */
	public static boolean usesLegacyNotation(DataBinding<?> binding) {
		return migratedText(binding) != null;
	}

}
