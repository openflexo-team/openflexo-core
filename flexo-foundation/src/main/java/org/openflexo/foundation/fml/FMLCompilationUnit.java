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

package org.openflexo.foundation.fml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.openflexo.model.annotations.Adder;
import org.openflexo.model.annotations.CloningStrategy;
import org.openflexo.model.annotations.CloningStrategy.StrategyType;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Getter.Cardinality;
import org.openflexo.model.annotations.ImplementationClass;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PastingPoint;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Remover;
import org.openflexo.model.annotations.Setter;
import org.openflexo.toolbox.StringUtils;

@ModelEntity
@ImplementationClass(FMLCompilationUnit.FMLCompilationUnitImpl.class)
public interface FMLCompilationUnit extends FMLObject {

	@PropertyIdentifier(type = FMLPrettyPrinter.class)
	public static final String PRETTY_PRINTER_KEY = "prettyPrinter";
	@PropertyIdentifier(type = VirtualModel.class)
	public static final String VIRTUAL_MODEL_KEY = "virtualModel";
	@PropertyIdentifier(type = String.class, cardinality = Cardinality.LIST)
	public static final String JAVA_IMPORTS_LIST_KEY = "javaImports";

	@Getter(value = PRETTY_PRINTER_KEY, ignoreType = true)
	@CloningStrategy(StrategyType.IGNORE)
	public FMLPrettyPrinter getPrettyPrinter();

	@Setter(PRETTY_PRINTER_KEY)
	public void setPrettyPrinter(FMLPrettyPrinter prettyPrinter);

	@Getter(value = VIRTUAL_MODEL_KEY)
	@CloningStrategy(StrategyType.IGNORE)
	public VirtualModel getVirtualModel();

	@Setter(VIRTUAL_MODEL_KEY)
	public void setVirtualModel(VirtualModel virtualModel);

	@Getter(value = JAVA_IMPORTS_LIST_KEY, cardinality = Cardinality.LIST)
	@CloningStrategy(StrategyType.CLONE)
	public List<String> getJavaImports();

	@Adder(JAVA_IMPORTS_LIST_KEY)
	@PastingPoint
	public void addToJavaImports(String aJavaImport);

	@Remover(JAVA_IMPORTS_LIST_KEY)
	public void removeFromJavaImports(String aJavaImport);

	public void initWithRawContents(String someContents);

	public void printRawContents();

	public Fragment makeFragment(int beginLine, int beginCol, int endLine, int endCol);

	public String getFMLPrettyPrint(FMLObject object);

	public String getFMLSerialization(FMLObject object);

	public interface Fragment {
		public int getBeginLine();

		public int getBeginCol();

		public int getEndLine();

		public int getEndCol();

		public void printFragment();

		public String getText();
	}

	public static abstract class FMLCompilationUnitImpl implements FMLCompilationUnit {

		private List<String> rawData;

		public FMLCompilationUnitImpl() {
			rawData = new ArrayList<>();
		}

		/*public IRCompilationUnitNode getRootNode() {
			return rootNode;
		}
		
		protected void setRootNode(IRCompilationUnitNode rootNode) {
			this.rootNode = rootNode;
		}*/

