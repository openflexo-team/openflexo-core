/**
 *
 * Copyright (c) 2014-2026, Openflexo
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.DefaultFlexoEditor;
import org.openflexo.foundation.FlexoEditor;
import org.openflexo.foundation.fml.action.LocalizeCompilationUnit;
import org.openflexo.foundation.fml.rm.CompilationUnitResource;
import org.openflexo.foundation.fml.rm.LocalizedDictionaryResource;
import org.openflexo.foundation.fml.rm.LocalizedDictionaryResourceFactory;
import org.openflexo.foundation.resource.DirectoryResourceCenter;
import org.openflexo.foundation.test.OpenflexoTestCase;
import org.openflexo.localization.Language;
import org.openflexo.localization.LocalizedDelegate;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;
import org.openflexo.toolbox.FileUtils;

/**
 * The localized dictionary of a compilation unit is a resource contained in its {@link CompilationUnitResource}, created on demand by
 * {@link LocalizeCompilationUnit}.
 *
 * <p>
 * Two fixtures of the test resource center, read only: <code>Cocon.fml</code>, which ships a <code>Localized/</code> directory, and
 * <code>TestContainerUI.fml</code>, which has none. Everything that writes happens in a temporary resource center.
 *
 * @author sylvain
 */
@RunWith(OrderedRunner.class)
public class TestLocalizedDictionaryResource extends OpenflexoTestCase {

	private static final String STORED_URI = "http://openflexo.org/test/TestResourceCenter/Cocon.fml";
	private static final String NOT_STORED_URI = "http://openflexo.org/test/TestResourceCenter/TestContainerUI.fml";

	private static final String NEW_VM_NAME = "TestLocalizedDictionary";
	private static final String NEW_VM_URI = "http://openflexo.org/test/TestLocalizedDictionary.fml";

	private static final String COLLECTED_KEY = "key_collected_before_localizing";
	private static final String FRENCH_VALUE = "Cle collectee";

	private static CompilationUnitResource newCompilationUnitResource;
	private static FlexoEditor editor;

	@Test
	@TestOrder(1)
	public void test1ExistingDictionaryIsAContainedResource() {

		instanciateTestServiceManager();

		CompilationUnitResource resource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(STORED_URI);
		assertNotNull(resource);

		LocalizedDictionaryResource dictionaryResource = resource.getLocalizedDictionaryResource();
		assertNotNull("Localized/ was not registered as a resource", dictionaryResource);
		assertSame(resource, dictionaryResource.getContainer());
		assertTrue(resource.getContents().contains(dictionaryResource));

		FMLCompilationUnit compilationUnit = resource.getCompilationUnit();
		assertSame(dictionaryResource.getDictionary(), compilationUnit.getLocalizedDictionary());
		assertSame(compilationUnit.getLocalizedDictionary(), compilationUnit.getVirtualModel().getLocales());
		assertEquals("Main", compilationUnit.getLocalizedDictionary().localizedForKeyAndLanguage("main", Language.ENGLISH, false));

		assertFalse("Reading a dictionary must not mark it modified", dictionaryResource.isModified());
	}

	@Test
	@TestOrder(2)
	public void test2CompilationUnitWithoutDictionaryLocalizesInMemory() {

		CompilationUnitResource resource = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(NOT_STORED_URI);
		assertNotNull(resource);
		assertNull(resource.getLocalizedDictionaryResource());

		LocalizedDelegate locales = resource.getCompilationUnit().getLocalizedDictionary();
		assertTrue(locales instanceof FMLLocalizedDelegate);
		assertNull("Held in memory, not stored", ((FMLLocalizedDelegate) locales).getResource());
		assertNotNull(locales.getParent());

		// The default policy registers a missing key in the first localizer of the chain: that must be this one, never the platform's
		String key = "key_asked_to_a_compilation_unit_without_dictionary";
		locales.localizedForKey(key);
		assertTrue("The key was not registered in memory", locales.hasKey(key, Language.ENGLISH, false));
		assertFalse("The key leaked into the platform localizer",
				serviceManager.getLocalizationService().getFlexoLocalizer().hasKey(key, Language.ENGLISH, true));

		assertNull("Localizing created a dictionary", resource.getLocalizedDictionaryResource());
		File container = ResourceLocator.retrieveResourceAsFile(resource.getDirectory());
		if (container != null) {
			// Only checkable when the test resource center is read from a directory rather than a jar
			assertFalse("Localizing wrote a dictionary", new File(container, LocalizedDictionaryResourceFactory.DIRECTORY_NAME).exists());
		}
	}

	@Test
	@TestOrder(3)
	public void test3LocalizeCreatesTheDictionary() throws Exception {

		DirectoryResourceCenter resourceCenter = makeNewDirectoryResourceCenter();
		FMLTechnologyAdapter fmlTA = serviceManager.getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		newCompilationUnitResource = fmlTA.getCompilationUnitResourceFactory().makeTopLevelCompilationUnitResource(NEW_VM_NAME, NEW_VM_URI,
				fmlTA.getGlobalRepository(resourceCenter).getRootFolder(), true);
		VirtualModel virtualModel = newCompilationUnitResource.getLoadedResourceData().getVirtualModel();
		File localized = localizedDirectoryOf(newCompilationUnitResource);

		// On demand only: neither creating the VirtualModel nor localizing through it creates a dictionary
		assertNull(newCompilationUnitResource.getLocalizedDictionaryResource());
		virtualModel.getLocales().localizedForKey(COLLECTED_KEY);
		assertFalse("A dictionary was written without being asked for", localized.exists());

		editor = new DefaultFlexoEditor(null, serviceManager);
		assertTrue(LocalizeCompilationUnit.actionType.isEnabledForSelection(virtualModel, null));
		LocalizeCompilationUnit action = LocalizeCompilationUnit.actionType.makeNewAction(virtualModel, null, editor);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		assertTrue(action.isCreated());

		LocalizedDictionaryResource dictionaryResource = action.getDictionaryResource();
		assertNotNull(dictionaryResource);
		assertSame(dictionaryResource, newCompilationUnitResource.getLocalizedDictionaryResource());
		assertSame(newCompilationUnitResource, dictionaryResource.getContainer());

		LocalizedDelegate dictionary = dictionaryResource.getDictionary();
		assertSame("The compilation unit must now answer its dictionary", dictionary, virtualModel.getLocales());
		assertTrue("The key collected in memory was lost", dictionary.hasKey(COLLECTED_KEY, Language.ENGLISH, false));

		// Creating is the explicit purpose of the action: the dictionary is written, and left unmodified
		for (Language language : Language.availableValues()) {
			assertTrue(new File(localized, language.getName() + ".dict").exists());
		}
		assertTrue(read(localized, Language.ENGLISH).containsKey(COLLECTED_KEY));
		assertFalse(dictionaryResource.isModified());
	}

