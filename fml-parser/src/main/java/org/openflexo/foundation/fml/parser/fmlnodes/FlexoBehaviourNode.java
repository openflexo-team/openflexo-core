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

package org.openflexo.foundation.fml.parser.fmlnodes;

import java.util.logging.Logger;

import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.md.FMLMetaData;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.node.AAnonymousConstructorBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.AAnonymousDestructorBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.ABlock;
import org.openflexo.foundation.fml.parser.node.ABlockFlexoBehaviourBody;
import org.openflexo.foundation.fml.parser.node.AEmptyFlexoBehaviourBody;
import org.openflexo.foundation.fml.parser.node.AFmlBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.AFmlFullyQualifiedBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.AMethodBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.ANamedConstructorBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.ANamedDestructorBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PFlexoBehaviourBody;
import org.openflexo.foundation.fml.parser.node.PType;
import org.openflexo.foundation.fml.parser.node.PVisibility;
import org.openflexo.foundation.fml.parser.node.TLidentifier;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.p2pp.RawSource.RawSourcePosition;

/**
 * @author sylvain
 * 
 */
public abstract class FlexoBehaviourNode<N extends Node, T extends FlexoBehaviour>
		extends FMLObjectNode<N, T, FMLCompilationUnitSemanticsAnalyzer> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FlexoBehaviourNode.class.getPackage().getName());

	// private ControlGraphFactory controlGraphFactory;

	public FlexoBehaviourNode(N astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public FlexoBehaviourNode(T property, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(property, analyzer);
		// controlGraphFactory = new ControlGraphFactory(null, analyzer);
	}

	/*@Override
	public ControlGraphFactory getControlGraphFactory() {
		if (controlGraphFactory == null) {
			controlGraphFactory = new ControlGraphFactory(getFlexoBehaviourBody(getASTNode()), getanalyzer());
		}
		return controlGraphFactory;
	}*/

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);
		append(childrenContents("", () -> getModelObject().getMetaData(), LINE_SEPARATOR, Indentation.DoNotIndent, FMLMetaData.class));
	}

	@Override
	public FlexoBehaviourNode<N, T> deserialize() {
		if (getParent() instanceof VirtualModelNode) {
			((VirtualModelNode) getParent()).getModelObject().addToFlexoBehaviours(getModelObject());
		}
		if (getParent() instanceof FlexoConceptNode) {
			((FlexoConceptNode) getParent()).getModelObject().addToFlexoBehaviours(getModelObject());
		}
		if (getParent() instanceof FlexoEventNode) {
			((FlexoEventNode) getParent()).getModelObject().addToFlexoBehaviours(getModelObject());
		}
		// getModelObject().getLabel();
		return this;
	}

	public PFlexoBehaviourBody getFlexoBehaviourBody(N astNode) {
		if (astNode instanceof AAnonymousConstructorBehaviourDecl) {
			return ((AAnonymousConstructorBehaviourDecl) astNode).getFlexoBehaviourBody();
		}
		if (astNode instanceof ANamedConstructorBehaviourDecl) {
			return ((ANamedConstructorBehaviourDecl) astNode).getFlexoBehaviourBody();
		}
		if (astNode instanceof AAnonymousDestructorBehaviourDecl) {
			return ((AAnonymousDestructorBehaviourDecl) astNode).getFlexoBehaviourBody();
		}
		if (astNode instanceof ANamedDestructorBehaviourDecl) {
			return ((ANamedDestructorBehaviourDecl) astNode).getFlexoBehaviourBody();
		}
		if (astNode instanceof AMethodBehaviourDecl) {
			return ((AMethodBehaviourDecl) astNode).getFlexoBehaviourBody();
		}
		if (astNode instanceof AFmlBehaviourDecl) {
			return ((AFmlBehaviourDecl) astNode).getFlexoBehaviourBody();
		}
		if (astNode instanceof AFmlFullyQualifiedBehaviourDecl) {
			return ((AFmlFullyQualifiedBehaviourDecl) astNode).getFlexoBehaviourBody();
		}
		return null;
	}

	/*protected void handleParameters(PBehaviourDecl behaviourDecl) {
		if (behaviourDecl instanceof AAnonymousConstructorBehaviourDecl) {
			handleParameters(((AAnonymousConstructorBehaviourDecl) behaviourDecl).getFormalArgumentsList());
		}
		else if (behaviourDecl instanceof AAnonymousDestructorBehaviourDecl) {
			handleParameters(((AAnonymousDestructorBehaviourDecl) behaviourDecl).getFormalArgumentsList());
		}
		else if (behaviourDecl instanceof AFmlBehaviourDecl) {
			handleParameters(((AFmlBehaviourDecl) behaviourDecl).getFormalArgumentsList());
		}
		else if (behaviourDecl instanceof AFmlFullyQualifiedBehaviourDecl) {
			handleParameters(((AFmlFullyQualifiedBehaviourDecl) behaviourDecl).getFormalArgumentsList());
		}
	else if(behaviourDecl instanceof AMethodBehaviourDecl)
	
	{
		handleParameters(((AMethodBehaviourDecl) behaviourDecl).getFormalArgumentsList());
	}else if(behaviourDecl instanceof ANamedConstructorBehaviourDecl)
	{
		handleParameters(((ANamedConstructorBehaviourDecl) behaviourDecl).getFormalArgumentsList());
	}else if(behaviourDecl instanceof ANamedDestructorBehaviourDecl)
	{
		handleParameters(((ANamedDestructorBehaviourDecl) behaviourDecl).getFormalArgumentsList());
	}
	}
	
	private void handleParameters(PFormalArgumentsList parametersList) {
		if (parametersList instanceof AManyFormalArgumentsList) {
			AManyFormalArgumentsList l = (AManyFormalArgumentsList) parametersList;
			handleParameter(l.getFormalArgument());
			handleParameters(l.getFormalArgumentsList());
		}
		else if (parametersList instanceof AOneFormalArgumentsList) {
			handleParameter(((AOneFormalArgumentsList) parametersList).getFormalArgument());
		}
	}
	
	private void handleParameter(PFormalArgument parameter) {
		System.out.println("On gere le parametre " + parameter);
		System.out.println("On gere rien en fait");
	
		BehaviourParameterNode paramNode = new BehaviourParameterNode(parameter, getanalyzer());
	}*/

	protected boolean hasNoImplementation() {
		if (getModelObject() != null) {
			return getModelObject().getControlGraph() == null;
		}
		if (getASTNode() != null) {
			return (getFlexoBehaviourBody(getASTNode()) instanceof AEmptyFlexoBehaviourBody);
		}
		/*else {
			return getModelObject().isAbstract();
		}*/
		return true;
	}

	/**
	 * Return Semi fragment (non-null only if behaviour body is not defined)
	 * 
	 * @return
	 */
	protected RawSourceFragment getSemiFragment() {
		if (getFlexoBehaviourBody(getASTNode()) instanceof AEmptyFlexoBehaviourBody) {
			return getFragment(((AEmptyFlexoBehaviourBody) getFlexoBehaviourBody(getASTNode())).getSemi());
		}
		return null;
	}

	/**
	 * Return LBrc fragment (non-null only if not simple semi)
	 * 
	 * @return
	 */
	protected RawSourceFragment getLBrcFragment() {
		if (getFlexoBehaviourBody(getASTNode()) instanceof ABlockFlexoBehaviourBody) {
			return getFragment(((ABlock) ((ABlockFlexoBehaviourBody) getFlexoBehaviourBody(getASTNode())).getBlock()).getLBrc());
		}
		return null;
	}

	/**
	 * Return LBrc fragment (non-null only if not simple semi)
	 * 
	 * @return
	 */
	protected RawSourceFragment getRBrcFragment() {
		if (getFlexoBehaviourBody(getASTNode()) instanceof ABlockFlexoBehaviourBody) {
			return getFragment(((ABlock) ((ABlockFlexoBehaviourBody) getFlexoBehaviourBody(getASTNode())).getBlock()).getRBrc());
		}
		return null;
	}

	protected PVisibility getVisibility() {
		if (getASTNode() instanceof AAnonymousConstructorBehaviourDecl) {
			return ((AAnonymousConstructorBehaviourDecl) getASTNode()).getVisibility();
		}
		if (getASTNode() instanceof ANamedConstructorBehaviourDecl) {
			return ((ANamedConstructorBehaviourDecl) getASTNode()).getVisibility();
		}
		if (getASTNode() instanceof AAnonymousDestructorBehaviourDecl) {
			return ((AAnonymousDestructorBehaviourDecl) getASTNode()).getVisibility();
		}
		if (getASTNode() instanceof ANamedDestructorBehaviourDecl) {
			return ((ANamedDestructorBehaviourDecl) getASTNode()).getVisibility();
		}
		if (getASTNode() instanceof AMethodBehaviourDecl) {
			return ((AMethodBehaviourDecl) getASTNode()).getVisibility();
		}
		if (getASTNode() instanceof AFmlBehaviourDecl) {
			return ((AFmlBehaviourDecl) getASTNode()).getVisibility();
		}
		if (getASTNode() instanceof AFmlFullyQualifiedBehaviourDecl) {
			return ((AFmlFullyQualifiedBehaviourDecl) getASTNode()).getVisibility();
		}
		return null;
	}

	protected RawSourceFragment getVisibilityFragment() {
		if (getVisibility() != null) {
			return getFragment(getVisibility());
		}
		return null;
	}

	protected TLidentifier getName() {
		if (getASTNode() instanceof ANamedConstructorBehaviourDecl) {
			return ((ANamedConstructorBehaviourDecl) getASTNode()).getName();
		}
		if (getASTNode() instanceof ANamedDestructorBehaviourDecl) {
			return ((ANamedDestructorBehaviourDecl) getASTNode()).getName();
		}
		if (getASTNode() instanceof AMethodBehaviourDecl) {
			return ((AMethodBehaviourDecl) getASTNode()).getName();
		}
		if (getASTNode() instanceof AFmlBehaviourDecl) {
			return ((AFmlBehaviourDecl) getASTNode()).getName();
		}
		if (getASTNode() instanceof AFmlFullyQualifiedBehaviourDecl) {
			return ((AFmlFullyQualifiedBehaviourDecl) getASTNode()).getName();
		}
		return null;
	}

	protected RawSourceFragment getNameFragment() {
		if (getName() != null) {
			return getFragment(getName());
		}
		return null;
	}

	protected PType getType() {
		if (getASTNode() instanceof AMethodBehaviourDecl) {
			return ((AMethodBehaviourDecl) getASTNode()).getType();
		}
		if (getASTNode() instanceof AFmlBehaviourDecl) {
			return ((AFmlBehaviourDecl) getASTNode()).getType();
		}
		if (getASTNode() instanceof AFmlFullyQualifiedBehaviourDecl) {
			return ((AFmlFullyQualifiedBehaviourDecl) getASTNode()).getType();
		}
		return null;
	}

	protected RawSourceFragment getTypeFragment() {
		if (getName() != null) {
			return getFragment(getType());
		}
		return null;
	}

	protected RawSourceFragment getLParFragment() {
		if (getASTNode() instanceof AAnonymousConstructorBehaviourDecl) {
			return getFragment(((AAnonymousConstructorBehaviourDecl) getASTNode()).getLPar());
		}
		if (getASTNode() instanceof ANamedConstructorBehaviourDecl) {
			return getFragment(((ANamedConstructorBehaviourDecl) getASTNode()).getLPar());
		}
		if (getASTNode() instanceof AAnonymousDestructorBehaviourDecl) {
			return getFragment(((AAnonymousDestructorBehaviourDecl) getASTNode()).getLPar());
		}
		if (getASTNode() instanceof ANamedDestructorBehaviourDecl) {
			return getFragment(((ANamedDestructorBehaviourDecl) getASTNode()).getLPar());
		}
		if (getASTNode() instanceof AMethodBehaviourDecl) {
			return getFragment(((AMethodBehaviourDecl) getASTNode()).getLPar());
		}
		if (getASTNode() instanceof AFmlBehaviourDecl) {
			return getFragment(((AFmlBehaviourDecl) getASTNode()).getLPar());
		}
		if (getASTNode() instanceof AFmlFullyQualifiedBehaviourDecl) {
			return getFragment(((AFmlFullyQualifiedBehaviourDecl) getASTNode()).getLPar());
		}
		return null;
	}

	protected RawSourceFragment getRParFragment() {
		if (getASTNode() instanceof AAnonymousConstructorBehaviourDecl) {
			return getFragment(((AAnonymousConstructorBehaviourDecl) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof ANamedConstructorBehaviourDecl) {
			return getFragment(((ANamedConstructorBehaviourDecl) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof AAnonymousDestructorBehaviourDecl) {
			return getFragment(((AAnonymousDestructorBehaviourDecl) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof ANamedDestructorBehaviourDecl) {
			return getFragment(((ANamedDestructorBehaviourDecl) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof AMethodBehaviourDecl) {
			return getFragment(((AMethodBehaviourDecl) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof AFmlBehaviourDecl) {
			return getFragment(((AFmlBehaviourDecl) getASTNode()).getRPar());
		}
		if (getASTNode() instanceof AFmlFullyQualifiedBehaviourDecl) {
			return getFragment(((AFmlFullyQualifiedBehaviourDecl) getASTNode()).getRPar());
		}
		return null;
	}

	/*@Override
	public void addToChildren(P2PPNode<?, ?> child) {
		if ((child instanceof FMLSimplePropertyValueNode) && child.getASTNode() != null && ((((FMLSimplePropertyValueNode<?, ?>) child)
				.getASTNode().getArgName().getText().contains("extendParentBoundsToHostThisShape")))) {
			System.out.println("C'est la que y'a un probleme");
	
			System.out.println("ASTNode: " + getASTNode());
			System.out.println(((Node) child.getASTNode()).parent());
	
			Thread.dumpStack();
		}
		super.addToChildren(child);
	}*/

	public RawSourcePosition getLBrcPosition() {
		if (getLBrcFragment() != null) {
			return getLBrcFragment().getStartPosition();
		}
		return null;
	}

	public RawSourcePosition getRBrcPosition() {
		if (getRBrcFragment() != null) {
			return getRBrcFragment().getStartPosition();
		}
		return null;
	}
}
