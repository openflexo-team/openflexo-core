package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.FMLCompilationUnit;
import org.openflexo.foundation.fml.FlexoBehaviour;
import org.openflexo.foundation.fml.controlgraph.Sequence;
import org.openflexo.foundation.fml.editionaction.DeclarationAction;
import org.openflexo.foundation.fml.parser.fmlnodes.FMLCompilationUnitNode;
import org.openflexo.foundation.fml.parser.fmlnodes.controlgraph.DeclarationActionNode;
import org.openflexo.foundation.fml.rt.editionaction.AbstractSelectFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectFlexoConceptInstance;
import org.openflexo.foundation.fml.rt.editionaction.SelectUniqueFlexoConceptInstance;
import org.openflexo.foundation.test.parser.FMLParserTestCase;
import org.openflexo.p2pp.P2PPNode;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * A declaration whose initializer is an edition action - <code>Item found = select unique Item from this where (...)</code> - is built
 * from its own production of the grammar (<code>initializer_fml_action</code>), not from the one of the expressions. This test shows that
 * the declaration is complete: it holds the action, which has its type and its conditions; the node of the declaration holds the node of
 * the action; and the text printed from the model gives the declaration back. (The log used to say that this case was "not implemented
 * yet": it is, and this is what shows it.)
 */
@RunWith(OrderedRunner.class)
public class TestDeclarationWithEditionAction extends FMLParserTestCase {

	static FMLCompilationUnit compilationUnit;
	static FMLCompilationUnitNode rootNode;

	@Test
	@TestOrder(1)
	public void initServiceManager() {
		instanciateTestServiceManager();
	}

	@Test
	@TestOrder(2)
	public void loadCompilationUnit() throws ParseException, ModelDefinitionException, IOException {
		Resource fmlFile = ResourceLocator.locateResource("TestResourceCenter/FML/TestDeclarationWithAction.fml/TestDeclarationWithAction.fml");
		assertNotNull(fmlFile);
		compilationUnit = testFMLCompilationUnit(fmlFile);
		assertNotNull(rootNode = (FMLCompilationUnitNode) compilationUnit.getPrettyPrintDelegate());
	}

	/** <code>Item found = select unique Item from this where (...)</code> */
	@Test
	@TestOrder(3)
	public void testSelectUniqueDeclaration() {
		DeclarationAction declaration = firstDeclarationOf("nameOfTheOneWithValue");

		assertEquals("found", declaration.getVariableName());
		assertEquals("Item", declaration.getDeclaredType().toString().replaceAll(".*[#.]", "").replace(">", ""));

		assertTrue("The declaration holds no action: " + declaration.getAssignableAction(),
				declaration.getAssignableAction() instanceof SelectUniqueFlexoConceptInstance);
		AbstractSelectFlexoConceptInstance<?, ?> select = (AbstractSelectFlexoConceptInstance<?, ?>) declaration.getAssignableAction();
		assertEquals("Item", select.getFlexoConceptType().getName());
		assertEquals("The condition of the select was lost", 1, select.getConditions().size());

		assertDeclarationNodeHoldsTheActionNode(declaration);
		assertPrintsBack(declaration, "Item found = select unique Item from this where");
	}

	/** <code>List&lt;Item&gt; all = select Item from this where (...)</code> */
	@Test
	@TestOrder(4)
	public void testSelectDeclaration() {
		DeclarationAction declaration = firstDeclarationOf("countWithValue");

		assertEquals("all", declaration.getVariableName());
		assertTrue("The declaration holds no action: " + declaration.getAssignableAction(),
				declaration.getAssignableAction() instanceof SelectFlexoConceptInstance);
		AbstractSelectFlexoConceptInstance<?, ?> select = (AbstractSelectFlexoConceptInstance<?, ?>) declaration.getAssignableAction();
		assertEquals("Item", select.getFlexoConceptType().getName());
		assertEquals("The condition of the select was lost", 1, select.getConditions().size());

		assertDeclarationNodeHoldsTheActionNode(declaration);
		assertPrintsBack(declaration, "List<Item> all = select Item from this where");
	}

	private DeclarationAction firstDeclarationOf(String behaviourName) {
		FlexoBehaviour behaviour = compilationUnit.getVirtualModel().getFlexoBehaviour(behaviourName);
		assertNotNull("No behaviour " + behaviourName, behaviour);
		assertTrue(behaviour.getControlGraph() instanceof Sequence);
		assertTrue(((Sequence) behaviour.getControlGraph()).getControlGraph1() instanceof DeclarationAction);
		return (DeclarationAction) ((Sequence) behaviour.getControlGraph()).getControlGraph1();
	}

	/** The node of the declaration has the node of its action as a child: the text of the action is part of the declaration */
	private void assertDeclarationNodeHoldsTheActionNode(DeclarationAction declaration) {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		DeclarationActionNode node = (DeclarationActionNode) (P2PPNode) rootNode.getObjectNode(declaration);
		assertNotNull("No node for the declaration", node);
		boolean found = false;
		for (P2PPNode<?, ?> child : node.getChildren()) {
			if (child.getModelObject() == declaration.getAssignableAction()) {
				found = true;
			}
		}
		assertTrue("The node of the declaration does not hold the node of its action", found);
	}

	/** The text printed from the model - not the parsed one - gives the declaration back, action included */
	private void assertPrintsBack(DeclarationAction declaration, String expectedStart) {
		String printed = declaration.getNormalizedFML().replaceAll("\\s+", " ").replace("( ", "(").trim();
		assertTrue("Printed '" + printed + "' does not start with '" + expectedStart + "'", printed.startsWith(expectedStart));
	}
}