	@Test
	@TestOrder(4)
	public void test4ChangeIsWrittenOnSaveOnly() throws Exception {

		LocalizedDictionaryResource dictionaryResource = newCompilationUnitResource.getLocalizedDictionaryResource();
		File localized = localizedDirectoryOf(newCompilationUnitResource);

		dictionaryResource.getDictionary().registerNewEntry(COLLECTED_KEY, Language.FRENCH, FRENCH_VALUE);
		assertTrue("A change must mark the dictionary modified", dictionaryResource.isModified());
		assertFalse("A change must not be written before the dictionary is saved",
				FRENCH_VALUE.equals(read(localized, Language.FRENCH).getProperty(COLLECTED_KEY)));

		dictionaryResource.save();
		assertFalse(dictionaryResource.isModified());
		assertEquals(FRENCH_VALUE, read(localized, Language.FRENCH).getProperty(COLLECTED_KEY));
	}

	@Test
	@TestOrder(5)
	public void test5LocalizeOpensAnExistingDictionary() {

		LocalizedDictionaryResource existing = newCompilationUnitResource.getLocalizedDictionaryResource();

		LocalizeCompilationUnit action = LocalizeCompilationUnit.actionType.makeNewAction(newCompilationUnitResource.getCompilationUnit(),
				null, editor);
		action.doAction();
		assertTrue(action.hasActionExecutionSucceeded());
		assertFalse(action.isCreated());
		assertSame(existing, action.getDictionaryResource());
	}

	@Test
	@TestOrder(6)
	public void test6DictionaryIsRediscoveredOnReload() throws IOException {

		File directory = ResourceLocator.retrieveResourceAsFile(newCompilationUnitResource.getDirectory());

		instanciateTestServiceManager();
		DirectoryResourceCenter resourceCenter = makeNewDirectoryResourceCenter();
		File copy = new File(resourceCenter.getRootDirectory(), directory.getName());
		copy.mkdirs();
		FileUtils.copyContentDirToDir(directory, copy);
		resourceCenter.performDirectoryWatchingNow();

		CompilationUnitResource reloaded = serviceManager.getVirtualModelLibrary().getCompilationUnitResource(NEW_VM_URI);
		assertNotNull(reloaded);

		LocalizedDictionaryResource dictionaryResource = reloaded.getLocalizedDictionaryResource();
		assertNotNull("The dictionary was not rediscovered", dictionaryResource);
		assertEquals(FRENCH_VALUE, dictionaryResource.getDictionary().localizedForKeyAndLanguage(COLLECTED_KEY, Language.FRENCH, false));
		assertSame(dictionaryResource.getDictionary(), reloaded.getCompilationUnit().getLocalizedDictionary());

		newCompilationUnitResource = reloaded;
	}

	@Test
	@TestOrder(7)
	public void test7ContainedVirtualModelFallsBackOnItsContainer() throws Exception {

		FMLTechnologyAdapter fmlTA = serviceManager.getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
		CompilationUnitResource inner = fmlTA.getCompilationUnitResourceFactory().makeContainedCompilationUnitResource("Inner",
				newCompilationUnitResource, true);

		LocalizedDictionaryResource containerDictionaryResource = newCompilationUnitResource.getLocalizedDictionaryResource();
		LocalizedDelegate containerDictionary = containerDictionaryResource.getDictionary();
		LocalizedDelegate innerLocales = inner.getCompilationUnit().getLocalizedDictionary();

		assertNull(inner.getLocalizedDictionaryResource());
		assertSame(containerDictionary, innerLocales.getParent());
		assertEquals(FRENCH_VALUE, innerLocales.localizedForKeyAndLanguage(COLLECTED_KEY, Language.FRENCH, true));

		String key = "key_asked_to_the_contained_virtual_model";
		innerLocales.localizedForKey(key);
		assertTrue(innerLocales.hasKey(key, Language.ENGLISH, false));
		assertFalse("The key leaked into the container's dictionary", containerDictionary.hasKey(key, Language.ENGLISH, false));
		assertFalse(containerDictionaryResource.isModified());
	}

	private static File localizedDirectoryOf(CompilationUnitResource resource) {
		return new File(ResourceLocator.retrieveResourceAsFile(resource.getDirectory()), LocalizedDictionaryResourceFactory.DIRECTORY_NAME);
	}

	private static Properties read(File localizedDirectory, Language language) throws IOException {
		Properties returned = new Properties();
		try (FileInputStream in = new FileInputStream(new File(localizedDirectory, language.getName() + ".dict"))) {
			returned.load(in);
		}
		return returned;
	}
}
