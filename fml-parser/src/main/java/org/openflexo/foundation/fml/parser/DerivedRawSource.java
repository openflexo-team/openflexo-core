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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.openflexo.foundation.fml.parser.RawSource.RawSourceFragment;
import org.openflexo.foundation.fml.parser.RawSource.RawSourcePosition;

/**
 * Represent a RawSource which stores evolution of a parsed code (a RawSource)
 * 
 * @author sylvain
 * 
 */
public class DerivedRawSource {

	private static final Logger logger = Logger.getLogger(DerivedRawSource.class.getPackage().getName());

	private final RawSourceFragment sourceFragment;
	private final List<Modification> modifications;

	public DerivedRawSource(RawSourceFragment sourceFragment) {
		this.sourceFragment = sourceFragment;
		modifications = new ArrayList<>();
	}

	public RawSourceFragment getSourceFragment() {
		return sourceFragment;
	}

	public void replace(RawSourceFragment fragment, String replacement) {
		if (!fragment.getRawText().equals(replacement)) {
			// System.out.println("On remplace " + fragment + " (values [" + fragment.getRawText() + "]) par " + replacement);
			modifications.add(new StringReplacement(fragment, replacement));
		}
	}

	public void replace(RawSourceFragment fragment, DerivedRawSource replacement) {
		if (!fragment.getRawText().equals(replacement)) {
			// System.out.println("On remplace " + fragment + " (values [" + fragment.getRawText() + "]) par " + replacement);
			modifications.add(new DerivedRawSourceReplacement(fragment, replacement));
		}
	}

	public void insert(RawSourcePosition insertionPoint, String insertion) {
		// System.out.println("On insere a la position " + insertionPoint + " la valeur [" + insertion + "])");
		modifications.add(new StringInsertion(insertionPoint, insertion));
	}

	public void remove(RawSourceFragment fragment) {
		// System.out.println("On supprime " + fragment + " (values [" + fragment.getRawText() + "])");
		modifications.add(new StringDeletion(fragment));
	}

