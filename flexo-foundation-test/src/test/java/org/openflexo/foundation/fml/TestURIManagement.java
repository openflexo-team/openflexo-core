/**
 * 
 * Copyright (c) 2014-2015, Openflexo
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

package org.openflexo.foundation.fml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.connie.type.PrimitiveType;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.fml.action.CreateFlexoBehaviour;
import org.openflexo.foundation.fml.action.CreateFlexoConcept;
import org.openflexo.foundation.fml.action.CreatePrimitiveRole;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rm.VirtualModelResourceFactory;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.resource.FileSystemBasedResourceCenter;
import org.openflexo.foundation.resource.ResourceLoadingCancelledException;
import org.openflexo.foundation.resource.SaveResourceException;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.toolbox.FileUtils;

/**
 * This unit test is intented to test URI management
 * 
 * @author sylvain
 * 
 */
@RunWith(OrderedRunner.class)
public class TestURIManagement extends OpenflexoTestCase {

	public static final String VIEWPOINT_NAME = "TopLevelVM";
	public static final String VIEWPOINT_URI = "http://openflexo.org/test/TestResourceCenter/TopLevelVM.fml";
	public static final String VIRTUAL_MODEL_NAME = "VMLevel1";
	public static final String VIRTUAL_MODEL_NAME_2 = "VMLevel2";

	static VirtualModel topLevelVM;
	static VirtualModelResource topLevelVMResource;
	static VirtualModel vmLevel1;
	static VirtualModelResource vmLevel1Resource;
	static VirtualModel vmLevel2;
	static VirtualModelResource vmLevel2Resource;

	private static DirectoryResourceCenter resourceCenter;
	private static FlexoEditor editor;

	private FlexoConcept makeConcept(String name, VirtualModel vm) {
		CreateFlexoConcept createConceptAction = CreateFlexoConcept.actionType.makeNewAction(vm, null, editor);
		createConceptAction.setNewFlexoConceptName(name);
		createConceptAction.doAction();
		return createConceptAction.getNewFlexoConcept();
	}

	private FlexoConcept makeEmbeddedConcept(String name, FlexoConcept containerConcept) {
		CreateFlexoConcept createConceptAction = CreateFlexoConcept.actionType.makeNewAction(containerConcept, null, editor);
		createConceptAction.setContainerFlexoConcept(containerConcept);
		createConceptAction.setNewFlexoConceptName(name);
		createConceptAction.doAction();
		return createConceptAction.getNewFlexoConcept();
	}

	private PrimitiveRole<String> makePrimitiveRole(String name, FlexoConcept concept) {
		CreatePrimitiveRole createPR = CreatePrimitiveRole.actionType.makeNewAction(concept, null, editor);
		createPR.setRoleName(name);
		createPR.setPrimitiveType(PrimitiveType.String);
		createPR.setCardinality(PropertyCardinality.One);
		createPR.doAction();
		return (PrimitiveRole<String>) createPR.getNewFlexoProperty();
	}

	private ActionScheme makeActionScheme(String name, FlexoConcept concept) {
		CreateFlexoBehaviour createAS = CreateFlexoBehaviour.actionType.makeNewAction(concept, null, editor);
		createAS.setFlexoBehaviourName(name);
		createAS.setFlexoBehaviourClass(ActionScheme.class);
		createAS.doAction();
		return (ActionScheme) createAS.getNewFlexoBehaviour();
	}

