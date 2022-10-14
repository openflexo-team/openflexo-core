/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.fml;

import java.util.Collection;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.openflexo.connie.DataBinding;
import org.openflexo.foundation.fml.FMLObject.BindingIsRequiredAndMustBeValid;
import org.openflexo.foundation.fml.FMLPrettyPrintDelegate.FragmentContext;
import org.openflexo.foundation.fml.editionaction.AbstractAssignationAction;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.pamela.validation.InformationIssue;
import org.openflexo.pamela.validation.ValidationError;
import org.openflexo.pamela.validation.ValidationIssue;
import org.openflexo.pamela.validation.ValidationModel;
import org.openflexo.pamela.validation.ValidationReport;
import org.openflexo.pamela.validation.ValidationWarning;

/**
 * This is the {@link ValidationReport} for a {@link VirtualModel}
 * 
 * We maintain here a collection of {@link ValidationReport} dedicated to each object found in the {@link VirtualModel}
 * <ul>
 * <li>for each {@link FlexoConcept} found
 * <li>
 * <li>for each {@link FlexoProperty} found
 * <li>
 * <li>for each {@link FlexoBehaviour} found
 * <li>
 * <li>for each {@link FlexoConceptInspector} found
 * <li>
 * </ul>
 * 
 * @author sylvain
 * 
 */
public class FMLValidationReport extends ValidationReport {

	private static final Logger logger = Logger.getLogger(FMLValidationReport.class.getPackage().getName());

	private FMLCompilationUnit compilationUnit;

	public FMLValidationReport(ValidationModel validationModel, FMLCompilationUnit compilationUnit) {
		super(validationModel, compilationUnit);
		this.compilationUnit = compilationUnit;
	}

	public FMLCompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public boolean hasErrors(FMLObject object) {
		Collection<ValidationError<?, ? super FMLObject>> errors = getErrors(object);
		if (errors.size() == 0 && object instanceof AbstractAssignationAction) {
			errors = getErrors(((AbstractAssignationAction<?>) object).getAssignableAction());
			return errors.size() > 0;
		}
		return errors.size() > 0;
	}

	public boolean hasWarnings(FMLObject object) {
		Collection<ValidationWarning<?, ? super FMLObject>> warnings = getWarnings(object);
		if (warnings.size() == 0 && object instanceof AbstractAssignationAction) {
			warnings = getWarnings(((AbstractAssignationAction<?>) object).getAssignableAction());
			return warnings.size() > 0;
		}
		return warnings.size() > 0;
	}

	public boolean hasInfoIssues(FMLObject object) {
		Collection<InformationIssue<?, ? super FMLObject>> infos = getInformationIssues(object);
		if (infos.size() == 0 && object instanceof AbstractAssignationAction) {
			infos = getInformationIssues(((AbstractAssignationAction<?>) object).getAssignableAction());
			return infos.size() > 0;
		}
		return infos.size() > 0;
	}

	public Collection<ValidationError<?, ? super FMLObject>> getErrors(FMLObject object) {
		return errorIssuesRegarding(object);
	}

	public Collection<ValidationWarning<?, ? super FMLObject>> getWarnings(FMLObject object) {
		return warningIssuesRegarding(object);
	}

	public Collection<InformationIssue<?, ? super FMLObject>> getInformationIssues(FMLObject object) {
		return infoIssuesRegarding(object);
	}

	// private long startTime;
	// private long intermediateTime;
	// private long endTime;

	private static <C extends FMLObject> void reanalyzeBinding(ValidationIssue<? extends BindingIsRequiredAndMustBeValid<C>, C> issue) {
		DataBinding<?> db = issue.getCause().getBinding(issue.getValidable());
		db.revalidate();
	}

	@Override
	public void revalidate() throws InterruptedException {

		// lineNumbers.clear();
		for (ValidationIssue issue : getAllIssues()) {
			if (issue.getCause() instanceof BindingIsRequiredAndMustBeValid) {
				reanalyzeBinding(issue);
			}
		}

		super.revalidate();
	}

	public void appendSemanticAnalysisIssue(SemanticAnalysisIssue<?, ?> semanticsIssue) {
		ValidationNode<?> validationNode = getValidationNode(semanticsIssue.getValidable());
		if (validationNode != null) {
			validationNode.addToValidationIssues((SemanticAnalysisIssue) semanticsIssue);
		}
		else {
			getRootNode().addToValidationIssues((SemanticAnalysisIssue) semanticsIssue);
		}
		notifyChange();
	}

	public void appendValidationError(ValidationError error, int line) {
		getRootNode().addToValidationIssues(error);
		// setLineNumber(parseError, line);
		notifyChange();
	}

	public void removeValidationError(ValidationError error) {
		if (error instanceof SemanticAnalysisIssue) {
			ValidationNode<?> validationNode = getValidationNode(((SemanticAnalysisIssue) error).getValidable());
			if (validationNode != null) {
				validationNode.removeFromValidationIssues(error);
				notifyChange();
				return;
			}
		}
		getRootNode().removeFromValidationIssues(error);
		notifyChange();
	}

	// private Map<ValidationIssue<?, ?>, Integer> lineNumbers = new HashMap<>();

	public int getLineNumber(ValidationIssue<?, ?> issue) {
		if (issue instanceof ParseError) {
			return ((ParseError) issue).getLine();
		}
		if (issue instanceof SemanticAnalysisIssue) {
			return ((SemanticAnalysisIssue) issue).getLine();
		}
		RawSourceFragment fragment = getFragment(issue);
		if (fragment != null) {
			return getFragment(issue).getStartPosition().getLine();
		}
		/*Integer returned = lineNumbers.get(issue);
		if (returned != null) {
			return returned;
		}*/
		return -1;
	}

	/*public void setLineNumber(ValidationIssue<?, ?> issue, Integer line) {
		lineNumbers.put(issue, line);
	}*/

	public RawSourceFragment getFragment(ValidationIssue<?, ?> issue) {
		if (issue != null && issue.getCause() != null && issue.getValidable() instanceof FMLPrettyPrintable) {
			if (StringUtils.isNotEmpty(issue.getCause().getFragmentContext())) {
				FragmentContext context = FragmentContext.valueOf(issue.getCause().getFragmentContext());
				if (context != null) {
					return ((FMLPrettyPrintable) issue.getValidable()).getPrettyPrintDelegate().getFragment(context);
				}
			}
			return ((FMLPrettyPrintable) issue.getValidable()).getPrettyPrintDelegate().getFragment();
		}
		return null;
	}

}
