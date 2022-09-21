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

import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.BindingFactory;
import org.openflexo.connie.binding.BindingPathElement;
import org.openflexo.connie.binding.BindingPathElement.BindingPathElementOwner;
import org.openflexo.connie.binding.IBindingPathElement;
import org.openflexo.foundation.fml.parser.FMLSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.FMLCompilationUnitSemanticsAnalyzer;
import org.openflexo.foundation.fml.parser.ObjectNode;
import org.openflexo.foundation.fml.parser.node.AFieldPrimaryNoId;
import org.openflexo.foundation.fml.parser.node.AIdentifierPrimary;
import org.openflexo.foundation.fml.parser.node.APrimaryFieldAccess;
import org.openflexo.foundation.fml.parser.node.APrimaryNoIdPrimary;
import org.openflexo.foundation.fml.parser.node.AReferenceSuperFieldAccess;
import org.openflexo.foundation.fml.parser.node.ASuperFieldAccess;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PFieldAccess;
import org.openflexo.foundation.fml.parser.node.PPrimary;
import org.openflexo.foundation.fml.parser.node.PPrimaryNoId;

/**
 * 
 * @author sylvain
 * 
 */
public abstract class AbstractBindingPathElementNode<N extends Node, BPE extends IBindingPathElement>
		extends ObjectNode<N, BPE, FMLCompilationUnitSemanticsAnalyzer> implements BindingPathElementOwner {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(AbstractBindingPathElementNode.class.getPackage().getName());

	private Bindable bindable;

	private boolean readyToBuildModelObject = false;

	public AbstractBindingPathElementNode(N astNode, FMLSemanticsAnalyzer analyzer, Bindable bindable) {
		super(astNode, analyzer);
		this.bindable = bindable;
		// buildModelObjectFromAST() was already called, but too early (bindable not yet set)
		// we do it again
		modelObject = buildModelObjectFromAST(astNode);
	}

	public AbstractBindingPathElementNode(BPE bindingPathElement, FMLSemanticsAnalyzer analyzer, Bindable bindable) {
		super(bindingPathElement, analyzer);
		this.bindable = bindable;
	}

	public Bindable getBindable() {
		return bindable;
	}

	public BindingFactory getBindingFactory() {
		if (getBindable() != null && getBindable().getBindingFactory() != null) {
			return getBindable().getBindingFactory();
		}
		return getSemanticsAnalyzer().getFMLBindingFactory();
	}

	// Take care that this method is never called !
	@Override
	public final AbstractBindingPathElementNode<N, BPE> deserialize() {
		return this;
	}

	public boolean readyToBuildModelObject() {
		return readyToBuildModelObject && getBindable() != null;
	}

	protected void setReadyToBuildModelObject(boolean readyToBuildModelObject) {
		this.readyToBuildModelObject = readyToBuildModelObject;
	}

	@Override
	public void bindingPathElementChanged(BindingPathElement newElement) {
		// System.out.println("bindingPathElementChanged() from " + getModelObject() + " to " + newElement);

		// Here, analysing of BindingPath leads to a new PathElement with different type, replace then the model object
		setModelObject((BPE) newElement);
	}

	protected String getLastPathIdentifier(PPrimary primary) {
		if (primary instanceof AIdentifierPrimary) {
			String fullQualifiedIdentifier = getSemanticsAnalyzer().makeFullQualifiedIdentifier(((AIdentifierPrimary) primary).getCompositeIdent());
			if (fullQualifiedIdentifier.contains(".")) {
				return fullQualifiedIdentifier.substring(fullQualifiedIdentifier.lastIndexOf(".") + 1);
			}
			return fullQualifiedIdentifier;
		}
		else if (primary instanceof APrimaryNoIdPrimary) {
			return getLastPathIdentifier(((APrimaryNoIdPrimary) primary).getPrimaryNoId());
		}
		return null;
	}

	protected String getLastPathIdentifier(PPrimaryNoId primary) {
		if (primary instanceof AFieldPrimaryNoId) {
			return getLastPathIdentifier(((AFieldPrimaryNoId) primary).getFieldAccess());
		}
		return null;
	}

	protected String getLastPathIdentifier(PFieldAccess fieldAccess) {
		if (fieldAccess instanceof APrimaryFieldAccess) {
			return (((APrimaryFieldAccess) fieldAccess).getLidentifier().getText());
		}
		if (fieldAccess instanceof AReferenceSuperFieldAccess) {
			return (((AReferenceSuperFieldAccess) fieldAccess).getIdentifier2().getText());
		}
		if (fieldAccess instanceof ASuperFieldAccess) {
			return (((ASuperFieldAccess) fieldAccess).getLidentifier().getText());
		}
		return null;
	}

}
