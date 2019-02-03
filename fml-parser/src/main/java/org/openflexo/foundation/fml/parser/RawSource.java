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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represent raw source at it has been serialized and parsed.<br>
 * 
 * Suppose the raw source text is this:
 * 
 * <pre>
 * ABCD
 * EFG
 * HI
 * JKL
 * </pre>
 * 
 * The text will be handled as:
 * 
 * <pre>
 *    pos 0 1 2 3 4 < represents RawSourcePosition
 *   char  1 2 3 4  < represent characters indexes
 * Line 1 |A|B|C|D|
 * Line 2 |E|F|G|
 * Line 3 |H|I|
 * Line 4 |J|K|L|
 * </pre>
 * 
 * Thus, first character of that source is (1:1) (first line, first character), while first position is (1:0) (first line, before first
 * char)
 * 
 * @author sylvain
 * 
 */
public class RawSource {

	private static final Logger logger = Logger.getLogger(RawSource.class.getPackage().getName());

	public List<String> rows;
	private final RawSourcePosition startPosition;
	private final RawSourcePosition endPosition;

	/**
	 * Encodes a position in the RawSource, using line and position in line<br>
	 * Note that line numbering starts at 1 and position numbering also starts at 1<br>
	 * This means that the first position in the RawSource is (1:1)
	 * 
	 * @author sylvain
	 *
	 */
	public class RawSourcePosition implements Comparable<RawSourcePosition> {
		public final int line;
		public final int pos;

		public RawSourcePosition(int line, int pos) {
			super();
			this.line = line;
			this.pos = pos;
		}

		public boolean canDecrement() {
			return line > 1 || (line == 1 && pos > 0);
		}

		public RawSourcePosition decrement() {
			if (!canDecrement()) {
				// Cannot proceed
				throw new ArrayIndexOutOfBoundsException("Cannot decrement from position " + this);
			}
			int newPos = pos - 1;
			int newLine = line;
			if (newPos == -1) {
				newLine = line - 1;
				newPos = rows.get(newLine - 1).length();
			}
			return new RawSourcePosition(newLine, newPos);
		}

		public boolean canIncrement() {
			// System.out.println("pos=" + pos);
			// System.out.println("rows.get(rows.size() - 1).length()=" + (rows.get(rows.size() - 1).length()));
			return line < rows.size() - 1 || pos < rows.get(rows.size() - 1).length();
		}

		public RawSourcePosition increment() {
			if (!canIncrement()) {
				// Cannot proceed
				throw new ArrayIndexOutOfBoundsException("Cannot increment from position " + this);
			}
			int newPos = pos + 1;
			int newLine = line;
			if (newPos > rows.get(newLine - 1).length()) {
				newLine++;
				newPos = 0;
			}
			return new RawSourcePosition(newLine, newPos);
		}

		@Override
		public int compareTo(RawSourcePosition o) {
			if (o.line < line) {
				return 1;
			}
			else if (o.line > line) {
				return -1;
			}
			else {
				if (o.pos < pos) {
					return 1;
				}
				else if (o.pos > pos) {
					return -1;
				}
				else {
					return 0;
				}
			}
		}

		@Override
		public String toString() {
			return "(" + line + ":" + pos + ")";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + line;
			result = prime * result + pos;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RawSourcePosition other = (RawSourcePosition) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (line != other.line)
				return false;
			if (pos != other.pos)
				return false;
			return true;
		}

		private RawSource getOuterType() {
			return RawSource.this;
		}
	}

	/**
	 * Encodes a fragment of {@link RawSource}, identified by start position, inclusive and end position, exclusive<br>
	 * 
	 * @author sylvain
	 *
	 */
	public class RawSourceFragment {
		public RawSourcePosition start; // inclusive
		public RawSourcePosition end; // exclusive

		/**
		 * Build new fragment, identified by start position and end position
		 * 
		 * @param start
		 *            start position (index of first character to take)
		 * @param end
		 *            end position (index of first character to exclude)
		 */
		public RawSourceFragment(RawSourcePosition start, RawSourcePosition end) {
			super();
			if (start == null) {
				System.out.println("Tiens le start est null !");
				Thread.dumpStack();
			}
			if (end == null) {
				System.out.println("Tiens le end est null !");
				Thread.dumpStack();
			}
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString() {
			return start.toString() + "-" + end.toString();
		}

		public String getRawText() {
			int startLine = start.line;
			int startPos = start.pos;
			int endLine = end.line;
			int endPos = end.pos;
			if (startLine > -1 && startPos > -1 && endLine > -1 && endPos > -1 && startLine <= endLine) {
				if (startLine == endLine) {
					// All in one line
					// System.out.println("On retourne [" + getRawSource().get(startLine - 1).substring(startChar - 1, endChar) + "]");
					return rows.get(startLine - 1).substring(startPos, endPos);
				}
				StringBuffer sb = new StringBuffer();
				for (int i = startLine; i <= endLine; i++) {
					if (i == startLine) {
						// First line
						sb.append(rows.get(i - 1).substring(startPos) + "\n");
					}
					else if (i == endLine) {
						// Last line
						// try {
						if (endPos > rows.get(i - 1).length()) {
							sb.append(rows.get(i - 1).substring(0, endPos) + "\n");
						}
						else {
							sb.append(rows.get(i - 1).substring(0, endPos));
						}
						/*} catch (StringIndexOutOfBoundsException e) {
							System.out.println("Bizarre, pour " + getClass().getSimpleName() + " from " + startLine + ":" + startChar
									+ " to " + endLine + ":" + endChar);
							System.out.println("String = [" + rows.get(i - 1) + "]");
							System.out.println("Je cherche a extraire 0-" + endChar);
							sb.append("ERROR!");
							Thread.dumpStack();
						}*/
					}
					else {
						sb.append(rows.get(i - 1) + "\n");
					}
				}
				return sb.toString();
			}
			return null;
		}
	}

	public RawSource(InputStream inputStream) throws IOException {
		rows = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
			String nextLine = null;
			do {
				nextLine = br.readLine();
				if (nextLine != null) {
					rows.add(nextLine);
				}
			} while (nextLine != null);
		}
		startPosition = new RawSourcePosition(1, 0);
		endPosition = new RawSourcePosition(rows.size(), rows.get(rows.size() - 1).length());
	}

	/**
	 * Create a position as a cursor BEFORE the targetted character
	 * 
	 * @param line
	 * @param pos
	 * @return
	 */
	public RawSourcePosition makePositionBeforeChar(int line, int character) {
		return new RawSourcePosition(line, character - 1);
	}

	/**
	 * Create a position as a cursor AFTER the targetted character
	 * 
	 * @param line
	 * @param pos
	 * @return
	 */
	public RawSourcePosition makePositionAfterChar(int line, int character) {
		return new RawSourcePosition(line, character);
	}

	public RawSourcePosition getStartPosition() {
		return startPosition;
	}

	public RawSourcePosition getEndPosition() {
		return endPosition;
	}

	/**
	 * Build new fragment, identified by start position and end position
	 * 
	 * @param start
	 *            start position (index of first character to take)
	 * @param end
	 *            end position (index of first character to exclude)
	 */
	public RawSourceFragment makeFragment(RawSourcePosition start, RawSourcePosition end) {
		return new RawSourceFragment(start, end);
	}

	public String debug() {
		StringBuffer sb = new StringBuffer();
		for (String row : rows) {
			sb.append(row + "\n");
		}
		return sb.toString();
	}

}
