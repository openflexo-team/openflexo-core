/*
 * (c) Copyright 2010-2011 AgileBirds
 *
 * This file is part of OpenFlexo.
 *
 * OpenFlexo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenFlexo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openflexo.foundation.fml;

import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.logging.Logger;

import org.junit.Test;
import org.openflexo.foundation.fml.rm.ViewPointResource;
import org.openflexo.foundation.fml.rm.VirtualModelResource;
import org.openflexo.foundation.fml.rt.rm.ViewResource;
import org.openflexo.foundation.fml.rt.rm.VirtualModelInstanceResource;
import org.openflexo.logging.FlexoLogger;
import org.openflexo.model.ModelContext;
import org.openflexo.model.ModelEntity;
import org.openflexo.model.exceptions.MissingImplementationException;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;

/**
 * Test PAMELA model for all resources
 * 
 */
public class ResourceModelFactoryTest {

	private static final Logger logger = FlexoLogger.getLogger(ResourceModelFactoryTest.class.getPackage().getName());

	@Test
	public void testInstantiateViewPointResourceModelFactory() {
		try {
			System.out.println("Instanciating ViewPointResource ModelFactory");

			ModelFactory factory = new ModelFactory(ViewPointResource.class);
			ModelContext modelContext = factory.getModelContext();
			for (Iterator<ModelEntity> it = modelContext.getEntities(); it.hasNext();) {
				ModelEntity e = it.next();
			}
			factory.checkMethodImplementations();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (MissingImplementationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testInstantiateVirtualModelResourceFactory() {
		try {
			System.out.println("Instanciating VirtualModelResource ModelFactory");

			ModelFactory factory = new ModelFactory(VirtualModelResource.class);
			ModelContext modelContext = factory.getModelContext();
			for (Iterator<ModelEntity> it = modelContext.getEntities(); it.hasNext();) {
				ModelEntity e = it.next();
			}
			factory.checkMethodImplementations();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (MissingImplementationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testInstantiateViewResourceModelFactory() {
		try {
			System.out.println("Instanciating ViewResource ModelFactory");

			ModelFactory factory = new ModelFactory(ViewResource.class);
			ModelContext modelContext = factory.getModelContext();
			for (Iterator<ModelEntity> it = modelContext.getEntities(); it.hasNext();) {
				ModelEntity e = it.next();
			}
			factory.checkMethodImplementations();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (MissingImplementationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testInstantiateVirtualModelInstanceResourceModelFactory() {
		try {
			System.out.println("Instanciating VirtualModelInstanceResource ModelFactory");

			ModelFactory factory = new ModelFactory(VirtualModelInstanceResource.class);
			ModelContext modelContext = factory.getModelContext();
			for (Iterator<ModelEntity> it = modelContext.getEntities(); it.hasNext();) {
				ModelEntity e = it.next();
			}
			factory.checkMethodImplementations();
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (MissingImplementationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
