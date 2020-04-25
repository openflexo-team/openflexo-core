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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.openflexo.connie.exception.NullReferenceException;
import org.openflexo.connie.exception.TypeMismatchException;
import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.ParameterizedTypeImpl;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.connie.type.UnresolvedType;
import org.openflexo.foundation.fml.ElementImportDeclaration;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoConceptInstanceType.DefaultFlexoConceptInstanceTypeFactory;
import org.openflexo.foundation.fml.FlexoConceptInstanceType.FlexoConceptInstanceTypeFactory;
import org.openflexo.foundation.fml.JavaImportDeclaration;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelInstanceType;
import org.openflexo.foundation.fml.VirtualModelInstanceType.DefaultVirtualModelInstanceTypeFactory;
import org.openflexo.foundation.fml.VirtualModelInstanceType.VirtualModelInstanceTypeFactory;
import org.openflexo.foundation.fml.parser.analysis.DepthFirstAdapter;
import org.openflexo.foundation.fml.parser.node.AAdditionalIdentifier;
import org.openflexo.foundation.fml.parser.node.ABooleanPrimitiveType;
import org.openflexo.foundation.fml.parser.node.AComplexType;
import org.openflexo.foundation.fml.parser.node.ACompositeIdent;
import org.openflexo.foundation.fml.parser.node.AConceptDecl;
import org.openflexo.foundation.fml.parser.node.ADiamondTypeArgumentsOrDiamond;
import org.openflexo.foundation.fml.parser.node.AFloatPrimitiveType;
import org.openflexo.foundation.fml.parser.node.AGtTypeArguments;
import org.openflexo.foundation.fml.parser.node.AIntPrimitiveType;
import org.openflexo.foundation.fml.parser.node.AModelDecl;
import org.openflexo.foundation.fml.parser.node.ANamedUriImportImportDecl;
import org.openflexo.foundation.fml.parser.node.APrimitiveType;
import org.openflexo.foundation.fml.parser.node.AReferenceType;
import org.openflexo.foundation.fml.parser.node.AReferenceTypeArgument;
import org.openflexo.foundation.fml.parser.node.AShrTypeArguments;
import org.openflexo.foundation.fml.parser.node.ATypeArgumentList;
import org.openflexo.foundation.fml.parser.node.ATypeArgumentListHead;
import org.openflexo.foundation.fml.parser.node.ATypeArgumentsTypeArgumentsOrDiamond;
import org.openflexo.foundation.fml.parser.node.AUshrTypeArguments;
import org.openflexo.foundation.fml.parser.node.AVoidType;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PAdditionalIdentifier;
import org.openflexo.foundation.fml.parser.node.PCompositeIdent;
import org.openflexo.foundation.fml.parser.node.PPrimitiveType;
import org.openflexo.foundation.fml.parser.node.PReferenceType;
import org.openflexo.foundation.fml.parser.node.PType;
import org.openflexo.foundation.fml.parser.node.PTypeArgument;
import org.openflexo.foundation.fml.parser.node.PTypeArgumentList;
import org.openflexo.foundation.fml.parser.node.PTypeArgumentListHead;
import org.openflexo.foundation.fml.parser.node.PTypeArguments;
import org.openflexo.foundation.fml.parser.node.PTypeArgumentsOrDiamond;
import org.openflexo.foundation.fml.parser.node.TIdentifier;
import org.openflexo.toolbox.StringUtils;

/**
 * @author sylvain
 * 
 */
public class TypeFactory extends SemanticsAnalyzerFactory {

	private static final Logger logger = Logger.getLogger(TypeFactory.class.getPackage().getName());

	private FlexoConceptInstanceTypeFactory FLEXO_CONCEPT_INSTANCE_TYPE_FACTORY;
	private VirtualModelInstanceTypeFactory<VirtualModelInstanceType> VIRTUAL_MODEL_INSTANCE_TYPE_FACTORY;

	private List<CustomType> unresolvedTypes;

