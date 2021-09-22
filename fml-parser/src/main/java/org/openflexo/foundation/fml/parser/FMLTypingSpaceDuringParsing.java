/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
 * 
 * This file is part of Connie-core, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.parser;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.UnresolvedType;
import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoConceptInstanceType.DefaultFlexoConceptInstanceTypeFactory;
import org.openflexo.foundation.fml.FlexoConceptInstanceType.FlexoConceptInstanceTypeFactory;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.VirtualModelInstanceType.DefaultVirtualModelInstanceTypeFactory;
import org.openflexo.foundation.fml.VirtualModelInstanceType.VirtualModelInstanceTypeFactory;
import org.openflexo.foundation.fml.parser.analysis.DepthFirstAdapter;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoConceptNode;
import org.openflexo.foundation.fml.parser.fmlnodes.VirtualModelNode;
import org.openflexo.foundation.fml.parser.node.AConceptDecl;
import org.openflexo.foundation.fml.parser.node.AModelDecl;
import org.openflexo.logging.FlexoLogger;

/**
 * FML typing space, related to a {@link FMLCompilationUnit} beeing parsed
 * 
 * Pre-analyze {@link FMLCompilationUnit} to find references to {@link VirtualModel}s and {@link FlexoConcept}s.
 * 
 * Support import of VirtualModels
 * 
 * @author sylvain
 *
 */
public class FMLTypingSpaceDuringParsing extends AbstractFMLTypingSpace {

	protected static final Logger logger = FlexoLogger.getLogger(FMLTypingSpaceDuringParsing.class.getPackage().getName());

	private final MainSemanticsAnalyzer analyser;

	private FlexoConceptInstanceTypeFactory FLEXO_CONCEPT_INSTANCE_TYPE_FACTORY;
	private VirtualModelInstanceTypeFactory<VirtualModelInstanceType> VIRTUAL_MODEL_INSTANCE_TYPE_FACTORY;

	private Map<String, AModelDecl> foundVirtualModels;
	private Map<String, AConceptDecl> foundConcepts;
	private List<CustomType> unresolvedTypes;

	public FMLTypingSpaceDuringParsing(MainSemanticsAnalyzer analyser) {
		this.analyser = analyser;
		unresolvedTypes = new ArrayList<>();
		FLEXO_CONCEPT_INSTANCE_TYPE_FACTORY = new DefaultFlexoConceptInstanceTypeFactory(getFMLTechnologyAdapter()) {
			// TODO : handle concepts found in imported VirtualModel
			@Override
			public FlexoConcept resolveFlexoConcept(FlexoConceptInstanceType typeToResolve) {
				// System.out.println("Resolving FlexoConcept " + typeToResolve);
				AConceptDecl node = foundConcepts.get(typeToResolve.getConceptURI());
				if (node != null) {
					FlexoConceptNode conceptNode = (FlexoConceptNode) analyser.getFMLNode(node);
					if (conceptNode != null) {
						return conceptNode.getModelObject();
					}
				}
				logger.warning("Cannot lookup concept " + typeToResolve.getConceptURI());
				return null;
			}
		};
		VIRTUAL_MODEL_INSTANCE_TYPE_FACTORY = new DefaultVirtualModelInstanceTypeFactory(getFMLTechnologyAdapter()) {
			@Override
			public VirtualModel resolveVirtualModel(VirtualModelInstanceType typeToResolve) {
				// System.out.println("Resolving FlexoConcept " + typeToResolve);
				AModelDecl node = foundVirtualModels.get(typeToResolve.getConceptURI());
				if (node != null) {
					VirtualModelNode virtualModelNode = (VirtualModelNode) analyser.getFMLNode(node);
					if (virtualModelNode != null) {
						return virtualModelNode.getModelObject();
					}
				}
				logger.warning("Cannot lookup virtual model " + typeToResolve.getConceptURI());
				return null;
			}
		};

		foundVirtualModels = new HashMap<>();
		foundConcepts = new HashMap<>();
		new ConceptTypesExplorer();
	}

	public MainSemanticsAnalyzer getAnalyser() {
		return analyser;
	}

	public FMLTechnologyAdapter getFMLTechnologyAdapter() {
		if (analyser.getServiceManager() != null) {
			return analyser.getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		}
		return null;
	}

	/**
	 * Return boolean indicating if supplied {@link Type} is actually in current typing space
	 * 
	 * @param type
	 * @return
	 */
	@Override
	public boolean isTypeImported(Type type) {
		return super.isTypeImported(type);
	}

	/**
	 * Import supplied type in this typing space
	 * 
	 * @param type
	 */
	@Override
	public void importType(Type type) {
		super.importType(type);
	}

	/**
	 * Resolve {@link Type} according to current typing space using supplied type {@link String} representation
	 * 
	 * @param typeAsString
	 * @return
	 */
	@Override
	public Type resolveType(String typeAsString) {
		Type returned = super.resolveType(typeAsString);
		if (returned instanceof UnresolvedType) {
			// Try to match a VirtualModel
			if (foundVirtualModels.get(typeAsString) != null) {
				VirtualModelInstanceType vmiType = new VirtualModelInstanceType(typeAsString, VIRTUAL_MODEL_INSTANCE_TYPE_FACTORY);
				unresolvedTypes.add(vmiType);
				return vmiType;
			}
			else if (foundConcepts.get(typeAsString) != null) {
				FlexoConceptInstanceType fciType = new FlexoConceptInstanceType(typeAsString, FLEXO_CONCEPT_INSTANCE_TYPE_FACTORY);
				unresolvedTypes.add(fciType);
				return fciType;
			}
		}
		return returned;
	}

	public List<CustomType> getUnresolvedTypes() {
		return unresolvedTypes;
	}

	public void resolveUnresovedTypes() {

		for (CustomType unresolvedType : new ArrayList<>(unresolvedTypes)) {

			unresolvedType.resolve();
			if (unresolvedType.isResolved()) {
				unresolvedTypes.remove(unresolvedType);
			}
			/*System.out.println("resolved: " + unresolvedType.isResolved());
			if (unresolvedType instanceof FlexoConceptInstanceType) {
				System.out.println("concept: " + ((FlexoConceptInstanceType) unresolvedType).getFlexoConcept());
			}*/
		}

		// System.out.println("Done");
	}

	@Override
	public String toString() {
		return "FMLTypingSpaceDuringParsing";
	}

	private class ConceptTypesExplorer extends DepthFirstAdapter {

		public ConceptTypesExplorer() {
			super();
			analyser.getRootNode().apply(this);
		}

		@Override
		public void inAModelDecl(AModelDecl node) {
			super.inAModelDecl(node);
			System.out.println("Trouve le VirtualModel " + node.getUidentifier());
			foundVirtualModels.put(node.getUidentifier().getText(), node);
		}

		@Override
		public void outAModelDecl(AModelDecl node) {
			super.outAModelDecl(node);
		}

		@Override
		public void inAConceptDecl(AConceptDecl node) {
			super.inAConceptDecl(node);
			System.out.println("Trouve le FlexoConcept " + node.getUidentifier());
			foundConcepts.put(node.getUidentifier().getText(), node);
		}

		@Override
		public void outAConceptDecl(AConceptDecl node) {
			super.outAConceptDecl(node);
		}

	}

}
