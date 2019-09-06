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

import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoConceptNode;
import org.openflexo.foundation.fml.parser.fmlnodes.JavaImportNode;
import org.openflexo.foundation.fml.parser.fmlnodes.MetaDataNode;
import org.openflexo.foundation.fml.parser.fmlnodes.NamedJavaImportNode;
import org.openflexo.foundation.fml.parser.fmlnodes.VirtualModelNode;
import org.openflexo.foundation.fml.parser.node.ABehaviourDeclarationInnerConceptDeclaration;
import org.openflexo.foundation.fml.parser.node.AConceptDeclaration;
import org.openflexo.foundation.fml.parser.node.AFmlCompilationUnit;
import org.openflexo.foundation.fml.parser.node.AJavaImportImportDeclaration;
import org.openflexo.foundation.fml.parser.node.AModelDeclaration;
import org.openflexo.foundation.fml.parser.node.ANamedJavaImportImportDeclaration;
import org.openflexo.foundation.fml.parser.node.APropertyDeclarationInnerConceptDeclaration;
import org.openflexo.foundation.fml.parser.node.ASingleAnnotation;
import org.openflexo.foundation.fml.parser.node.AValueAnnotation;
import org.openflexo.foundation.fml.parser.node.Start;
import org.openflexo.p2pp.RawSource;

/**
 * This class implements the main semantics analyzer for a parsed FML compilation unit.<br>
 * 
 * @author sylvain
 * 
 */
public class MainSemanticsAnalyzer extends FMLSemanticsAnalyzer {

	private final TypeFactory typeFactory;
	private final FlexoPropertyFactory propertyFactory;
	private final FlexoBehaviourFactory behaviourFactory;

	// Raw source as when this analyzer was last parsed
	private RawSource rawSource;

	private FragmentManager fragmentManager;

	private FMLCompilationUnitNode compilationUnitNode;

	public MainSemanticsAnalyzer(FMLModelFactory factory, Start tree, RawSource rawSource) {
		super(factory, tree);
		this.rawSource = rawSource;
		fragmentManager = new FragmentManager(rawSource);
		typeFactory = new TypeFactory(this);
		propertyFactory = new FlexoPropertyFactory(this);
		behaviourFactory = new FlexoBehaviourFactory(this);
		if (tree != null) {
			tree.apply(this);
			finalizeDeserialization();
		}
	}

	@Override
	public MainSemanticsAnalyzer getMainAnalyzer() {
		return this;
	}

	@Override
	public Start getRootNode() {
		return (Start) super.getRootNode();
	}

	@Override
	public FragmentManager getFragmentManager() {
		return fragmentManager;
	}

	public TypeFactory getTypeFactory() {
		return typeFactory;
	}

	public FlexoPropertyFactory getPropertyFactory() {
		return propertyFactory;
	}

	public FlexoBehaviourFactory getBehaviourFactory() {
		return behaviourFactory;
	}

	protected final void finalizeDeserialization() {
		compilationUnitNode.initializePrettyPrint(compilationUnitNode, compilationUnitNode.makePrettyPrintContext());
		typeFactory.resolveUnresovedTypes();
		finalizeDeserialization(compilationUnitNode);
	}

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
		return compilationUnitNode.getModelObject();
	}

	public FMLCompilationUnitNode getCompilationUnitNode() {
		return compilationUnitNode;
	}

	@Override
	public void inAFmlCompilationUnit(AFmlCompilationUnit node) {
		super.inAFmlCompilationUnit(node);
		push(compilationUnitNode = new FMLCompilationUnitNode(node, this));
	}

	@Override
	public void outAFmlCompilationUnit(AFmlCompilationUnit node) {
		super.outAFmlCompilationUnit(node);
		pop();
	}

	@Override
	public void inAJavaImportImportDeclaration(AJavaImportImportDeclaration node) {
		super.inAJavaImportImportDeclaration(node);
		push(new JavaImportNode(node, this));
	}

	@Override
	public void outAJavaImportImportDeclaration(AJavaImportImportDeclaration node) {
		super.outAJavaImportImportDeclaration(node);
		JavaImportNode returned = pop();
		// System.out.println("Je cree un import depuis " + returned.getLastParsedFragment() + " which is ["
		// + returned.getLastParsedFragment().getRawText() + "]");

	}

	@Override
	public void inANamedJavaImportImportDeclaration(ANamedJavaImportImportDeclaration node) {
		super.inANamedJavaImportImportDeclaration(node);
		push(new NamedJavaImportNode(node, this));
	}

	@Override
	public void outANamedJavaImportImportDeclaration(ANamedJavaImportImportDeclaration node) {
		super.outANamedJavaImportImportDeclaration(node);
		pop();
	}

	@Override
	public void inAModelDeclaration(AModelDeclaration node) {
		super.inAModelDeclaration(node);
		push(new VirtualModelNode(node, this));
		System.out.println(">>> On entre dans le modele");
	}

	@Override
	public void outAModelDeclaration(AModelDeclaration node) {
		super.outAModelDeclaration(node);
		pop();
		System.out.println("<<< On sort du modele");
	}

	@Override
	public void inAConceptDeclaration(AConceptDeclaration node) {
		super.inAConceptDeclaration(node);
		// System.out.println("DEBUT Nouveau concept " + node.getIdentifier().getText());
		push(new FlexoConceptNode(node, this));
	}

	@Override
	public void outAConceptDeclaration(AConceptDeclaration node) {
		super.outAConceptDeclaration(node);
		pop();
	}

	@Override
	public void inAPropertyDeclarationInnerConceptDeclaration(APropertyDeclarationInnerConceptDeclaration node) {
		super.inAPropertyDeclarationInnerConceptDeclaration(node);
		push(getPropertyFactory().makePropertyNode(node.getPropertyDeclaration()));
	}

	@Override
	public void outAPropertyDeclarationInnerConceptDeclaration(APropertyDeclarationInnerConceptDeclaration node) {
		super.outAPropertyDeclarationInnerConceptDeclaration(node);
		pop();
	}

	@Override
	public void inABehaviourDeclarationInnerConceptDeclaration(ABehaviourDeclarationInnerConceptDeclaration node) {
		super.inABehaviourDeclarationInnerConceptDeclaration(node);
		push(getBehaviourFactory().makeBehaviourNode(node.getBehaviourDeclaration()));
	}

	@Override
	public void outABehaviourDeclarationInnerConceptDeclaration(ABehaviourDeclarationInnerConceptDeclaration node) {
		super.outABehaviourDeclarationInnerConceptDeclaration(node);
		pop();
	}

	@Override
	public void inASingleAnnotation(ASingleAnnotation node) {
		super.inASingleAnnotation(node);
		System.out.println("Tiens une annotation: " + node);
		System.exit(-1);
	}

	@Override
	public void inAValueAnnotation(AValueAnnotation node) {
		super.inAValueAnnotation(node);
		System.out.println("Tiens une value annotation: " + node);
		push(new MetaDataNode(node, this));
	}

	@Override
	public void outAValueAnnotation(AValueAnnotation node) {
		super.outAValueAnnotation(node);
		pop();
	}

}
