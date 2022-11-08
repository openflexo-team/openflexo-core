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

package org.openflexo.foundation.fml.parser;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.expr.Constant;
import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.AbstractProperty;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.ElementImportDeclaration;
import org.openflexo.foundation.fml.ExpressionProperty;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLInstancePropertyValue;
import org.openflexo.foundation.fml.FMLInstancesListPropertyValue;
import org.openflexo.foundation.fml.FMLModelContext.FMLProperty;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLPrettyPrintDelegate;
import org.openflexo.foundation.fml.FMLSimplePropertyValue;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoEnum;
import org.openflexo.foundation.fml.FlexoEnumValue;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.GetSetProperty;
import org.openflexo.foundation.fml.JavaImportDeclaration;
import org.openflexo.foundation.fml.JavaRole;
import org.openflexo.foundation.fml.NamespaceDeclaration;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.PropertyCardinality;
import org.openflexo.foundation.fml.SemanticAnalysisIssue;
import org.openflexo.foundation.fml.UseModelSlotDeclaration;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.Visibility;
import org.openflexo.foundation.fml.WrappedFMLObject;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.EmptyControlGraph;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.editionaction.AbstractFetchRequest;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.editionaction.LogAction;
import org.openflexo.foundation.fml.editionaction.ReturnStatement;
import org.openflexo.foundation.fml.editionaction.TechnologySpecificAction;
import org.openflexo.foundation.fml.md.BasicMetaData;
import org.openflexo.foundation.fml.md.ListMetaData;
import org.openflexo.foundation.fml.md.MetaDataKeyValue;
import org.openflexo.foundation.fml.md.MultiValuedMetaData;
import org.openflexo.foundation.fml.md.SingleMetaData;
import org.openflexo.foundation.fml.parser.fmlnodes.AbstractPropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.ActionSchemeNode;
import org.openflexo.foundation.fml.parser.fmlnodes.BasicMetaDataNode;
import org.openflexo.foundation.fml.parser.fmlnodes.BehaviourParameterNode;
import org.openflexo.foundation.fml.parser.fmlnodes.CreationSchemeNode;
import org.openflexo.foundation.fml.parser.fmlnodes.DeletionSchemeNode;
import org.openflexo.foundation.fml.parser.fmlnodes.ElementImportNode;
import org.openflexo.foundation.fml.parser.fmlnodes.ExpressionPropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLBehaviourNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLInstancePropertyValueNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLInstancesListPropertyValueNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLSimplePropertyValueNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoConceptNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoEnumNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoEnumValueNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoRolePropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.GetSetPropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.JavaImportNode;
import org.openflexo.foundation.fml.parser.fmlnodes.JavaRoleNode;
import org.openflexo.foundation.fml.parser.fmlnodes.ListMetaDataNode;
import org.openflexo.foundation.fml.parser.fmlnodes.MetaDataKeyValueNode;
import org.openflexo.foundation.fml.parser.fmlnodes.ModelSlotPropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.MultiValuedMetaDataNode;
import org.openflexo.foundation.fml.parser.fmlnodes.NamespaceDeclarationNode;
import org.openflexo.foundation.fml.parser.fmlnodes.PrimitiveRoleNode;
import org.openflexo.foundation.fml.parser.fmlnodes.SingleMetaDataNode;
import org.openflexo.foundation.fml.parser.fmlnodes.UseDeclarationNode;
import org.openflexo.foundation.fml.parser.fmlnodes.VirtualModelNode;
import org.openflexo.foundation.fml.parser.fmlnodes.WrappedFMLObjectNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.AssignationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.BeginMatchActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.BehaviourCallArgumentNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ConditionalNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.DeclarationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.EmptyControlGraphNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.EndMatchActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ExpressionActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.FMLEditionActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.FetchRequestNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.IterationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.LogActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.MatchActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ReturnStatementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.SequenceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.DataBindingNode;
import org.openflexo.foundation.fml.parser.node.ACharacterLiteral;
import org.openflexo.foundation.fml.parser.node.AFalseLiteral;
import org.openflexo.foundation.fml.parser.node.AFloatingPointLiteral;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedFmlParameters;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedQualifiedInstance;
import org.openflexo.foundation.fml.parser.node.AIdentifierVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.AInitializerExpressionVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.AInitializerFmlActionVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.AInstanceQualifiedArgument;
import org.openflexo.foundation.fml.parser.node.AIntegerLiteral;
import org.openflexo.foundation.fml.parser.node.AListInstancesQualifiedArgument;
import org.openflexo.foundation.fml.parser.node.AManyQualifiedArgumentList;
import org.openflexo.foundation.fml.parser.node.AMultiple1Cardinality;
import org.openflexo.foundation.fml.parser.node.AMultiple2Cardinality;
import org.openflexo.foundation.fml.parser.node.ANullLiteral;
import org.openflexo.foundation.fml.parser.node.AOneQualifiedArgumentList;
import org.openflexo.foundation.fml.parser.node.APrivateVisibility;
import org.openflexo.foundation.fml.parser.node.AProtectedVisibility;
import org.openflexo.foundation.fml.parser.node.APublicVisibility;
import org.openflexo.foundation.fml.parser.node.ASimpleQualifiedArgument;
import org.openflexo.foundation.fml.parser.node.ASimpleQualifiedInstance;
import org.openflexo.foundation.fml.parser.node.AStringLiteral;
import org.openflexo.foundation.fml.parser.node.ATrueLiteral;
import org.openflexo.foundation.fml.parser.node.AWithExplicitBoundsCardinality;
import org.openflexo.foundation.fml.parser.node.AWithLowerBoundsCardinality;
import org.openflexo.foundation.fml.parser.node.AWithUpperBoundsCardinality;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PAnnotationTag;
import org.openflexo.foundation.fml.parser.node.PCardinality;
import org.openflexo.foundation.fml.parser.node.PCompositeIdent;
import org.openflexo.foundation.fml.parser.node.PCompositeTident;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.parser.node.PFmlActionExp;
import org.openflexo.foundation.fml.parser.node.PFmlParameters;
import org.openflexo.foundation.fml.parser.node.PIdentifierPrefix;
import org.openflexo.foundation.fml.parser.node.PLiteral;
import org.openflexo.foundation.fml.parser.node.PQualifiedArgument;
import org.openflexo.foundation.fml.parser.node.PQualifiedArgumentList;
import org.openflexo.foundation.fml.parser.node.PQualifiedInstance;
import org.openflexo.foundation.fml.parser.node.PVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.PVisibility;
import org.openflexo.foundation.fml.parser.node.TCidentifier;
import org.openflexo.foundation.fml.parser.node.TLidentifier;
import org.openflexo.foundation.fml.parser.node.TLitInteger;
import org.openflexo.foundation.fml.parser.node.TUidentifier;
import org.openflexo.foundation.fml.parser.node.Token;
import org.openflexo.foundation.fml.rm.CompilationUnitResourceFactory;
import org.openflexo.foundation.fml.rt.editionaction.BehaviourCallArgument;
import org.openflexo.foundation.fml.rt.editionaction.FinalizeMatching;
import org.openflexo.foundation.fml.rt.editionaction.InitiateMatching;
import org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.p2pp.P2PPNode;
import org.openflexo.p2pp.PrettyPrintContext;
import org.openflexo.p2pp.RawSource;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.p2pp.RawSource.RawSourcePosition;
import org.openflexo.toolbox.ChainedCollection;
import org.openflexo.toolbox.StringUtils;

