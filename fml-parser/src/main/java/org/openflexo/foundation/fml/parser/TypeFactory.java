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
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.DiamondType;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.connie.type.UnresolvedType;
import org.openflexo.connie.type.WildcardTypeImpl.DefaultWildcardType;
import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.TechnologySpecificType;
import org.openflexo.foundation.fml.UseModelSlotDeclaration;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.parser.analysis.DepthFirstAdapter;
import org.openflexo.foundation.fml.parser.node.ABooleanPrimitiveType;
import org.openflexo.foundation.fml.parser.node.ABytePrimitiveType;
import org.openflexo.foundation.fml.parser.node.ACastFmlUnaryExpNotPlusMinus;
import org.openflexo.foundation.fml.parser.node.ACharPrimitiveType;
import org.openflexo.foundation.fml.parser.node.AComplexType;
import org.openflexo.foundation.fml.parser.node.ACompositeTident;
import org.openflexo.foundation.fml.parser.node.ACompositeTidentSuffix;
import org.openflexo.foundation.fml.parser.node.ADiamondTypeArguments;
import org.openflexo.foundation.fml.parser.node.ADoublePrimitiveType;
import org.openflexo.foundation.fml.parser.node.AEscapedIdentifierPrefix;
import org.openflexo.foundation.fml.parser.node.AExtendsWildcardBounds;
import org.openflexo.foundation.fml.parser.node.AFloatPrimitiveType;
import org.openflexo.foundation.fml.parser.node.AFmlType;
import org.openflexo.foundation.fml.parser.node.AGtTypeArguments;
import org.openflexo.foundation.fml.parser.node.AIntPrimitiveType;
import org.openflexo.foundation.fml.parser.node.ALongPrimitiveType;
import org.openflexo.foundation.fml.parser.node.ANormalIdentifierPrefix;
import org.openflexo.foundation.fml.parser.node.ANormalTypeDeclType;
import org.openflexo.foundation.fml.parser.node.APrimitiveType;
import org.openflexo.foundation.fml.parser.node.AReferenceType;
import org.openflexo.foundation.fml.parser.node.AReferenceTypeArgument;
import org.openflexo.foundation.fml.parser.node.AShortPrimitiveType;
import org.openflexo.foundation.fml.parser.node.AShrTypeArguments;
import org.openflexo.foundation.fml.parser.node.ASimpleQualifiedArgument;
import org.openflexo.foundation.fml.parser.node.ASuperWildcardBounds;
import org.openflexo.foundation.fml.parser.node.ATechnologySpecificType;
import org.openflexo.foundation.fml.parser.node.ATypeArgumentList;
import org.openflexo.foundation.fml.parser.node.ATypeArgumentListHead;
import org.openflexo.foundation.fml.parser.node.AUshrTypeArguments;
import org.openflexo.foundation.fml.parser.node.AVoidType;
import org.openflexo.foundation.fml.parser.node.AWildcardTypeArgument;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PCompositeTident;
import org.openflexo.foundation.fml.parser.node.PCompositeTidentSuffix;
import org.openflexo.foundation.fml.parser.node.PIdentifierPrefix;
import org.openflexo.foundation.fml.parser.node.PReferenceType;
import org.openflexo.foundation.fml.parser.node.PTypeArgumentList;
import org.openflexo.foundation.fml.parser.node.PTypeArgumentListHead;
import org.openflexo.foundation.fml.parser.node.PTypeArguments;
import org.openflexo.foundation.fml.parser.node.TIdent;
import org.openflexo.foundation.technologyadapter.SpecificTypeInfo;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;

/**
 * This class implements the semantics analyzer for a parsed Type<br>
 * Its main purpose is to structurally build a {@link Type} from a parsed AST (an instance of {@link Node}).<br>
 * No semantics nor type checking is performed at this stage
 * 
 * @author sylvain
 * 
 */
