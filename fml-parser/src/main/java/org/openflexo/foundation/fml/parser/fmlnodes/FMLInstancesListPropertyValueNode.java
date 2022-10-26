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

import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLInstancesListPropertyValue;
import org.openflexo.foundation.fml.FMLModelContext.FMLProperty;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.WrappedFMLObject;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.node.AListInstancesQualifiedArgument;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * 
 * <pre>
 *     {list_instances} [arg_name]:identifier assign l_brc qualified_argument_list_instances? r_brc
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class FMLInstancesListPropertyValueNode<M extends FMLObject, T extends FMLObject>
		extends AbstractFMLPropertyValueNode<AListInstancesQualifiedArgument, FMLInstancesListPropertyValue<M, T>, M, List<T>> {

	private static final Logger logger = Logger.getLogger(FMLInstancesListPropertyValueNode.class.getPackage().getName());

	public FMLInstancesListPropertyValueNode(AListInstancesQualifiedArgument astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public FMLInstancesListPropertyValueNode(FMLInstancesListPropertyValue<M, T> propertyValue,
			FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(propertyValue, analyzer);
	}

	@Override
	public FMLInstancesListPropertyValueNode<M, T> deserialize() {

		String propertyName = getASTNode().getArgName().getText();

		FMLProperty fmlProperty = ((FMLObject) getParent().getModelObject()).getFMLProperty(propertyName, getFactory());
		if (fmlProperty == null) {
			getModelObject().setUnresolvedPropertyName(propertyName);
			logger.warning("Cannot find FML property " + propertyName + " in " + getParent().getModelObject());
		}
		else {
			getModelObject().setProperty(fmlProperty);
		}

		return (FMLInstancesListPropertyValueNode<M, T>) super.deserialize();
	}

	@Override
	public FMLInstancesListPropertyValue<M, T> buildModelObjectFromAST(AListInstancesQualifiedArgument astNode) {

		return (FMLInstancesListPropertyValue<M, T>) getFactory().newInstancesListPropertyValue();

	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(dynamicContents(() -> getModelObject().getProperty().getLabel()), getArgNameFragment());
		append(staticContents("="), getAssignFragment());
		append(staticContents("{"), getLBrcFragment());
		append(childrenContents("", "", () -> getModelObject().getInstances(), "," + SPACE, "", Indentation.DoNotIndent,
				WrappedFMLObject.class));
		append(staticContents("}"), getRBrcFragment());
	}

	private RawSourceFragment getArgNameFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getArgName());
		}
		return null;
	}

	private RawSourceFragment getAssignFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getAssign());
		}
		return null;
	}

	private RawSourceFragment getLBrcFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getLBrc());
		}
		return null;
	}

	private RawSourceFragment getRBrcFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getRBrc());
		}
		return null;
	}
}