/**
 * Maintains consistency between the model (represented by an {@link FMLObject}) and source code represented in FML language
 * 
 * Works
 * 
 * @author sylvain
 * 
 */
public abstract class ObjectNode<N extends Node, T, A extends FMLSemanticsAnalyzer> extends P2PPNode<N, T>
		implements FMLPrettyPrintDelegate<T> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ObjectNode.class.getPackage().getName());

	private final FMLSemanticsAnalyzer semanticsAnalyzer;

	public ObjectNode(N astNode, FMLSemanticsAnalyzer analyzer) {
		super(null, astNode, analyzer != null ? analyzer.getFragmentManager() : null);
		this.semanticsAnalyzer = analyzer;

		/*if (analyzer == null) {
			System.out.println("Tiens qui me cree sans analyzer ???");
			Thread.dumpStack();
			System.exit(-1);
		}*/

		modelObject = buildModelObjectFromAST(astNode);
	}

	public ObjectNode(T aFMLObject, FMLSemanticsAnalyzer analyzer) {
		super(aFMLObject, null, null);
		this.semanticsAnalyzer = analyzer;

		/*if (analyzer == null) {
			System.out.println("Tiens qui me cree sans analyzer ???");
			Thread.dumpStack();
			System.exit(-1);
		}*/
	}

	public FMLModelFactory getFactory() {
		return semanticsAnalyzer.getModelFactory();
	}

	/**
	 * Return fragment matching AST node
	 * 
	 * @return
	 */
	@Override
	public RawSourceFragment getFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode());
		}
		return null;
	}

	@Override
	public RawSourcePosition getStartLocation() {
		RawSourceFragment fragment = getFragment();
		if (fragment != null) {
			return fragment.getStartPosition();
		}
		return null;
	}

	@Override
	public RawSourcePosition getEndLocation() {
		RawSourceFragment fragment = getFragment();
		if (fragment != null) {
			return fragment.getEndPosition();
		}
		return null;
	}

	public FMLSemanticsAnalyzer getSemanticsAnalyzer() {
		return semanticsAnalyzer;
	}

	/*public TypeFactory getTypeFactory() {
		return getanalyzer().getTypeFactory();
	}*/

	public FMLFactory getFMLFactory() {
		if (getSemanticsAnalyzer() instanceof FMLCompilationUnitSemanticsAnalyzer) {
			return ((FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer()).getFMLFactory();
		}
		return null;
	}

	protected FMLCompilationUnit getCompilationUnit() {
		return getSemanticsAnalyzer().getCompilationUnit();
	}

	// Make this method visible
	@Override
	public void addToChildren(P2PPNode<?, ?> child) {
		super.addToChildren(child);
	}

	// Make this method visible
	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);
	}

	/*protected void handleToken(Token token) {
	
		// System.out.println("Receiving Token " + token.getLine() + ":" + token.getPos() + ":" + token.getText() + " tokenEnd=" + tokenEnd
		// + " endPosition=" + endPosition);
	
		RawSourcePosition tokenStart = getRawSource().makePositionBeforeChar(token.getLine(), token.getPos());
		RawSourcePosition tokenEnd = getRawSource().makePositionBeforeChar(token.getLine(), token.getPos() + token.getText().length());
	
		if (startPosition == null || tokenStart.compareTo(startPosition) < 0) {
			startPosition = tokenStart;
			parsedFragment = null;
		}
		if (endPosition == null || tokenEnd.compareTo(endPosition) > 0) {
			endPosition = tokenEnd;
			parsedFragment = null;
		}
	
		if (getParent() instanceof FMLObjectNode) {
			((FMLObjectNode<?, ?>) getParent()).handleToken(token);
		}
	}*/

	/**
	 * Return original version of last serialized raw source, FOR THE ENTIRE compilation unit
	 * 
	 * @return
	 */
	@Override
	public RawSource getRawSource() {
		return getSemanticsAnalyzer().getRawSource();
	}

	@Override
	public String getRepresentation(PrettyPrintContext context) {
		return getTextualRepresentation(context);
	}

	@Override
	public String getNormalizedRepresentation(PrettyPrintContext context) {
		return getNormalizedTextualRepresentation(context);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <C> P2PPNode<?, C> makeObjectNode(C object) {
		if (getSemanticsAnalyzer() instanceof FMLCompilationUnitSemanticsAnalyzer) {
			if (object instanceof NamespaceDeclaration) {
				return (P2PPNode<?, C>) new NamespaceDeclarationNode((NamespaceDeclaration) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof UseModelSlotDeclaration) {
				return (P2PPNode<?, C>) new UseDeclarationNode((UseModelSlotDeclaration) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof JavaImportDeclaration) {
				return (P2PPNode<?, C>) new JavaImportNode((JavaImportDeclaration) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof ElementImportDeclaration) {
				return (P2PPNode<?, C>) new ElementImportNode((ElementImportDeclaration) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof BasicMetaData) {
				return (P2PPNode<?, C>) new BasicMetaDataNode((BasicMetaData) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof SingleMetaData) {
				return (P2PPNode<?, C>) new SingleMetaDataNode((SingleMetaData) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof MultiValuedMetaData) {
				return (P2PPNode<?, C>) new MultiValuedMetaDataNode((MultiValuedMetaData) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof MetaDataKeyValue) {
				return (P2PPNode<?, C>) new MetaDataKeyValueNode((MetaDataKeyValue) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof ListMetaData) {
				return (P2PPNode<?, C>) new ListMetaDataNode((ListMetaData) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof VirtualModel) {
				return (P2PPNode<?, C>) new VirtualModelNode((VirtualModel) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof FlexoEnumValue) {
				return (P2PPNode<?, C>) new FlexoEnumValueNode((FlexoEnumValue) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof FlexoEnum) {
				return (P2PPNode<?, C>) new FlexoEnumNode((FlexoEnum) object, (FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof FlexoConcept) {
				return (P2PPNode<?, C>) new FlexoConceptNode((FlexoConcept) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof SingleMetaData) {
				return (P2PPNode<?, C>) new SingleMetaDataNode((SingleMetaData) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof PrimitiveRole) {
				return (P2PPNode<?, C>) new PrimitiveRoleNode((PrimitiveRole) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof JavaRole) {
				return (P2PPNode<?, C>) new JavaRoleNode((JavaRole) object, (FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof AbstractProperty) {
				return (P2PPNode<?, C>) new AbstractPropertyNode((AbstractProperty) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof ExpressionProperty) {
				return (P2PPNode<?, C>) new ExpressionPropertyNode((ExpressionProperty) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof GetSetProperty) {
				return (P2PPNode<?, C>) new GetSetPropertyNode((GetSetProperty) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof AbstractProperty) {
				return (P2PPNode<?, C>) new AbstractPropertyNode((AbstractProperty) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof ModelSlot) {
				return new ModelSlotPropertyNode((ModelSlot) object, (FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof FlexoRole) {
				return new FlexoRolePropertyNode((FlexoRole) object, (FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof ActionScheme) {
				return (P2PPNode<?, C>) new ActionSchemeNode((ActionScheme) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof CreationScheme) {
				return (P2PPNode<?, C>) new CreationSchemeNode((CreationScheme) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof DeletionScheme) {
				return (P2PPNode<?, C>) new DeletionSchemeNode((DeletionScheme) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof FlexoBehaviour) {
				return new FMLBehaviourNode((FlexoBehaviour) object, (FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof FlexoBehaviourParameter) {
				return (P2PPNode<?, C>) new BehaviourParameterNode((FlexoBehaviourParameter) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof EmptyControlGraph) {
				return (P2PPNode<?, C>) new EmptyControlGraphNode((EmptyControlGraph) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof Sequence) {
				return (P2PPNode<?, C>) new SequenceNode((Sequence) object, (FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof AssignationAction) {
				return (P2PPNode<?, C>) new AssignationActionNode((AssignationAction) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof DeclarationAction) {
				return (P2PPNode<?, C>) new DeclarationActionNode((DeclarationAction) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof ExpressionAction) {
				return (P2PPNode<?, C>) new ExpressionActionNode((ExpressionAction) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof ReturnStatement) {
				return (P2PPNode<?, C>) new ReturnStatementNode((ReturnStatement) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof ConditionalAction) {
				return (P2PPNode<?, C>) new ConditionalNode((ConditionalAction) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof IterationAction) {
				return (P2PPNode<?, C>) new IterationActionNode((IterationAction) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			/*if (object instanceof AddFlexoConceptInstance) {
			return (P2PPNode<?, C>) new AddFlexoConceptInstanceNode((AddFlexoConceptInstance) object, getanalyzer());
			}
			if (object instanceof AddVirtualModelInstance) {
			return (P2PPNode<?, C>) new AddVirtualModelInstanceNode((AddVirtualModelInstance) object, getanalyzer());
			}
			if (object instanceof AddClassInstance) {
			return (P2PPNode<?, C>) new AddClassInstanceNode((AddClassInstance) object, getanalyzer());
			}*/
			if (object instanceof LogAction) {
				return (P2PPNode<?, C>) new LogActionNode((LogAction) object, (FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof AbstractFetchRequest) {
				return new FetchRequestNode((AbstractFetchRequest) object, (FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof BehaviourCallArgument) {
				return (P2PPNode<?, C>) new BehaviourCallArgumentNode((BehaviourCallArgument) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof InitiateMatching) {
				return (P2PPNode<?, C>) new BeginMatchActionNode((InitiateMatching) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof MatchFlexoConceptInstance) {
				return (P2PPNode<?, C>) new MatchActionNode((MatchFlexoConceptInstance) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof FinalizeMatching) {
				return (P2PPNode<?, C>) new EndMatchActionNode((FinalizeMatching) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof TechnologySpecificAction) {
				return new FMLEditionActionNode((TechnologySpecificAction) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof FMLSimplePropertyValue) {
				return new FMLSimplePropertyValueNode((FMLSimplePropertyValue) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof FMLInstancePropertyValue) {
				return new FMLInstancePropertyValueNode((FMLInstancePropertyValue) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof FMLInstancesListPropertyValue) {
				return new FMLInstancesListPropertyValueNode((FMLInstancesListPropertyValue) object,
						(FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
			if (object instanceof WrappedFMLObject) {
				return new WrappedFMLObjectNode((WrappedFMLObject) object, (FMLCompilationUnitSemanticsAnalyzer) getSemanticsAnalyzer());
			}
		}
		System.err.println("Not supported: " + object);
		Thread.dumpStack();
		return null;
	}

	/*public ControlGraphFactory getControlGraphFactory() {
		if (getAbstractanalyzer() instanceof ControlGraphFactory) {
			return (ControlGraphFactory) getAbstractanalyzer();
		}
		if (getParent() instanceof FMLObjectNode) {
			return ((FMLObjectNode) getParent()).getControlGraphFactory();
		}
		return null;
	}*/

	/**
	 * Return fragment matching supplied node in AST
	 * 
	 * @param token
	 * @return
	 */
	public RawSourceFragment getFragment(Node node) {
		if (node == null) {
			return null;
		}
		if (node instanceof Token) {
			Token token = (Token) node;
			return getRawSource().makeFragment(getRawSource().makePositionBeforeChar(token.getLine(), token.getPos()),
					getRawSource().makePositionBeforeChar(token.getLine(), token.getPos() + token.getText().length()));
		}
		else {
			return getSemanticsAnalyzer().getFragmentManager().retrieveFragment(node);
		}
	}

	/**
	 * Return fragment matching supplied nodes in AST
	 * 
	 * @param token
	 * @return
	 */
	public RawSourceFragment getFragment(Node node, Node otherNode) {
		return getFragment(node, Collections.singletonList(otherNode));
	}

	/**
	 * Return fragment matching supplied nodes in AST
	 * 
	 * @param token
	 * @return
	 */
	public RawSourceFragment getFragment(Node node, List<? extends Node> otherNodes) {
		ChainedCollection<Node> collection = new ChainedCollection<>();
		collection.add(node);
		collection.add(otherNodes);
		return getSemanticsAnalyzer().getFragmentManager().getFragment(collection);
	}

	public String getText(Node node) {
		return getFragment(node).getRawText();
	}

	public List<String> makeFullQualifiedIdentifierList(List<PIdentifierPrefix> prefixes, TLidentifier identifier) {
		return semanticsAnalyzer.makeFullQualifiedIdentifierList(prefixes, identifier);
	}

	public String makeFullQualifiedIdentifier(List<PIdentifierPrefix> prefixes, TLidentifier identifier) {
		return semanticsAnalyzer.makeFullQualifiedIdentifier(prefixes, identifier);
	}

	public String makeFullQualifiedIdentifier(PCompositeIdent compositeIdentifier) {
		return semanticsAnalyzer.makeFullQualifiedIdentifier(compositeIdentifier);
	}

	public String makeFullQualifiedIdentifier(PAnnotationTag annotationTag) {
		return semanticsAnalyzer.makeFullQualifiedIdentifier(annotationTag);
	}

	public List<String> makeFullQualifiedIdentifierList(List<PIdentifierPrefix> prefixes, TUidentifier identifier) {
		return semanticsAnalyzer.makeFullQualifiedIdentifierList(prefixes, identifier);
	}

	public String makeFullQualifiedIdentifier(List<PIdentifierPrefix> prefixes, TUidentifier identifier) {
		return semanticsAnalyzer.makeFullQualifiedIdentifier(prefixes, identifier);
	}

	public String makeFullQualifiedIdentifier(PCompositeTident compositeIdentifier) {
		return semanticsAnalyzer.makeFullQualifiedIdentifier(compositeIdentifier);
	}

	public int getLiteralValue(TLitInteger node) {
		String f = node.getText();
		try {
			return Integer.parseInt(f);
		} catch (NumberFormatException e) {
			throwIssue("Cannot parse as integer: " + f, getFragment(node));
			return -1;
		}
	}

	public Object getLiteralValue(PLiteral node) throws ParseException {
		if (node instanceof ACharacterLiteral) {
			return ((ACharacterLiteral) node).getLitCharacter().getText().charAt(1);
		}
		else if (node instanceof AFalseLiteral) {
			return false;
		}
		else if (node instanceof ATrueLiteral) {
			return true;
		}
		else if (node instanceof AStringLiteral) {
			String t = ((AStringLiteral) node).getLitString().getText();
			return t.substring(1, t.length() - 2);
		}
		else if (node instanceof AFloatingPointLiteral) {
			String f = ((AFloatingPointLiteral) node).getLitFloat().getText();
			try {
				return Double.parseDouble(f);
			} catch (NumberFormatException e) {
				throwIssue("Cannot parse as double: " + f, getFragment(node));
				return null;
			}
		}
		else if (node instanceof AIntegerLiteral) {
			String f = ((AIntegerLiteral) node).getLitInteger().getText();
			try {
				return Long.parseLong(f);
			} catch (NumberFormatException e) {
				throwIssue("Cannot parse as long: " + f, getFragment(node));
				return null;
			}
		}
		else if (node instanceof ANullLiteral) {
			return null;
		}
		RawSourceFragment fragment = getFragment(node);
		throwIssue("Unexpected " + node, fragment);
		return null;
	}

	/*
	// We should parse expression instead
	@Deprecated
	protected <T> DataBinding<T> makeBinding(Node node, Type type, BindingDefinitionType bindingType, Bindable bindable) {
		return new DataBinding(getText(node), bindable, type, bindingType);
	}
	
	// We should parse expression instead
	@Deprecated
	protected <T> DataBinding<T> makeBinding(Node node, Bindable bindable) {
		return new DataBinding(getText(node), bindable, Object.class, BindingDefinitionType.GET);
	}
	
	// We should parse expression instead
	@Deprecated
	protected <T> DataBinding<T> makeBinding(PCompositeIdent compositeIdentifier, Type type, BindingDefinitionType bindingType,
			Bindable bindable) {
		// TODO: implement this
		logger.warning("Un truc a faire la pour " + compositeIdentifier);
		return new DataBinding(analyzer.makeFullQualifiedIdentifier(compositeIdentifier), bindable, type, bindingType);
	}
	
	// We should parse expression instead
	@Deprecated
	protected <T> DataBinding<T> makeBinding(PCompositeIdent compositeIdentifier, Bindable bindable) {
		// TODO: implement this
		logger.warning("Un truc a faire la pour " + compositeIdentifier);
		return new DataBinding(analyzer.makeFullQualifiedIdentifier(compositeIdentifier), bindable, Object.class,
				BindingDefinitionType.GET);
	}*/

	protected String getVisibilityAsString(Visibility visibility) {
		if (visibility != null) {
			switch (visibility) {
				case Default:
					return "";
				case Public:
					return "public";
				case Protected:
					return "protected";
				case Private:
					return "private";
			}
		}
		return "";
	}

	protected Visibility getVisibility(PVisibility visibility) {
		if (visibility == null) {
			return Visibility.Default;
		}
		else if (visibility instanceof APublicVisibility) {
			return Visibility.Public;
		}
		else if (visibility instanceof AProtectedVisibility) {
			return Visibility.Protected;
		}
		else if (visibility instanceof APrivateVisibility) {
			return Visibility.Private;
		}
		return null;
	}

	protected PropertyCardinality getCardinality(PCardinality cardinality) {
		if (cardinality == null) {
			return PropertyCardinality.ZeroOne;
		}
		Integer upperBounds = null;
		Integer lowerBounds = null;
		if (cardinality instanceof AWithExplicitBoundsCardinality) {
			lowerBounds = getLiteralValue(((AWithExplicitBoundsCardinality) cardinality).getLower());
			upperBounds = getLiteralValue(((AWithExplicitBoundsCardinality) cardinality).getUpper());
		}
		else if (cardinality instanceof AWithLowerBoundsCardinality) {
			lowerBounds = getLiteralValue(((AWithLowerBoundsCardinality) cardinality).getLower());
			upperBounds = null;
		}
		else if (cardinality instanceof AWithUpperBoundsCardinality) {
			lowerBounds = null;
			upperBounds = getLiteralValue(((AWithUpperBoundsCardinality) cardinality).getUpper());
		}
		else if (cardinality instanceof AMultiple1Cardinality) {
			lowerBounds = null;
			upperBounds = null;
		}
		else if (cardinality instanceof AMultiple2Cardinality) {
			lowerBounds = null;
			upperBounds = null;
		}
		if (lowerBounds != null && lowerBounds == 0) {
			if (upperBounds != null && upperBounds == 1) {
				return PropertyCardinality.ZeroOne;
			}
			else {
				return PropertyCardinality.ZeroMany;
			}
		}
		if (lowerBounds != null && lowerBounds == 1) {
			if (upperBounds != null && upperBounds == 1) {
				return PropertyCardinality.One;
			}
			else {
				return PropertyCardinality.OneMany;
			}
		}
		return PropertyCardinality.ZeroMany;
	}

	protected final String serializeCardinality(PropertyCardinality cardinality) {
		if (cardinality == null) {
			return "";
		}
		switch (cardinality) {
			case One:
				return "";
			case ZeroOne:
				return "";
			case ZeroMany:
				return "[0,*]";
			case OneMany:
				return "[1,*]";
			default:
				return "";
		}

	}

	protected TLidentifier getName(PVariableDeclarator variableDeclarator) {
		if (variableDeclarator instanceof AIdentifierVariableDeclarator) {
			return ((AIdentifierVariableDeclarator) variableDeclarator).getLidentifier();
		}
		if (variableDeclarator instanceof AInitializerExpressionVariableDeclarator) {
			return ((AInitializerExpressionVariableDeclarator) variableDeclarator).getLidentifier();
		}
		if (variableDeclarator instanceof AInitializerFmlActionVariableDeclarator) {
			return ((AInitializerFmlActionVariableDeclarator) variableDeclarator).getLidentifier();
		}
		return null;
	}

	protected PExpression getInitializerExpression(PVariableDeclarator variableDeclarator) {
		if (variableDeclarator instanceof AIdentifierVariableDeclarator) {
			return null;
		}
		if (variableDeclarator instanceof AInitializerExpressionVariableDeclarator) {
			return ((AInitializerExpressionVariableDeclarator) variableDeclarator).getExpression();
		}
		if (variableDeclarator instanceof AInitializerFmlActionVariableDeclarator) {
			return null;
		}
		return null;
	}

	protected PFmlActionExp getInitializerFMLAction(PVariableDeclarator variableDeclarator) {
		if (variableDeclarator instanceof AIdentifierVariableDeclarator) {
			return null;
		}
		if (variableDeclarator instanceof AInitializerExpressionVariableDeclarator) {
			return null;
		}
		if (variableDeclarator instanceof AInitializerFmlActionVariableDeclarator) {
			return ((AInitializerFmlActionVariableDeclarator) variableDeclarator).getFmlActionExp();
		}
		return null;
	}

	protected final String serializeType(Type type, boolean escapeVoid) {
		if (escapeVoid && ((Void.class.equals(type)) || (Void.TYPE.equals(type)))) {
			return "";
		}
		return serializeType(type);
	}

	protected final String serializeType(Type type) {
		// TODO: generate required imports !
		/*if (type != null) {
			if (type.equals(Boolean.class)) {
				return "boolean";
			}
		}*/
		if (type instanceof CustomType) {
			if (!((CustomType) type).isResolved()) {
				((CustomType) type).resolve();
			}
		}

		if (type instanceof VirtualModelInstanceType && ((VirtualModelInstanceType) type).getVirtualModel() == null) {
			String uri = ((VirtualModelInstanceType) type).getConceptURI();
			if (StringUtils.isNotEmpty(uri)) {
				if (uri.contains("/")) {
					uri = uri.substring(uri.lastIndexOf("/") + 1);
				}
				if (uri.endsWith(CompilationUnitResourceFactory.FML_SUFFIX)) {
					uri = uri.substring(0, uri.length() - CompilationUnitResourceFactory.FML_SUFFIX.length());
				}
				return uri;
			}
			return "ModelInstance";
		}

		if (type instanceof FlexoConceptInstanceType && ((FlexoConceptInstanceType) type).getFlexoConcept() == null) {
			String uri = ((FlexoConceptInstanceType) type).getConceptURI();
			if (StringUtils.isNotEmpty(uri)) {
				if (uri.contains("/")) {
					uri = uri.substring(uri.lastIndexOf("/") + 1);
				}
				return uri;
			}
			return "ConceptInstance";
		}

		String returned = TypeUtils.simpleRepresentation(type);
		/*if (returned.startsWith("#")) {
			System.out.println("Nimporte quoi: " + type);
			System.out.println("type: " + type.getClass());
			if (type instanceof FlexoConceptInstanceType) {
				FlexoConceptInstanceType fciType = (FlexoConceptInstanceType) type;
				System.out.println("uri: " + fciType.getConceptURI());
				System.out.println("concept: " + fciType.getFlexoConcept());
				if (fciType.getFlexoConcept() != null) {
					System.out.println("concept.name: " + fciType.getFlexoConcept().getName());
					System.out.println("resolved: " + fciType.isResolved());
					System.out.println("simpleRepresentation: " + fciType.simpleRepresentation());
					System.out.println("simpleRepresentation: " + TypeUtils.simpleRepresentation(type));
				}
			}
			Thread.dumpStack();
		}*/
		return returned;

	}

	protected final String serializeType(FlexoConcept type) {
		// TODO: generate required imports !
		if (type == null) {
			return "UndefinedConcept";
		}
		return type.getName();

	}

	protected String serializeFlexoBehaviour(FlexoBehaviour behaviour) {
		if (behaviour != null) {
			return behaviour.getName();
		}
		return "undefinedBehaviour";
	}

	protected DataBindingNode makeDataBinding(PExpression expression, Bindable bindable) {

		DataBindingNode dataBindingNode = getSemanticsAnalyzer().retrieveFMLNode(expression,
				n -> new DataBindingNode(n, bindable, BindingDefinitionType.GET, Object.class, getSemanticsAnalyzer()));
		addToChildren(dataBindingNode);

		ExpressionFactory._makeExpression(expression, bindable, getSemanticsAnalyzer(), dataBindingNode);

		return dataBindingNode;
	}

	protected void throwIssue(String errorMessage) {
		throwIssue(errorMessage, null);
	}

	protected final void throwIssue(String errorMessage, RawSourceFragment fragment) {
		getSemanticsAnalyzer().throwIssue(getModelObject(), errorMessage, fragment, getStartPosition());
	}

	@Override
	public List<SemanticAnalysisIssue> getSemanticAnalysisIssues() {
		return getSemanticsAnalyzer().getSemanticAnalysisIssues();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + getASTNode();
	}

	// MUST be overriden to be usefull
	@Override
	public RawSourceFragment getFragment(FragmentContext context) {
		return getFragment();
	}

	protected void decodeFMLProperties(PFmlParameters properties, FMLObject modelObject) {
		if (properties == null) {
			return;
		}
		if (!modelObject.hasFMLProperties(getFactory())) {
			return;
		}
		if (properties instanceof AFullQualifiedFmlParameters) {
			Map<FMLProperty<?, ?>, Object> propertyValues = new HashMap<>();
			PQualifiedArgumentList qualifiedArgumentList = ((AFullQualifiedFmlParameters) properties).getQualifiedArgumentList();
			decodeFMLProperties(qualifiedArgumentList, modelObject, propertyValues);
			// modelObject.decodeFMLProperties(serializedMap);
		}

	}

	private void decodeFMLProperties(PQualifiedArgumentList argList, FMLObject modelObject, Map<FMLProperty<?, ?>, Object> propertyValues) {
		if (argList instanceof AOneQualifiedArgumentList) {
			AOneQualifiedArgumentList one = (AOneQualifiedArgumentList) argList;
			// decodeFMLProperty(one.getArgName(), one.getExpression(), modelObject, propertyValues);
			decodeFMLProperty(one.getQualifiedArgument(), modelObject, propertyValues);
		}
		else if (argList instanceof AManyQualifiedArgumentList) {
			AManyQualifiedArgumentList many = (AManyQualifiedArgumentList) argList;
			decodeFMLProperty(many.getQualifiedArgument(), modelObject, propertyValues);
			// decodeFMLProperty(many.getArgName(), many.getExpression(), modelObject, propertyValues);
			decodeFMLProperties(many.getQualifiedArgumentList(), modelObject, propertyValues);
		}
	}

	private void decodeFMLProperty(PQualifiedArgument qualifiedArg, FMLObject modelObject, Map<FMLProperty<?, ?>, Object> propertyValues) {
		if (qualifiedArg instanceof ASimpleQualifiedArgument) {
			decodeSimpleFMLProperty(((ASimpleQualifiedArgument) qualifiedArg).getArgName(),
					((ASimpleQualifiedArgument) qualifiedArg).getExpression(), modelObject, propertyValues);
		}
		else if (qualifiedArg instanceof AInstanceQualifiedArgument) {
			decodeInstanceFMLProperty(((AInstanceQualifiedArgument) qualifiedArg).getArgName(),
					((AInstanceQualifiedArgument) qualifiedArg).getQualifiedInstance(), modelObject, propertyValues);
		}
		else if (qualifiedArg instanceof AListInstancesQualifiedArgument) {
			System.out.println("TODO for AListInstancesQualifiedArgument: " + qualifiedArg);
		}
	}

	private void decodeSimpleFMLProperty(TLidentifier propertyName, PExpression expressionValue, FMLObject modelObject,
			Map<FMLProperty<?, ?>, Object> propertyValues) {

		logger.info("Decoding " + propertyName.getText() + "=" + expressionValue);
		FMLProperty fmlProperty = modelObject.getFMLProperty(propertyName.getText(), getFactory());
		if (fmlProperty == null) {
			logger.warning("Cannot retrieve FMLProperty " + propertyName + " for " + modelObject);
			return;
		}

		DataBinding<?> value = makeDataBinding(expressionValue, modelObject).getModelObject();
		System.out.println("FMLProperty=" + fmlProperty + " type=" + fmlProperty.getType());
		if (DataBinding.class.equals(TypeUtils.getBaseClass(fmlProperty.getType()))) {
			logger.info("Set " + fmlProperty.getName() + " = " + value);
			fmlProperty.set(value, modelObject);
		}
		else if (value.isConstant()) {
			Object constantValue = ((Constant) value.getExpression()).getValue();
			if (constantValue != null) {
				if (TypeUtils.isTypeAssignableFrom(fmlProperty.getType(), constantValue.getClass())) {
					logger.info("Set " + fmlProperty.getName() + " = " + constantValue);
					fmlProperty.set(constantValue, modelObject);
				}
				else {
					logger.warning("Invalid value for property " + fmlProperty.getName() + " expected type: " + fmlProperty.getType()
							+ " value: " + constantValue);
					Thread.dumpStack();
				}
			}
		}
		else {
			if (getCompilationUnit() != null) {
				for (ElementImportDeclaration elementImportDeclaration : getCompilationUnit().getElementImports()) {
					// System.out.println(
					// "> J'ai deja: " + elementImportDeclaration.getAbbrev() + "=" + elementImportDeclaration.getReferencedObject());
					if (elementImportDeclaration.getAbbrev().equals(value.toString())) {
						// System.out.println("Trouve !!!");
						fmlProperty.set(elementImportDeclaration.getReferencedObject(), modelObject);
						break;
					}
				}
			}

			logger.warning("Unexpected value for property " + fmlProperty.getName() + " expected type: " + fmlProperty.getType()
					+ " value: " + value);
		}

	}

	private <O extends FMLObject> O decodeInstanceFMLProperty(TLidentifier propertyName, PQualifiedInstance qualifiedInstance,
			FMLObject modelObject, Map<FMLProperty<?, ?>, Object> propertyValues) {
		Class<O> objectClass = null;
		if (qualifiedInstance instanceof ASimpleQualifiedInstance) {
			TUidentifier instanceType = ((ASimpleQualifiedInstance) qualifiedInstance).getArgType();
			objectClass = (Class<O>) getFMLFactory().getFMLObjectClass(instanceType);
		}
		else if (qualifiedInstance instanceof AFullQualifiedQualifiedInstance) {
			TCidentifier taID = ((AFullQualifiedQualifiedInstance) qualifiedInstance).getTaId();
			TUidentifier instanceType = ((AFullQualifiedQualifiedInstance) qualifiedInstance).getArgType();
			objectClass = (Class<O>) getFMLFactory().getFMLObjectClass(taID, instanceType);
		}

		O returned = getFactory().newInstance(objectClass);
		return returned;
	}

}
