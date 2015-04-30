/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Flexo-foundation, a component of the software infrastructure 
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

package org.openflexo.foundation.fml;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.openflexo.toolbox.StringUtils;

public class FMLRepresentationContext {

	private static int INDENTATION = 2;
	// private int currentIndentation = 0;
	private final HashMap<String, FMLObject> nameSpaces;

	public FMLRepresentationContext() {
		// currentIndentation = 0;
		nameSpaces = new HashMap<String, FMLObject>();
	}

	public void addToNameSpaces(FMLObject object) {
		nameSpaces.put(object.getURI(), object);
	}

	/*public int getCurrentIndentation() {
		return currentIndentation;
	}*/

	public FMLRepresentationContext makeSubContext() {
		FMLRepresentationContext returned = new FMLRepresentationContext();
		for (String uri : nameSpaces.keySet()) {
			returned.nameSpaces.put(uri, nameSpaces.get(uri));
		}
		// returned.currentIndentation = currentIndentation + 1;
		return returned;
	}

	public static class FMLRepresentationOutput {

		StringBuffer sb;

		public FMLRepresentationOutput(FMLRepresentationContext aContext) {
			sb = new StringBuffer();
		}

		public void append(String s, FMLRepresentationContext context) {
			append(s, context, 0);
		}

		public void appendnl() {
			sb.append(StringUtils.LINE_SEPARATOR);
		}

		public void append(String s, FMLRepresentationContext context, int indentation) {
			if (s == null) {
				return;
			}
			StringTokenizer st = new StringTokenizer(s, StringUtils.LINE_SEPARATOR, true);
			while (st.hasMoreTokens()) {
				String l = st.nextToken();
				sb.append(StringUtils.buildWhiteSpaceIndentation((indentation) * INDENTATION) + l);
			}

			/*if (s.equals(StringUtils.LINE_SEPARATOR)) {
				appendnl();
				return;
			}

			BufferedReader rdr = new BufferedReader(new StringReader(s));
			boolean isFirst = true;
			for (;;) {
				String line = null;
				try {
					line = rdr.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (line == null) {
					break;
				}
				if (!isFirst) {
					sb.append(StringUtils.LINE_SEPARATOR);
				}
				sb.append(StringUtils.buildWhiteSpaceIndentation((indentation) * INDENTATION) + line);
				isFirst = false;
			}*/

		}

		/*public void append(FMLObject o) {
			FMLRepresentationContext subContext = context.makeSubContext();
			String lr = o.getFMLRepresentation(subContext);
			for (int i = 0; i < StringUtils.linesNb(lr); i++) {
				String l = StringUtils.extractStringFromLine(lr, i);
				sb.append(StringUtils.buildWhiteSpaceIndentation(subContext.indentation * 2 + 2) + l);
			}
		}*/

		@Override
		public String toString() {
			return sb.toString();
		}
	}
}
