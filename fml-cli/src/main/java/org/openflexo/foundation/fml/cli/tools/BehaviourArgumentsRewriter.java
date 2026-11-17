/**
 *
 * Copyright (c) 2014-2026, Openflexo
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

package org.openflexo.foundation.fml.cli.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openflexo.connie.BindingModel;
import org.openflexo.connie.BindingVariable;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoBehaviourArgument;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FMLPrettyPrintDelegate;
import org.openflexo.foundation.fml.binding.FlexoPropertyBindingVariable;
import org.openflexo.foundation.fml.controlgraph.AbstractIterationAction;
import org.openflexo.foundation.fml.controlgraph.CatchClause;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraphVisitor;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.p2pp.RawSource.RawSourceFragment;

/**
 * Computes, for the behaviours of a VirtualModel, the edits of its FML text that move from <code>parameters.aValue</code> to
 * <code>aValue</code> (CORE-F-14). The model decides what is rewritten, the text is edited only where it must be (no re-serialization).
 * <ul>
 * <li><code>parameters.a</code> becomes <code>a</code>, in the whole behaviour, unless a local variable of the behaviour is named
 * <code>a</code> (reported);</li>
 * <li>when an argument has the name of a property of the concept, and the behaviour still reads an argument through
 * <code>parameters</code> (FML written before the new notation), every unqualified use of that name in the body meant the property (an
 * argument was only reachable through <code>parameters</code>): it becomes <code>this.a</code>, otherwise it would now designate the
 * argument. A name followed by <code>(</code>, or a key of a <code>with (key=value)</code> clause, is not a use.</li>
 * </ul>
 * 
 * @author sylvain
 */
public class BehaviourArgumentsRewriter {

	/** A replacement of the characters [start, end) of the source by a text */
	public static class Edit {
		public final int start, end;
		public final String replacement;

		Edit(int start, int end, String replacement) {
			this.start = start;
			this.end = end;
			this.replacement = replacement;
		}
	}

	private final List<String> reports = new ArrayList<>();

	/** What a person has to look at */
	public List<String> getReports() {
		return reports;
	}

	/**
	 * Return the edits for the behaviours of supplied concepts (nested concepts and behaviours inherited are not considered here: the
	 * caller gives every concept of the VirtualModel); offsets are in the raw source of supplied text
	 */
	public List<Edit> computeEdits(List<FlexoConcept> concepts, String sourceText) {
		List<Edit> edits = new ArrayList<>();
		for (FlexoConcept concept : concepts) {
			for (FlexoBehaviour behaviour : concept.getDeclaredFlexoBehaviours()) {
				if (!behaviour.getParameters().isEmpty() && behaviour.getPrettyPrintDelegate() != null) {
					editsFor(behaviour, sourceText, edits);
				}
			}
		}
		Collections.sort(edits, Comparator.comparingInt((Edit e) -> e.start));
		return edits;
	}

