package org.openflexo.foundation.fml.parser;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.controlgraph.FMLControlGraph;
import org.openflexo.foundation.fml.parser.node.Node;

/**
 * A factory based on {@link FMLSemanticsAnalyzer}, used to instantiate {@link FMLControlGraph} from AST
 * 
 * @author sylvain
 *
 */
public class ExpressionFactory extends FMLSemanticsAnalyzer {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(ExpressionFactory.class.getPackage().getName());

	private MainSemanticsAnalyzer mainAnalyzer;

	public static DataBinding<?> makeExpression(Node node, MainSemanticsAnalyzer analyzer, Bindable bindable) {
		return _makeExpression(node, analyzer, bindable, Object.class);
	}

	private static DataBinding<?> _makeExpression(Node node, MainSemanticsAnalyzer analyzer, Bindable bindable, Type expectedType) {
		// TODO
		// Faire pour le vrai...
		return new DataBinding(analyzer.getText(node), bindable, expectedType, BindingDefinitionType.GET);

	}

	private ExpressionFactory(FMLModelFactory factory, Node rootNode) {
		super(factory, rootNode);
	}

	@Override
	public MainSemanticsAnalyzer getMainAnalyzer() {
		return mainAnalyzer;
	}

}
