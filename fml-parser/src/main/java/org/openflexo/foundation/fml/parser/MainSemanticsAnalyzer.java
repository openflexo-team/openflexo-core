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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.ElementImportDeclaration;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.FlexoRole;
import org.openflexo.foundation.fml.parser.fmlnodes.BasicMetaDataNode;
import org.openflexo.foundation.fml.parser.fmlnodes.BehaviourParameterNode;
import org.openflexo.foundation.fml.parser.fmlnodes.ElementImportNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoBehaviourNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoConceptNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoRolePropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.JavaImportNode;
import org.openflexo.foundation.fml.parser.fmlnodes.ListMetaDataNode;
import org.openflexo.foundation.fml.parser.fmlnodes.MetaDataKeyValueNode;
import org.openflexo.foundation.fml.parser.fmlnodes.ModelSlotPropertyNode;
import org.openflexo.foundation.fml.parser.fmlnodes.MultiValuedMetaDataNode;
import org.openflexo.foundation.fml.parser.fmlnodes.NamedJavaImportNode;
import org.openflexo.foundation.fml.parser.fmlnodes.NamespaceDeclarationNode;
import org.openflexo.foundation.fml.parser.fmlnodes.SingleMetaDataNode;
import org.openflexo.foundation.fml.parser.fmlnodes.UseDeclarationNode;
import org.openflexo.foundation.fml.parser.fmlnodes.VirtualModelNode;
import org.openflexo.foundation.fml.parser.node.AAbstractPropertyInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.AAnnotationKeyValuePair;
import org.openflexo.foundation.fml.parser.node.ABasicAnnotationAnnotation;
import org.openflexo.foundation.fml.parser.node.ABehaviourDeclarationInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.ABlockFlexoBehaviourBody;
import org.openflexo.foundation.fml.parser.node.AComplexAnnotationAnnotation;
import org.openflexo.foundation.fml.parser.node.AComplexFormalArgument;
import org.openflexo.foundation.fml.parser.node.AConceptDecl;
import org.openflexo.foundation.fml.parser.node.AExpressionPropertyInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.AFmlCompilationUnit;
import org.openflexo.foundation.fml.parser.node.AFmlFullyQualifiedInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.AFmlInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.AGetDecl;
import org.openflexo.foundation.fml.parser.node.AGetSetPropertyInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.AJavaImportImportDecl;
import org.openflexo.foundation.fml.parser.node.AJavaInnerConceptDecl;
import org.openflexo.foundation.fml.parser.node.AListAnnotationAnnotation;
import org.openflexo.foundation.fml.parser.node.AModelDecl;
import org.openflexo.foundation.fml.parser.node.ANamedJavaImportImportDecl;
import org.openflexo.foundation.fml.parser.node.ANamedUriImportImportDecl;
import org.openflexo.foundation.fml.parser.node.ANamespaceDecl;
import org.openflexo.foundation.fml.parser.node.APrimitiveFormalArgument;
import org.openflexo.foundation.fml.parser.node.ASingleAnnotationAnnotation;
import org.openflexo.foundation.fml.parser.node.AUriImportImportDecl;
import org.openflexo.foundation.fml.parser.node.AUseDecl;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.Start;
import org.openflexo.foundation.technologyadapter.ModelSlot;
import org.openflexo.p2pp.RawSource;

/**
 * This class implements the main semantics analyzer for a parsed FML compilation unit.<br>
 * 
 * @author sylvain
 * 
 */
public class MainSemanticsAnalyzer extends FMLSemanticsAnalyzer {

	// private final TypeFactory typeFactory;
	private final FlexoPropertyFactory propertyFactory;
	private final FlexoBehaviourFactory behaviourFactory;
	private final FMLFactory fmlFactory;
	private final AbstractFMLTypingSpace typingSpace;

	// Raw source as when this analyzer was last parsed
	private RawSource rawSource;

	private FragmentManager fragmentManager;

	private FMLCompilationUnitNode compilationUnitNode;
	private FMLCompilationUnit compilationUnit;

	public MainSemanticsAnalyzer(FMLCompilationUnit compilationUnit) {
		super(compilationUnit.getFMLModelFactory(), null);
		this.compilationUnit = compilationUnit;
		fragmentManager = new FragmentManager(null);
		// typeFactory = new TypeFactory(this);
		fmlFactory = new FMLFactory(this);
		propertyFactory = new FlexoPropertyFactory(this);
		behaviourFactory = new FlexoBehaviourFactory(this);
		typingSpace = compilationUnit.getTypingSpace();
	}

