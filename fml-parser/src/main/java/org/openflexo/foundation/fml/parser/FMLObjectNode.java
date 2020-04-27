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
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.AbstractProperty;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.DeletionScheme;
import org.openflexo.foundation.fml.ElementImportDeclaration;
import org.openflexo.foundation.fml.ExpressionProperty;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLPrettyPrintDelegate;
import org.openflexo.foundation.fml.FMLPrettyPrintable;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.GetSetProperty;
import org.openflexo.foundation.fml.JavaImportDeclaration;
import org.openflexo.foundation.fml.JavaRole;
import org.openflexo.foundation.fml.NamespaceDeclaration;
import org.openflexo.foundation.fml.PrimitiveRole;
import org.openflexo.foundation.fml.PropertyCardinality;
import org.openflexo.foundation.fml.UseModelSlotDeclaration;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.Visibility;
import org.openflexo.foundation.fml.controlgraph.ConditionalAction;
import org.openflexo.foundation.fml.controlgraph.EmptyControlGraph;
import org.openflexo.foundation.fml.controlgraph.IterationAction;
import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.editionaction.AbstractFetchRequest;
import org.openflexo.foundation.fml.editionaction.AddClassInstance;
import org.openflexo.foundation.fml.editionaction.AssignationAction;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.editionaction.ExpressionAction;
import org.openflexo.foundation.fml.editionaction.LogAction;
import org.openflexo.foundation.fml.editionaction.ReturnStatement;
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
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoConceptNode;
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
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.AddClassInstanceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.AddFlexoConceptInstanceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.AddVirtualModelInstanceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.AssignationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.BeginMatchActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.BehaviourCallArgumentNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ConditionalNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.DeclarationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.EmptyControlGraphNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.EndMatchActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ExpressionActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.FetchRequestNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.IterationActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.LogActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.MatchActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ReturnStatementNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.SequenceNode;
import org.openflexo.foundation.fml.parser.node.ACharacterLiteral;
import org.openflexo.foundation.fml.parser.node.ACompositeIdent;
import org.openflexo.foundation.fml.parser.node.AFalseLiteral;
import org.openflexo.foundation.fml.parser.node.AFloatingPointLiteral;
import org.openflexo.foundation.fml.parser.node.AIdentifierVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.AInitializerVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.AIntegerLiteral;
import org.openflexo.foundation.fml.parser.node.AMultiple1Cardinality;
import org.openflexo.foundation.fml.parser.node.AMultiple2Cardinality;
import org.openflexo.foundation.fml.parser.node.ANullLiteral;
import org.openflexo.foundation.fml.parser.node.APrivateVisibility;
import org.openflexo.foundation.fml.parser.node.AProtectedVisibility;
import org.openflexo.foundation.fml.parser.node.APublicVisibility;
import org.openflexo.foundation.fml.parser.node.AStringLiteral;
import org.openflexo.foundation.fml.parser.node.ATrueLiteral;
import org.openflexo.foundation.fml.parser.node.AWithExplicitBoundsCardinality;
import org.openflexo.foundation.fml.parser.node.AWithLowerBoundsCardinality;
import org.openflexo.foundation.fml.parser.node.AWithUpperBoundsCardinality;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PAdditionalIdentifier;
import org.openflexo.foundation.fml.parser.node.PCardinality;
import org.openflexo.foundation.fml.parser.node.PCompositeIdent;
import org.openflexo.foundation.fml.parser.node.PLiteral;
import org.openflexo.foundation.fml.parser.node.PVariableDeclarator;
import org.openflexo.foundation.fml.parser.node.PVisibility;
import org.openflexo.foundation.fml.parser.node.TIdentifier;
import org.openflexo.foundation.fml.parser.node.TLitInteger;
import org.openflexo.foundation.fml.parser.node.Token;
import org.openflexo.foundation.fml.rt.editionaction.AddFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.AddVirtualModelInstance;
import org.openflexo.foundation.fml.rt.editionaction.BehaviourCallArgument;
import org.openflexo.foundation.fml.rt.editionaction.FinalizeMatching;
import org.openflexo.foundation.fml.rt.editionaction.InitiateMatching;
import org.openflexo.foundation.fml.rt.editionaction.MatchFlexoConceptInstance;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.p2pp.P2PPNode;
import org.openflexo.p2pp.PrettyPrintContext;
import org.openflexo.p2pp.RawSource;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.toolbox.ChainedCollection;