	public TypeFactory(MainSemanticsAnalyzer analyzer) {
		super(analyzer);
		FLEXO_CONCEPT_INSTANCE_TYPE_FACTORY = new DefaultFlexoConceptInstanceTypeFactory(getFMLTechnologyAdapter()) {
			@Override
			public FlexoConcept resolveFlexoConcept(FlexoConceptInstanceType typeToResolve) {
				// System.out.println("Tiens, faut resoudre le concept " + typeToResolve);
				return tryToLookupConcept(typeToResolve.getConceptURI());
			}
		};
		VIRTUAL_MODEL_INSTANCE_TYPE_FACTORY = new DefaultVirtualModelInstanceTypeFactory(getFMLTechnologyAdapter()) {
			@Override
			public VirtualModel resolveVirtualModel(VirtualModelInstanceType typeToResolve) {
				VirtualModel returned = tryToLookupVirtualModel(typeToResolve.getConceptURI());
				if (returned != null) {
					return returned;
				}

				if (typeToResolve.getConceptURI().startsWith("@") && getAnalyzer().getCompilationUnit() != null) {
					String id = typeToResolve.getConceptURI().substring(1).trim();
					for (ElementImportDeclaration importDeclaration : getAnalyzer().getCompilationUnit().getElementImports()) {
						if (StringUtils.isNotEmpty(importDeclaration.getAbbrev()) && importDeclaration.getAbbrev().equals(id)) {
							try {
								String resourceReferenceValue = importDeclaration.getResourceReference()
										.getBindingValue(getAnalyzer().getCompilationUnit().getReflectedBindingEvaluationContext());
								String objectReferenceValue = importDeclaration.getObjectReference()
										.getBindingValue(getAnalyzer().getCompilationUnit().getReflectedBindingEvaluationContext());
								/*System.out.println("Tiens, c'est la que ca se passe !!!");
								System.out.println("resourceReferenceValue " + importDeclaration.getResourceReference() + "="
										+ resourceReferenceValue);
								System.out.println(
										"objectReferenceValue= " + importDeclaration.getObjectReference() + "=" + objectReferenceValue);
								DataBinding<String> resourceReference = importDeclaration.getResourceReference();
								System.out.println("binding: " + resourceReference);
								System.out.println(
										"valid: " + resourceReference.isValid() + " reason: " + resourceReference.invalidBindingReason());
								System.out.println("BM: " + resourceReference.getOwner().getBindingModel());
								for (int i = 0; i < resourceReference.getOwner().getBindingModel().getBindingVariablesCount(); i++) {
									BindingVariable bv = resourceReference.getOwner().getBindingModel().getBindingVariableAt(i);
									System.out.println(
											"  > " + bv.getLabel() + " = " + bv.getType() + " of " + bv.getClass().getSimpleName());
								}
								*/
								if (resourceReferenceValue != null) {
									typeToResolve.redefineWithURI(resourceReferenceValue);
								}
								// System.out.println(getAnalyzer().getCompilationUnit().getServiceManager().getVirtualModelLibrary()
								// .getCompilationUnitResource(resourceReferenceValue));
								// System.exit(-1);

								return null;
							} catch (TypeMismatchException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (NullReferenceException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// System.exit(-1);
						}
					}
				}

				logger.warning("Cannot find "
						+ typeToResolve.getConceptURI()/* + " getVirtualModel()=" + getVirtualModel()
														+ " virtualModelBeingDeserialized=" + virtualModelBeingDeserialized*/);

				/*System.out.println("On cherche " + typeToResolve.getConceptURI());
				for (ElementImportDeclaration importDeclaration : getAnalyzer().getCompilationUnit().getElementImports()) {
					System.out.println(" > " + importDeclaration.getAbbrev() + " " + importDeclaration);
				}
				Thread.dumpStack();
				System.exit(-1);*/

				return null;

			}
		};
		unresolvedTypes = new ArrayList<>();
	}

	public void resolveUnresovedTypes() {

		// System.out.println("resolveUnresovedTypes");

		/*for (CustomType unresolvedType : unresolvedTypes) {
			System.out.println(" **** " + unresolvedType);
		}*/

		// System.out.println("OK on y va");

		for (CustomType unresolvedType : unresolvedTypes) {
			// System.out.println(" **** " + unresolvedType);
			unresolvedType.resolve();
			// System.out.println("resolved: " + unresolvedType.isResolved());
		}

		// System.out.println("Done");
		// Thread.dumpStack();
	}

	public List<String> makeFullQualifiedIdentifierList(TIdentifier identifier, List<PAdditionalIdentifier> additionalIdentifiers) {
		List<String> returned = new ArrayList<>();
		returned.add(identifier.getText());
		for (PAdditionalIdentifier pAdditionalIdentifier : additionalIdentifiers) {
			if (pAdditionalIdentifier instanceof AAdditionalIdentifier) {
				returned.add(((AAdditionalIdentifier) pAdditionalIdentifier).getIdentifier().getText());
			}
		}
		return returned;
	}

	public String makeFullQualifiedIdentifier(TIdentifier identifier, List<PAdditionalIdentifier> additionalIdentifiers) {
		StringBuffer returned = new StringBuffer();
		returned.append(identifier.getText());
		for (PAdditionalIdentifier pAdditionalIdentifier : additionalIdentifiers) {
			if (pAdditionalIdentifier instanceof AAdditionalIdentifier) {
				returned.append("." + ((AAdditionalIdentifier) pAdditionalIdentifier).getIdentifier().getText());
			}
		}
		return returned.toString();
	}

	public String makeFullQualifiedIdentifier(PCompositeIdent compositeIdentifier) {
		if (compositeIdentifier instanceof ACompositeIdent) {
			return makeFullQualifiedIdentifier(((ACompositeIdent) compositeIdentifier).getIdentifier(),
					((ACompositeIdent) compositeIdentifier).getAdditionalIdentifiers());
		}
		return null;
	}

	public Type makeType(PCompositeIdent compositeIdentifier) {
		if (compositeIdentifier instanceof ACompositeIdent) {
			return makeType(((ACompositeIdent) compositeIdentifier).getIdentifier(),
					((ACompositeIdent) compositeIdentifier).getAdditionalIdentifiers());
		}
		else {
			logger.warning("Unexpected " + compositeIdentifier);
			return null;
		}
	}

	public Type makeType(PCompositeIdent compositeIdentifier, PTypeArgumentsOrDiamond args) {

		Type baseType = makeType(compositeIdentifier);
		if (args != null) {
			if (args instanceof ADiamondTypeArgumentsOrDiamond) {
				return baseType;
			}
			else if (args instanceof ATypeArgumentsTypeArgumentsOrDiamond) {
				if (baseType instanceof Class) {
					ATypeArgumentsTypeArgumentsOrDiamond tArgs = (ATypeArgumentsTypeArgumentsOrDiamond) args;
					List<Type> typeArgs = makeTypeArguments(tArgs.getTypeArguments());
					if (((Class) baseType).getTypeParameters().length == typeArgs.size()) {
						return new ParameterizedTypeImpl((Class) baseType, typeArgs.toArray(new Type[typeArgs.size()]));
					}
					else {
						logger.warning("Inconsistent arguments size for " + baseType + " args: " + typeArgs);
						return baseType;
					}
				}
				else {
					logger.warning("Unexpected " + args + " for type " + baseType);
					return baseType;
				}
			}
			logger.warning("Unimplemented " + compositeIdentifier);
		}
		return baseType;
	}

	public PrimitiveType makePrimitiveType(PPrimitiveType primitiveType) {
		if (primitiveType instanceof ABooleanPrimitiveType) {
			return PrimitiveType.Boolean;
		}
		else if (primitiveType instanceof AFloatPrimitiveType) {
			return PrimitiveType.Float;
		}
		else if (primitiveType instanceof AIntPrimitiveType) {
			return PrimitiveType.Integer;
		}
		return null;
	}

	public Type makeType(TIdentifier identifier, List<PAdditionalIdentifier> additionalIdentifiers) {
		String typeName = makeFullQualifiedIdentifier(identifier, additionalIdentifiers);

		try {
			return Class.forName(typeName);
		} catch (ClassNotFoundException e) {
			// OK, continue
		}
		for (JavaImportDeclaration javaImportDeclaration : getAnalyzer().getCompilationUnit().getJavaImports()) {
			if (typeName.equals(javaImportDeclaration.getClassName())) {
				try {
					return Class.forName(javaImportDeclaration.getFullQualifiedClassName());
				} catch (ClassNotFoundException e) {
					// OK, continue
				}
			}
		}

		Type conceptType = lookupConceptNamed(typeName);
		if (conceptType != null) {
			return conceptType;
		}

		logger.warning("Not found type: " + typeName);

		return new UnresolvedType(typeName);
	}

	/*
		public Type makeType(TIdentifier identifier, List<PAdditionalIdentifier> additionalIdentifiers) {
			String typeName = makeFullQualifiedIdentifier(identifier, additionalIdentifiers);
			try {
				return Class.forName(typeName);
			} catch (ClassNotFoundException e) {
				// OK, continue
			}
			for (JavaImportDeclaration javaImportDeclaration : analyser.getCompilationUnit().getJavaImports()) {
				if (typeName.equals(javaImportDeclaration.getClassName())) {
					try {
						return Class.forName(javaImportDeclaration.getFullQualifiedClassName());
					} catch (ClassNotFoundException e) {
						// OK, continue
					}
				}
			}
			return new UnresolvedType(typeName);
		}
	 */
	public Type makeType(PType pType) {
		if (pType == null) {
			logger.warning("Unexpected null type");
			return null;
		}
		if (pType instanceof AVoidType) {
			return Void.TYPE;
		}
		else if (pType instanceof APrimitiveType) {
			PPrimitiveType primitiveType = ((APrimitiveType) pType).getPrimitiveType();
			if (primitiveType instanceof ABooleanPrimitiveType) {
				return Boolean.TYPE;
			}
			else if (primitiveType instanceof AIntPrimitiveType) {
				return Integer.TYPE;
			}
			else if (primitiveType instanceof AFloatPrimitiveType) {
				return Float.TYPE;
			}
		}
		// else if (pType instanceof ASimpleType) {
		// return makeType(((ASimpleType) pType).getIdentifier(), ((ASimpleType) pType).getAdditionalIdentifiers());
		// }
		else if (pType instanceof AComplexType) {
			return makeType(((AComplexType) pType).getReferenceType());
		}
		logger.warning("Unexpected " + pType + " of " + pType.getClass());
		return null;
	}

	public Type makeType(PReferenceType referenceType) {
		if (referenceType instanceof AReferenceType) {
			if (((AReferenceType) referenceType).getArgs() == null) {
				return makeType(((AReferenceType) referenceType).getIdentifier());
			}
			Type baseType = makeType(((AReferenceType) referenceType).getIdentifier());
			if (baseType instanceof Class) {
				List<Type> typeArguments = makeTypeArguments(((AReferenceType) referenceType).getArgs());
				return new ParameterizedTypeImpl((Class) baseType, typeArguments.toArray(new Type[typeArguments.size()]));
			}
			else {
				logger.warning("Unexpected base type " + baseType + " for " + referenceType);
			}
		}
		logger.warning("Unexpected " + referenceType + " of " + referenceType.getClass());
		return null;
	}

	private List<Type> makeTypeArguments(PTypeArguments someArgs) {
		if (someArgs instanceof AGtTypeArguments) {
			return makeTypeArguments(((AGtTypeArguments) someArgs).getTypeArgumentList());
		}
		if (someArgs instanceof AShrTypeArguments) {
			return makeTypeArguments(((AShrTypeArguments) someArgs).getTypeArgumentListHead(),
					((AShrTypeArguments) someArgs).getTypeArgumentList());
		}
		if (someArgs instanceof AUshrTypeArguments) {
			return makeTypeArguments(((AUshrTypeArguments) someArgs).getHeads1(), ((AUshrTypeArguments) someArgs).getHeads2(),
					((AUshrTypeArguments) someArgs).getTypeArgumentList());
		}
		logger.warning("Unexpected " + someArgs + " of " + someArgs.getClass());
		return null;
	}

	private List<Type> makeTypeArguments(PTypeArgumentList someArgs) {
		if (someArgs instanceof ATypeArgumentList) {
			List<Type> returned = new ArrayList<>();
			returned.add(makeTypeArgument(((ATypeArgumentList) someArgs).getTypeArgument()));
			for (PTypeArgumentListHead pTypeArgumentListHead : ((ATypeArgumentList) someArgs).getTypeArgumentListHead()) {
				if (pTypeArgumentListHead instanceof ATypeArgumentListHead) {
					returned.add(makeTypeArgument(((ATypeArgumentListHead) pTypeArgumentListHead).getTypeArgument()));
				}
				else {
					logger.warning("Unexpected " + pTypeArgumentListHead + " of " + pTypeArgumentListHead.getClass());
				}
			}
			return returned;
		}
		logger.warning("Unexpected " + someArgs + " of " + someArgs.getClass());
		return null;
	}

	private List<Type> makeTypeArguments(List<PTypeArgumentListHead> argumentListHead) {
		List<Type> returned = new ArrayList<Type>();
		for (PTypeArgumentListHead pTypeArgumentListHead : argumentListHead) {
			returned.add(makeTypeArgument(((ATypeArgumentListHead) pTypeArgumentListHead).getTypeArgument()));
		}
		return returned;
	}

	private List<Type> makeTypeArguments(List<PTypeArgumentListHead> argumentListHead, PTypeArgumentList someArgs) {
		List<Type> returned = new ArrayList<Type>();
		returned.addAll(makeTypeArguments(argumentListHead));
		returned.addAll(makeTypeArguments(someArgs));
		return returned;
	}

	private List<Type> makeTypeArguments(List<PTypeArgumentListHead> head1, List<PTypeArgumentListHead> head2, PTypeArgumentList someArgs) {
		List<Type> returned = new ArrayList<Type>();
		returned.addAll(makeTypeArguments(head1));
		returned.addAll(makeTypeArguments(head2));
		returned.addAll(makeTypeArguments(someArgs));
		return returned;
	}

	private Type makeTypeArgument(PTypeArgument arg) {
		if (arg instanceof AReferenceTypeArgument) {
			return makeType(((AReferenceTypeArgument) arg).getReferenceType());
		}
		logger.warning("Unexpected " + arg + " of " + arg.getClass());
		return null;
	}

	public PrimitiveType getPrimitiveType(PType pType) {
		return getPrimitiveType(makeType(pType));
	}

	public PrimitiveType getPrimitiveType(Type t) {
		for (PrimitiveType primitiveType : PrimitiveType.values()) {
			if (TypeUtils.isTypeAssignableFrom(primitiveType.getType(), t, true)) {
				return primitiveType;
			}
		}
		return null;
	}

	/**
	 * Try to find FlexoConcept with supplied compositeIdentifier, with a semantics compatible with loading life-cyle of underlying
	 * VirtualModel
	 * 
	 * @param typeName
	 * @return
	 */
	public FlexoConceptInstanceType lookupConceptNamed(PCompositeIdent compositeIdentifier) {
		return lookupConceptNamed(makeFullQualifiedIdentifier(compositeIdentifier));
	}

	/**
	 * Try to find FlexoConcept with supplied name, with a semantics compatible with loading life-cyle of underlying VirtualModel
	 * 
	 * @param typeName
	 * @return
	 */
	public FlexoConceptInstanceType lookupConceptNamed(String typeName) {
		if (StringUtils.isEmpty(typeName)) {
			return null;
		}
		// Looking up concept 'typeName'
		ConceptRetriever r = new ConceptRetriever(typeName);
		if (r.isFound()) {
			/*System.out.println("Was: " + r.getType());
			if (r.getType() instanceof FlexoConceptInstanceType) {
				System.out.println("uri: " + r.getType().getConceptURI());
				System.out.println("Hash: " + Integer.toHexString(r.getType().hashCode()));
				System.out.println("factory: " + r.getType().getCustomTypeFactory().getClass());
			}*/
			r.getType().resolve();
			/*System.out.println("After resolution: " + r.getType());
			if (r.getType() instanceof FlexoConceptInstanceType) {
				System.out.println("uri: " + r.getType().getConceptURI());
				System.out.println("Hash: " + Integer.toHexString(r.getType().hashCode()));
			}*/
			unresolvedTypes.add(r.getType());
			return r.getType();
		}

		System.err.println("Cannot find " + typeName);

		/*if (r.modelDeclaration != null) {
			return new VirtualModelInstanceType(getVirtualModel());
		}
		if (r.conceptDeclaration != null) {
			System.out.println("tiens je trouve " + r.conceptDeclaration);
			System.out.println("VM: " + getVirtualModel());
			System.out.println("URI=" + getVirtualModel().getURI());
		}*/
		// return tryToLokupConcept(typeName, getVirtualModel());
		return null;
	}

	/**
	 * Try to find VirtualModel with supplied name, with a semantics compatible with loading life-cyle of underlying VirtualModel
	 * 
	 * @param typeName
	 * @param vm
	 * @return
	 */
	private VirtualModel tryToLookupVirtualModel(String virtualModelName) {

		System.out.println("virtualModelName=" + virtualModelName);
		System.out.println("getAnalyzer()=" + getAnalyzer());

		if (virtualModelName == null) {
			System.out.println("Merde alors");
			Thread.dumpStack();
		}

		if (virtualModelName.startsWith("@") && getAnalyzer().getCompilationUnit() != null) {
			String id = virtualModelName.substring(1).trim();
			// System.out.println("Hop ici, id = [" + id + "]");
			for (ElementImportDeclaration importDeclaration : getAnalyzer().getCompilationUnit().getElementImports()) {
				// System.out.println("> " + importDeclaration.getAbbrev());
				// System.out.println("> " + importDeclaration.getAbbrev().equals(id));
				if (StringUtils.isNotEmpty(importDeclaration.getAbbrev()) && importDeclaration.getAbbrev().equals(id)) {
					// System.out.println(">>>>>> " + importDeclaration.getReferencedObject());
					if (importDeclaration.getReferencedObject() instanceof VirtualModel) {
						// System.out.println("On retourne " + importDeclaration.getReferencedObject());
						return (VirtualModel) importDeclaration.getReferencedObject();
					}
					else {
						logger.warning("Unexpected " + importDeclaration.getReferencedObject());
						return null;
					}
				}
			}
		}

		if (getVirtualModel() != null && getVirtualModel().getName().equals(virtualModelName)) {
			return getVirtualModel();
		}
		if (virtualModelBeingDeserialized != null && virtualModelBeingDeserialized.getName().equals(virtualModelName)) {
			return virtualModelBeingDeserialized;
		}
		return null;
	}

	private VirtualModel virtualModelBeingDeserialized;

	public void setDeserializedVirtualModel(VirtualModel returned) {
		virtualModelBeingDeserialized = returned;
	}

	/**
	 * Try to find FlexoConcept with supplied name in supplied virtualModel, with a semantics compatible with loading life-cyle of
	 * underlying VirtualModel
	 * 
	 * @param typeName
	 * @param vm
	 * @return
	 */
	private FlexoConcept tryToLookupConcept(String relativeURI) {
		FlexoConcept current = null;
		StringTokenizer st = new StringTokenizer(relativeURI, ".");
		if (st.hasMoreTokens()) {
			VirtualModel vm = tryToLookupVirtualModel(st.nextToken());
			if (vm != null) {
				while (st.hasMoreTokens()) {
					String next = st.nextToken();
					if (current == null) {
						current = vm.getFlexoConcept(next);
					}
					else {
						current = current.getEmbeddedFlexoConcept(next);
					}
				}
			}
		}
		return current;
	}

	class ConceptRetriever extends DepthFirstAdapter {

		private String fullQualifiedConceptName;
		private List<String> fullQualifiedConceptNamePath;
		private Stack<Node> nodes;
		private String relativeURI;
		private boolean found = false;
		private FlexoConceptInstanceType type;

		public ConceptRetriever(String conceptName) {
			this.fullQualifiedConceptName = conceptName;
			fullQualifiedConceptNamePath = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(conceptName, ".");
			while (st.hasMoreTokens()) {
				fullQualifiedConceptNamePath.add(st.nextToken());
			}
			nodes = new Stack<>();
			getAnalyzer().getRootNode().apply(ConceptRetriever.this);
		}

		public boolean isFound() {
			return found;
		}

		public FlexoConceptInstanceType getType() {
			return type;
		}

		private void found() {
			found = true;
			Node foundNode = nodes.pop();
			relativeURI = getIdentifier(foundNode);
			while (!nodes.isEmpty()) {
				relativeURI = getIdentifier(nodes.pop()) + "." + relativeURI;
			}
			if (foundNode instanceof AModelDecl) {
				System.out.println("ici1 avec " + relativeURI);
				type = new VirtualModelInstanceType(relativeURI, VIRTUAL_MODEL_INSTANCE_TYPE_FACTORY);
			}
			if (foundNode instanceof AConceptDecl) {
				System.out.println("ici2 avec " + relativeURI);
				type = new FlexoConceptInstanceType(relativeURI, FLEXO_CONCEPT_INSTANCE_TYPE_FACTORY);
			}
		}

		private String getIdentifier(Node node) {
			if (node instanceof AModelDecl) {
				return ((AModelDecl) node).getIdentifier().getText();
			}
			if (node instanceof AConceptDecl) {
				return ((AConceptDecl) node).getIdentifier().getText();
			}
			return null;
		}

		@Override
		public void inAModelDecl(AModelDecl node) {
			super.inAModelDecl(node);
			if (!found) {
				nodes.push(node);
				if (fullQualifiedConceptName.equals(node.getIdentifier().getText())) {
					found();
				}
			}
		}

		@Override
		public void outAModelDecl(AModelDecl node) {
			super.outAModelDecl(node);
			if (!found) {
				nodes.pop();
			}
		}

		@Override
		public void inAConceptDecl(AConceptDecl node) {
			super.inAConceptDecl(node);
			if (!found) {
				nodes.push(node);
				if (fullQualifiedConceptName.equals(node.getIdentifier().getText())) {
					found();
				}
			}
		}

		@Override
		public void outAConceptDecl(AConceptDecl node) {
			super.outAConceptDecl(node);
			if (!found) {
				nodes.pop();
			}
		}

		@Override
		public void inANamedUriImportImportDecl(ANamedUriImportImportDecl node) {
			super.inANamedUriImportImportDecl(node);
			if (fullQualifiedConceptNamePath.size() > 0 && fullQualifiedConceptNamePath.get(0).equals(node.getName().getText())) {
				found = true;
				if (fullQualifiedConceptNamePath.size() == 1) {
					System.out.println("ici3 avec " + "@" + node.getName());
					type = new VirtualModelInstanceType("@" + node.getName(), VIRTUAL_MODEL_INSTANCE_TYPE_FACTORY);
					System.out.println("Hash: " + Integer.toHexString(type.hashCode()));
				}
				else {
					System.out.println("ici4 avec " + "@" + node.getName());
					type = new FlexoConceptInstanceType("@" + node.getName(), FLEXO_CONCEPT_INSTANCE_TYPE_FACTORY);
				}
			}
		}
	}

}