	public MainSemanticsAnalyzer(FMLModelFactory factory, Start tree, RawSource rawSource) {
		super(factory, tree);
		this.rawSource = rawSource;
		fragmentManager = new FragmentManager(rawSource);
		// typeFactory = new TypeFactory(this);
		fmlFactory = new FMLFactory(this);
		propertyFactory = new FlexoPropertyFactory(this);
		behaviourFactory = new FlexoBehaviourFactory(this);
		typingSpace = new FMLTypingSpaceDuringParsing(this);
	}

	@Override
	public MainSemanticsAnalyzer getMainAnalyzer() {
		return this;
	}

	public AbstractFMLTypingSpace getTypingSpace() {
		return typingSpace;
	}

	@Override
	public Start getRootNode() {
		return (Start) super.getRootNode();
	}

	public ObjectNode<?, ?, ?> getFMLNode(Node astNode) {
		return nodesForAST.get(astNode);
	}

	private Map<Node, ObjectNode> nodesForAST = new HashMap<>();

	/*public Map<Node, FMLObjectNode> getNodesForAST() {
		return nodesForAST;
	}*/

	public <N extends Node, FMLN extends ObjectNode> FMLN retrieveFMLNode(N astNode, Function<N, FMLN> function) {
		FMLN returned = (FMLN) nodesForAST.get(astNode);
		if (returned == null) {
			returned = function.apply(astNode);
			nodesForAST.put(astNode, returned);
		}
		return returned;
	}

	public <N extends Node, FMLN extends ObjectNode> FMLN registerFMLNode(N astNode, FMLN node) {
		nodesForAST.put(astNode, node);
		return node;
	}

	@Override
	public FragmentManager getFragmentManager() {
		return fragmentManager;
	}

	/*public TypeFactory getTypeFactory() {
		return typeFactory;
	}*/

	public FMLFactory getFMLFactory() {
		return fmlFactory;
	}

	public FlexoPropertyFactory getPropertyFactory() {
		return propertyFactory;
	}

	public FlexoBehaviourFactory getBehaviourFactory() {
		return behaviourFactory;
	}

	protected final void finalizeDeserialization() {

		/*System.out.println("Bon, on resout maintenant les types a resoudre");
		for (CustomType unresolvedType : typeFactory.getUnresolvedTypes()) {
			System.out.println(" > " + unresolvedType);
		}*/
		compilationUnitNode.initializePrettyPrint(compilationUnitNode, compilationUnitNode.makePrettyPrintContext());
		// typeFactory.resolveUnresovedTypes();
		finalizeDeserialization(compilationUnitNode);

		// Now ensure load of required imports, before to resolve all types
		for (ElementImportDeclaration elementImportDeclaration : getCompilationUnit().getElementImports()) {
			elementImportDeclaration.getReferencedObject();
			// System.out.println(" > Loading " + elementImportDeclaration.getReferencedObject());
			// System.out.println("resourceReference=" + elementImportDeclaration.getResourceReference());
			// System.out.println("objectReference=" + elementImportDeclaration.getObjectReference());
			if (elementImportDeclaration.getReferencedObject() instanceof FMLCompilationUnit) {
				System.out.println(((FMLCompilationUnit) elementImportDeclaration.getReferencedObject()).getResource());
			}
		}

		/*if (typeFactory.getUnresolvedTypes().size() > 0) {
			//System.out.println("Some more unresolved types, trying again");
			//for (CustomType unresolvedType : typeFactory.getUnresolvedTypes()) {
			//	System.out.println(" > " + unresolvedType);
			//}
			typeFactory.resolveUnresovedTypes();
			if (typeFactory.getUnresolvedTypes().size() > 0) {
				System.out.println("Atfer types resolution still some types unresolved");
				for (UnresolvedTypeReference unresolvedTypeReference : typeFactory.getUnresolvedTypes()) {
					// System.out.println(" > " + unresolvedType);
					getCompilationUnitNode().throwIssue("cannot_resolve " + unresolvedTypeReference.type, unresolvedTypeReference.fragment);
				}
			}
		}*/
	}

