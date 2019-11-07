package org.openflexo.foundation.fml.parser;

import org.openflexo.foundation.fml.editionaction.AssignableAction;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.AddFlexoConceptInstanceNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.AssignableActionNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ControlGraphNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.ExpressionActionNode;
import org.openflexo.foundation.fml.parser.node.AFmlInstanceCreationFmlActionExp;
import org.openflexo.foundation.fml.parser.node.Node;
import org.openflexo.foundation.fml.parser.node.PFmlActionExp;
import org.openflexo.toolbox.StringUtils;

/**
 * A factory based on {@link FMLSemanticsAnalyzer}, used to instantiate base {@link AssignableAction} from AST
 * 
 * Today, this implementation is not clean, as it rely on Connie (DataBinding parsing in Connie), except for some FML constructions
 *
 * TODO: Reimplement this one day, with a Connie-compatible design (using a BindingFactory for example)
 * 
 * @author sylvain
 *
 */
// TODO reimplement this properly using BindingFactory
public class AssignableActionFactory extends FMLSemanticsAnalyzer {

	public boolean debug = false;

	private MainSemanticsAnalyzer analyser;

	private AssignableActionNode<?, ?> rootControlGraphNode = null;

	private PFmlActionExp pFMLActionExpression;

	public static AssignableActionNode<?, ?> makeAssignableActionNode(Node cgNode, MainSemanticsAnalyzer analyser) {
		AssignableActionFactory f = new AssignableActionFactory(cgNode, analyser);
		cgNode.apply(f);
		return f.rootControlGraphNode;
	}

	private AssignableActionFactory(Node cgNode, MainSemanticsAnalyzer analyser) {
		super(analyser.getFactory(), cgNode);
		this.analyser = analyser;
		cgNode.apply(this);
		if (pFMLActionExpression != null) {
			// We consider only this FML action and ignore everything else
		}
		else {
			push(new ExpressionActionNode(cgNode, analyser));
			pop();
		}
	}

	@Override
	public MainSemanticsAnalyzer getMainAnalyzer() {
		return analyser;
	}

	public ControlGraphNode<?, ?> getRootControlGraphNode() {
		return rootControlGraphNode;
	}

	public AssignableAction<?> getAssignableAction() {
		if (getRootControlGraphNode() != null) {
			return (AssignableAction<?>) getRootControlGraphNode().getModelObject();
		}
		else {
			return null;
		}
	}

	@Override
	protected void push(FMLObjectNode<?, ?, ?> fmlNode) {
		if (rootControlGraphNode == null && fmlNode instanceof AssignableActionNode) {
			rootControlGraphNode = (AssignableActionNode<?, ?>) fmlNode;
		}
		super.push(fmlNode);
		// analyzer.push(fmlNode);
	}

	@Override
	protected <N extends FMLObjectNode<?, ?, ?>> N pop() {
		N returned = super.pop();
		// analyzer.pop();
		return returned;
	}

	int indent = 0;

	@Override
	public void defaultIn(Node node) {
		super.defaultIn(node);
		if (debug) {
			System.out.println(StringUtils.buildWhiteSpaceIndentation(indent * 2) + "> " + node.getClass().getSimpleName());
			indent++;
		}
	}

	@Override
	public void defaultOut(Node node) {
		super.defaultOut(node);
		if (debug) {
			indent--;
		}
	}

	/*
	 * <pre>
	primary =
	    {literal}                literal |
	    //   {primitive}  primitive_type                                             [dims]:dim* dot class_token |
	    //   {reference}  identifier [additional_identifiers]:additional_identifier* [dims]:dim* dot class_token |
	    //   {void}       void dot class_token |
	    //   {this}       this |
	    //   {class}      identifier [additional_identifiers]:additional_identifier* dot this |
	    {expression}             l_par expression                                                 r_par |
	    {identifier}             l_par identifier [additional_identifiers]:additional_identifier* r_par |
	    {field}                  field_access |
	    {method}                 method_invocation |
	    {fml_action_expression}  fml_action_expression;
	 * </pre>
	 */

	/*
	 * <pre>
	   fml_action_exp =
	       {java_instance_creation} kw_new composite_ident type_arguments_or_diamond? l_par argument_list? r_par
	//FD: conflict with previous  {simplified} new [concept_name]:identifier l_par argument_list? r_par
	     | {fml_instance_creation}  kw_new [concept_name]:identifier colon_colon [constructor_name]:identifier l_par argument_list? r_par
	     | {ta_edition_action}      [ta_id]:identifier colon_colon [edition_action]:identifier fml_parameters in_clause? from_clause? //FD remove ? on fml_parameters
	     | {delete_action}          kw_delete l_par expression r_par from_clause?
	     | {select_action}          kw_select [concept_name]:identifier from_clause where_clause?
	     ;
	 * </pre>
	 */

	// TODO: simplified constructor for FML instances

	@Override
	public void inAFmlInstanceCreationFmlActionExp(AFmlInstanceCreationFmlActionExp node) {
		super.inAFmlInstanceCreationFmlActionExp(node);
		pFMLActionExpression = node;
		push(new AddFlexoConceptInstanceNode(node, analyser));
	}

	@Override
	public void outAFmlInstanceCreationFmlActionExp(AFmlInstanceCreationFmlActionExp node) {
		super.outAFmlInstanceCreationFmlActionExp(node);
		pop();
	}

}
