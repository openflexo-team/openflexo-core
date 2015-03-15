/**
 * 
 * Copyright (c) 2014-2015, Openflexo
 * 
 * This file is part of Fml-technologyadapter-ui, a component of the software infrastructure 
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

package org.openflexo.fml.fib;

import org.junit.Test;
import org.openflexo.fib.utils.GenericFIBTestCase;
import org.openflexo.rm.FileResourceImpl;
import org.openflexo.rm.ResourceLocator;

public class TestFMLFibs extends GenericFIBTestCase {

	public static void main(String[] args) {
		System.out.println(generateFIBTestCaseClass(((FileResourceImpl) ResourceLocator.locateResource("Fib/FML")).getFile(), "Fib/FML/"));
	}

	@Test
	public void testFlexoBehaviourPanel() {
		validateFIB("Fib/FML/FlexoBehaviourPanel.fib");
	}

	@Test
	public void testFlexoConceptInspectorPanel() {
		validateFIB("Fib/FML/FlexoConceptInspectorPanel.fib");
	}

	@Test
	public void testFlexoConceptPanel() {
		validateFIB("Fib/FML/FlexoConceptPanel.fib");
	}

	@Test
	public void testLocalizedDictionaryPanel() {
		validateFIB("Fib/FML/LocalizedDictionaryPanel.fib");
	}

	@Test
	public void testStandardFlexoConceptView() {
		validateFIB("Fib/FML/StandardFlexoConceptView.fib");
	}

	@Test
	public void testViewPointLocalizedDictionaryView() {
		validateFIB("Fib/FML/ViewPointLocalizedDictionaryView.fib");
	}

	@Test
	public void testViewPointView() {
		validateFIB("Fib/FML/ViewPointView.fib");
	}

	@Test
	public void testVirtualModelStructuralPanel() {
		validateFIB("Fib/FML/VirtualModelStructuralPanel.fib");
	}

	@Test
	public void testVirtualModelView() {
		validateFIB("Fib/FML/VirtualModelView.fib");
	}

}