	/**
	 * Test top-level VirtualModel
	 * 
	 * @throws ModelDefinitionException
	 * @throws IOException
	 * @throws FlexoException
	 * @throws ResourceLoadingCancelledException
	 */
	@Test
	@TestOrder(1)
	public void testTopLevelVirtualModel() throws ModelDefinitionException, IOException, ResourceLoadingCancelledException, FlexoException {
		instanciateTestServiceManager();
		resourceCenter = makeNewDirectoryResourceCenter();
		editor = new DefaultFlexoEditor(null, serviceManager);
		assertNotNull(resourceCenter);
		System.out.println("ResourceCenter= " + resourceCenter);

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		VirtualModelResourceFactory factory = fmlTechnologyAdapter.getVirtualModelResourceFactory();

		topLevelVMResource = factory.makeTopLevelVirtualModelResource(VIEWPOINT_NAME, VIEWPOINT_URI,
				fmlTechnologyAdapter.getGlobalRepository(resourceCenter).getRootFolder(), true);
		topLevelVM = topLevelVMResource.getLoadedResourceData();

		System.out.println("newViewPoint.getURI()=" + topLevelVM.getURI());
		assertSame(topLevelVM, serviceManager.getVirtualModelLibrary().getFMLObject(topLevelVM.getURI(), false));
		assertSame(topLevelVM, serviceManager.getVirtualModelLibrary().getVirtualModel(topLevelVM.getURI(), false));

		PrimitiveRole<String> pVM = makePrimitiveRole("p", topLevelVM);
		System.out.println("p1.getURI()=" + pVM.getURI());
		assertSame(pVM, serviceManager.getVirtualModelLibrary().getFMLObject(pVM.getURI(), false));
		assertSame(pVM, serviceManager.getVirtualModelLibrary().getFlexoProperty(pVM.getURI(), false));

		ActionScheme asVM = makeActionScheme("action", topLevelVM);
		System.out.println("as1.getURI()=" + asVM.getURI());
		assertSame(asVM, serviceManager.getVirtualModelLibrary().getFMLObject(asVM.getURI(), false));
		assertSame(asVM, serviceManager.getVirtualModelLibrary().getFlexoBehaviour(asVM.getURI(), false));

		FlexoConcept A1 = makeConcept("A1", topLevelVM);
		System.out.println("A1.getURI()=" + A1.getURI());
		assertSame(A1, serviceManager.getVirtualModelLibrary().getFMLObject(A1.getURI(), false));
		assertSame(A1, serviceManager.getVirtualModelLibrary().getFlexoConcept(A1.getURI(), false));

		PrimitiveRole<String> pA1 = makePrimitiveRole("p1", A1);
		System.out.println("p1.getURI()=" + pA1.getURI());
		assertSame(pA1, serviceManager.getVirtualModelLibrary().getFMLObject(pA1.getURI(), false));
		assertSame(pA1, serviceManager.getVirtualModelLibrary().getFlexoProperty(pA1.getURI(), false));

		ActionScheme asA1 = makeActionScheme("action1", A1);
		System.out.println("as1.getURI()=" + asA1.getURI());
		assertSame(asA1, serviceManager.getVirtualModelLibrary().getFMLObject(asA1.getURI(), false));
		assertSame(asA1, serviceManager.getVirtualModelLibrary().getFlexoBehaviour(asA1.getURI(), false));

		FlexoConcept B1 = makeEmbeddedConcept("B1", A1);
		System.out.println("B1.getURI()=" + B1.getURI());
		assertSame(B1, serviceManager.getVirtualModelLibrary().getFMLObject(B1.getURI(), false));
		assertSame(B1, serviceManager.getVirtualModelLibrary().getFlexoConcept(B1.getURI(), false));

		PrimitiveRole<String> pB1 = makePrimitiveRole("p1", B1);
		System.out.println("pB1.getURI()=" + pB1.getURI());
		assertSame(pB1, serviceManager.getVirtualModelLibrary().getFMLObject(pB1.getURI(), false));
		assertSame(pB1, serviceManager.getVirtualModelLibrary().getFlexoProperty(pB1.getURI(), false));

		ActionScheme asB1 = makeActionScheme("action1", B1);
		System.out.println("asB1.getURI()=" + asB1.getURI());
		assertSame(asB1, serviceManager.getVirtualModelLibrary().getFMLObject(asB1.getURI(), false));
		assertSame(asB1, serviceManager.getVirtualModelLibrary().getFlexoBehaviour(asB1.getURI(), false));

	}