/**
 * Maintains consistency between the model (represented by an {@link FMLObject}) and source code represented in FML language
 * 
 * Works
 * 
 * @author sylvain
 * 
 */
public abstract class FMLObjectNode<N extends Node, T extends FMLPrettyPrintable, A extends FMLSemanticsAnalyzer> extends P2PPNode<N, T>
		implements FMLPrettyPrintDelegate<T> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(FMLObjectNode.class.getPackage().getName());

	private final MainSemanticsAnalyzer analyser;

	public FMLObjectNode(N astNode, MainSemanticsAnalyzer analyser) {
		super(null, astNode, analyser.getFragmentManager());
		this.analyser = analyser;

		modelObject = buildModelObjectFromAST(astNode);
		if (modelObject != null) {
			modelObject.setPrettyPrintDelegate(this);
			modelObject.initializeDeserialization(getFactory());
		}
	}

	public FMLObjectNode(T aFMLObject, MainSemanticsAnalyzer analyser) {
		super(aFMLObject, null, null);
		this.analyser = analyser;

		modelObject.setPrettyPrintDelegate(this);
		preparePrettyPrint(false);
	}

	public FMLModelFactory getFactory() {
		return analyser.getFactory();
	}

	@Override
	public void setModelObject(T modelObject) {
		super.setModelObject(modelObject);
		modelObject.setPrettyPrintDelegate(this);
	}

	/*public A getAbstractAnalyser() {
		return analyser;
	}
	
	public MainSemanticsAnalyzer getAnalyser() {
		return getAbstractAnalyser().getMainAnalyzer();
	}*/

	public MainSemanticsAnalyzer getAnalyser() {
		return analyser;
	}

	public TypeFactory getTypeFactory() {
		return getAnalyser().getTypeFactory();
	}

	public FMLFactory getFMLFactory() {
		return getAnalyser().getFMLFactory();
	}

	protected FMLCompilationUnit getCompilationUnit() {
		return getAnalyser().getCompilationUnit();
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
		return getAnalyser().getRawSource();
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
		if (object instanceof NamespaceDeclaration) {
			return (P2PPNode<?, C>) new NamespaceDeclarationNode((NamespaceDeclaration) object, getAnalyser());
		}
		if (object instanceof UseModelSlotDeclaration) {
			return (P2PPNode<?, C>) new UseDeclarationNode((UseModelSlotDeclaration) object, getAnalyser());
		}
		if (object instanceof JavaImportDeclaration) {
			return (P2PPNode<?, C>) new JavaImportNode((JavaImportDeclaration) object, getAnalyser());
		}
		if (object instanceof ElementImportDeclaration) {
			return (P2PPNode<?, C>) new ElementImportNode((ElementImportDeclaration) object, getAnalyser());
		}
		if (object instanceof BasicMetaData) {
			return (P2PPNode<?, C>) new BasicMetaDataNode((BasicMetaData) object, getAnalyser());
		}
		if (object instanceof SingleMetaData) {
			return (P2PPNode<?, C>) new SingleMetaDataNode((SingleMetaData) object, getAnalyser());
		}
		if (object instanceof MultiValuedMetaData) {
			return (P2PPNode<?, C>) new MultiValuedMetaDataNode((MultiValuedMetaData) object, getAnalyser());
		}
		if (object instanceof MetaDataKeyValue) {
			return (P2PPNode<?, C>) new MetaDataKeyValueNode((MetaDataKeyValue) object, getAnalyser());
		}
		if (object instanceof ListMetaData) {
			return (P2PPNode<?, C>) new ListMetaDataNode((ListMetaData) object, getAnalyser());
		}
		if (object instanceof VirtualModel) {
			return (P2PPNode<?, C>) new VirtualModelNode((VirtualModel) object, getAnalyser());
		}
		if (object instanceof FlexoConcept) {
			return (P2PPNode<?, C>) new FlexoConceptNode((FlexoConcept) object, getAnalyser());
		}
		if (object instanceof SingleMetaData) {
			return (P2PPNode<?, C>) new SingleMetaDataNode((SingleMetaData) object, getAnalyser());
		}
		if (object instanceof PrimitiveRole) {
			return (P2PPNode<?, C>) new PrimitiveRoleNode((PrimitiveRole) object, getAnalyser());
		}
		if (object instanceof JavaRole) {
			return (P2PPNode<?, C>) new JavaRoleNode((JavaRole) object, getAnalyser());
		}
		if (object instanceof AbstractProperty) {
			return (P2PPNode<?, C>) new AbstractPropertyNode((AbstractProperty) object, getAnalyser());
		}
		if (object instanceof ExpressionProperty) {
			return (P2PPNode<?, C>) new ExpressionPropertyNode((ExpressionProperty) object, getAnalyser());
		}
		if (object instanceof GetSetProperty) {
			return (P2PPNode<?, C>) new GetSetPropertyNode((GetSetProperty) object, getAnalyser());
		}
		if (object instanceof AbstractProperty) {
			return (P2PPNode<?, C>) new AbstractPropertyNode((AbstractProperty) object, getAnalyser());
		}
		if (object instanceof ModelSlot) {
			return new ModelSlotPropertyNode((ModelSlot) object, getAnalyser());
		}
		if (object instanceof FlexoRole) {
			return new FlexoRolePropertyNode((FlexoRole) object, getAnalyser());
		}
		if (object instanceof ActionScheme) {
			return (P2PPNode<?, C>) new ActionSchemeNode((ActionScheme) object, getAnalyser());
		}
		if (object instanceof CreationScheme) {
			return (P2PPNode<?, C>) new CreationSchemeNode((CreationScheme) object, getAnalyser());
		}
		if (object instanceof DeletionScheme) {
			return (P2PPNode<?, C>) new DeletionSchemeNode((DeletionScheme) object, getAnalyser());
		}
		if (object instanceof FlexoBehaviour) {
			return new FMLBehaviourNode((DeletionScheme) object, getAnalyser());
		}
		if (object instanceof FlexoBehaviourParameter) {
			return (P2PPNode<?, C>) new BehaviourParameterNode((FlexoBehaviourParameter) object, getAnalyser());
		}
		if (object instanceof EmptyControlGraph) {
			return (P2PPNode<?, C>) new EmptyControlGraphNode((EmptyControlGraph) object, getAnalyser());
		}
		if (object instanceof Sequence) {
			return (P2PPNode<?, C>) new SequenceNode((Sequence) object, getAnalyser());
		}
		if (object instanceof AssignationAction) {
			return (P2PPNode<?, C>) new AssignationActionNode((AssignationAction) object, getAnalyser());
		}
		if (object instanceof DeclarationAction) {
			return (P2PPNode<?, C>) new DeclarationActionNode((DeclarationAction) object, getAnalyser());
		}
		if (object instanceof ExpressionAction) {
			return (P2PPNode<?, C>) new ExpressionActionNode((ExpressionAction) object, getAnalyser());
		}
		if (object instanceof ReturnStatement) {
			return (P2PPNode<?, C>) new ReturnStatementNode((ReturnStatement) object, getAnalyser());
		}
		if (object instanceof ConditionalAction) {
			return (P2PPNode<?, C>) new ConditionalNode((ConditionalAction) object, getAnalyser());
		}
		if (object instanceof IterationAction) {
			return (P2PPNode<?, C>) new IterationActionNode((IterationAction) object, getAnalyser());
		}
		if (object instanceof AddFlexoConceptInstance) {
			return (P2PPNode<?, C>) new AddFlexoConceptInstanceNode((AddFlexoConceptInstance) object, getAnalyser());
		}
		if (object instanceof AddVirtualModelInstance) {
			return (P2PPNode<?, C>) new AddVirtualModelInstanceNode((AddVirtualModelInstance) object, getAnalyser());
		}
		if (object instanceof AddClassInstance) {
			return (P2PPNode<?, C>) new AddClassInstanceNode((AddClassInstance) object, getAnalyser());
		}
		if (object instanceof LogAction) {
			return (P2PPNode<?, C>) new LogActionNode((LogAction) object, getAnalyser());
		}
		if (object instanceof AbstractFetchRequest) {
			return new FetchRequestNode((AbstractFetchRequest) object, getAnalyser());
		}
		if (object instanceof BehaviourCallArgument) {
			return (P2PPNode<?, C>) new BehaviourCallArgumentNode((BehaviourCallArgument) object, getAnalyser());
		}
		if (object instanceof InitiateMatching) {
			return (P2PPNode<?, C>) new BeginMatchActionNode((InitiateMatching) object, getAnalyser());
		}
		if (object instanceof MatchFlexoConceptInstance) {
			return (P2PPNode<?, C>) new MatchActionNode((MatchFlexoConceptInstance) object, getAnalyser());
		}
		if (object instanceof FinalizeMatching) {
			return (P2PPNode<?, C>) new EndMatchActionNode((FinalizeMatching) object, getAnalyser());
		}
		System.err.println("Not supported: " + object);
		Thread.dumpStack();
		return null;
	}

	/*public ControlGraphFactory getControlGraphFactory() {
		if (getAbstractAnalyser() instanceof ControlGraphFactory) {
			return (ControlGraphFactory) getAbstractAnalyser();
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
		if (node instanceof Token) {
			Token token = (Token) node;
			return getRawSource().makeFragment(getRawSource().makePositionBeforeChar(token.getLine(), token.getPos()),
					getRawSource().makePositionBeforeChar(token.getLine(), token.getPos() + token.getText().length()));
		}
		else {
			return getAnalyser().getFragmentManager().retrieveFragment(node);
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
		return getAnalyser().getFragmentManager().getFragment(collection);
	}

	public String getText(Node node) {
		return getFragment(node).getRawText();
	}

	public int getLiteralValue(TLitInteger node) {
		String f = node.getText();
		try {
			return Integer.parseInt(f);
		} catch (NumberFormatException e) {
			(new ParseException("Cannot parse as long: " + f)).printStackTrace();
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
				throw new ParseException("Cannot parse as double: " + f);
			}
		}
		else if (node instanceof AIntegerLiteral) {
			String f = ((AIntegerLiteral) node).getLitInteger().getText();
			try {
				return Long.parseLong(f);
			} catch (NumberFormatException e) {
				throw new ParseException("Cannot parse as long: " + f);
			}
		}
		else if (node instanceof ANullLiteral) {
			return null;
		}
		throw new ParseException("Unexpected " + node);
	}

	public List<String> makeFullQualifiedIdentifierList(TIdentifier identifier, List<PAdditionalIdentifier> additionalIdentifiers) {
		return getTypeFactory().makeFullQualifiedIdentifierList(identifier, additionalIdentifiers);
	}

	public String makeFullQualifiedIdentifier(PCompositeIdent compositeIdentifier) {
		if (compositeIdentifier instanceof ACompositeIdent) {
			return getTypeFactory().makeFullQualifiedIdentifier(((ACompositeIdent) compositeIdentifier).getIdentifier(),
					((ACompositeIdent) compositeIdentifier).getAdditionalIdentifiers());
		}
		return null;
	}

	public String makeFullQualifiedIdentifier(TIdentifier identifier, List<PAdditionalIdentifier> additionalIdentifiers) {
		return getTypeFactory().makeFullQualifiedIdentifier(identifier, additionalIdentifiers);
	}

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
		return new DataBinding(makeFullQualifiedIdentifier(compositeIdentifier), bindable, type, bindingType);
	}

	// We should parse expression instead
	@Deprecated
	protected <T> DataBinding<T> makeBinding(PCompositeIdent compositeIdentifier, Bindable bindable) {
		return new DataBinding(makeFullQualifiedIdentifier(compositeIdentifier), bindable, Object.class, BindingDefinitionType.GET);
	}

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

	protected TIdentifier getName(PVariableDeclarator variableDeclarator) {
		if (variableDeclarator instanceof AIdentifierVariableDeclarator) {
			return ((AIdentifierVariableDeclarator) variableDeclarator).getIdentifier();
		}
		if (variableDeclarator instanceof AInitializerVariableDeclarator) {
			return ((AInitializerVariableDeclarator) variableDeclarator).getIdentifier();
		}
		return null;
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
			System.exit(-1);
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

	protected void throwIssue(String errorMessage) {
		throwIssue(errorMessage);
	}

	protected void throwIssue(String errorMessage, RawSourceFragment fragment) {
		logger.warning("Compilation issue: " + errorMessage + " " + (fragment != null ? fragment.getStartPosition() : getStartPosition()));
	}

}
