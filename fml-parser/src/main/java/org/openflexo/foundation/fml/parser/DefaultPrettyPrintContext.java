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

package org.openflexo.foundation.fml.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.FMLPrettyPrintDelegate.PrettyPrintContext;

/**
 * @author sylvain
 * 
 */
public class DefaultPrettyPrintContext implements PrettyPrintContext {

	private static final Logger logger = Logger.getLogger(DefaultPrettyPrintContext.class.getPackage().getName());

	private int indentation;
	private String resultingIndentation = null;

	private static final String INDENTATION = "\t";

	public DefaultPrettyPrintContext(int indentation) {
		this.indentation = indentation;
		System.out.println("Nouveau context, indentation=" + indentation);
	}

	@Override
	public PrettyPrintContext derive(int relativeIndentation) {
		return new DefaultPrettyPrintContext(indentation + relativeIndentation);
	}

	@Override
	public String getResultingIndentation() {
		if (resultingIndentation == null) {
			if (indentation > 0) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < indentation; i++) {
					sb.append(INDENTATION);
				}
				resultingIndentation = sb.toString();
			}
			else {
				resultingIndentation = "";
			}
		}
		return resultingIndentation;
	}

	@Override
	public String indent(String stringToIndent) {
		if (indentation == 0) {
			return stringToIndent;
		}
		List<String> rows = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new StringReader(stringToIndent))) {
			String nextLine = null;
			do {
				nextLine = br.readLine();
				if (nextLine != null) {
					rows.add(nextLine);
				}
			} while (nextLine != null);
		} catch (IOException e) {
			logger.warning("Unexpected exception " + e.getMessage());
			return stringToIndent;
		}

		StringBuffer sb = new StringBuffer();
		for (String row : rows) {
			sb.append(getResultingIndentation() + row);
		}
		return sb.toString();
	}

}
