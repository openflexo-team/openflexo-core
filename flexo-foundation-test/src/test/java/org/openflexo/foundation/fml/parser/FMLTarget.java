package org.openflexo.foundation.fml.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.p2pp.testing.PrettyPrintTarget;

/**
 * FML as a language of the p2pp test harness ({@code p2pp-test}): a source is parsed by {@link FMLCompilationUnitParser} with a model factory
 * of the supplied service manager.
 * <p>
 * The model of a compilation unit is compared through its normalized pretty-print, which is computed from the model alone: two units are the
 * same when that text is, Java imports left out (the parser adds the ones that the types need). The edits are those of {@link FMLEdits}.
 */
public class FMLTarget implements PrettyPrintTarget {

	private static final Pattern COMMENT = Pattern.compile("//[^\n]*|/\\*.*?\\*/", Pattern.DOTALL);

	/** A Java import (not an import of a model by its URI): the FML parser adds the ones that the types of a unit need, which is intended */
	private static final Pattern JAVA_IMPORT = Pattern.compile("(?m)^import [A-Za-z_][A-Za-z0-9_.]*;[ \t]*(\n|$)");

	private final FlexoServiceManager serviceManager;
	private final FlexoEditor editor;

	public FMLTarget(FlexoServiceManager serviceManager) {
		this.serviceManager = serviceManager;
		this.editor = new DefaultFlexoEditor(null, serviceManager);
	}

	@Override
	public Document parse(String source) throws Exception {
		FMLCompilationUnit unit = new FMLCompilationUnitParser().parse(source, new FMLModelFactory(null, serviceManager),
				modelSlotClasses -> null, true);
		return new Document() {
			@Override
			public String dump() {
				return withoutJavaImports(unit.getNormalizedFML());
			}

			@Override
			public String derived() {
				return unit.getFMLPrettyPrint();
			}

			@Override
			public String normalized() {
				return unit.getNormalizedFML();
			}

			@Override
			public void apply(String command) {
				FMLEdits.apply(unit, editor, command);
			}
		};
	}

	/** The text without its Java imports, implicit or not: the invariants do not tell the two apart */
	static String withoutJavaImports(String text) {
		// the blank rows that were around an import are not told apart either
		return JAVA_IMPORT.matcher(text).replaceAll("").replaceAll("\n{3,}", "\n\n").replaceAll("^\n+", "");
	}

	@Override
	public boolean sameText(String expected, String actual) {
		return withoutJavaImports(expected).equals(withoutJavaImports(actual));
	}

	@Override
	public List<String> comments(String text) {
		List<String> returned = new ArrayList<>();
		Matcher m = COMMENT.matcher(text);
		while (m.find()) {
			returned.add(m.group());
		}
		return returned;
	}
}