	/**
	 * Test contained VirtualModel
	 * 
	 * @throws ModelDefinitionException
	 */
	@Test
	@TestOrder(2)
	public void testContainedVirtualModel() throws SaveResourceException, ModelDefinitionException {

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		VirtualModelResourceFactory factory = fmlTechnologyAdapter.getVirtualModelResourceFactory();
		vmLevel1Resource = factory.makeContainedVirtualModelResource(VIRTUAL_MODEL_NAME, topLevelVM.getVirtualModelResource(), true);
		vmLevel1 = vmLevel1Resource.getLoadedResourceData();

		System.out.println("vmLevel1.getURI()=" + vmLevel1.getURI());
		assertSame(vmLevel1, serviceManager.getVirtualModelLibrary().getFMLObject(vmLevel1.getURI(), false));

		PrimitiveRole<String> pVM1 = makePrimitiveRole("p", vmLevel1);
		System.out.println("pVM1.getURI()=" + pVM1.getURI());
		assertSame(pVM1, serviceManager.getVirtualModelLibrary().getFMLObject(pVM1.getURI(), false));
		assertSame(pVM1, serviceManager.getVirtualModelLibrary().getFlexoProperty(pVM1.getURI(), false));

		ActionScheme asVM1 = makeActionScheme("action", vmLevel1);
		System.out.println("asVM1.getURI()=" + asVM1.getURI());
		assertSame(asVM1, serviceManager.getVirtualModelLibrary().getFMLObject(asVM1.getURI(), false));
		assertSame(asVM1, serviceManager.getVirtualModelLibrary().getFlexoBehaviour(asVM1.getURI(), false));

		FlexoConcept A2 = makeConcept("A2", vmLevel1);
		System.out.println("A2.getURI()=" + A2.getURI());
		assertSame(A2, serviceManager.getVirtualModelLibrary().getFMLObject(A2.getURI(), false));
		assertSame(A2, serviceManager.getVirtualModelLibrary().getFlexoConcept(A2.getURI(), false));

		PrimitiveRole<String> pA2 = makePrimitiveRole("p2", A2);
		System.out.println("p2.getURI()=" + pA2.getURI());
		assertSame(pA2, serviceManager.getVirtualModelLibrary().getFMLObject(pA2.getURI(), false));
		assertSame(pA2, serviceManager.getVirtualModelLibrary().getFlexoProperty(pA2.getURI(), false));

		ActionScheme asA2 = makeActionScheme("action2", A2);
		System.out.println("as2.getURI()=" + asA2.getURI());
		assertSame(asA2, serviceManager.getVirtualModelLibrary().getFMLObject(asA2.getURI(), false));
		assertSame(asA2, serviceManager.getVirtualModelLibrary().getFlexoBehaviour(asA2.getURI(), false));

		FlexoConcept B2 = makeEmbeddedConcept("B2", A2);
		System.out.println("B2.getURI()=" + B2.getURI());
		assertSame(B2, serviceManager.getVirtualModelLibrary().getFMLObject(B2.getURI(), false));
		assertSame(B2, serviceManager.getVirtualModelLibrary().getFlexoConcept(B2.getURI(), false));

		PrimitiveRole<String> pB2 = makePrimitiveRole("p2", B2);
		System.out.println("pB2.getURI()=" + pB2.getURI());
		assertSame(pB2, serviceManager.getVirtualModelLibrary().getFMLObject(pB2.getURI(), false));
		assertSame(pB2, serviceManager.getVirtualModelLibrary().getFlexoProperty(pB2.getURI(), false));

		ActionScheme asB2 = makeActionScheme("action2", B2);
		System.out.println("asB2.getURI()=" + asB2.getURI());
		assertSame(asB2, serviceManager.getVirtualModelLibrary().getFMLObject(asB2.getURI(), false));
		assertSame(asB2, serviceManager.getVirtualModelLibrary().getFlexoBehaviour(asB2.getURI(), false));

	}

