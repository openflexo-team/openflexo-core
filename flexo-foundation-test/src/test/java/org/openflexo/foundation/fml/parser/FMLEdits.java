package org.openflexo.foundation.fml.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openflexo.connie.DataBinding;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.InvalidNameException;
import org.openflexo.foundation.fml.AbstractInvariant;
import org.openflexo.foundation.fml.SimpleInvariant;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.editionaction.LogAction;
import org.openflexo.foundation.fml.CreationScheme;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.JavaImportDeclaration;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.Visibility;

/**
 * The language of edits of FML for the p2pp test harness (scenarios {@code .p2pt}): changes of the model of a compilation unit, applied by
 * {@link FMLTarget}. They are the edits of the {@code TestFMLPrettyPrintN} tests, by name.
 *
 * <pre>
 * rename PATH to NAME                      (the model, a concept, a behaviour or the constructor)
 * set abstract PATH true|false             (a concept or a behaviour)
 * set visibility PATH public|protected|private|default
 * set body PATH none|empty                 (a behaviour: no control graph, or an empty one)
 * set anonymous constructor true|false     (the first creation scheme of the model)
 * set import INDEX CLASS                   (the full qualified class of the Java import at INDEX, from 0)
 * update add-line TEXT                    (the way the FML editor changes a unit: TEXT, a line of the model, is added to the printed text, which is parsed and
 *                                          then updates the unit; the unit keeps the objects that did not change)
 * add primitive NAME TYPE                  (a property of the model, TYPE being a primitive type: string, date, integer...)
 * remove property NAME
 * set failure CONCEPT INDEX none           (the failure clause of the invariant at INDEX of the concept, from 0)
 * set failure CONCEPT INDEX log EXPRESSION (a clause that logs, EXPRESSION being a binding, a quoted string for example)
 * append failure CONCEPT INDEX log EXPRESSION   (one more log after those of the clause)
 * </pre>
 *
 * A PATH is {@code model}, {@code Concept}, {@code Concept.behaviour} or {@code model.behaviour} (a behaviour of the model itself), or
 * {@code constructor}.
 */
public final class FMLEdits {

	private static final String PATH = "([A-Za-z_][A-Za-z0-9_.]*)";
	private static final String ID = "([A-Za-z_][A-Za-z0-9_]*)";

	private static final Pattern RENAME = Pattern.compile("rename " + PATH + " to " + ID);
	private static final Pattern SET_ABSTRACT = Pattern.compile("set abstract " + PATH + " (true|false)");
	private static final Pattern SET_VISIBILITY = Pattern.compile("set visibility " + PATH + " (public|protected|private|default)");
	private static final Pattern SET_BODY = Pattern.compile("set body " + PATH + " (none|empty)");
	private static final Pattern SET_ANONYMOUS = Pattern.compile("set anonymous constructor (true|false)");
	private static final Pattern SET_IMPORT = Pattern.compile("set import (\\d+) ([A-Za-z_][A-Za-z0-9_.]*)");
	private static final Pattern UPDATE_ADD_LINE = Pattern.compile("update add-line (.+)");
	private static final Pattern ADD_PRIMITIVE = Pattern.compile("add primitive " + ID + " " + ID);
	private static final Pattern SET_FAILURE = Pattern.compile("(set|append) failure " + ID + " (\\d+) (?:(none)|log (.+))");
	private static final Pattern REMOVE_PROPERTY = Pattern.compile("remove property " + ID);

	private FMLEdits() {
	}

