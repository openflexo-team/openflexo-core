/**
 * 
 * Copyright (c) 2019, Openflexo
 * 
 * This file is part of FML-parser, a component of the software infrastructure 
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

package org.openflexo.foundation.fml.parser.fmlnodes.expr;

import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.binding.CreationSchemePathElement;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedFmlParameters;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedNewInstance;
import org.openflexo.foundation.fml.parser.node.APreciseFmlParametersClause;
import org.openflexo.foundation.fml.parser.node.ASimpleNewInstance;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PFmlParameters;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * An abstract PathElement representing a new FlexoConcept or VirtualModel instance
 * 
 * Handle both {@link ASimpleNewInstance} or {@link AFullQualifiedNewInstance}
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractAddFlexoConceptInstanceNode extends AbstractCallBindingPathElementNode<Node, CreationSchemePathElement<?>> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractAddFlexoConceptInstanceNode.class.getPackage().getName());

	private IBindingPathElement parent;

	public AbstractAddFlexoConceptInstanceNode(ASimpleNewInstance astNode, FMLSemanticsAnalyzer analyzer, IBindingPathElement parent,
			Bindable bindable) {
		super(astNode, analyzer, bindable);
		this.parent = parent;
		setReadyToBuildModelObject(true);
		// buildModelObjectFromAST() was already called, but too early (parent not yet set)
		// we do it again
		modelObject = buildModelObjectFromAST(astNode);
	}

	public AbstractAddFlexoConceptInstanceNode(AFullQualifiedNewInstance astNode, FMLSemanticsAnalyzer analyzer, IBindingPathElement parent,
			Bindable bindable) {
		super(astNode, analyzer, bindable);
		this.parent = parent;
		setReadyToBuildModelObject(true);
		// buildModelObjectFromAST() was already called, but too early (parent not yet set)
		// we do it again
		modelObject = buildModelObjectFromAST(astNode);
	}

	public AbstractAddFlexoConceptInstanceNode(CreationSchemePathElement<?> bindingPathElement, FMLSemanticsAnalyzer analyzer,
			Bindable bindable) {
		super(bindingPathElement, analyzer, bindable);
	}

	public IBindingPathElement getParentPathElement() {
		return parent;
	}

	@Override
	public void finalizeDeserialization() {
		super.finalizeDeserialization();
	}

	protected boolean isFullQualified() {
		FlexoConceptInstanceType type = getModelObject().getType();
		if (type.getFlexoConcept() != null) {
			return type.getFlexoConcept().getCreationSchemes().size() > 1;
		}
		/*if (getModelObject() instanceof NewFlexoConceptInstanceBindingPathElement) {
			FlexoConcept flexoConcept = ((NewFlexoConceptInstanceBindingPathElement) getModelObject()).getFlexoConcept();
			return flexoConcept.getCreationSchemes().size() > 1;
		}
		if (getModelObject() instanceof NewVirtualModelInstanceBindingPathElement) {
			FlexoConcept virtualModel = ((NewVirtualModelInstanceBindingPathElement) getModelObject()).getVirtualModel();
			return virtualModel.getCreationSchemes().size() > 1;
		}*/
		if (getASTNode() instanceof AFullQualifiedNewInstance) {
			return true;
		}
		return false;
	}

	/*protected boolean hasFMLProperties() {
	if (getFMLParametersClause() != null) {
		return true;
	}
	if (getModelObject() != null) {
		return getModelObject().hasFMLProperties(getFactory());
	}
	return false;
	}
	 
	 
	 private boolean isContainerFullQualified() {
		if (getModelObject() != null) {
			return StringUtils.isNotEmpty(getModelObject().getContainer().toString())
					&& !FlexoConceptBindingModel.THIS_PROPERTY.equals(getModelObject().getContainer().toString());
		}
		else {
			return getContainmentClause() != null;
		}
	}
	
	private ANewContainmentClause getContainmentClause() {
		if (getASTNode() instanceof AFmlInstanceCreationFmlActionExp) {
			return (ANewContainmentClause) ((AFmlInstanceCreationFmlActionExp) getASTNode()).getNewContainmentClause();
		}
		if (getASTNode() instanceof AJavaInstanceCreationFmlActionExp) {
			return (ANewContainmentClause) ((AJavaInstanceCreationFmlActionExp) getASTNode()).getNewContainmentClause();
		}
		return null;
	}
	
	private RawSourceFragment getContainerFragment() {
		if (getContainmentClause() != null) {
			return getFragment(getContainmentClause().getCompositeIdent());
		}
		return null;
	}
	
	private RawSourceFragment getContainerDotFragment() {
		if (getContainmentClause() != null) {
			return getFragment(getContainmentClause().getDot());
		}
		return null;
	}*/

	protected RawSourceFragment getNewFragment() {
		if (getASTNode() instanceof ASimpleNewInstance) {
			return getFragment(((ASimpleNewInstance) getASTNode()).getKwNew());
		}
		if (getASTNode() instanceof AFullQualifiedNewInstance) {
			return getFragment(((AFullQualifiedNewInstance) getASTNode()).getKwNew());
		}
		return null;
	}

	protected String getConceptName() {
		if (getASTNode() instanceof ASimpleNewInstance) {
			return getText(((ASimpleNewInstance) getASTNode()).getType());
		}
		if (getASTNode() instanceof AFullQualifiedNewInstance) {
			return ((AFullQualifiedNewInstance) getASTNode()).getConceptName().getText();
		}
		return null;
	}

	protected Node getConceptNameNode() {
		if (getASTNode() instanceof ASimpleNewInstance) {
			return ((ASimpleNewInstance) getASTNode()).getType();
		}
		if (getASTNode() instanceof AFullQualifiedNewInstance) {
			return ((AFullQualifiedNewInstance) getASTNode()).getConceptName();
		}
		return null;
	}

	protected RawSourceFragment getConceptNameFragment() {
		return getFragment(getConceptNameNode());
	}

	protected RawSourceFragment getColonColonFragment() {
		if (getASTNode() instanceof AFullQualifiedNewInstance) {
			return getFragment(((AFullQualifiedNewInstance) getASTNode()).getColonColon());
		}
		return null;
	}

	protected RawSourceFragment getConstructorNameFragment() {
		if (getASTNode() instanceof AFullQualifiedNewInstance) {
			return getFragment(((AFullQualifiedNewInstance) getASTNode()).getConstructorName());
		}
		return null;
	}

	protected RawSourceFragment getLParFragment() {
		if (getASTNode() instanceof ASimpleNewInstance) {
			return getFragment(((ASimpleNewInstance) getASTNode()).getLPar());
		}
		if (getASTNode() instanceof AFullQualifiedNewInstance) {
			return getFragment(((AFullQualifiedNewInstance) getASTNode()).getLPar());
		}
		return null;
	}

	protected RawSourceFragment getRParFragment() {
		if (getASTNode() instanceof ASimpleNewInstance) {
			return getFragment(((ASimpleNewInstance) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof AFullQualifiedNewInstance) {
			return getFragment(((AFullQualifiedNewInstance) getASTNode()).getRPar());
		}
		return null;
	}

	protected RawSourceFragment getArgsFragment() {
		if (getASTNode() instanceof ASimpleNewInstance) {
			return getFragment(((ASimpleNewInstance) getASTNode()).getLPar(), ((ASimpleNewInstance) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof AFullQualifiedNewInstance) {
			return getFragment(((AFullQualifiedNewInstance) getASTNode()).getLPar(), ((AFullQualifiedNewInstance) getASTNode()).getRPar());
		}
		return null;
	}

	protected RawSourceFragment getTypeFragment() {
		if (getASTNode() instanceof ASimpleNewInstance) {
			return getFragment(((ASimpleNewInstance) getASTNode()).getType());
		}
		return null;
	}

	protected APreciseFmlParametersClause getFMLParametersClause() {
		if (getASTNode() instanceof ASimpleNewInstance) {
			return (APreciseFmlParametersClause) ((ASimpleNewInstance) getASTNode()).getPreciseFmlParametersClause();
		}
		if (getASTNode() instanceof AFullQualifiedNewInstance) {
			return (APreciseFmlParametersClause) ((AFullQualifiedNewInstance) getASTNode()).getPreciseFmlParametersClause();
		}
		return null;
	}

	protected RawSourceFragment getFMLParametersWithFragment() {
		if (getFMLParametersClause() != null) {
			return getFragment(getFMLParametersClause().getKwWith());

		}
		return null;
	}

	protected RawSourceFragment getFMLParametersLParFragment() {
		if (getFMLParametersClause() != null) {
			return getFragment(((AFullQualifiedFmlParameters) getFMLParametersClause().getFmlParameters()).getLPar());

		}
		return null;
	}

	protected RawSourceFragment getFMLParametersRParFragment() {
		if (getFMLParametersClause() != null) {
			return getFragment(((AFullQualifiedFmlParameters) getFMLParametersClause().getFmlParameters()).getRPar());

		}
		return null;
	}

	protected PFmlParameters getFMLParameters() {
		if (getFMLParametersClause() != null) {
			return getFMLParametersClause().getFmlParameters();
		}
		return null;
	}

	protected RawSourceFragment getFMLParametersFragment() {
		if (getFMLParameters() != null) {
			return getFragment(getFMLParameters());
		}
		return null;
	}

}