	private void editsFor(FlexoBehaviour behaviour, String source, List<Edit> edits) {
		FMLPrettyPrintDelegate<?> delegate = behaviour.getPrettyPrintDelegate();
		RawSourceFragment whole = delegate.getFragment();
		int start = offsetOf(source, whole);
		int end = endOffsetOf(source, whole);
		if (start < 0 || end < start) {
			reports.add(behaviour.getName() + ": source position unknown, not migrated");
			return;
		}
		int bodyStart = start;
		FMLControlGraph body = behaviour.getControlGraph();
		if (body != null && body.getPrettyPrintDelegate() != null && body.getPrettyPrintDelegate().getFragment() != null) {
			int bs = offsetOf(source, body.getPrettyPrintDelegate().getFragment());
			if (bs >= start && bs <= end) {
				bodyStart = bs;
			}
		}

		Set<String> locals = localNames(behaviour);
		Set<String> arguments = new HashSet<>();
		Set<String> collisions = new HashSet<>();
		BindingModel conceptBM = behaviour.getFlexoConcept() != null ? behaviour.getFlexoConcept().getBindingModel() : null;
		for (FlexoBehaviourArgument argument : behaviour.getParameters()) {
			String name = argument.getName();
			if (locals.contains(name)) {
				reports.add(behaviour.getName() + ": a local variable has the name of the argument " + name + ", kept as is");
				continue;
			}
			arguments.add(name);
			BindingVariable other = conceptBM != null ? conceptBM.bindingVariableNamed(name) : null;
			if (other instanceof FlexoPropertyBindingVariable && behaviour.getFlexoConcept().getAccessibleProperty(name) != null) {
				collisions.add(name);
			}
			else if (other != null) {
				reports.add(behaviour.getName() + ": the argument " + name + " has the name of " + other.getVariableName()
						+ " (not a property): look at its uses");
			}
		}
		List<Edit> local = new ArrayList<>();
		rewrite(source, start, end, bodyStart, arguments, collisions, local);
		// An unqualified name meant the property only in FML written before the new notation, which reads the arguments through
		// parameters: without that mark the behaviour is already migrated (or new), and this. would be wrong. That makes the tool safe to
		// run again on FML it has migrated.
		// Only the names read through parameters somewhere in the behaviour are concerned (per name, so that a behaviour already using
		// the new notation for another argument is left alone)
		Set<String> legacyNames = new HashSet<>();
		for (Edit e : local) {
			if (!e.replacement.startsWith("this.")) {
				legacyNames.add(e.replacement);
			}
		}
		for (Edit e : local) {
			if (!e.replacement.startsWith("this.") || legacyNames.contains(e.replacement.substring("this.".length()))) {
				edits.add(e);
			}
		}
	}

	/** Offset in source of the start of supplied fragment, or -1 when the fragment does not match the text */
	private static int offsetOf(String source, RawSourceFragment fragment) {
		if (fragment == null) {
			return -1;
		}
		int start = offset(source, fragment.getStartPosition().getLine(), fragment.getStartPosition().getPos());
		int end = endOffsetOf(source, fragment);
		if (start < 0 || end < start) {
			return -1;
		}
		// The text found where the fragment says it is must be the text of the fragment
		String found = source.substring(start, end).replace("\r\n", "\n").replace('\r', '\n').trim();
		String expected = fragment.getRawText().replace("\r\n", "\n").replace('\r', '\n').trim();
		return found.equals(expected) ? start : -1;
	}

	private static int endOffsetOf(String source, RawSourceFragment fragment) {
		return fragment == null ? -1 : offset(source, fragment.getEndPosition().getLine(), fragment.getEndPosition().getPos());
	}

	/** Offset of the character at (line, pos), lines counted from 1 and separated by \n, \r\n or \r, as the RawSource does */
	private static int offset(String source, int line, int pos) {
		int current = 1;
		int i = 0;
		while (current < line) {
			if (i >= source.length()) {
				return -1;
			}
			char c = source.charAt(i++);
			if (c == '\r') {
				if (i < source.length() && source.charAt(i) == '\n') {
					i++;
				}
				current++;
			}
			else if (c == '\n') {
				current++;
			}
		}
		int result = i + pos;
		return result <= source.length() ? result : -1;
	}

	private static Set<String> localNames(FlexoBehaviour behaviour) {
		Set<String> locals = new HashSet<>();
		if (behaviour.getControlGraph() != null) {
			behaviour.getControlGraph().accept(new FMLControlGraphVisitor() {
				@Override
				public void visit(FMLControlGraph controlGraph) {
					if (controlGraph instanceof DeclarationAction && ((DeclarationAction<?>) controlGraph).getVariableName() != null) {
						locals.add(((DeclarationAction<?>) controlGraph).getVariableName());
					}
					if (controlGraph instanceof AbstractIterationAction) {
						locals.add(((AbstractIterationAction) controlGraph).getIteratorName());
					}
					if (controlGraph instanceof CatchClause) {
						locals.add(((CatchClause) controlGraph).getVariableName());
					}
				}
			});
		}
		return locals;
	}