	@Override
	public RawSource getRawSource() {
		return rawSource;
	}

	/*@Override
	public void defaultCase(Node node) {
		super.defaultCase(node);
		if (node instanceof Token && !fmlNodes.isEmpty()) {
			FMLObjectNode<?, ?> currentNode = fmlNodes.peek();
			if (currentNode != null) {
				// System.out.println("Token: " + ((Token) node).getText() + " de " + ((Token) node).getLine() + ":" + ((Token)
				// node).getPos()
				// + ":" + ((Token) node).getOffset());
				currentNode.handleToken((Token) node);
			}
		}
	}*/

	public FMLCompilationUnit getCompilationUnit() {
		if (compilationUnitNode != null) {
			return compilationUnitNode.getModelObject();
		}
		return compilationUnit;
	}

	public FMLCompilationUnitNode getCompilationUnitNode() {
		return compilationUnitNode;
	}

	@Override
	public void inAFmlCompilationUnit(AFmlCompilationUnit node) {

		super.inAFmlCompilationUnit(node);
		push(compilationUnitNode = new FMLCompilationUnitNode(node, this));
		nodesForAST.put(node, compilationUnitNode);
	}

	@Override
	public void outAFmlCompilationUnit(AFmlCompilationUnit node) {
		super.outAFmlCompilationUnit(node);
		pop();
	}

	@Override
	public void inANamespaceDecl(ANamespaceDecl node) {
		super.inANamespaceDecl(node);
		push(retrieveFMLNode(node, n -> new NamespaceDeclarationNode(n, getMainAnalyzer())));
	}

	@Override
	public void outANamespaceDecl(ANamespaceDecl node) {
		super.outANamespaceDecl(node);
		pop();
	}

	@Override
	public void inAUseDecl(AUseDecl node) {
		super.inAUseDecl(node);
		push(retrieveFMLNode(node, n -> new UseDeclarationNode(n, getMainAnalyzer())));
	}

	@Override
	public void outAUseDecl(AUseDecl node) {
		super.outAUseDecl(node);
		pop();
	}

	@Override
	public void inAJavaImportImportDecl(AJavaImportImportDecl node) {
		super.inAJavaImportImportDecl(node);
		push(retrieveFMLNode(node, n -> new JavaImportNode(n, getMainAnalyzer())));
	}

	@Override
	public void outAJavaImportImportDecl(AJavaImportImportDecl node) {
		super.outAJavaImportImportDecl(node);
		pop();
	}

	@Override
	public void inANamedJavaImportImportDecl(ANamedJavaImportImportDecl node) {
		super.inANamedJavaImportImportDecl(node);
		push(retrieveFMLNode(node, n -> new NamedJavaImportNode(n, getMainAnalyzer())));
	}

	@Override
	public void outANamedJavaImportImportDecl(ANamedJavaImportImportDecl node) {
		super.outANamedJavaImportImportDecl(node);
		pop();
	}

	@Override
	public void inAUriImportImportDecl(AUriImportImportDecl node) {
		super.inAUriImportImportDecl(node);
		push(retrieveFMLNode(node, n -> new ElementImportNode(n, getMainAnalyzer())));
	}

	@Override
	public void outAUriImportImportDecl(AUriImportImportDecl node) {
		super.outAUriImportImportDecl(node);
		pop();
	}

	@Override
	public void inANamedUriImportImportDecl(ANamedUriImportImportDecl node) {
		super.inANamedUriImportImportDecl(node);
		push(retrieveFMLNode(node, n -> new ElementImportNode(n, getMainAnalyzer())));
	}

	@Override
	public void outANamedUriImportImportDecl(ANamedUriImportImportDecl node) {
		super.outANamedUriImportImportDecl(node);
		pop();
	}

	@Override
	public void inAModelDecl(AModelDecl node) {
		super.inAModelDecl(node);
		push(retrieveFMLNode(node, n -> new VirtualModelNode(n, getMainAnalyzer())));
	}

	@Override
	public void outAModelDecl(AModelDecl node) {
		super.outAModelDecl(node);
		pop();
	}

	@Override
	public void inAConceptDecl(AConceptDecl node) {
		super.inAConceptDecl(node);
		push(retrieveFMLNode(node, n -> new FlexoConceptNode(n, getMainAnalyzer())));
	}