public class TypeFactory extends DepthFirstAdapter {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SemanticsAnalyzerFactory.class.getPackage().getName());

	private final AbstractFMLTypingSpace typingSpace;

	private final Map<Node, Type> typeNodes;
	private Node rootNode = null;

	public static Type makeType(Node node, AbstractFMLTypingSpace typingSpace) {

		// System.out.println("Resolving type for node " + node + " of " + node.getClass());
		TypeFactory bsa = new TypeFactory(node, typingSpace);
		node.apply(bsa);

		/*for (Node n : bsa.typeNodes.keySet()) {
			System.out.println("*** " + n + " -> " + TypeUtils.simpleRepresentation(bsa.typeNodes.get(n)));
		}*/

		// System.out.println("Returning: " + bsa.getType(node) + " of " + (bsa.getType(node) != null ? bsa.getType(node).getClass() :
		// null));
		return bsa.getType(node);
	}


	/**
	 * The qualified name written by an expression that is only a name ({@code Foo}, {@code java.util.List}), null for any other
	 * expression. Where the grammar cannot tell a type from an expression (the type of a cast, a qualified argument {@code x = Foo}),
	 * it parses an expression and the semantic layer reads it back as a type (CORE-F-5)
	 */
	public static String getTypeName(Node expression) {
		if (expression == null) {
			return null;
		}
		// SableCC prints the tokens of a node separated by spaces
		String text = expression.toString().replaceAll("\\s+", "");
		// The characters of the ident token: a letter or '_' first, then letters, digits, '$' and '_' (Unicode letters included)
		for (String segment : text.split("\\.", -1)) {
			if (segment.isEmpty() || !(Character.isLetter(segment.charAt(0)) || segment.charAt(0) == '_')) {
				return null;
			}
			for (int i = 1; i < segment.length(); i++) {
				char c = segment.charAt(i);
				if (!Character.isLetterOrDigit(c) && c != '$' && c != '_') {
					return null;
				}
			}
		}
		return text;
	}

	/**
	 * The type named by an expression that is only a name (see {@link #getTypeName(Node)}), null when it is not a name
	 */
	public static Type makeTypeFromExpression(Node expression, AbstractFMLTypingSpace typingSpace) {
		String typeName = getTypeName(expression);
		if (typeName == null) {
			return null;
		}
		return new TypeFactory(expression, typingSpace).makeType(typeName);
	}

	/**
	 * The type a qualified name resolves to in the typing space, without importing it; null when it resolves to nothing (an
	 * {@link UnresolvedType}). Used to decide whether a name is a type or a value
	 */
	public static Type lookupType(String typeName, AbstractFMLTypingSpace typingSpace) {
		if (typeName == null || typingSpace == null) {
			return null;
		}
		Type returned = typingSpace.resolveType(typeName);
		if (returned == null || returned instanceof UnresolvedType) {
			return null;
		}
		return returned;
	}

	public static VirtualModelInstanceType makeVirtualModelInstanceType(Node node, AbstractFMLTypingSpace typingSpace) {
		Type type = makeType(node, typingSpace);
		if (type instanceof VirtualModelInstanceType) {
			return (VirtualModelInstanceType) type;
		}
		if (type instanceof UnresolvedType) {
			FMLTechnologyAdapter fmlTechnologyAdapter = typingSpace.getServiceManager().getTechnologyAdapterService()
					.getTechnologyAdapter(FMLTechnologyAdapter.class);
			return new VirtualModelInstanceType(((UnresolvedType) type).getUnresolvedTypeName(),
					fmlTechnologyAdapter.getVirtualModelInstanceTypeFactory());
		}
		logger.warning("Unexpected type: " + type);
		return null;
	}

	/**
	 * The concept type of a place where only a concept is expected (a select, a match, a new with a creation scheme): a type that does
	 * not resolve (an import of a unit that is not there) becomes an unresolved {@link FlexoConceptInstanceType} keeping the name
	 * written, so that what refers to it is kept in the model and printed back as written (CORE-D-58); the caller reports it. Null
	 * for a type that is neither.
	 */
	public static FlexoConceptInstanceType asFlexoConceptInstanceType(Type type, AbstractFMLTypingSpace typingSpace) {
		if (type instanceof FlexoConceptInstanceType) {
			return (FlexoConceptInstanceType) type;
		}
		if (type instanceof UnresolvedType && typingSpace != null && typingSpace.getServiceManager() != null) {
			FMLTechnologyAdapter fmlTechnologyAdapter = typingSpace.getServiceManager().getTechnologyAdapterService()
					.getTechnologyAdapter(FMLTechnologyAdapter.class);
			return new FlexoConceptInstanceType(((UnresolvedType) type).getUnresolvedTypeName(),
					fmlTechnologyAdapter.getFlexoConceptInstanceTypeFactory());
		}
		return null;
	}

	private TypeFactory(Node node, AbstractFMLTypingSpace typingSpace) {
		this.typingSpace = typingSpace;
		this.rootNode = node;
		typeNodes = new Hashtable<>();
	}

	public AbstractFMLTypingSpace getTypingSpace() {
		return typingSpace;
	}

	public Node getRootNode() {
		return rootNode;
	}

	private void registerTypeNode(Node n, Type t) {
		typeNodes.put(n, t);
	}

	protected Type getType(Node n) {
		if (n != null) {
			Type returned = typeNodes.get(n);

			if (returned == null) {
				if (n instanceof TIdent) {
					return makeType(((TIdent) n).getText());
				}
				if (n instanceof APrimitiveType) {
					return getType(((APrimitiveType) n).getPrimitiveType());
				}
				if (n instanceof AComplexType) {
					return getType(((AComplexType) n).getReferenceType());
				}
				if (n instanceof AReferenceTypeArgument) {
					return getType(((AReferenceTypeArgument) n).getReferenceType());
				}
				if (n instanceof AFmlType) {
					return getType(((AFmlType) n).getTechnologySpecificType());
				}
				if (n instanceof ANormalTypeDeclType) {
					return getType(((ANormalTypeDeclType) n).getReferenceType());
				}
				System.out.println("No expression registered for " + n + " of  " + n.getClass());
				/*System.out.println("rootNode: " + rootNode + " of " + rootNode.getClass());
				ASTDebugger.debug(rootNode);
				Thread.dumpStack();
				for (Iterator<Node> it = typeNodes.keySet().iterator(); it.hasNext();) {
					Node node = it.next();
					System.out.println(" > " + node + " -> " + typeNodes.get(node));
				}
				System.exit(-1);*/
			}
			return returned;
		}
		return null;
	}

	@Override
	public void outAVoidType(AVoidType node) {
		super.outAVoidType(node);
		registerTypeNode(node, Void.TYPE);
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
			if (node.getWildcardBounds() instanceof AExtendsWildcardBounds) {
				Type upperBound = getType(((AExtendsWildcardBounds) node.getWildcardBounds()).getReferenceType());
				registerTypeNode(node, DefaultWildcardType.makeUpperBoundWilcard(upperBound));
			}
			else if (node.getWildcardBounds() instanceof ASuperWildcardBounds) {
				Type lowerBound = getType(((ASuperWildcardBounds) node.getWildcardBounds()).getReferenceType());
				registerTypeNode(node, DefaultWildcardType.makeLowerBoundWilcard(lowerBound));
			}
			// registerTypeNode(node, getType(node.getWildcardBounds()));
		}
		else {
			registerTypeNode(node, new DefaultWildcardType());
		}
	}

	/*@Override
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
	}*/

	private Type makeType(String typeName) {
		if (getTypingSpace() != null) {
			Type returnedType = getTypingSpace().resolveType(typeName);
			if (!getTypingSpace().isTypeImported(returnedType)) {
				getTypingSpace().importType(returnedType);
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
				if (identifierPrefix instanceof ANormalIdentifierPrefix) {
					fullQualifiedName.append(((ANormalIdentifierPrefix) identifierPrefix).getIdent().getText() + ".");
				}
				if (identifierPrefix instanceof AEscapedIdentifierPrefix) {
					fullQualifiedName.append(
							FMLSemanticsAnalyzer.getEscapedLiteral(((AEscapedIdentifierPrefix) identifierPrefix).getEscapedIdentifier()));
				}
			}
			fullQualifiedName.append(node.getIdentifier().getText());
			for (PCompositeTidentSuffix pCompositeTidentSuffix : node.getSuffixes()) {
				fullQualifiedName.append("$" + ((ACompositeTidentSuffix) pCompositeTidentSuffix).getIdent().getText());
			}
			return makeType(fullQualifiedName.toString());
		}
		System.err.println("Unexpected " + aPCompositeTident + " of " + aPCompositeTident.getClass());
		return null;
	}

	private Type makeParameterizedType(Type baseType, List<Type> typeArguments) {
		if (baseType instanceof UnresolvedType) {
			return getTypingSpace().attemptToResolveType((UnresolvedType) baseType, typeArguments);
		}
		return new ParameterizedTypeImpl(baseType, typeArguments.toArray(new Type[typeArguments.size()]));

	}

	private Type makeType(PReferenceType referenceType) {
		if (referenceType instanceof AReferenceType) {
			if (((AReferenceType) referenceType).getArgs() == null) {
				return makeType(((AReferenceType) referenceType).getIdentifier());
			}
			Type baseType = makeType(((AReferenceType) referenceType).getIdentifier());
			List<Type> typeArguments = makeTypeArguments(((AReferenceType) referenceType).getArgs());
			return makeParameterizedType(baseType, typeArguments);
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
		if (someArgs instanceof ADiamondTypeArguments) {
			return Collections.singletonList(DiamondType.INSTANCE);
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
		returned.add(makeParameterizedType(baseType, typeArguments));
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
		returned.add(makeParameterizedType(baseType, typeArguments));
		return returned;
	}

	// Stack handling technology-specific types
	private Stack<SpecificTypeInfo> specificTypesInfoStack = new Stack<>();

	@Override
	public void inATechnologySpecificType(ATechnologySpecificType node) {
		super.inATechnologySpecificType(node);
		SpecificTypeInfo newSpecificTypeInfo = new SpecificTypeInfo(retrieveTechnologySpecificType(node.getIdent()));
		specificTypesInfoStack.push(newSpecificTypeInfo);

		if (typingSpace instanceof FMLTypingSpaceDuringParsing) {
			newSpecificTypeInfo
					.setSerializationForm(((FMLTypingSpaceDuringParsing) typingSpace).getAnalyzer().getFragment(node).getRawText());
		}

	}

	@Override
	public void outATechnologySpecificType(ATechnologySpecificType node) {
		super.outATechnologySpecificType(node);

		registerTypeNode(node, instantiateType(specificTypesInfoStack.pop()));
	}

	private Type instantiateType(SpecificTypeInfo specificTypeInfo) {
		TechnologyAdapter<?> ta = typingSpace.getServiceManager().getTechnologyAdapterService()
				.getTechnologyAdapter(specificTypeInfo.getTechnologyAdapterClass());
		return ta.instantiateType(specificTypeInfo);
	}

	/**
	 * The technology-specific type of a cast {@code (Foo(a=1)) o}: the grammar has a dedicated alternative for it (CORE-F-5), whose
	 * type is not a {@link ATechnologySpecificType} node. Only the type is walked, not the operand
	 */
	public static Type makeTechnologySpecificType(ACastFmlUnaryExpNotPlusMinus node, AbstractFMLTypingSpace typingSpace) {
		TypeFactory factory = new TypeFactory(node, typingSpace);
		SpecificTypeInfo specificTypeInfo = new SpecificTypeInfo(factory.retrieveTechnologySpecificType(node.getType()));
		if (typingSpace instanceof FMLTypingSpaceDuringParsing) {
			FMLCompilationUnitSemanticsAnalyzer analyzer = ((FMLTypingSpaceDuringParsing) typingSpace).getAnalyzer();
			specificTypeInfo.setSerializationForm(
					node.getType().getText() + "(" + analyzer.getFragment(node.getQualifiedArgumentList()).getRawText() + ")");
		}
		factory.specificTypesInfoStack.push(specificTypeInfo);
		node.getQualifiedArgumentList().apply(factory);
		factory.specificTypesInfoStack.pop();
		return factory.instantiateType(specificTypeInfo);
	}

	private Class<? extends TechnologySpecificType<?>> retrieveTechnologySpecificType(TIdent tsType) {

		String identifier = tsType.getText();
		for (UseModelSlotDeclaration useModelSlotDeclaration : typingSpace.getFMLCompilationUnit().getUseDeclarations()) {
			Class<? extends TechnologySpecificType<?>> technologySpecificType = typingSpace.getServiceManager()
					.getTechnologyAdapterService().getTechnologySpecificType(useModelSlotDeclaration.getModelSlotClass(), identifier);
			if (technologySpecificType != null) {
				return technologySpecificType;
			}
		}
		logger.warning("Unexpected " + tsType + " of " + tsType.getClass());
		return null;
	}

	@Override
	public final void inASimpleQualifiedArgument(ASimpleQualifiedArgument node) {
		super.inASimpleQualifiedArgument(node);
		if (!(typingSpace instanceof FMLTypingSpaceDuringParsing)) {
			return;
		}

		// A parameter whose value names a type (x = Foo) was a distinct alternative of the grammar, never handled here: it is still left
		// aside now that the grammar parses it as an expression (CORE-F-5)
		if (lookupType(getTypeName(node.getExpression()), typingSpace) != null) {
			return;
		}

		SpecificTypeInfo specificTypeInfo = specificTypesInfoStack.peek();
		DataBinding<Object> value = ExpressionFactory.makeDataBinding(node.getExpression(), typingSpace.getFMLCompilationUnit(),
				BindingDefinitionType.GET, Object.class, ((FMLTypingSpaceDuringParsing) typingSpace).getAnalyzer(), null);
		// System.out.println("value=" + value);
		// System.out.println("valid: " + value.isValid());
		// System.out.println("reason: " + value.invalidBindingReason());
		// System.out.println("isConstant: " + value.isConstant());

		try {
			Object parameterValue = value.getBindingValue(typingSpace.getFMLCompilationUnit());
			// System.out.println("parameterValue: " + parameterValue + " of " + (parameterValue != null ? parameterValue.getClass() :
			// null));
			specificTypeInfo.setParameter(node.getArgName().getText(), parameterValue);
		} catch (TypeMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullReferenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReflectiveOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*if (DataBinding.class.equals(TypeUtils.getBaseClass(fmlProperty.getType()))) {
			// logger.info("Set " + fmlProperty.getName() + " = " + value);
			// fmlProperty.set(value, modelObject);
			getModelObject().setValue((T) value);
		}
		else if (value.isConstant()) {
			Object constantValue = ((Constant) value.getExpression()).getValue();
			if (constantValue != null) {
				if (TypeUtils.isTypeAssignableFrom(fmlProperty.getType(), constantValue.getClass())) {
					// logger.info("Set " + fmlProperty.getName() + " = " + constantValue);
					// fmlProperty.set(constantValue, modelObject);
					getModelObject().setValue((T) constantValue);
				}
				else {
					logger.warning("Invalid value for property " + fmlProperty.getLabel() + " expected type: " + fmlProperty.getType()
							+ " value: " + constantValue + " of " + constantValue.getClass());
				}
			}
		}
		else {
			boolean found = false;
			if (getCompilationUnit() != null) {
				for (ElementImportDeclaration elementImportDeclaration : getCompilationUnit().getElementImports()) {
					// System.out.println(
					// "> J'ai deja: " + elementImportDeclaration.getAbbrev() + "=" + elementImportDeclaration.getReferencedObject());
					if (elementImportDeclaration.getAbbrev().equals(value.toString())) {
						// System.out.println("Trouve !!!");
						// fmlProperty.set(elementImportDeclaration.getReferencedObject(), modelObject);
						found = true;
						getModelObject().setValue((T) elementImportDeclaration.getReferencedObject());
					}
				}
			}
		
			if (!found) {
				logger.warning("Unexpected value for property " + fmlProperty.getLabel() + " expected type: " + fmlProperty.getType()
						+ " value: " + value);
			}
		}
		 */
	}

	@Override
	public final void outASimpleQualifiedArgument(ASimpleQualifiedArgument node) {
		super.outASimpleQualifiedArgument(node);
	}

}
