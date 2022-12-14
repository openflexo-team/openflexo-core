/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2012-2012, AgileBirds
 * 
 * This file is part of Flexo-ui, a component of the software infrastructure 
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

package org.openflexo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jdom2.JDOMException;
import org.junit.Assert;
import org.junit.Test;
import org.openflexo.pamela.exceptions.InvalidDataException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.view.controller.FlexoServerAddressBook;
import org.openflexo.view.controller.FlexoServerInstance;

public class TestFlexoServerAddressBook {

    private static final String WS_URL  = "http://www.mytest.openflexo.org/ws/coucou";
    private static final String URL     = "http://www.mytest.openflexo.org";
    private static final String NAME    = "My super name";
    private static final String MY_ID   = "MyID";
    private static final String COUCOU2 = "Coucou2";
    private static final String COUCOU  = "Coucou";

    @Test
    public void testAddressBookModel() {
        PamelaModelFactory factory = null;
        try {
            factory = new PamelaModelFactory(FlexoServerAddressBook.class);
        } catch (ModelDefinitionException e) {
            e.printStackTrace();
            Assert.fail("Model definition exception: " + e.getMessage());
        }
        FlexoServerAddressBook book = factory.newInstance(FlexoServerAddressBook.class);
        FlexoServerInstance instance = factory.newInstance(FlexoServerInstance.class);
        instance.addToUserTypes(COUCOU);
        instance.addToUserTypes(COUCOU2);
        instance.setID(MY_ID);
        instance.setName(NAME);
        instance.setURL(URL);
        instance.setWSURL(WS_URL);
        book.addToInstances(instance);
        Assert.assertNotNull(book.getInstanceWithID(MY_ID));
        Assert.assertTrue(instance == book.getInstanceWithID(MY_ID));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            factory.serialize(book, baos);
        } catch (Exception e) {
            // Not sure this can happen with a BAOS
            e.printStackTrace();
            Assert.fail("Serialization failed: " + e.getMessage());
        }
        try {
            book = (FlexoServerAddressBook) factory.deserialize(new ByteArrayInputStream(baos.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("Serialization failed (IO): " + e.getMessage());
        } catch (JDOMException e) {
            e.printStackTrace();
            Assert.fail("Serialization failed (JDOM): " + e.getMessage());
        } catch (InvalidDataException e) {
            e.printStackTrace();
            Assert.fail("Serialization failed (invalid XML): " + e.getMessage());
        } catch (ModelDefinitionException e) {
            e.printStackTrace();
            Assert.fail("Serialization failed (ModelDefinition): " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Serialization failed (Unknown): " + e.getMessage());
        }
        Assert.assertNotNull(book);
        Assert.assertEquals(1, book.getInstances().size());
        FlexoServerInstance instanceWithID = book.getInstanceWithID(MY_ID);
        Assert.assertNotNull(instanceWithID);
        Assert.assertEquals(2, instanceWithID.getUserTypes().size());
        Assert.assertEquals(COUCOU, instanceWithID.getUserTypes().get(0));
        Assert.assertEquals(COUCOU2, instanceWithID.getUserTypes().get(1));
        Assert.assertEquals(WS_URL, instanceWithID.getWSURL());
        Assert.assertEquals(URL, instanceWithID.getURL());
        Assert.assertEquals(MY_ID, instanceWithID.getID());
        Assert.assertEquals(NAME, instanceWithID.getName());
    }
}