	@Override
	public void outAConceptDecl(AConceptDecl node) {
		super.outAConceptDecl(node);
		pop();
	}

	@Override
	public void inAAbstractPropertyInnerConceptDecl(AAbstractPropertyInnerConceptDecl node) {
		super.inAAbstractPropertyInnerConceptDecl(node);
		push(retrieveFMLNode(node, n -> getPropertyFactory().makeAbstractPropertyNode(n)));
	}

	@Override
	public void outAAbstractPropertyInnerConceptDecl(AAbstractPropertyInnerConceptDecl node) {
		super.outAAbstractPropertyInnerConceptDecl(node);
		pop();
	}

	@Override
	public void inAJavaInnerConceptDecl(AJavaInnerConceptDecl node) {
		super.inAJavaInnerConceptDecl(node);
		// TODO handle short version of FlexoConceptInstanceRole declaration
		push(retrieveFMLNode(node, n -> getPropertyFactory().makeBasicPropertyNode(n)));
	}

	@Override
	public void outAJavaInnerConceptDecl(AJavaInnerConceptDecl node) {
		super.outAJavaInnerConceptDecl(node);
		pop();
	}

	@Override
	public void inAFmlInnerConceptDecl(AFmlInnerConceptDecl node) {
		super.inAFmlInnerConceptDecl(node);
		Class<? extends FlexoRole<?>> roleClass = getFMLFactory().getRoleClass(node.getRole());
		if (roleClass != null) {
			if (ModelSlot.class.isAssignableFrom(roleClass)) {
				push(retrieveFMLNode(node, n -> (ModelSlotPropertyNode) getPropertyFactory().makeModelSlotPropertyNode(n)));
			}
			else {
				push(retrieveFMLNode(node, n -> (FlexoRolePropertyNode) getPropertyFactory().makeFlexoRolePropertyNode(n)));
			}
		}
		else {
			compilationUnitNode.throwIssue("role_not_found: " + node.getRole().getText(), getFragment(node.getRole()));
		}
	}

	@Override
	public void outAFmlInnerConceptDecl(AFmlInnerConceptDecl node) {
		super.outAFmlInnerConceptDecl(node);
		Class<? extends FlexoRole<?>> roleClass = getFMLFactory().getRoleClass(node.getRole());
		if (roleClass != null) {
			pop();
		}
	}

	@Override
	public void inAFmlFullyQualifiedInnerConceptDecl(AFmlFullyQualifiedInnerConceptDecl node) {
		super.inAFmlFullyQualifiedInnerConceptDecl(node);
		Class<? extends FlexoRole<?>> roleClass = getFMLFactory().getRoleClass(node.getTaId(), node.getRole());
		if (ModelSlot.class.isAssignableFrom(roleClass)) {
			push(retrieveFMLNode(node, n -> (ModelSlotPropertyNode) getPropertyFactory().makeModelSlotPropertyNode(n)));
		}
		else {
			push(retrieveFMLNode(node, n -> (FlexoRolePropertyNode) getPropertyFactory().makeFlexoRolePropertyNode(n)));
		}
	}

	@Override
	public void outAFmlFullyQualifiedInnerConceptDecl(AFmlFullyQualifiedInnerConceptDecl node) {
		super.outAFmlFullyQualifiedInnerConceptDecl(node);
		pop();
	}

	@Override
	public void inAExpressionPropertyInnerConceptDecl(AExpressionPropertyInnerConceptDecl node) {
		super.inAExpressionPropertyInnerConceptDecl(node);
		push(retrieveFMLNode(node, n -> getPropertyFactory().makeExpressionPropertyNode(n)));
	}

	@Override
	public void outAExpressionPropertyInnerConceptDecl(AExpressionPropertyInnerConceptDecl node) {
		super.outAExpressionPropertyInnerConceptDecl(node);
		pop();
	}

	@Override
	public void inAGetSetPropertyInnerConceptDecl(AGetSetPropertyInnerConceptDecl node) {
		super.inAGetSetPropertyInnerConceptDecl(node);
		push(retrieveFMLNode(node, n -> getPropertyFactory().makeGetSetPropertyNode(n)));
	}

	@Override
	public void outAGetSetPropertyInnerConceptDecl(AGetSetPropertyInnerConceptDecl node) {
		super.outAGetSetPropertyInnerConceptDecl(node);
		pop();
	}