		@Override
		public void initWithRawContents(String someContents) {
			rawData.clear();
			BufferedReader rdr = new BufferedReader(new StringReader(someContents));
			for (;;) {
				String line = null;
				try {
					line = rdr.readLine();
					if (line == null) {
						break;
					}
					rawData.add(line);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private String getLineMarker(int lineNb, int maxLineNb) {
			int maxLength = ("" + maxLineNb).length();
			String lAsString = "" + lineNb;
			return "[" + StringUtils.buildWhiteSpaceIndentation(maxLength - lAsString.length()) + lAsString + "]";
		}

		@Override
		public void printRawContents() {
			int lineNb = 1;
			for (String line : rawData) {
				System.out.println(getLineMarker(lineNb, rawData.size() - 1) + " " + line);
				lineNb++;
			}
		}

		@Override
		public Fragment makeFragment(int beginLine, int beginCol, int endLine, int endCol) {
			return new FragmentImpl(beginLine, beginCol, endLine, endCol);
		}

		@Override
		public String getFMLPrettyPrint(FMLObject object) {
			if (getPrettyPrinter() != null) {
				return getPrettyPrinter().getFMLPrettyPrint(object);
			}
			return null;
		}

		@Override
		public String getFMLSerialization(FMLObject object) {
			if (getPrettyPrinter() != null) {
				return getPrettyPrinter().getFMLSerialization(object);
			}
			return null;
		}

		public class FragmentImpl implements Fragment {
			private int beginLine = -1;
			private int beginCol = -1;
			private int endLine = -1;
			private int endCol = -1;

			public FragmentImpl(int beginLine, int beginCol, int endLine, int endCol) {
				super();
				this.beginLine = beginLine;
				this.beginCol = beginCol;
				this.endLine = endLine;
				this.endCol = endCol;

				if (endCol == 1) {
					this.endLine = endLine - 1;
					this.endCol = rawData.get(this.endLine - 1).length();
				}
				else {
					this.endCol = endCol - 1;
				}
			}

			@Override
			public int getBeginLine() {
				return beginLine;
			}

			@Override
			public int getBeginCol() {
				return beginCol;
			}

			@Override
			public int getEndLine() {
				return endLine;
			}

			@Override
			public int getEndCol() {
				return endCol;
			}

			@Override
			public String toString() {
				return "Fragment [" + beginLine + ":" + beginCol + "]-[" + endLine + ":" + endCol + "]";

			}

			@Override
			public void printFragment() {

				for (int lineNb = getBeginLine(); lineNb <= getEndLine(); lineNb++) {
					String line = lineNb <= rawData.size() ? rawData.get(lineNb - 1) : null;
					if (line == null) {
						System.out.println("Zut alors, je sais pas quoi faire pour " + lineNb + " endCol=" + getEndCol());
						continue;
					}
					if (getBeginLine() == getEndLine()) {
						System.out.println(
								getLineMarker(lineNb, rawData.size() - 1) + " " + StringUtils.buildWhiteSpaceIndentation(getBeginCol() - 1)
										+ line.substring(getBeginCol() - 1, getEndCol()));
					}
					else {
						if (lineNb == getBeginLine()) {
							System.out.println(getLineMarker(lineNb, rawData.size() - 1) + " "
									+ StringUtils.buildWhiteSpaceIndentation(getBeginCol() - 1) + line.substring(getBeginCol() - 1));
						}
						else if (lineNb == getEndLine()) {
							System.out.println(getLineMarker(lineNb, rawData.size() - 1) + " " + line.substring(0, getEndCol()));
						}
						else {
							System.out.println(getLineMarker(lineNb, rawData.size() - 1) + " " + line);
						}
					}
				}
			}

			@Override
			public String getText() {
				StringBuffer returned = new StringBuffer();
				for (int lineNb = getBeginLine(); lineNb <= getEndLine(); lineNb++) {
					String line = lineNb <= rawData.size() ? rawData.get(lineNb - 1) : null;
					if (line == null) {
						continue;
					}
					if (getBeginLine() == getEndLine()) {
						returned.append(line.substring(getBeginCol() - 1, getEndCol()));
					}
					else {
						if (lineNb == getBeginLine()) {
							returned.append(line.substring(getBeginCol() - 1));
							returned.append(StringUtils.LINE_SEPARATOR);
						}
						else if (lineNb == getEndLine()) {
							returned.append(line.substring(0, getEndCol()));
						}
						else {
							returned.append(line);
							returned.append(StringUtils.LINE_SEPARATOR);
						}
					}
				}
				return returned.toString();
			}

		}

	}

}
