/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Fml-parser, a component of the software infrastructure 
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

import java.util.HashMap;
import java.util.Map;

import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.parser.analysis.ReversedDepthFirstAdapter;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.Token;

/**
 * This class implements the semantics analyzer for a parsed FML compilation unit.<br>
 * 
 * @author sylvain
 * 
 */
public class FMLSyntaxAnalyzer extends ReversedDepthFirstAdapter {

	private final FMLCompilationUnit fmlCompilationUnit;
	private final FlexoServiceManager serviceManager;
	private final Map<Node, Integer> beginLines;
	private final Map<Node, Integer> beginColumns;
	private final Map<Node, Integer> endLines;
	private final Map<Node, Integer> endColumns;

	// line/column -1 means "end of file"
	int line = -1;
	int column = -1;

	public FMLSyntaxAnalyzer(FMLCompilationUnit fmlCompilationUnit, FlexoServiceManager serviceManager) {
		this.fmlCompilationUnit = fmlCompilationUnit;
		this.serviceManager = serviceManager;
		beginLines = new HashMap<>();
		beginColumns = new HashMap<>();
		endLines = new HashMap<>();
		endColumns = new HashMap<>();
	}

	public FlexoServiceManager getServiceManager() {
		return serviceManager;
	}

	@Override
	public void defaultIn(Node node) {
		super.defaultIn(node);
		endLines.put(node, new Integer(line));
		endColumns.put(node, new Integer(column));
	}

	@Override
	public void defaultOut(Node node) {
		super.defaultOut(node);
		beginLines.put(node, new Integer(line));
		beginColumns.put(node, new Integer(column));
	}

	@Override
	public void defaultCase(Node node) {
		super.defaultCase(node);
		if (node instanceof Token) {
			Token token = (Token) node;
			line = token.getLine();
			column = token.getPos();
		}
	}

	public int getBeginLine(Node node) {
		Integer returned = beginLines.get(node);
		if (returned != null) {
			return returned;
		}
		return -1;
	}

	public int getBeginColumn(Node node) {
		Integer returned = beginColumns.get(node);
		if (returned != null) {
			return returned;
		}
		return -1;
	}

	public int getEndLine(Node node) {
		Integer returned = endLines.get(node);
		if (returned != null) {
			return returned;
		}
		return -1;
	}

	public int getEndColumn(Node node) {
		Integer returned = endColumns.get(node);
		if (returned != null) {
			return returned;
		}
		return -1;
	}

	public FMLCompilationUnit getFMLCompilationUnit() {
		return fmlCompilationUnit;
	}
}