	public String getStringRepresentation() {
		try {
			checkNoModificationsOverlapping();
		} catch (ModificationOverlapping e) {
			logger.warning("ModificationOverlapping detected !!!");
		}
		Collections.sort(modifications, new Comparator<Modification>() {
			@Override
			public int compare(Modification o1, Modification o2) {
				return o1.getInitialFragment().getStartPosition().compareTo(o2.getInitialFragment().getStartPosition());
			}
		});

		/*System.out.println("Les modifications:");
		for (Modification modification : modifications) {
			System.out.println(" > " + modification);
		}*/

		if (modifications.size() == 0) {
			return getSourceFragment().getRawText();
		}

		RawSourcePosition current = getSourceFragment().getStartPosition();
		StringBuffer sb = new StringBuffer();
		for (Modification modification : modifications) {
			RawSourceFragment replacedFragment = modification.getInitialFragment();
			RawSourcePosition toPosition = replacedFragment.getStartPosition();
			RawSourceFragment prelude = getSourceFragment().getRawSource().makeFragment(current, toPosition);
			sb.append(prelude.getRawText());
			if (modification instanceof StringReplacement) {
				sb.append(((StringReplacement) modification).getReplacement());
			}
			if (modification instanceof DerivedRawSourceReplacement) {
				sb.append(((DerivedRawSourceReplacement) modification).replacement.getStringRepresentation());
			}
			if (modification instanceof StringInsertion) {
				sb.append(((StringInsertion) modification).getInsertion());
			}
			if (modification instanceof StringDeletion) {
				// Do not append it !
			}
			current = replacedFragment.getEndPosition();
		}
		RawSourceFragment postlude = getSourceFragment().getRawSource().makeFragment(current, getSourceFragment().getEndPosition());
		sb.append(postlude.getRawText());

		return sb.toString();

		// if (debug) {
		// System.out.println("currentLine=" + currentLine + " currentChar=" + currentChar);
		// }

		/*for (PrettyPrintableContents childObject : childrenObjects) {
			if (childObject instanceof ChildContents) {
				FMLObjectNode<?, ?> childNode = ((ChildContents) childObject).childNode;
				// System.out.println(
				// "> " + childNode.getClass().getSimpleName() + " from " + childNode.getStartLine() + ":" + childNode.getStartChar()
				// + "-" + childNode.getEndLine() + ":" + childNode.getEndChar() + " for " + childNode.getFMLObject());
				RawSourcePosition toPosition = childNode.getStartPosition();
		
				RawSourceFragment prelude = getRawSource().makeFragment(current, toPosition);
		
				// RawSourceFragment prelude = extract(currentLine, currentChar, toLine, toChar);
				if (debug) {
					System.out.println("Before handling " + childNode.getFMLObject() + " / Adding " + prelude + " value ["
							+ prelude.getRawText() + "]");
				}
				sb.append(prelude.getRawText());
		
				String updatedChildrenPP = childNode.updateFMLRepresentation(context.derive());
				if (debug) {
					System.out.println("Now consider " + getFMLObject() + " " + childNode.getLastParsedFragment() + " value ["
							+ updatedChildrenPP + "]");
				}
				sb.append(updatedChildrenPP);
		
				current = childNode.getEndPosition();
		
				if (debug) {
					System.out.println("AFTER adding children current=" + current);
				}
			}
		}
		
		System.out.println("current=" + current);
		System.out.println("getEndPosition()=" + getEndPosition());
		RawSourceFragment postlude = getRawSource().makeFragment(current, getEndPosition());
		
		if (debug) {
			System.out.println("At the end for " + getFMLObject() + " / Adding remaining " + postlude + ") value [" + postlude + "]");
		}
		sb.append(postlude.getRawText());
		
		if (debug) {
			System.out.println("<------------------------> DONE Pretty-Print for " + getClass().getSimpleName());
			System.out.println("RESULT: [" + sb.toString() + "]");
		}
		return sb.toString();*/

		// return sourceFragment.getRawText();
	}

	public void checkNoModificationsOverlapping() throws ModificationOverlapping {
	}

	@SuppressWarnings("serial")
	public class ModificationOverlapping extends Exception {

	}

	public class Modification {

		private RawSourceFragment initialFragment;

		public Modification(RawSourceFragment initialFragment) {
			super();
			this.initialFragment = initialFragment;
		}

		public RawSourceFragment getInitialFragment() {
			return initialFragment;
		}
	}

	public class StringReplacement extends Modification {

		private String replacement;

		public StringReplacement(RawSourceFragment initialFragment, String replacement) {
			super(initialFragment);
			this.replacement = replacement;
		}

		public String getReplacement() {
			return replacement;
		}

		@Override
		public String toString() {
			return "StringReplacement " + getInitialFragment() + " with " + getReplacement();
		}
	}

	public class DerivedRawSourceReplacement extends Modification {

		public DerivedRawSource replacement;

		public DerivedRawSourceReplacement(RawSourceFragment initialFragment, DerivedRawSource replacement) {
			super(initialFragment);
			this.replacement = replacement;
		}

		@Override
		public String toString() {
			return "DerivedRawSourceReplacement " + getInitialFragment();
		}

	}

	public class StringInsertion extends Modification {

		private String insertion;

		public StringInsertion(RawSourcePosition insertionPoint, String insertion) {
			super(insertionPoint.getOuterType().makeFragment(insertionPoint, insertionPoint));
			this.insertion = insertion;
		}

		public String getInsertion() {
			return insertion;
		}

		@Override
		public String toString() {
			return "StringInsertion " + getInitialFragment() + " with " + getInsertion();
		}
	}

	public class StringDeletion extends Modification {

		public StringDeletion(RawSourceFragment initialFragment) {
			super(initialFragment);
		}

		@Override
		public String toString() {
			return "StringDeletion " + getInitialFragment();
		}
	}

}
