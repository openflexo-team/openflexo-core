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

import org.openflexo.foundation.fml.FMLInstancePropertyValue;
import org.openflexo.foundation.fml.FMLInstancesListPropertyValue;
import org.openflexo.foundation.fml.FMLObject;
import org.openflexo.foundation.fml.FMLPropertyValue;
import org.openflexo.foundation.fml.WrappedFMLObject;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLObjectNode;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedFmlParameters;
import org.openflexo.foundation.fml.parser.node.AFullQualifiedQualifiedInstance;
import org.openflexo.foundation.fml.parser.node.ASimpleQualifiedInstance;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PFmlParameters;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

// Node is either ASimpleQualifiedInstance or AFullQualifiedQualifiedInstance
public class WrappedFMLObjectNode<T extends FMLObject>
		extends FMLObjectNode<Node, WrappedFMLObject<T>, FMLCompilationUnitSemanticsAnalyzer> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(WrappedFMLObjectNode.class.getPackage().getName());

	public WrappedFMLObjectNode(Node astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public WrappedFMLObjectNode(WrappedFMLObject<T> modelObject, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(modelObject, analyzer);
	}

	@Override
	public WrappedFMLObjectNode deserialize() {
		if (getParent() instanceof FMLInstancePropertyValueNode) {
			FMLInstancePropertyValue propertyValue = ((FMLInstancePropertyValueNode<?, ?>) getParent()).getModelObject();
			propertyValue.setInstance(getModelObject());
		}
		if (getParent() instanceof FMLInstancesListPropertyValueNode) {
			FMLInstancesListPropertyValue propertyValue = ((FMLInstancesListPropertyValueNode<?, ?>) getParent()).getModelObject();
			propertyValue.addToInstances(getModelObject());
		}
		return this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public WrappedFMLObject<T> buildModelObjectFromAST(Node astNode) {
		WrappedFMLObject<T> returned = (WrappedFMLObject<T>) getFactory().newWrappedFMLObject();

		Class<T> objectClass = null;
		if (astNode instanceof ASimpleQualifiedInstance) {
			objectClass = (Class<T>) getFMLFactory().getFMLObjectClass(((ASimpleQualifiedInstance) astNode).getArgType());
			if (objectClass == null) {
				throwIssue("Cannot find object class " + ((ASimpleQualifiedInstance) astNode).getArgType().getText(),
						getFragment(((ASimpleQualifiedInstance) astNode).getArgType()));
			}
		}
		if (astNode instanceof AFullQualifiedQualifiedInstance) {
			objectClass = (Class<T>) getFMLFactory().getFMLObjectClass(((AFullQualifiedQualifiedInstance) astNode).getTaId(),
					((AFullQualifiedQualifiedInstance) astNode).getArgType());
			if (objectClass == null) {
				throwIssue("Cannot find object class " + ((AFullQualifiedQualifiedInstance) astNode).getArgType().getText(), getFragment(
						((AFullQualifiedQualifiedInstance) astNode).getTaId(), ((AFullQualifiedQualifiedInstance) astNode).getArgType()));
			}
		}

		T object = getFactory().newInstance(objectClass);
		returned.setObject(object);

		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		when(() -> isFullQualified()).thenAppend(dynamicContents(() -> serializeTAId()), getTaIdFragment()).thenAppend(staticContents("::"),
				getColonColonFragment());
		append(dynamicContents(() -> serializeObjectType()), getArgTypeFragment());
		append(staticContents(":"), getColonFragment());
		append(staticContents("("), getFMLParametersLParFragment());
		append(childrenContents("", "", () -> getModelObject().getFMLPropertyValues(getFactory()), ", ", "", Indentation.DoNotIndent,
				FMLPropertyValue.class));
		append(staticContents(")"), getFMLParametersRParFragment());
	}

	protected boolean isFullQualified() {
		if (getASTNode() instanceof AFullQualifiedQualifiedInstance) {
			return true;
		}
		return false;
	}

	private String serializeTAId() {
		return "DIAGRAM";
	}

	private String serializeObjectType() {
		return getModelObject().getObject().getFMLKeyword(getFactory());
	}

	// Node is either ASimpleQualifiedInstance or AFullQualifiedQualifiedInstance

	protected RawSourceFragment getTaIdFragment() {
		if (getASTNode() instanceof AFullQualifiedQualifiedInstance) {
			return getFragment(((AFullQualifiedQualifiedInstance) getASTNode()).getTaId());
		}
		return null;
	}

	protected RawSourceFragment getColonColonFragment() {
		if (getASTNode() instanceof AFullQualifiedQualifiedInstance) {
			return getFragment(((AFullQualifiedQualifiedInstance) getASTNode()).getColonColon());
		}
		return null;
	}

	protected RawSourceFragment getArgTypeFragment() {
		if (getASTNode() instanceof ASimpleQualifiedInstance) {
			return getFragment(((ASimpleQualifiedInstance) getASTNode()).getArgType());
		}
		if (getASTNode() instanceof AFullQualifiedQualifiedInstance) {
			return getFragment(((AFullQualifiedQualifiedInstance) getASTNode()).getArgType());
		}
		return null;
	}

	protected RawSourceFragment getColonFragment() {
		if (getASTNode() instanceof ASimpleQualifiedInstance) {
			return getFragment(((ASimpleQualifiedInstance) getASTNode()).getColon());
		}
		if (getASTNode() instanceof AFullQualifiedQualifiedInstance) {
			return getFragment(((AFullQualifiedQualifiedInstance) getASTNode()).getColon());
		}
		return null;
	}

	protected PFmlParameters getFMLParameters() {
		if (getASTNode() instanceof ASimpleQualifiedInstance) {
			return ((ASimpleQualifiedInstance) getASTNode()).getFmlParameters();
		}
		if (getASTNode() instanceof AFullQualifiedQualifiedInstance) {
			return ((AFullQualifiedQualifiedInstance) getASTNode()).getFmlParameters();
		}
		return null;
	}

	protected RawSourceFragment getFMLParametersFragment() {
		if (getFMLParameters() != null) {
			return getFragment(getFMLParameters());
		}
		return null;
	}

	protected RawSourceFragment getFMLParametersLParFragment() {
		if (getFMLParameters() instanceof AFullQualifiedFmlParameters) {
			return getFragment(((AFullQualifiedFmlParameters) getFMLParameters()).getLPar());
		}
		return null;
	}

	protected RawSourceFragment getFMLParametersRParFragment() {
		if (getFMLParameters() instanceof AFullQualifiedFmlParameters) {
			return getFragment(((AFullQualifiedFmlParameters) getFMLParameters()).getRPar());
		}
		return null;
	}

}