	/**
	 * Test contained VirtualModel level 2
	 * 
	 * @throws ModelDefinitionException
	 */
	@Test
	@TestOrder(3)
	public void testContainedVirtualModel2() throws SaveResourceException, ModelDefinitionException {

		FMLTechnologyAdapter fmlTechnologyAdapter = serviceManager.getTechnologyAdapterService()
				.getTechnologyAdapter(FMLTechnologyAdapter.class);
		VirtualModelResourceFactory factory = fmlTechnologyAdapter.getVirtualModelResourceFactory();
		vmLevel2Resource = factory.makeContainedVirtualModelResource(VIRTUAL_MODEL_NAME_2, vmLevel1.getVirtualModelResource(), true);
		vmLevel2 = vmLevel2Resource.getLoadedResourceData();

		System.out.println("vmLevel2.getURI()=" + vmLevel2.getURI());
		assertSame(vmLevel2, serviceManager.getVirtualModelLibrary().getFMLObject(vmLevel2.getURI(), false));

		PrimitiveRole<String> pVM2 = makePrimitiveRole("p", vmLevel2);
		System.out.println("pVM2.getURI()=" + pVM2.getURI());
		assertSame(pVM2, serviceManager.getVirtualModelLibrary().getFMLObject(pVM2.getURI(), false));
		assertSame(pVM2, serviceManager.getVirtualModelLibrary().getFlexoProperty(pVM2.getURI(), false));

		ActionScheme asVM2 = makeActionScheme("action", vmLevel2);
		System.out.println("asVM2.getURI()=" + asVM2.getURI());
		assertSame(asVM2, serviceManager.getVirtualModelLibrary().getFMLObject(asVM2.getURI(), false));
		assertSame(asVM2, serviceManager.getVirtualModelLibrary().getFlexoBehaviour(asVM2.getURI(), false));

		FlexoConcept A3 = makeConcept("A3", vmLevel2);
		System.out.println("A3.getURI()=" + A3.getURI());
		assertSame(A3, serviceManager.getVirtualModelLibrary().getFMLObject(A3.getURI(), false));
		assertSame(A3, serviceManager.getVirtualModelLibrary().getFlexoConcept(A3.getURI(), false));

		PrimitiveRole<String> pA3 = makePrimitiveRole("p3", A3);
		System.out.println("p3.getURI()=" + pA3.getURI());
		assertSame(pA3, serviceManager.getVirtualModelLibrary().getFMLObject(pA3.getURI(), false));
		assertSame(pA3, serviceManager.getVirtualModelLibrary().getFlexoProperty(pA3.getURI(), false));

		ActionScheme asA3 = makeActionScheme("action3", A3);
		System.out.println("as3.getURI()=" + asA3.getURI());
		assertSame(asA3, serviceManager.getVirtualModelLibrary().getFMLObject(asA3.getURI(), false));
		assertSame(asA3, serviceManager.getVirtualModelLibrary().getFlexoBehaviour(asA3.getURI(), false));

		FlexoConcept B3 = makeEmbeddedConcept("B3", A3);
		System.out.println("B3.getURI()=" + B3.getURI());
		assertSame(B3, serviceManager.getVirtualModelLibrary().getFMLObject(B3.getURI(), false));
		assertSame(B3, serviceManager.getVirtualModelLibrary().getFlexoConcept(B3.getURI(), false));

		PrimitiveRole<String> pB3 = makePrimitiveRole("p3", B3);
		System.out.println("pB3.getURI()=" + pB3.getURI());
		assertSame(pB3, serviceManager.getVirtualModelLibrary().getFMLObject(pB3.getURI(), false));
		assertSame(pB3, serviceManager.getVirtualModelLibrary().getFlexoProperty(pB3.getURI(), false));

		ActionScheme asB3 = makeActionScheme("action3", B3);
		System.out.println("asB3.getURI()=" + asB3.getURI());
		assertSame(asB3, serviceManager.getVirtualModelLibrary().getFMLObject(asB3.getURI(), false));
		assertSame(asB3, serviceManager.getVirtualModelLibrary().getFlexoBehaviour(asB3.getURI(), false));

	}

	@Test
	@TestOrder(3)
	public void testReloadViewPoint() throws IOException {

		log("testReloadViewPoint()");

		instanciateTestServiceManager();
		resourceCenter = makeNewDirectoryResourceCenter();

		File directory = ResourceLocator.retrieveResourceAsFile(topLevelVMResource.getDirectory());
		File newDirectory = new File(((FileSystemBasedResourceCenter) resourceCenter).getRootDirectory(), directory.getName());
		newDirectory.mkdirs();

		try {
			FileUtils.copyContentDirToDir(directory, newDirectory);
			// We wait here for the thread monitoring ResourceCenters to detect
			// new files
			((FileSystemBasedResourceCenter) resourceCenter).performDirectoryWatchingNow();
		} catch (IOException e) {
			e.printStackTrace();
		}

		VirtualModelResource retrievedVPResource = serviceManager.getVirtualModelLibrary().getVirtualModelResource(VIEWPOINT_URI);
		assertNotNull(retrievedVPResource);

		assertFalse(retrievedVPResource.isLoaded());

		assertNull(serviceManager.getVirtualModelLibrary().getFMLObject(topLevelVM.getURI(), false));
		assertNotNull(serviceManager.getVirtualModelLibrary().getFMLObject(topLevelVM.getURI(), true));
		assertTrue(retrievedVPResource.isLoaded());

	}

}
