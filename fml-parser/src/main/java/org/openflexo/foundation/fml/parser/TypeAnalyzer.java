/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.ContextualizedBindable;
import org.openflexo.connie.expr.BindingValue;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.connie.type.UnresolvedType;
import org.openflexo.connie.type.WildcardTypeImpl;
import org.openflexo.foundation.fml.parser.analysis.DepthFirstAdapter;
import org.openflexo.foundation.fml.parser.node.ABooleanPrimitiveType;
import org.openflexo.foundation.fml.parser.node.ABytePrimitiveType;
import org.openflexo.foundation.fml.parser.node.ACharPrimitiveType;
import org.openflexo.foundation.fml.parser.node.AComplexType;
import org.openflexo.foundation.fml.parser.node.ACompositeTident;
import org.openflexo.foundation.fml.parser.node.ADoublePrimitiveType;
import org.openflexo.foundation.fml.parser.node.AExtendsWildcardBounds;
import org.openflexo.foundation.fml.parser.node.AFloatPrimitiveType;
import org.openflexo.foundation.fml.parser.node.AGtTypeArguments;
import org.openflexo.foundation.fml.parser.node.AIdentifierPrefix;
import org.openflexo.foundation.fml.parser.node.AIntPrimitiveType;
import org.openflexo.foundation.fml.parser.node.ALongPrimitiveType;
import org.openflexo.foundation.fml.parser.node.APrimitiveType;
import org.openflexo.foundation.fml.parser.node.AReferenceType;
import org.openflexo.foundation.fml.parser.node.AReferenceTypeArgument;
import org.openflexo.foundation.fml.parser.node.AShortPrimitiveType;
import org.openflexo.foundation.fml.parser.node.AShrTypeArguments;
import org.openflexo.foundation.fml.parser.node.ASuperWildcardBounds;
import org.openflexo.foundation.fml.parser.node.ATypeArgumentList;
import org.openflexo.foundation.fml.parser.node.ATypeArgumentListHead;
import org.openflexo.foundation.fml.parser.node.AUshrTypeArguments;
import org.openflexo.foundation.fml.parser.node.AVoidType;
import org.openflexo.foundation.fml.parser.node.AWildcardTypeArgument;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PCompositeTident;
import org.openflexo.foundation.fml.parser.node.PIdentifierPrefix;
import org.openflexo.foundation.fml.parser.node.PReferenceType;
import org.openflexo.foundation.fml.parser.node.PType;
import org.openflexo.foundation.fml.parser.node.PTypeArgumentList;
import org.openflexo.foundation.fml.parser.node.PTypeArgumentListHead;
import org.openflexo.foundation.fml.parser.node.PTypeArguments;

/**
 * This class implements the semantics analyzer for a parsed {@link BindingValue}<br>
 * Its main purpose is to structurally build a binding from a parsed AST (an instance of PPrimary).<br>
 * No semantics nor type checking is performed at this stage
 * 
 * @author sylvain
 * 
 */
class TypeAnalyzer extends DepthFirstAdapter {

	private final ExpressionSemanticsAnalyzer expressionAnalyzer;

	private final Map<Node, Type> typeNodes;
	private PType rootNode = null;
	// private int depth = -1;
	private String currentTypeRepresentation;

	int ident = 0;

	/*private boolean weAreDealingWithTheRightType() {
		return depth == 0;
	}*/

	public static Type makeType(PType node, ExpressionSemanticsAnalyzer expressionAnalyzer) {

		// System.out.println("Resolving type for node " + node + " of " + node.getClass());
		TypeAnalyzer bsa = new TypeAnalyzer(node, expressionAnalyzer);
		node.apply(bsa);

		/*for (Node n : bsa.typeNodes.keySet()) {
			System.out.println("*** " + n + " -> " + TypeUtils.simpleRepresentation(bsa.typeNodes.get(n)));
		}*/

		// System.out.println("Returning: " + bsa.getType(node) + " of " + bsa.getType(node).getClass());
		return bsa.getType(node);
	}

	private TypeAnalyzer(PType node, ExpressionSemanticsAnalyzer expressionAnalyzer) {
		this.expressionAnalyzer = expressionAnalyzer;
		this.rootNode = node;
		typeNodes = new Hashtable<>();
	}

	public Bindable getBindable() {
		return expressionAnalyzer.getBindable();
	}

	public ContextualizedBindable getContextualizedBindable() {
		if (getBindable() instanceof ContextualizedBindable) {
			return (ContextualizedBindable) getBindable();
		}
		return null;
	}

	private void registerTypeNode(Node n, Type t) {
		typeNodes.put(n, t);
	}

	protected Type getType(Node n) {
		if (n != null) {
			Type returned = typeNodes.get(n);

			if (returned == null) {
				if (n instanceof APrimitiveType) {
					return getType(((APrimitiveType) n).getPrimitiveType());
				}
				if (n instanceof AComplexType) {
					return getType(((AComplexType) n).getReferenceType());
				}
				if (n instanceof AReferenceTypeArgument) {
					return getType(((AReferenceTypeArgument) n).getReferenceType());
				}
				System.out.println("No expression registered for " + n + " of  " + n.getClass());
			}
			return returned;
		}
		return null;
	}

