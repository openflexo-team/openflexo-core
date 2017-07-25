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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.editionaction.AbstractAssignationAction;
import org.openflexo.foundation.fml.inspector.FlexoConceptInspector;
import org.openflexo.model.validation.InformationIssue;
import org.openflexo.model.validation.Validable;
import org.openflexo.model.validation.ValidationError;
import org.openflexo.model.validation.ValidationIssue;
import org.openflexo.model.validation.ValidationModel;
import org.openflexo.model.validation.ValidationReport;
import org.openflexo.model.validation.ValidationWarning;

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

	private VirtualModel virtualModel;
	private Map<FMLObject, ValidationReport> embeddedValidationReports = new HashMap<>();

	public FMLValidationReport(ValidationModel validationModel, VirtualModel virtualModel) throws InterruptedException {
		super(validationModel, virtualModel);
		this.virtualModel = virtualModel;
		for (FlexoProperty<?> property : virtualModel.getFlexoProperties()) {
			embeddedValidationReports.put(property, new ValidationReport(validationModel, property));
		}
		for (FlexoBehaviour behaviour : virtualModel.getFlexoBehaviours()) {
			embeddedValidationReports.put(behaviour, new ValidationReport(validationModel, behaviour));
		}
		embeddedValidationReports.put(virtualModel.getInspector(), new ValidationReport(validationModel, virtualModel.getInspector()));
		for (FlexoConcept concept : virtualModel.getFlexoConcepts()) {
			embeddedValidationReports.put(concept, new ValidationReport(validationModel, concept));
			for (FlexoProperty<?> property : concept.getFlexoProperties()) {
				embeddedValidationReports.put(property, new ValidationReport(validationModel, property));
			}
			for (FlexoBehaviour behaviour : concept.getFlexoBehaviours()) {
				embeddedValidationReports.put(behaviour, new ValidationReport(validationModel, behaviour));
			}
			embeddedValidationReports.put(concept.getInspector(), new ValidationReport(validationModel, concept.getInspector()));
		}
	}

	public ValidationReport getValidationReport(FMLObject object) {
		if (object == virtualModel) {
			return this;
		}
		return embeddedValidationReports.get(object);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Collection<ValidationIssue<?, ?>> issuesRegarding(Validable object) {
		if (object instanceof FlexoFacet) {
			return Collections.emptyList();
		}
		if (object instanceof VirtualModel && getValidationReport((VirtualModel) object) != null) {
			return (Collection) getValidationReport((VirtualModel) object).getFilteredIssues();
		}
		if (object instanceof FlexoConcept && getValidationReport((FlexoConcept) object) != null) {
			return (Collection) getValidationReport((FlexoConcept) object).getFilteredIssues();
		}
		if (object instanceof FlexoProperty && getValidationReport((FlexoProperty) object) != null) {
			return (Collection) getValidationReport((FlexoProperty) object).getFilteredIssues();
		}
		if (object instanceof FlexoBehaviour && getValidationReport((FlexoBehaviour) object) != null) {
			return (Collection) getValidationReport((FlexoBehaviour) object).getFilteredIssues();
		}
		if (object instanceof FlexoConceptInspector && getValidationReport((FlexoConceptInspector) object) != null) {
			return (Collection) getValidationReport((FlexoConceptInspector) object).getFilteredIssues();
		}
		else if (object instanceof FlexoConceptObject && getValidationReport(((FlexoConceptObject) object).getFlexoConcept()) != null) {
			return getValidationReport(((FlexoConceptObject) object).getFlexoConcept()).issuesRegarding(object);
		}

		return super.issuesRegarding(object);
	}

	public boolean hasErrors(FMLObject object) {
		List<ValidationError<?, ?>> errors = getErrors(object);
		if (errors.size() == 0 && object instanceof AbstractAssignationAction) {
			errors = getErrors(((AbstractAssignationAction<?>) object).getAssignableAction());
			return errors.size() > 0;
		}
		return errors.size() > 0;
	}

	public boolean hasWarnings(FMLObject object) {
		List<ValidationWarning<?, ?>> warnings = getWarnings(object);
		if (warnings.size() == 0 && object instanceof AbstractAssignationAction) {
			warnings = getWarnings(((AbstractAssignationAction<?>) object).getAssignableAction());
			return warnings.size() > 0;
		}
		return warnings.size() > 0;
	}

	public boolean hasInfoIssues(FMLObject object) {
		List<InformationIssue<?, ?>> infos = getInformationIssues(object);
		if (infos.size() == 0 && object instanceof AbstractAssignationAction) {
			infos = getInformationIssues(((AbstractAssignationAction<?>) object).getAssignableAction());
			return infos.size() > 0;
		}
		return infos.size() > 0;
	}

	public List<ValidationError<?, ?>> getErrors(FMLObject object) {
		if (object instanceof FlexoFacet) {
			return Collections.emptyList();
		}
		if (object instanceof VirtualModel && getValidationReport(object) != null) {
			return getValidationReport(object).getErrors();
		}
		if (object instanceof FlexoConcept && getValidationReport(object) != null) {
			return getValidationReport(object).getErrors();
		}
		if (object instanceof FlexoProperty && getValidationReport(object) != null) {
			return getValidationReport(object).getErrors();
		}
		if (object instanceof FlexoBehaviour && getValidationReport(object) != null) {
			return getValidationReport(object).getErrors();
		}
		if (object instanceof FlexoConceptInspector && getValidationReport(object) != null) {
			return getValidationReport(object).getErrors();
		}
		else if (object instanceof FlexoConceptObject) {
			if (getValidationReport(((FlexoConceptObject) object).getFlexoConcept()) != null) {
				return getValidationReport(((FlexoConceptObject) object).getFlexoConcept()).errorIssuesRegarding(object);
			}
			else {
				logger.warning("pas de validation report pour " + ((FlexoConceptObject) object).getFlexoConcept() + " de: " + object);
			}
		}

		logger.warning("Unexpected validable " + object);
		return Collections.emptyList();
	}

	public List<ValidationWarning<?, ?>> getWarnings(FMLObject object) {
		if (object instanceof FlexoFacet) {
			return Collections.emptyList();
		}
		if (object instanceof VirtualModel && getValidationReport(object) != null) {
			return getValidationReport(object).getWarnings();
		}
		if (object instanceof FlexoConcept && getValidationReport(object) != null) {
			return getValidationReport(object).getWarnings();
		}
		if (object instanceof FlexoProperty && getValidationReport(object) != null) {
			return getValidationReport(object).getWarnings();
		}
		if (object instanceof FlexoBehaviour && getValidationReport(object) != null) {
			return getValidationReport(object).getWarnings();
		}
		if (object instanceof FlexoConceptInspector && getValidationReport(object) != null) {
			return getValidationReport(object).getWarnings();
		}
		else if (object instanceof FlexoConceptObject && getValidationReport(((FlexoConceptObject) object).getFlexoConcept()) != null) {
			return getValidationReport(((FlexoConceptObject) object).getFlexoConcept()).warningIssuesRegarding(object);
		}

		logger.warning("Unexpected validable " + object);
		return Collections.emptyList();
	}

	public List<InformationIssue<?, ?>> getInformationIssues(FMLObject object) {
		if (object instanceof FlexoFacet) {
			return Collections.emptyList();
		}
		if (object instanceof VirtualModel && getValidationReport(object) != null) {
			return getValidationReport(object).getInfoIssues();
		}
		if (object instanceof FlexoConcept && getValidationReport(object) != null) {
			return getValidationReport(object).getInfoIssues();
		}
		if (object instanceof FlexoProperty && getValidationReport(object) != null) {
			return getValidationReport(object).getInfoIssues();
		}
		if (object instanceof FlexoBehaviour && getValidationReport(object) != null) {
			return getValidationReport(object).getInfoIssues();
		}
		if (object instanceof FlexoConceptInspector && getValidationReport(object) != null) {
			return getValidationReport(object).getInfoIssues();
		}
		else if (object instanceof FlexoConceptObject && getValidationReport(((FlexoConceptObject) object).getFlexoConcept()) != null) {
			return getValidationReport(((FlexoConceptObject) object).getFlexoConcept()).infoIssuesRegarding(object);
		}

		logger.warning("Unexpected validable " + object);
		return Collections.emptyList();
	}

	@Override
	public void revalidate(Validable validable) {
		System.out.println("On revalide " + validable + " pour le VM " + virtualModel);
		super.revalidate(validable);
	}
}
