/**
 * 
 * Copyright (c) 2014, Openflexo
 * 
 * This file is part of Cartoeditor, a component of the software infrastructure 
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openflexo.foundation.fml.parser.RawSource.RawSourcePosition;
import org.openflexo.test.OrderedRunner;
import org.openflexo.test.TestOrder;

/**
 * Some tests on raw source management
 * 
 * @author sylvain
 *
 */
@RunWith(OrderedRunner.class)
public class TestRawSource {

	private static RawSource rawSource;

	@Test
	@TestOrder(1)
	public void initRawSource() throws IOException {

		String initialString = "ABCD\nEFG\nHI\n\nJKL";
		InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
		rawSource = new RawSource(targetStream);
		assertEquals(5, rawSource.rows.size());
		System.out.println(rawSource.debug());
	}

	@Test
	@TestOrder(2)
	public void testIncrementDecrement() throws IOException {
		RawSourcePosition topLeft = rawSource.makePositionBeforeChar(1, 1);
		RawSourcePosition bottomRight = rawSource.makePositionAfterChar(5, 3);
		assertFalse(topLeft.canDecrement());
		assertTrue(topLeft.canIncrement());
		assertFalse(bottomRight.canIncrement());
		assertTrue(bottomRight.canDecrement());
		RawSourcePosition p1, p2, p3, p4, p5, p6;
		p1 = topLeft.increment();
		p2 = p1.increment();
		p3 = p2.increment();
		p4 = p3.increment();
		p5 = p4.increment();
		p6 = p5.increment();
		assertEquals("(1:1)", p1.toString());
		assertEquals("(1:2)", p2.toString());
		assertEquals("(1:3)", p3.toString());
		assertEquals("(1:4)", p4.toString());
		assertEquals("(2:0)", p5.toString());
		assertEquals("(2:1)", p6.toString());
		RawSourcePosition p7, p8, p9, p10, p11, p12;
		p7 = bottomRight.decrement();
		p8 = p7.decrement();
		p9 = p8.decrement();
		p10 = p9.decrement();
		p11 = p10.decrement();
		p12 = p11.decrement();
		assertEquals("(5:3)", bottomRight.toString());
		assertEquals("(5:2)", p7.toString());
		assertEquals("(5:1)", p8.toString());
		assertEquals("(5:0)", p9.toString());
		assertEquals("(4:0)", p10.toString());
		assertEquals("(3:2)", p11.toString());
		assertEquals("(3:1)", p12.toString());
		assertEquals(p5, p6.decrement());
		assertEquals(p4, p5.decrement());
		assertEquals(p3, p4.decrement());
		assertEquals(p2, p3.decrement());
		assertEquals(p1, p2.decrement());
		assertEquals(topLeft, p1.decrement());
		assertEquals(p11, p12.increment());
		assertEquals(p10, p11.increment());
		assertEquals(p9, p10.increment());
		assertEquals(p8, p9.increment());
		assertEquals(p7, p8.increment());
		assertEquals(bottomRight, p7.increment());

	}

	@Test
	@TestOrder(3)
	public void testFragment() throws IOException {
		RawSourcePosition topLeft = rawSource.makePositionBeforeChar(1, 1);
		RawSourcePosition bottomRight = rawSource.makePositionAfterChar(5, 3);
		assertEquals("ABCD\nEFG\nHI\n\nJKL", rawSource.makeFragment(topLeft, bottomRight).getRawText());
		RawSourcePosition p1 = rawSource.makePositionAfterChar(1, 4);
		assertEquals("ABCD", rawSource.makeFragment(topLeft, p1).getRawText());
		RawSourcePosition p2 = p1.increment();
		assertEquals("ABCD\n", rawSource.makeFragment(topLeft, p2).getRawText());
		RawSourcePosition p3 = rawSource.makePositionBeforeChar(1, 2);
		RawSourcePosition p4 = rawSource.makePositionAfterChar(3, 1);
		assertEquals("BCD\nEFG\nH", rawSource.makeFragment(p3, p4).getRawText());
	}
}