	public static void apply(FMLCompilationUnit unit, FlexoEditor editor, String command) {
		VirtualModel vm = unit.getVirtualModel();
		Matcher m;
		if ((m = SET_ANONYMOUS.matcher(command)).matches()) {
			constructor(vm).setAnonymous(Boolean.parseBoolean(m.group(1)));
		}
		else if ((m = SET_IMPORT.matcher(command)).matches()) {
			JavaImportDeclaration declaration = unit.getJavaImports().get(Integer.parseInt(m.group(1)));
			declaration.setFullQualifiedClassName(m.group(2));
		}
		else if ((m = REMOVE_PROPERTY.matcher(command)).matches()) {
			if (vm.getAccessibleProperty(m.group(1)) == null) {
				throw new IllegalArgumentException("No property '" + m.group(1) + "'");
			}
			FlexoProperty<?> property = vm.getAccessibleProperty(m.group(1));
			vm.removeFromFlexoProperties(property);
			property.delete();
		}
		else if ((m = UPDATE_ADD_LINE.matcher(command)).matches()) {
			String fml = unit.getFMLPrettyPrint().trim();
			fml = fml.substring(0, fml.length() - 1) + "\n" + m.group(1) + "\n}\n";
			try {
				FMLCompilationUnit returned = new FMLCompilationUnitParser().parse(fml, unit.getFMLModelFactory(), modelSlotClasses -> null, false);
				unit.updateWith(returned);
				unit.manageImports();
			} catch (Exception e) {
				throw new IllegalArgumentException("Cannot update the unit: " + e, e);
			}
		}
		else if ((m = ADD_PRIMITIVE.matcher(command)).matches()) {
			PrimitiveType type = null;
			for (PrimitiveType candidate : PrimitiveType.values()) {
				if (candidate.name().equalsIgnoreCase(m.group(2))) {
					type = candidate;
				}
			}
			if (type == null) {
				throw new IllegalArgumentException("Unknown primitive type " + m.group(2));
			}
			CreatePrimitiveRole action = CreatePrimitiveRole.actionType.makeNewAction(vm, null, editor);
			action.setRoleName(m.group(1));
			action.setPrimitiveType(type);
			action.doAction();
		}
		else if ((m = SET_FAILURE.matcher(command)).matches()) {
			FlexoConcept concept = vm.getFlexoConcept(m.group(2));
			if (concept == null) {
				throw new IllegalArgumentException("No concept '" + m.group(2) + "'");
			}
			AbstractInvariant abstractInvariant = concept.getInvariants().get(Integer.parseInt(m.group(3)));
			SimpleInvariant invariant = (SimpleInvariant) abstractInvariant;
			if (m.group(4) != null) {
				invariant.setViolationControlGraph(null);
			}
			else {
				LogAction log = invariant.getFMLModelFactory().newLogAction();
				log.setLogString(new DataBinding<String>(m.group(5)));
				if (m.group(1).equals("set")) {
					invariant.setViolationControlGraph(log);
				}
				else {
					invariant.getViolationControlGraph().sequentiallyAppend(log);
				}
			}
		}
		else if ((m = SET_ABSTRACT.matcher(command)).matches()) {
			boolean value = Boolean.parseBoolean(m.group(2));
			Object target = resolve(vm, m.group(1));
			if (target instanceof FlexoBehaviour) {
				((FlexoBehaviour) target).setAbstract(value);
			}
			else {
				((FlexoConcept) target).setAbstract(value);
			}
		}
		else if ((m = SET_VISIBILITY.matcher(command)).matches()) {
			Visibility visibility = Visibility.valueOf(Character.toUpperCase(m.group(2).charAt(0)) + m.group(2).substring(1));
			Object target = resolve(vm, m.group(1));
			if (target instanceof FlexoBehaviour) {
				((FlexoBehaviour) target).setVisibility(visibility);
			}
			else {
				((FlexoConcept) target).setVisibility(visibility);
			}
		}
		else if ((m = SET_BODY.matcher(command)).matches()) {
			Object target = resolve(vm, m.group(1));
			if (!(target instanceof FlexoBehaviour)) {
				throw new IllegalArgumentException("'" + m.group(1) + "' is not a behaviour");
			}
			FlexoBehaviour behaviour = (FlexoBehaviour) target;
			behaviour.setControlGraph(m.group(2).equals("none") ? null : behaviour.getFMLModelFactory().newEmptyControlGraph());
		}
		else if ((m = RENAME.matcher(command)).matches()) {
			Object target = resolve(vm, m.group(1));
			try {
				if (target instanceof FlexoBehaviour) {
					((FlexoBehaviour) target).setName(m.group(2));
				}
				else {
					((FlexoConcept) target).setName(m.group(2));
				}
			} catch (InvalidNameException e) {
				throw new IllegalArgumentException("Invalid name " + m.group(2), e);
			}
		}
		else {
			throw new IllegalArgumentException("Unknown edit: " + command);
		}
	}

	private static CreationScheme constructor(VirtualModel vm) {
		if (vm.getCreationSchemes().isEmpty()) {
			throw new IllegalArgumentException("The model has no constructor");
		}
		return vm.getCreationSchemes().get(0);
	}

	/** The concept, the behaviour or the constructor a path designates */
	private static Object resolve(VirtualModel vm, String path) {
		if (path.equals("constructor")) {
			return constructor(vm);
		}
		String[] segments = path.split("\\.");
		FlexoConcept concept = segments[0].equals("model") ? vm : vm.getFlexoConcept(segments[0]);
		if (concept == null) {
			throw new IllegalArgumentException("No concept '" + segments[0] + "'");
		}
		if (segments.length == 1) {
			return concept;
		}
		for (FlexoBehaviour behaviour : concept.getFlexoBehaviours()) {
			if (segments[1].equals(behaviour.getName())) {
				return behaviour;
			}
		}
		throw new IllegalArgumentException("No behaviour '" + segments[1] + "' in '" + segments[0] + "'");
	}
}
