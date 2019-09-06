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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.openflexo.connie.type.CustomType;
import org.openflexo.connie.type.TypeUtils;
import org.openflexo.toolbox.StringUtils;

/**
 * Used to compute a FML representation of a graph of FlexoObject
 * 
 * @author sylvain
 *
 */
@Deprecated
public class FMLRepresentationContext {

	private static final Logger logger = Logger.getLogger(FMLRepresentationContext.class.getPackage().getName());

	private static int INDENTATION = 2;

	/**
	 * References all {@link VirtualModel} involved in serialization of {@link FMLObject} graph
	 */
	private List<VirtualModel> fmlImports;

	/**
	 * References all java packages involved in serialization of {@link FMLObject} graph
	 */
	private List<Package> javaImports;

	public FMLRepresentationContext() {
		fmlImports = new ArrayList<>();
		javaImports = new ArrayList<>();
	}

	public String serializeType(Type type) {
		if (type instanceof CustomType) {
			if (type instanceof FlexoConceptInstanceType) {
				FlexoConcept concept = ((FlexoConceptInstanceType) type).getFlexoConcept();
				if (concept != null) {
					ensureReferenceInImport(concept);
					return concept.getName();
				}
			}
			logger.warning("FMLSerialization: cannot serialize CustomType " + type);
			return serializeType(((CustomType) type).getBaseClass());
		}
		Class<?> baseClass = TypeUtils.getBaseClass(type);
		ensureReferenceInImport(baseClass);
		return baseClass.getSimpleName();
	}

	private void ensureReferenceInImport(FlexoConcept concept) {
		if (concept == null) {
			return;
		}
		/*VirtualModel virtualModel = concept.getDeclaringCompilationUnit();
		if (!fmlImports.contains(virtualModel)) {
			fmlImports.add(virtualModel);
		}*/
	}

	private void ensureReferenceInImport(Class<?> usedClass) {
		if (usedClass == null) {
			return;
		}
		Package classPackage = usedClass.getPackage();
		if (!javaImports.contains(classPackage)) {
			javaImports.add(classPackage);
		}
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
