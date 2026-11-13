package org.openflexo.foundation.fml.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openflexo.foundation.fml.AbstractInvariant;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.FlexoProperty;
import org.openflexo.foundation.fml.SimpleInvariant;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.Visibility;
import org.openflexo.p2pp.testing.matrix.EditCase;

/**
 * Generates atomic edits of the model of an FML unit, in the language of {@link FMLEdits}: rename, abstract and visibility of the concepts
 * and of the behaviours, removal of a body, add and remove a property, remove a failure clause. The new names follow the lexical rules of FML
 * ({@code Qx} for a concept or the model, which an all-capitals name is not, {@code q} for a behaviour, {@code gen} for a property); only a
 * property that the unit does not use is removed. To keep the matrix at a reasonable size, an
 * edit is generated for the first, a middle and the last element of each list only, which still covers every position class.
 */
final class FMLEditGenerator {

	private final String sourceName;
	private final String source;
	private final List<EditCase> cases = new ArrayList<>();

	private FMLEditGenerator(String sourceName, String source) {
		this.sourceName = sourceName;
		this.source = source;
	}

	static List<EditCase> generate(String sourceName, String source, FMLCompilationUnit unit) {
		FMLEditGenerator g = new FMLEditGenerator(sourceName, source);
		if (unit.getVirtualModel() != null) {
			g.generate(unit.getVirtualModel());
		}
		return g.cases;
	}

	private void add(String kind, String position, String edit, String inverse) {
		cases.add(new EditCase(sourceName, source, kind, position, Collections.singletonList(edit),
				inverse == null ? null : Collections.singletonList(inverse)));
	}

	private void generate(VirtualModel vm) {
		String vmName = vm.getName();
		add("rename-model", "-", "rename model to Qx", "rename model to " + vmName);

		List<FlexoConcept> concepts = new ArrayList<>();
		for (FlexoConcept c : vm.getFlexoConcepts()) {
			if (c.getName() != null && count(vm.getFlexoConcepts(), c.getName()) == 1) {
				concepts.add(c);
			}
		}
		for (int i : sample(concepts.size())) {
			FlexoConcept c = concepts.get(i);
			String pos = position(concepts.size(), i);
			String path = c.getName();
			add("rename-concept", pos, "rename " + path + " to Qx", "rename Qx to " + path);
			add("abstract-concept", pos, "set abstract " + path + " " + !c.isAbstract(), "set abstract " + path + " " + c.isAbstract());
			for (Visibility v : Visibility.values()) {
				if (v != c.getVisibility()) {
					add("visibility-concept", pos, "set visibility " + path + " " + v.name().toLowerCase(),
							"set visibility " + path + " " + c.getVisibility().name().toLowerCase());
				}
			}
			behaviours(c, path);
			invariants(c, path);
		}
		behaviours(vm, "model");

		List<FlexoProperty<?>> properties = new ArrayList<>(vm.getFlexoProperties());
		for (int i : sample(properties.size())) {
			FlexoProperty<?> p = properties.get(i);
			// A property that the rest of the unit uses cannot be removed without leaving the unit meaningless (`null = ...`)
			if (p.getName() != null && vm.getAccessibleProperty(p.getName()) == p
					&& java.util.regex.Pattern.compile("\\b" + java.util.regex.Pattern.quote(p.getName()) + "\\b").matcher(source).results().count() == 1) {
				add("remove-property", position(properties.size(), i), "remove property " + p.getName(), null);
			}
		}
		add("add-property", properties.isEmpty() ? "empty" : "last", "add primitive gen string", "remove property gen");
	}

	private void behaviours(FlexoConcept concept, String path) {
		List<FlexoBehaviour> behaviours = new ArrayList<>();
		for (FlexoBehaviour b : concept.getFlexoBehaviours()) {
			if (b.getName() != null && count(concept.getFlexoBehaviours(), b.getName()) == 1) {
				behaviours.add(b);
			}
		}
		for (int i : sample(behaviours.size())) {
			FlexoBehaviour b = behaviours.get(i);
			String pos = position(behaviours.size(), i);
			String bpath = path + "." + b.getName();
			add("rename-behaviour", pos, "rename " + bpath + " to q", "rename " + path + ".q to " + b.getName());
			add("abstract-behaviour", pos, "set abstract " + bpath + " " + !b.isAbstract(), "set abstract " + bpath + " " + b.isAbstract());
			for (Visibility v : Visibility.values()) {
				if (v != b.getVisibility()) {
					add("visibility-behaviour", pos, "set visibility " + bpath + " " + v.name().toLowerCase(),
							"set visibility " + bpath + " " + b.getVisibility().name().toLowerCase());
				}
			}
			if (b.getControlGraph() != null) {
				add("remove-body", pos, "set body " + bpath + " none", null);
			}
		}
	}

	private void invariants(FlexoConcept concept, String path) {
		List<AbstractInvariant> invariants = concept.getInvariants();
		for (int i : sample(invariants.size())) {
			if (invariants.get(i) instanceof SimpleInvariant && ((SimpleInvariant) invariants.get(i)).getViolationControlGraph() != null) {
				add("remove-failure", position(invariants.size(), i), "set failure " + path + " " + i + " none", null);
			}
		}
	}

	/** The first, a middle and the last index of a list of that size */
	private static Set<Integer> sample(int size) {
		Set<Integer> returned = new LinkedHashSet<>();
		if (size > 0) {
			returned.addAll(Arrays.asList(0, size / 2, size - 1));
		}
		return returned;
	}

	private static String position(int size, int index) {
		return size == 1 ? "only" : index == 0 ? "first" : index == size - 1 ? "last" : "middle";
	}

	private static int count(List<? extends Object> list, String name) {
		int n = 0;
		for (Object o : list) {
			String other = o instanceof FlexoConcept ? ((FlexoConcept) o).getName() : o instanceof FlexoBehaviour ? ((FlexoBehaviour) o).getName() : null;
			if (name.equals(other)) {
				n++;
			}
		}
		return n;
	}
}
