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
import java.util.logging.Logger;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.EventListener;
import org.openflexo.foundation.fml.FMLKeywords;
import org.openflexo.foundation.fml.FlexoConceptInstanceType;
import org.openflexo.foundation.fml.FlexoEvent;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.ExpressionFactory;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.TypeFactory;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ControlGraphNode;
import org.openflexo.foundation.fml.parser.node.ABlockFlexoBehaviourBody;
import org.openflexo.foundation.fml.parser.node.AListenerBehaviourDecl;
import org.openflexo.foundation.fml.parser.node.PExpression;
import org.openflexo.foundation.fml.parser.node.PFlexoBehaviourBody;
import org.openflexo.foundation.fml.rt.VirtualModelInstance;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * <pre>
 *   | {listener} [annotations]:annotation* kw_listen [event_type]:reference_type kw_from [listened]:expression flexo_behaviour_body
 * </pre>
 * 
 * @author sylvain
 * 
 */
public class EventListenerNode extends FlexoBehaviourNode<AListenerBehaviourDecl, EventListener> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(EventListenerNode.class.getPackage().getName());

	public EventListenerNode(AListenerBehaviourDecl astNode, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(astNode, analyzer);
	}

	public EventListenerNode(EventListener behaviour, FMLCompilationUnitSemanticsAnalyzer analyzer) {
		super(behaviour, analyzer);
	}

	@Override
	public EventListener buildModelObjectFromAST(AListenerBehaviourDecl astNode) {

		EventListener returned = getFactory().newEventListener();

		Type type = TypeFactory.makeType(astNode.getEventType(), getSemanticsAnalyzer().getTypingSpace());
		if (type instanceof FlexoConceptInstanceType && ((FlexoConceptInstanceType) type).getFlexoConcept() instanceof FlexoEvent) {
			returned.setEventType((FlexoEvent) ((FlexoConceptInstanceType) type).getFlexoConcept());
		}
		else {
			throwIssue("Unexpected event type: " + type);
		}

		PExpression fromExpression = astNode.getListened();
		DataBinding<VirtualModelInstance<?, ?>> listened = (DataBinding) ExpressionFactory.makeDataBinding(fromExpression, returned,
				BindingDefinitionType.GET, VirtualModelInstance.class, getSemanticsAnalyzer(), this);
		returned.setListenedVirtualModelInstance(listened);

		PFlexoBehaviourBody flexoBehaviourBody = getFlexoBehaviourBody(astNode);
		if (flexoBehaviourBody instanceof ABlockFlexoBehaviourBody) {
			ControlGraphNode<?, ?> cgNode = ControlGraphFactory.makeControlGraphNode(getFlexoBehaviourBody(astNode),
					getSemanticsAnalyzer());
			if (cgNode != null) {
				returned.setControlGraph(cgNode.getModelObject());
				addToChildren(cgNode);
			}
		}
		else {
			// AEmptyFlexoBehaviourBody : keep the ControlGraph null
		}

		return returned;
	}

	@Override
	public PFlexoBehaviourBody getFlexoBehaviourBody(AListenerBehaviourDecl astNode) {
		return astNode.getFlexoBehaviourBody();
	}

	/**
	 * <pre>
	 | {listener} [annotations]:annotation* kw_listen [event_type]:reference_type kw_from [listened]:expression flexo_behaviour_body
	 * </pre>
	 */
	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);

		// @formatter:off	
		//append(childrenContents("", () -> getModelObject().getMetaData(), LINE_SEPARATOR, Indentation.DoNotIndent,
		//		FMLMetaData.class));
		
		append(staticContents("", FMLKeywords.Listen.getKeyword(), SPACE), getListenFragment());

		append(dynamicContents(() -> serializeType(getModelObject().getEventType())), getEventTypeFragment());

		append(staticContents(SPACE, "from", ""), getFromFragment());
		append(dynamicContents(SPACE, () -> getFromAsString()), getFromExpressionFragment());

		when(() -> hasNoImplementation())
			.thenAppend(staticContents(";"), getSemiFragment())
			.elseAppend(staticContents(SPACE,"{", ""), getLBrcFragment())
			.elseAppend(childContents(LINE_SEPARATOR, () -> getModelObject().getControlGraph(), LINE_SEPARATOR, Indentation.Indent))
			.elseAppend(staticContents(LINE_SEPARATOR, "}", ""), getRBrcFragment());

		// @formatter:on

	}

	private String getFromAsString() {
		return getModelObject().getListenedVirtualModelInstance().toString();
	}

	private RawSourceFragment getListenFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwListen());
		}
		return null;
	}

	private RawSourceFragment getEventTypeFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getEventType());
		}
		return null;
	}

	private RawSourceFragment getFromFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getKwFrom());
		}
		return null;
	}

	private RawSourceFragment getFromExpressionFragment() {
		if (getASTNode() != null) {
			return getFragment(getASTNode().getListened());
		}
		return null;
	}

}