	@Override
	public void defaultIn(Node node) {
		super.defaultIn(node);
		ident++;
		// System.out.println(StringUtils.buildWhiteSpaceIndentation(ident) + " > " + node.getClass().getSimpleName() + " " + node);
	}

	@Override
	public void defaultOut(Node node) {
		// TODO Auto-generated method stub
		super.defaultOut(node);
		ident--;
	}

	/*@Override
	public void inAPrimitiveType(APrimitiveType node) {
		super.inAPrimitiveType(node);
		depth++;
	}
	
	@Override
	public void outAPrimitiveType(APrimitiveType node) {
		super.outAPrimitiveType(node);
		depth--;
	}
	
	@Override
	public void inAComplexType(AComplexType node) {
		super.inAComplexType(node);
		depth++;
	}
	
	@Override
	public void outAComplexType(AComplexType node) {
		super.outAComplexType(node);
		depth--;
	}
	
	@Override
	public void inAVoidType(AVoidType node) {
		super.inAVoidType(node);
		depth++;
	}*/

	@Override
	public void outAVoidType(AVoidType node) {
		super.outAVoidType(node);
		registerTypeNode(node, Void.TYPE);
		// depth--;
	}

	@Override
	public void outAIntPrimitiveType(AIntPrimitiveType node) {
		super.outAIntPrimitiveType(node);
		registerTypeNode(node, Integer.TYPE);
	}

	@Override
	public void outABytePrimitiveType(ABytePrimitiveType node) {
		super.outABytePrimitiveType(node);
		registerTypeNode(node, Byte.TYPE);
	}

	@Override
	public void outAShortPrimitiveType(AShortPrimitiveType node) {
		super.outAShortPrimitiveType(node);
		registerTypeNode(node, Short.TYPE);
	}

	@Override
	public void outALongPrimitiveType(ALongPrimitiveType node) {
		super.outALongPrimitiveType(node);
		registerTypeNode(node, Long.TYPE);
	}

	@Override
	public void outAFloatPrimitiveType(AFloatPrimitiveType node) {
		super.outAFloatPrimitiveType(node);
		registerTypeNode(node, Float.TYPE);
	}

	@Override
	public void outADoublePrimitiveType(ADoublePrimitiveType node) {
		super.outADoublePrimitiveType(node);
		registerTypeNode(node, Double.TYPE);
	}

	@Override
	public void outACharPrimitiveType(ACharPrimitiveType node) {
		super.outACharPrimitiveType(node);
		registerTypeNode(node, Character.TYPE);
	}

	@Override
	public void outABooleanPrimitiveType(ABooleanPrimitiveType node) {
		super.outABooleanPrimitiveType(node);
		registerTypeNode(node, Boolean.TYPE);
	}

	@Override
	public void outACompositeTident(ACompositeTident node) {
		super.outACompositeTident(node);
		registerTypeNode(node, makeType(node));
	}

	@Override
	public void outAReferenceType(AReferenceType node) {
		super.outAReferenceType(node);
		registerTypeNode(node, makeType(node));
	}

	@Override
	public void outAWildcardTypeArgument(AWildcardTypeArgument node) {
		super.outAWildcardTypeArgument(node);
		if (node.getWildcardBounds() != null) {
			registerTypeNode(node, getType(node.getWildcardBounds()));
		}
	}

	@Override
	public void outAExtendsWildcardBounds(AExtendsWildcardBounds node) {
		super.outAExtendsWildcardBounds(node);
		Type upperBound = getType(node.getReferenceType());
		registerTypeNode(node, WildcardTypeImpl.makeUpperBoundWilcard(upperBound));
	}

	@Override
	public void outASuperWildcardBounds(ASuperWildcardBounds node) {
		super.outASuperWildcardBounds(node);
		Type lowerBound = getType(node.getReferenceType());
		registerTypeNode(node, WildcardTypeImpl.makeLowerBoundWilcard(lowerBound));
	}

	private Type makeType(String typeName) {
		if (getContextualizedBindable() != null) {
			Type returnedType = getContextualizedBindable().resolveType(typeName);
			if (getContextualizedBindable().shouldImportType(returnedType) && !getContextualizedBindable().isTypeImported(returnedType)) {
				getContextualizedBindable().importType(returnedType);
			}
			return returnedType;
		}
		else {
			try {
				return Class.forName(typeName);
			} catch (ClassNotFoundException e1) {
				return new UnresolvedType(typeName);
			}
		}
	}

	private Type makeType(PCompositeTident aPCompositeTident) {
		if (aPCompositeTident instanceof ACompositeTident) {
			ACompositeTident node = (ACompositeTident) aPCompositeTident;
			StringBuffer fullQualifiedName = new StringBuffer();
			for (PIdentifierPrefix identifierPrefix : node.getPrefixes()) {
				fullQualifiedName.append(((AIdentifierPrefix) identifierPrefix).getLidentifier().getText() + ".");
			}
			fullQualifiedName.append(node.getIdentifier().getText());
			return makeType(fullQualifiedName.toString());
		}
		System.err.println("Unexpected " + aPCompositeTident + " of " + aPCompositeTident.getClass());
		return null;
	}

