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

package org.openflexo.foundation.fml.parser.fmlnodes.controlgraph;

import java.util.logging.Logger;

import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourParameter;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.MainSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoBehaviourNode;
import org.openflexo.foundation.fml.parser.node.AComplexFormalArgument;
import org.openflexo.foundation.fml.parser.node.APrimitiveFormalArgument;
import org.openflexo.foundation.fml.parser.node.PFormalArgument;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

// @formatter:off	
/**
 * @author sylvain
 * 
 * 
 * 	concept ConceptC {
		String c1;
		boolean c2;
		@ui(
			@label("create")
			@TextField(value="aC1", label="give_a_c1")
			@CheckBox(value="aC2", label="give_a_c2")
			@CheckBox(value="aC3", label="give_a_c3")
		)
		create::_create (
			required String aC1 default="toto", 
			required Boolean aC2,
			Boolean aC3 default=true) {
			c1 = parameters.aC1;
			c2 = parameters.aC2;
		}
		@label("delete")
		delete::_delete () {
		}
	}

 * 
 */
// @formatter:on	

public class BehaviourParameterNode extends FMLObjectNode<PFormalArgument, FlexoBehaviourParameter, MainSemanticsAnalyzer> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(BehaviourParameterNode.class.getPackage().getName());

	public BehaviourParameterNode(PFormalArgument astNode, MainSemanticsAnalyzer analyser) {
		super(astNode, analyser);
	}

	public BehaviourParameterNode(FlexoBehaviourParameter modelObject, MainSemanticsAnalyzer analyser) {
		super(modelObject, analyser);
	}

	@Override
	public BehaviourParameterNode deserialize() {
		if (getParent() instanceof FlexoBehaviourNode) {
			FlexoBehaviour behaviour = ((FlexoBehaviourNode<?, ?>) getParent()).getModelObject();
			behaviour.addToParameters(getModelObject());
		}
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public FlexoBehaviourParameter buildModelObjectFromAST(PFormalArgument astNode) {
		FlexoBehaviourParameter returned = getFactory().newParameter(null);

		if (astNode instanceof APrimitiveFormalArgument) {
			PrimitiveType primitiveType = getTypeFactory().makePrimitiveType(((APrimitiveFormalArgument) astNode).getPrimitiveType());
			if (primitiveType != null) {
				returned.setType(primitiveType.getType());
			}
			else {
				logger.warning("Unexpected: " + ((APrimitiveFormalArgument) astNode).getPrimitiveType());
			}
			returned.setName(((APrimitiveFormalArgument) astNode).getArgName().getText());
		}
		else if (astNode instanceof AComplexFormalArgument) {
			returned.setType(getTypeFactory().makeType(((AComplexFormalArgument) astNode).getReferenceType()));
			returned.setName(((AComplexFormalArgument) astNode).getArgName().getText());
		}
		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		append(dynamicContents(() -> serializeType(getModelObject().getType())), getTypeFragment());
		append(dynamicContents(SPACE, () -> getModelObject().getName()), getNameFragment());
	}

	protected RawSourceFragment getTypeFragment() {
		if (getASTNode() instanceof APrimitiveFormalArgument) {
			return getFragment(((APrimitiveFormalArgument) getASTNode()).getPrimitiveType());
		}
		if (getASTNode() instanceof AComplexFormalArgument) {
			return getFragment(((AComplexFormalArgument) getASTNode()).getReferenceType());
		}
		return null;
	}

	protected RawSourceFragment getNameFragment() {
		if (getASTNode() instanceof APrimitiveFormalArgument) {
			return getFragment(((APrimitiveFormalArgument) getASTNode()).getArgName());
		}
		if (getASTNode() instanceof AComplexFormalArgument) {
			return getFragment(((AComplexFormalArgument) getASTNode()).getArgName());
		}
		return null;
	}
}
