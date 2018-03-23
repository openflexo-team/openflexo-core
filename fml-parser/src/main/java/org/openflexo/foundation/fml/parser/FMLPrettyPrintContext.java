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

package org.openflexo.foundation.fml.parser;

import java.util.StringTokenizer;

import org.openflexo.toolbox.StringUtils;

public class FMLPrettyPrintContext {

	private static int INDENTATION = 2;

	public FMLPrettyPrintContext() {
	}

	public FMLPrettyPrintContext makeSubContext() {
		FMLPrettyPrintContext returned = new FMLPrettyPrintContext();
		return returned;
	}

	// TODO: rename to FMLPrettyPrintOutput
	public static class FMLRepresentationOutput {

		StringBuffer sb;

		public FMLRepresentationOutput(FMLPrettyPrintContext aContext) {
			sb = new StringBuffer();
		}

		public void append(String s, FMLPrettyPrintContext context) {
			append(s, context, 0);
		}

		public void appendnl() {
			sb.append(StringUtils.LINE_SEPARATOR);
		}

		public void append(String s, FMLPrettyPrintContext context, int indentation) {
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
			FMLPrettyPrintContext subContext = context.makeSubContext();
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