	private Type makeType(PReferenceType referenceType) {
		if (referenceType instanceof AReferenceType) {
			if (((AReferenceType) referenceType).getArgs() == null) {
				return makeType(((AReferenceType) referenceType).getIdentifier());
			}
			Type baseType = makeType(((AReferenceType) referenceType).getIdentifier());
			List<Type> typeArguments = makeTypeArguments(((AReferenceType) referenceType).getArgs());
			return new ParameterizedTypeImpl(baseType, typeArguments.toArray(new Type[typeArguments.size()]));
		}
		System.err.println("Unexpected " + referenceType + " of " + referenceType.getClass());
		return null;
	}

	private List<Type> makeTypeArguments(PTypeArguments someArgs) {
		if (someArgs instanceof AGtTypeArguments) {
			return makeTypeArguments(((AGtTypeArguments) someArgs).getTypeArgumentList());
		}
		if (someArgs instanceof AShrTypeArguments) {
			List<Type> returned = makeTypeArguments(((AShrTypeArguments) someArgs).getTypeArgumentListHead(),
					((AShrTypeArguments) someArgs).getIdentifier(), ((AShrTypeArguments) someArgs).getTypeArgumentList());
			return returned;
		}
		if (someArgs instanceof AUshrTypeArguments) {
			return makeTypeArguments(((AUshrTypeArguments) someArgs).getHeads1(), ((AUshrTypeArguments) someArgs).getSpecifier1(),
					((AUshrTypeArguments) someArgs).getHeads2(), ((AUshrTypeArguments) someArgs).getSpecifier2(),
					((AUshrTypeArguments) someArgs).getTypeArgumentList());
		}
		System.err.println("Unexpected " + someArgs + " of " + someArgs.getClass());
		return null;
	}

	// type_argument_list = type_argument_list_head* type_argument;

	// type_argument_list_head = type_argument comma;

	private List<Type> makeTypeArguments(PTypeArgumentList someArgs) {
		if (someArgs instanceof ATypeArgumentList) {
			List<Type> returned = new ArrayList<>();
			for (PTypeArgumentListHead pTypeArgumentListHead : ((ATypeArgumentList) someArgs).getTypeArgumentListHead()) {
				if (pTypeArgumentListHead instanceof ATypeArgumentListHead) {
					returned.add(getType(((ATypeArgumentListHead) pTypeArgumentListHead).getTypeArgument()));
				}
				else {
					System.err.println("Unexpected " + pTypeArgumentListHead + " of " + pTypeArgumentListHead.getClass());
				}
			}
			returned.add(getType(((ATypeArgumentList) someArgs).getTypeArgument()));
			return returned;
		}
		System.err.println("Unexpected " + someArgs + " of " + someArgs.getClass());
		return null;
	}

	private List<Type> makeTypeArguments(List<PTypeArgumentListHead> argumentListHead) {
		List<Type> returned = new ArrayList<Type>();
		for (PTypeArgumentListHead pTypeArgumentListHead : argumentListHead) {
			returned.add(getType(((ATypeArgumentListHead) pTypeArgumentListHead).getTypeArgument()));
		}
		return returned;
	}

	// Shr syntax
	// {shr} [lt1]:lt type_argument_list_head* [identifier]:composite_tident [lt2]:lt type_argument_list shr
	private List<Type> makeTypeArguments(List<PTypeArgumentListHead> argumentListHead, PCompositeTident identifier,
			PTypeArgumentList someArgs) {
		List<Type> returned = new ArrayList<Type>();
		returned.addAll(makeTypeArguments(argumentListHead));
		Type baseType = makeType(identifier);
		List<Type> typeArguments = makeTypeArguments(someArgs);
		returned.add(new ParameterizedTypeImpl(baseType, typeArguments.toArray(new Type[typeArguments.size()])));
		return returned;
	}

	// Ushr syntax
	// {ushr} [lt1]:lt [heads1]:type_argument_list_head* [specifier1]:composite_tident [lt2]:lt [heads2]:type_argument_list_head*
	// [specifier2]:composite_tident [lt3]:lt type_argument_list ushr
	private List<Type> makeTypeArguments(List<PTypeArgumentListHead> head1, PCompositeTident specifier1, List<PTypeArgumentListHead> head2,
			PCompositeTident specifier2, PTypeArgumentList someArgs) {
		List<Type> returned = new ArrayList<Type>();
		returned.addAll(makeTypeArguments(head1));

		Type baseType = makeType(specifier1);
		List<Type> typeArguments = makeTypeArguments(head2, specifier2, someArgs);
		returned.add(new ParameterizedTypeImpl(baseType, typeArguments.toArray(new Type[typeArguments.size()])));
		return returned;
	}

}
