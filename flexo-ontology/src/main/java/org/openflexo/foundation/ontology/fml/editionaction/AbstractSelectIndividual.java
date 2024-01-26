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

package org.openflexo.foundation.ontology.fml.editionaction;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.annotations.FMLAttribute;
import org.openflexo.foundation.fml.editionaction.AbstractFetchRequest;
import org.openflexo.foundation.ontology.IFlexoOntology;
import org.openflexo.foundation.ontology.IFlexoOntologyClass;
import org.openflexo.foundation.ontology.IFlexoOntologyIndividual;
import org.openflexo.foundation.ontology.IndividualOfClass;
import org.openflexo.foundation.technologyadapter.FlexoMetaModel;
import org.openflexo.foundation.technologyadapter.FlexoModel;
import org.openflexo.foundation.technologyadapter.TechnologyObject;
import org.openflexo.foundation.technologyadapter.TypeAwareModelSlot;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.toolbox.StringUtils;

/**
 * Generic {@link AbstractFetchRequest} allowing to retrieve a selection of some individuals matching some conditions and a given type.<br>
 * This action is technology-specific and must be redefined in a given technology
 * 
 * @author sylvain
 * 
 * @param <M>
 * @param <MM>
 * @param <T>
 */
@ModelEntity(isAbstract = true)
@ImplementationClass(AbstractSelectIndividual.AbstractSelectIndividualImpl.class)
public abstract interface AbstractSelectIndividual<MS extends TypeAwareModelSlot<M, ?>, M extends FlexoModel<M, ?> & TechnologyObject<?>, T extends IFlexoOntologyIndividual, AT>
		extends AbstractFetchRequest<MS, M, T, AT> {

	@PropertyIdentifier(type = String.class)
	public static final String ONTOLOGY_CLASS_URI_KEY = "ontologyClassURI";

	@Getter(value = ONTOLOGY_CLASS_URI_KEY)
	@XMLAttribute
	@FMLAttribute(value = ONTOLOGY_CLASS_URI_KEY, required = false, description = "<html>reference of the class</html>")
	public String _getOntologyClassURI();

	@Setter(ONTOLOGY_CLASS_URI_KEY)
	public void _setOntologyClassURI(String ontologyClassURI);

	public IFlexoOntologyClass getType();

	public void setType(IFlexoOntologyClass ontologyClass);

	public FlexoMetaModel getMetaModelData();

	public IFlexoOntology<?> getMetaModelAsOntology();

	public static abstract class AbstractSelectIndividualImpl<MS extends TypeAwareModelSlot<M, ?>, M extends FlexoModel<M, ?> & TechnologyObject<?>, T extends IFlexoOntologyIndividual, AT>
			extends AbstractFetchRequestImpl<MS, M, T, AT> implements AbstractSelectIndividual<MS, M, T, AT> {

		protected static final Logger logger = FlexoLogger.getLogger(AbstractSelectIndividual.class.getPackage().getName());

		private IFlexoOntologyClass type;

		@Deprecated
		private String typeURI = null;

		public AbstractSelectIndividualImpl() {
			super();
		}

		/*@Override
		public String getFMLRepresentation(FMLRepresentationContext context) {
			FMLRepresentationOutput out = new FMLRepresentationOutput(context);
			if (getAssignation().isSet()) {
				out.append(getAssignation().toString() + " = (", context);
			}
			out.append(getImplementedInterface().getSimpleName() + (getModelSlot() != null ? " from " + getModelSlot().getName() : " ")
					+ (getType() != null ? " as " + getType().getName() : "")
					+ (getConditions().size() > 0 ? " " + getWhereClausesFMLRepresentation(context) : ""), context);
			if (getAssignation().isSet()) {
				out.append(")", context);
			}
			return out.toString();
		}*/

		@Override
		public Type getFetchedType() {
			Type returned = (Type) performSuperGetter(FETCHED_TYPE_KEY);
			if (returned == null) {
				return IndividualOfClass.getIndividualOfClass(getType());
			}
			return returned;
		}

		@Override
		public IFlexoOntologyClass getType() {

			if (type != null) {
				return type;
			}

			if (StringUtils.isNotEmpty(typeURI) && getInferedModelSlot() != null && getInferedModelSlot().getMetaModelResource() != null
					&& getInferedModelSlot().getMetaModelResource().getMetaModelData() != null) {
				return (IFlexoOntologyClass) getInferedModelSlot().getMetaModelResource().getMetaModelData().getObject(typeURI);
			}
			/*System.out.println("Pas trouve " + typeURI + " getModelSlot()=" + getInferedModelSlot());
			if (getInferedModelSlot() != null) {
				System.out.println("Pas trouve, getModelSlot().getMetaModelResource()=" + getInferedModelSlot().getMetaModelResource());
				if (getInferedModelSlot().getMetaModelResource() != null) {
					System.out.println("Pas trouve, mmData=" + getInferedModelSlot().getMetaModelResource().getMetaModelData());
				}
			}*/
			return null;
		}

		@Override
		public void setType(IFlexoOntologyClass ontologyClass) {
			this.type = ontologyClass;
			if (ontologyClass != null) {
				typeURI = ontologyClass.getURI();
			}
			else {
				typeURI = null;
			}
		}

		@Override
		public FlexoMetaModel getMetaModelData() {
			/*if (StringUtils.isNotEmpty(typeURI) && getModelSlot() != null && getModelSlot().getMetaModelResource() != null
					&& getModelSlot().getMetaModelResource().getMetaModelData() != null) {
				return getModelSlot().getMetaModelResource().getMetaModelData();
			}*/
			if (getInferedModelSlot() != null && getInferedModelSlot().getMetaModelResource() != null
					&& getInferedModelSlot().getMetaModelResource().getMetaModelData() != null) {
				return getInferedModelSlot().getMetaModelResource().getMetaModelData();
			}
			return null;
		}

		@Override
		public IFlexoOntology<?> getMetaModelAsOntology() {
			if (getMetaModelData() instanceof IFlexoOntology) {
				return (IFlexoOntology<?>) getMetaModelData();
			}
			return null;
		}

		@Override
		public String _getOntologyClassURI() {
			if (getType() != null) {
				return getType().getURI();
			}
			return typeURI;
		}

		@Override
		public void _setOntologyClassURI(String ontologyClassURI) {
			this.typeURI = ontologyClassURI;
		}

	}
}