	/**
	 * Scan source[start, end), skipping literals and comments, and add the edits. bodyStart is where the body of the behaviour begins
	 * (the collisions are only rewritten from there: the signature declares the arguments)
	 */
	static void rewrite(String s, int start, int end, int bodyStart, Set<String> arguments, Set<String> collisions, List<Edit> edits) {
		int depth = 0;
		int i = start;
		while (i < end) {
			char c = s.charAt(i);
			if (c == '"' || c == '\'') {
				i = skipLiteral(s, i, end, c);
				continue;
			}
			if (c == '/' && i + 1 < end && s.charAt(i + 1) == '/') {
				while (i < end && s.charAt(i) != '\n') {
					i++;
				}
				continue;
			}
			if (c == '/' && i + 1 < end && s.charAt(i + 1) == '*') {
				int close = s.indexOf("*/", i + 2);
				i = close < 0 ? end : Math.min(end, close + 2);
				continue;
			}
			if (c == '(') {
				depth++;
			}
			else if (c == ')') {
				depth--;
			}
			if (Character.isJavaIdentifierStart(c)) {
				int j = i + 1;
				while (j < end && Character.isJavaIdentifierPart(s.charAt(j))) {
					j++;
				}
				String word = s.substring(i, j);
				boolean qualified = isQualified(s, i, start);
				if (!qualified && "parameters".equals(word) && j < end && s.charAt(j) == '.') {
					int k = j + 1;
					int l = k;
					while (l < end && Character.isJavaIdentifierPart(s.charAt(l))) {
						l++;
					}
					String name = s.substring(k, l);
					if (arguments.contains(name)) {
						edits.add(new Edit(i, l, name));
						i = l;
						continue;
					}
				}
				else if (!qualified && i >= bodyStart && collisions.contains(word) && !followedByParenthesis(s, j, end)
						&& !isNamedArgumentKey(s, j, end, depth)) {
					edits.add(new Edit(i, j, "this." + word));
				}
				i = j;
				continue;
			}
			i++;
		}
	}

	private static int skipLiteral(String s, int i, int end, char quote) {
		int j = i + 1;
		while (j < end && s.charAt(j) != quote) {
			j += s.charAt(j) == '\\' ? 2 : 1;
		}
		return Math.min(end, j + 1);
	}

	/** True when the identifier at position i follows a dot or a name character (a member, not a variable) */
	private static boolean isQualified(String s, int i, int start) {
		int k = i - 1;
		while (k >= start && (s.charAt(k) == ' ' || s.charAt(k) == '\t')) {
			k--;
		}
		return k >= start && (s.charAt(k) == '.' || s.charAt(k) == '$');
	}

	private static boolean followedByParenthesis(String s, int j, int end) {
		while (j < end && (s.charAt(j) == ' ' || s.charAt(j) == '\t')) {
			j++;
		}
		return j < end && s.charAt(j) == '(';
	}

	/** In a with (key=value, ...) clause, a name followed by = is a key, not a variable */
	private static boolean isNamedArgumentKey(String s, int j, int end, int depth) {
		while (j < end && (s.charAt(j) == ' ' || s.charAt(j) == '\t')) {
			j++;
		}
		return depth > 0 && j < end && s.charAt(j) == '=' && (j + 1 >= end || s.charAt(j + 1) != '=');
	}

	/** Apply edits (sorted, not overlapping) to supplied text */
	public static String apply(String source, List<Edit> edits) {
		StringBuilder sb = new StringBuilder();
		int last = 0;
		for (Edit e : edits) {
			if (e.start < last) {
				continue;
			}
			sb.append(source, last, e.start).append(e.replacement);
			last = e.end;
		}
		sb.append(source, last, source.length());
		return sb.toString();
	}
}
