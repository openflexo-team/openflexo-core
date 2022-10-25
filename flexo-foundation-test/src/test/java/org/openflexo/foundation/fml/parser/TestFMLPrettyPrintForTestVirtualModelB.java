/**
 * 
 * Copyright (c) 2014, Openflexo
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

package org.openflexo.foundation.fml.parser;

import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.ActionScheme;
import org.openflexo.foundation.fml.FlexoConcept;
import org.openflexo.foundation.fml.VirtualModel;
import org.openflexo.foundation.fml.VirtualModelLibrary;
import org.openflexo.foundation.fml.parser.fmlnodes.FlexoBehaviourNode;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.test.parser.FMLParserTestCase;
import org.openflexo.p2pp.P2PPNode;
import org.openflexo.p2pp.RawSource;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Test FlexoConceptInstance embedding features
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestFMLPrettyPrintForTestVirtualModelB extends FMLParserTestCase {

	private static VirtualModel rootVM;
	private static VirtualModel vm1;
	private static FlexoConcept conceptA;
	private static FlexoConcept conceptB;
	private static FlexoConcept conceptC;

	private P2PPNode<?, ?> rootNode;

	/**
	 * Retrieve the ViewPoint
	 * 
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 * @throws FileNotFoundException
	 */
	@Test
	@TestOrder(1)
	public void testLoadViewPoint() throws FileNotFoundException, ResourceLoadingCancelledException, FlexoException {
		instanciateTestServiceManager();
		VirtualModelLibrary vpLib = serviceManager.getVirtualModelLibrary();
		assertNotNull(vpLib);
		rootVM = vpLib.getVirtualModel("http://openflexo.org/test/TestResourceCenter/TestVirtualModelB.fml");
		assertNotNull(rootVM);
		assertNotNull(vm1 = rootVM.getVirtualModelNamed("MyVM1"));
		assertNotNull(conceptA = vm1.getFlexoConcept("ConceptA"));
		assertNotNull(conceptB = vm1.getFlexoConcept("ConceptB"));
		assertNotNull(conceptC = vm1.getFlexoConcept("ConceptC"));

		assertVirtualModelIsValid(rootVM);
		assertVirtualModelIsValid(vm1);

		ActionScheme actionScheme = vm1.getActionSchemes().get(0);

		// assertNotNull(rootNode = (FMLCompilationUnitNode) vm1.getCompilationUnit().getPrettyPrintDelegate());
		assertNotNull(rootNode = (FlexoBehaviourNode) actionScheme.getPrettyPrintDelegate());

		RawSource rawSource = rootNode.getRawSource();
		System.out.println(rawSource.debug());

		debug(rootNode, 0);

		System.out.println("-----------------------------> ");

		System.out.println("FML=" + actionScheme.getFMLPrettyPrint());

	}

}
