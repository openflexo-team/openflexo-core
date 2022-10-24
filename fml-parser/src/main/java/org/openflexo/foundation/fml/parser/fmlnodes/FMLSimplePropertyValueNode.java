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

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.expr.Constant;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.foundation.fml.ElementImportDeclaration;
import org.openflexo.foundation.fml.FMLModelContext.FMLProperty;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLSimplePropertyValue;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.ASimpleQualifiedArgument;
import org.openflexo.p2pp.RawSource.RawSourceFragment;
import org.openflexo.pamela.exceptions.InvalidDataException;

/**
 * 
 * <pre>
 *     {simple}         [arg_name]:identifier assign expression
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class FMLSimplePropertyValueNode<M extends FMLObject, T>
		extends AbstractFMLPropertyValueNode<ASimpleQualifiedArgument, FMLSimplePropertyValue<M, T>, M, T> {

	private static final Logger logger = Logger.getLogger(FMLSimplePropertyValueNode.class.getPackage().getName());

	public FMLSimplePropertyValueNode(ASimpleQualifiedArgument astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public FMLSimplePropertyValueNode(FMLSimplePropertyValue<M, T> propertyValue, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(propertyValue, analyzer);
	}

	@Override
	public FMLSimplePropertyValueNode<M, T> deserialize() {

		String propertyName = getASTNode().getArgName().getText();
		// System.out.println("Node: " + getASTNode());
		// System.out.println("Pour la propriete: " + propertyName);
		// System.out.println("parent: " + getParent().getModelObject());
		FMLProperty fmlProperty = ((FMLObject) getParent().getModelObject()).getFMLProperty(propertyName, getFactory());
		// System.out.println("fmlProperty: " + fmlProperty);

		if (fmlProperty == null) {
			logger.warning("Cannot find FML property " + propertyName + " in " + getParent().getModelObject());
			// if (!getParent().getModelObject().toString().contains("WrappedFMLObject")) {
			// System.out.println("Available properties");
			// for (FMLProperty<?, ?> p : ((FMLObject) getParent().getModelObject()).getFMLProperties(getFactory())) {
			// System.out.println(" > " + p);
			// }
			// }
			return this;
		}

		getModelObject().setProperty(fmlProperty);

		// DataBinding<Object> value = makeBinding(getASTNode().getExpression(), modelObject);

		DataBinding<Object> value = ExpressionFactory.makeDataBinding(getASTNode().getExpression(), modelObject, BindingDefinitionType.GET,
				Object.class, getSemanticsAnalyzer(), this);
		// System.out.println("value=" + value);

		if (DataBinding.class.equals(TypeUtils.getBaseClass(fmlProperty.getType()))) {
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

		return (FMLSimplePropertyValueNode<M, T>) super.deserialize();
	}

	@Override
	public FMLSimplePropertyValue<M, T> buildModelObjectFromAST(ASimpleQualifiedArgument astNode) {

		return (FMLSimplePropertyValue<M, T>) getFactory().newSimplePropertyValue();
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(dynamicContents(() -> getModelObject().getProperty().getLabel()), getArgNameFragment());
		append(staticContents("="), getAssignFragment());
		append(dynamicContents(() -> encodeFMLProperty(getModelObject().getValue())), getValueFragment());
	}

	private String encodeFMLProperty(T value) {
		if (value == null) {
			return null;
		}
		if (getCompilationUnit() != null) {
			for (ElementImportDeclaration elementImportDeclaration : getCompilationUnit().getElementImports()) {
				if (elementImportDeclaration.getReferencedObject() == value) {
					return elementImportDeclaration.getAbbrev();
				}
			}
		}
		String returned;
		try {
			returned = getFactory().getStringEncoder().toString(value);
			if (value instanceof String) {
				returned = "\"" + returned + "\"";
			}
		} catch (InvalidDataException e) {
			logger.warning("Don't know what to do with " + value);
			e.printStackTrace();
			return null;
		}

		return returned;
	}

	private RawSourceFragment getArgNameFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getArgName());
		}
		return null;
	}

	private RawSourceFragment getValueFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getExpression());
		}
		return null;
	}

	private RawSourceFragment getAssignFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getAssign());
		}
		return null;
	}
}
