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

import java.lang.reflect.Type;

import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.AbstractInvariant;
import org.openflexo.foundation.fml.FMLKeywords;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoEvent;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.md.FMLMetaData;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.node.AEventDecl;
import org.openflexo.foundation.fml.parser.node.AInsideClause;
import org.openflexo.foundation.fml.parser.node.ASuperClause;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * @author sylvain
 * 
 */
// TODO: is there a way to factorize code with FlexoConceptNode ???
public class FlexoEventNode extends AbstractFlexoConceptNode<AEventDecl, FlexoEvent> {

	public FlexoEventNode(AEventDecl astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public FlexoEventNode(FlexoEvent event, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(event, analyzer);
	}

	@Override
	public FlexoEvent buildModelObjectFromAST(AEventDecl astNode) {
		FlexoEvent returned = getFactory().newFlexoEvent();
		try {
			returned.setName(astNode.getUidentifier().getText());
		} catch (InvalidNameException e) {
			throwIssue("Invalid name: " + astNode.getUidentifier().getText());
		}
		returned.setAbstract(astNode.getKwAbstract() != null);
		returned.setVisibility(getVisibility(astNode.getVisibility()));
		buildParentConcepts(returned, astNode.getSuperClause());

		if (astNode.getInsideClause() != null) {
			AInsideClause insideClause = (AInsideClause) astNode.getInsideClause();
			Type insideType = TypeFactory.makeType(insideClause.getCompositeTident(), getSemanticsAnalyzer().getTypingSpace());
			if (insideType instanceof FlexoConceptInstanceType) {
				containerType = (FlexoConceptInstanceType) insideType;
			}
		}

		return returned;
	}

	@Override
	public FlexoEventNode deserialize() {
		if (getParent() instanceof VirtualModelNode) {
			((VirtualModelNode) getParent()).getModelObject().addToFlexoConcepts(getModelObject());
		}
		if (getParent() instanceof FlexoEventNode) {
			((FlexoEventNode) getParent()).getModelObject().addToEmbeddedFlexoConcepts(getModelObject());
		}
		return this;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {

		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off
		append(childrenContents("", () -> getModelObject().getMetaData(), LINE_SEPARATOR, Indentation.DoNotIndent, FMLMetaData.class, "MetaData"));
		append(dynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE), getVisibilityFragment(),"Visibility");
		when(() -> isAbstract(),"Abstract").thenAppend(staticContents("","abstract", SPACE), getAbstractFragment());
		append(staticContents("", FMLKeywords.Event.getKeyword(), SPACE), getEventFragment(),"Event");
		append(dynamicContents(() -> getModelObject().getName()), getNameFragment(),"Name");

		when(() -> getModelObject().getParentFlexoConcepts().size() > 0, "ParentConceptsDefinition")
				.thenAppend(staticContents(SPACE, FMLKeywords.Extends.getKeyword(), SPACE), getExtendsFragment())
				.thenAppend(dynamicContents(() -> getModelObject().getParentFlexoConceptsDeclaration()), getSuperTypeListFragment());

		when(() -> getModelObject().requiresExternalContainerDeclaration(),"ExternalContainer")
			.thenAppend(staticContents(SPACE, FMLKeywords.Inside.getKeyword(), SPACE), getInsideFragment())
			.thenAppend(dynamicContents(() -> serializeType(getModelObject().getContainerFlexoConcept())), getInsideTypeFragment());	
		
		append(staticContents(SPACE, "{", LINE_SEPARATOR), getLBrcFragment(),"LBrc");
		append(childrenContents("", () -> getModelObject().getFlexoProperties(), LINE_SEPARATOR, Indentation.Indent, FlexoProperty.class),"Properties");
		append(childrenContents(LINE_SEPARATOR, () -> getModelObject().getFlexoBehaviours(), LINE_SEPARATOR, Indentation.Indent,
				FlexoBehaviour.class),"Behaviours");
		append(childrenContents(LINE_SEPARATOR, () -> getModelObject().getAllEmbeddedFlexoConceptsDeclaringThisConceptAsContainer(), LINE_SEPARATOR, Indentation.Indent,
				FlexoConcept.class),"EmbeddedConcepts");
		append(childrenContents(LINE_SEPARATOR, () -> getModelObject().getInvariants(), LINE_SEPARATOR, Indentation.Indent,
				AbstractInvariant.class),"Invariants");
		append(staticContents("", "}", LINE_SEPARATOR), getRBrcFragment(),"RBrc");

		// @formatter:on

		/*appendDynamicContents(() -> getVisibilityAsString(getModelObject().getVisibility()), SPACE, getVisibilityFragment());
		
		appendStaticContents("concept", SPACE, getConceptFragment());
		appendDynamicContents(() -> getModelObject().getName(), getNameFragment());
		appendStaticContents(SPACE, "{", LINE_SEPARATOR, getLBrcFragment());
		
		appendToChildrenPrettyPrintContents("", () -> getModelObject().getFlexoProperties(), LINE_SEPARATOR, Indentation.Indent,
				FlexoProperty.class);
		appendToChildrenPrettyPrintContents(LINE_SEPARATOR, () -> getModelObject().getFlexoBehaviours(), LINE_SEPARATOR, Indentation.Indent,
				FlexoBehaviour.class);
		appendToChildrenPrettyPrintContents(LINE_SEPARATOR, () -> getModelObject().getChildFlexoConcepts(), LINE_SEPARATOR,
				Indentation.Indent, FlexoConcept.class);
		
		appendStaticContents("}", LINE_SEPARATOR, getRBrcFragment());*/
	}

	public boolean isAbstract() {
		if (getModelObject() != null) {
			return getModelObject().isAbstract();
		}
		if (getASTNode() != null) {
			return getASTNode().getKwAbstract() != null;
		}
		return false;
	}

	@Override
	protected RawSourceFragment getVisibilityFragment() {
		if (getASTNode() != null && getASTNode().getVisibility() != null) {
			return getFragment(getASTNode().getVisibility());
		}
		return null;
	}

	protected RawSourceFragment getAbstractFragment() {
		if (getASTNode() != null && getASTNode().getKwAbstract() != null) {
			return getFragment(getASTNode().getKwAbstract());
		}
		return null;
	}

	private RawSourceFragment getEventFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwEvent());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getNameFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getUidentifier());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getLBrcFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getLBrc());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getRBrcFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getRBrc());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getSuperClauseFragment() {
		if (getASTNode() != null && getASTNode().getSuperClause() != null) {
			return getFragment(getASTNode().getSuperClause());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getExtendsFragment() {
		if (getASTNode() != null && getASTNode().getSuperClause() != null) {
			ASuperClause superClause = (ASuperClause) getASTNode().getSuperClause();
			return getFragment(superClause.getKwExtends());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getSuperTypeListFragment() {
		if (getASTNode() != null && getASTNode().getSuperClause() != null) {
			ASuperClause superClause = (ASuperClause) getASTNode().getSuperClause();
			return getFragment(superClause.getSuperTypeList());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getInsideClauseFragment() {
		if (getASTNode() != null && getASTNode().getInsideClause() != null) {
			return getFragment(getASTNode().getInsideClause());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getInsideFragment() {
		if (getASTNode() != null && getASTNode().getInsideClause() != null) {
			AInsideClause insideClause = (AInsideClause) getASTNode().getInsideClause();
			return getFragment(insideClause.getKwInside());
		}
		return null;
	}

	@Override
	protected RawSourceFragment getInsideTypeFragment() {
		if (getASTNode() != null && getASTNode().getInsideClause() != null) {
			AInsideClause insideClause = (AInsideClause) getASTNode().getInsideClause();
			return getFragment(insideClause.getCompositeTident());
		}
		return null;
	}

}
