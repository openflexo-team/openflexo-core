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

package org.openflexo.foundation.fml.parser.fmlnodes.expr;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.connie.binding.javareflect.JavaNewInstanceMethodPathElement;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.ASimpleNewInstance;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * 
 * <pre>
 *     {simple} new_containment_clause? kw_new [type]:reference_type l_par argument_list? r_par precise_fml_parameters_clause?
 * </pre>
 * 
 * Handle {@link AJavaInstanceCreationFmlActionExp}
 * 
 * @author sylvain
 * 
 */
public class AddClassInstanceNode extends AbstractCallBindingPathElementNode<ASimpleNewInstance, JavaNewInstanceMethodPathElement> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AddClassInstanceNode.class.getPackage().getName());

	private IBindingPathElement parent;

	public AddClassInstanceNode(ASimpleNewInstance astNode, FMLSemanticsAnalyzer analyzer, IBindingPathElement parent, Bindable bindable) {
		super(astNode, analyzer, bindable);
		this.parent = parent;
		setReadyToBuildModelObject(true);
		// buildModelObjectFromAST() was already called, but too early (parent not yet set)
		// we do it again
		modelObject = buildModelObjectFromAST(astNode);
	}

	public AddClassInstanceNode(JavaNewInstanceMethodPathElement bindingPathElement, FMLSemanticsAnalyzer analyzer, Bindable bindable) {
		super(bindingPathElement, analyzer, bindable);
	}

	@Override
	public JavaNewInstanceMethodPathElement buildModelObjectFromAST(ASimpleNewInstance astNode) {

		if (readyToBuildModelObject()) {
			handleArguments(astNode.getArgumentList());
			Type type = TypeFactory.makeType(astNode.getType(), getSemanticsAnalyzer().getTypingSpace());

			JavaNewInstanceMethodPathElement pathElement = (JavaNewInstanceMethodPathElement) getBindingFactory()
					.makeNewInstancePathElement(type, parent, null, getArguments(), getBindable());
			pathElement.setBindingPathElementOwner(this);
			return pathElement;
		}
		return null;

	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off
		// when(() -> isContainerFullQualified())
		// .thenAppend(dynamicContents(() -> getModelObject().getContainer().toString()), getContainerFragment())
		// .thenAppend(staticContents("."), getContainerDotFragment());
		append(staticContents("", "new", SPACE), getNewFragment());
		append(dynamicContents(() -> serializeType(getModelObject().getType())), getTypeFragment());
		append(staticContents("("), getLParFragment());
		append(dynamicContents(() -> serializeArguments(getModelObject().getArguments())), getArgumentsFragment());
		append(staticContents(")"), getRParFragment());
		// @formatter:on

	}

	private RawSourceFragment getNewFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwNew());
		}
		return null;
	}

	private RawSourceFragment getTypeFragment() {

		if (getASTNode() != null) {
			return getFragment(getASTNode().getType());
		}
		return null;
	}

	private RawSourceFragment getLParFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getLPar());
		}
		return null;
	}

	private RawSourceFragment getArgumentsFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getArgumentList());
		}
		return null;
	}

	private RawSourceFragment getRParFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getRPar());
		}
		return null;
	}

}