	@Override
	public void inAGetDecl(AGetDecl node) {
		// TODO Auto-generated method stub
		super.inAGetDecl(node);
	}

	@Override
	public void inABehaviourDeclarationInnerConceptDecl(ABehaviourDeclarationInnerConceptDecl node) {
		super.inABehaviourDeclarationInnerConceptDecl(node);
		push(retrieveFMLNode(node, n -> (FlexoBehaviourNode) getBehaviourFactory().makeBehaviourNode(n.getBehaviourDecl())));
	}

	@Override
	public void outABehaviourDeclarationInnerConceptDecl(ABehaviourDeclarationInnerConceptDecl node) {
		pop();
	}

	@Override
	public void inABasicAnnotationAnnotation(ABasicAnnotationAnnotation node) {
		super.inABasicAnnotationAnnotation(node);
		push(retrieveFMLNode(node, n -> new BasicMetaDataNode(n, getMainAnalyzer())));
	}

	@Override
	public void outABasicAnnotationAnnotation(ABasicAnnotationAnnotation node) {
		super.inABasicAnnotationAnnotation(node);
		pop();
	}

	@Override
	public void inASingleAnnotationAnnotation(ASingleAnnotationAnnotation node) {
		super.inASingleAnnotationAnnotation(node);
		push(retrieveFMLNode(node, n -> new SingleMetaDataNode(n, getMainAnalyzer())));
	}

	@Override
	public void outASingleAnnotationAnnotation(ASingleAnnotationAnnotation node) {
		super.outASingleAnnotationAnnotation(node);
		pop();
	}

	@Override
	public void inAComplexAnnotationAnnotation(AComplexAnnotationAnnotation node) {
		super.inAComplexAnnotationAnnotation(node);
		push(retrieveFMLNode(node, n -> new MultiValuedMetaDataNode(n, getMainAnalyzer())));
	}

	@Override
	public void outAComplexAnnotationAnnotation(AComplexAnnotationAnnotation node) {
		super.outAComplexAnnotationAnnotation(node);
		pop();
	}

	@Override
	public void inAAnnotationKeyValuePair(AAnnotationKeyValuePair node) {
		super.inAAnnotationKeyValuePair(node);
		push(retrieveFMLNode(node, n -> new MetaDataKeyValueNode(n, getMainAnalyzer())));
	}

	@Override
	public void outAAnnotationKeyValuePair(AAnnotationKeyValuePair node) {
		super.outAAnnotationKeyValuePair(node);
		pop();
	}

	@Override
	public void inAListAnnotationAnnotation(AListAnnotationAnnotation node) {
		super.inAListAnnotationAnnotation(node);
		push(retrieveFMLNode(node, n -> new ListMetaDataNode(n, getMainAnalyzer())));
	}

	@Override
	public void outAListAnnotationAnnotation(AListAnnotationAnnotation node) {
		super.outAListAnnotationAnnotation(node);
		pop();
	}

	@Override
	public void inAPrimitiveFormalArgument(APrimitiveFormalArgument node) {
		super.inAPrimitiveFormalArgument(node);
		push(retrieveFMLNode(node, n -> new BehaviourParameterNode(n, getMainAnalyzer())));
	}

	@Override
	public void outAPrimitiveFormalArgument(APrimitiveFormalArgument node) {
		super.outAPrimitiveFormalArgument(node);
		pop();
	}

	@Override
	public void inAComplexFormalArgument(AComplexFormalArgument node) {
		super.inAComplexFormalArgument(node);
		push(retrieveFMLNode(node, n -> new BehaviourParameterNode(n, getMainAnalyzer())));
	}

	@Override
	public void outAComplexFormalArgument(AComplexFormalArgument node) {
		super.outAComplexFormalArgument(node);
		pop();
	}

	private boolean insideBehaviourBody = false;

	@Override
	public void inABlockFlexoBehaviourBody(ABlockFlexoBehaviourBody node) {
		super.inABlockFlexoBehaviourBody(node);
		insideBehaviourBody = true;
	}

	@Override
	public void outABlockFlexoBehaviourBody(ABlockFlexoBehaviourBody node) {
		super.outABlockFlexoBehaviourBody(node);
		insideBehaviourBody = false;
	}

	@Override
	protected boolean handleFMLArgument() {
		return !insideBehaviourBody && !insideMatchAction;
	}

}
