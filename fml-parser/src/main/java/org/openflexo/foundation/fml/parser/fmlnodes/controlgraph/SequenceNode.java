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

import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.parser.ControlGraphFactory;
import org.openflexo.foundation.fml.parser.node.ABlock;
import org.openflexo.p2pp.PrettyPrintContext.Indentation;

/**
 * @author sylvain
 * 
 */
public class SequenceNode extends ControlGraphNode<ABlock, Sequence> {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(SequenceNode.class.getPackage().getName());

	public SequenceNode(ABlock astNode, ControlGraphNode<?, ?> relativeStart, ControlGraphNode<?, ?> relativeEnd,
			ControlGraphFactory cgFactory) {
		super(astNode, cgFactory);
		if (relativeStart != null) {
			setStartPosition(relativeStart.getStartPosition());
		}
		if (relativeEnd != null) {
			setEndPosition(relativeEnd.getEndPosition());
		}
	}

	public SequenceNode(Sequence sequence, ControlGraphFactory cgFactory) {
		super(sequence, cgFactory);
	}

	@Override
	public Sequence buildModelObjectFromAST(ABlock astNode) {
		Sequence returned = getFactory().newSequence();
		return returned;
	}

	@Override
	public void preparePrettyPrint(boolean hasParsedVersion) {
		super.preparePrettyPrint(hasParsedVersion);
		append(childContents("", () -> getModelObject().getControlGraph1(), LINE_SEPARATOR, Indentation.DoNotIndent));
		append(childContents("", () -> getModelObject().getControlGraph2(), "", Indentation.DoNotIndent));
	}

}
