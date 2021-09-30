package org.openflexo.foundation.fml.parser;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.openflexo.connie.Bindable;
import org.openflexo.connie.DataBinding;
import org.openflexo.connie.DataBinding.BindingDefinitionType;
import org.openflexo.connie.expr.Expression;
import org.openflexo.foundation.fml.AbstractFMLTypingSpace;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FMLModelFactory;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.ConcatenationExpressionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.DataBindingNode;
import org.openflexo.foundation.fml.parser.fmlnodes.expr.URIStringConstantNode;
import org.openflexo.foundation.fml.parser.node.AConcatenationUriExpression;
import org.openflexo.foundation.fml.parser.node.ALidentifierUriExpressionPrimary;
import org.openflexo.foundation.fml.parser.node.ALitteralUriExpressionPrimary;
import org.openflexo.foundation.fml.parser.node.AUidentifierUriExpressionPrimary;
import org.openflexo.foundation.fml.parser.node.PUriExpression;

/**
 * A factory based on {@link FMLSemanticsAnalyzer}, used to instantiate a {@link DataBinding} from AST in the context of imports (only
 * concatenation operator is supported, but upper case identifiers are allowed)
 * 
 * @see PUriExpression
 * 
 * @author sylvain
 *
 */
public class URIExpressionFactory extends AbstractExpressionFactory {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(URIExpressionFactory.class.getPackage().getName());

	public static DataBinding<String> makeDataBinding(PUriExpression node, Bindable bindable, BindingDefinitionType bindingDefinitionType,
			Type expectedType, FMLCompilationUnit compilationUnit) {
		return _makeDataBinding(node, bindable, bindingDefinitionType, expectedType, compilationUnit.getTypingSpace(),
				compilationUnit.getFMLModelFactory(), null, null);
	}

	public static DataBinding<String> makeDataBinding(PUriExpression node, Bindable bindable, BindingDefinitionType bindingDefinitionType,
			Type expectedType, MainSemanticsAnalyzer mainAnalyzer, ObjectNode<?, ?, ?> parentNode) {
		return _makeDataBinding(node, bindable, bindingDefinitionType, expectedType, mainAnalyzer.getTypingSpace(),
				mainAnalyzer.getFactory(), mainAnalyzer, parentNode);
	}

	private static Expression _makeExpression(PUriExpression node, Bindable bindable, AbstractFMLTypingSpace typingSpace,
			FMLModelFactory modelFactory, MainSemanticsAnalyzer mainAnalyzer, DataBindingNode dataBindingNode) {

		URIExpressionFactory factory = new URIExpressionFactory(node, bindable, typingSpace, modelFactory, mainAnalyzer, dataBindingNode);
		factory.push(dataBindingNode);

		node.apply(factory);
		factory.pop();

		return factory.getExpression();
	}

	@SuppressWarnings({ "unchecked" })
	private static DataBinding<String> _makeDataBinding(PUriExpression node, Bindable bindable, BindingDefinitionType bindingDefinitionType,
			Type expectedType, AbstractFMLTypingSpace typingSpace, FMLModelFactory modelFactory, MainSemanticsAnalyzer mainAnalyzer,
			ObjectNode<?, ?, ?> parentNode) {

		DataBindingNode dataBindingNode = mainAnalyzer.retrieveFMLNode(node,
				n -> new DataBindingNode(n, bindable, bindingDefinitionType, expectedType, mainAnalyzer));

		if (parentNode != null) {
			parentNode.addToChildren(dataBindingNode);
		}

		_makeExpression(node, bindable, typingSpace, modelFactory, mainAnalyzer, dataBindingNode);

		return (DataBinding<String>) dataBindingNode.getModelObject();
	}

	private URIExpressionFactory(PUriExpression rootNode, Bindable aBindable, AbstractFMLTypingSpace typingSpace,
			FMLModelFactory fmlModelFactory, MainSemanticsAnalyzer mainAnalyzer, DataBindingNode dataBindingNode) {
		super(rootNode, aBindable, typingSpace, fmlModelFactory, mainAnalyzer, dataBindingNode);
	}

	@Override
	public void inAConcatenationUriExpression(AConcatenationUriExpression node) {
		super.inAConcatenationUriExpression(node);
		push(retrieveFMLNode(node, n -> new ConcatenationExpressionNode(n, this)));
	}

	@Override
	public void outAConcatenationUriExpression(AConcatenationUriExpression node) {
		super.outAConcatenationUriExpression(node);
		pop();
	}

	@Override
	public void inALitteralUriExpressionPrimary(ALitteralUriExpressionPrimary node) {
		super.inALitteralUriExpressionPrimary(node);
		push(retrieveFMLNode(node, n -> new URIStringConstantNode(n, this)));
	}

	@Override
	public void outALitteralUriExpressionPrimary(ALitteralUriExpressionPrimary node) {
		super.outALitteralUriExpressionPrimary(node);
		pop();
	}

	@Override
	public void inAUidentifierUriExpressionPrimary(AUidentifierUriExpressionPrimary node) {
		super.inAUidentifierUriExpressionPrimary(node);
		pushBindingPathNode(node);
	}

	@Override
	public void outAUidentifierUriExpressionPrimary(AUidentifierUriExpressionPrimary node) {
		super.outAUidentifierUriExpressionPrimary(node);
		popBindingPathNode(node);
	}

	@Override
	public void inALidentifierUriExpressionPrimary(ALidentifierUriExpressionPrimary node) {
		pushBindingPathNode(node);
	}

	@Override
	public void outALidentifierUriExpressionPrimary(ALidentifierUriExpressionPrimary node) {
		super.outALidentifierUriExpressionPrimary(node);
		popBindingPathNode(node);
	}

}
