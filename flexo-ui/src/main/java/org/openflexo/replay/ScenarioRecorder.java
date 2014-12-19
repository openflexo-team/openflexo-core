/*
 * (c) Copyright 2010-2011 AgileBirds
 * (c) Copyright 2013-2014 Openflexo
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
package org.openflexo.replay;

import java.awt.AWTEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import org.openflexo.application.FlexoApplication.EventPreprocessor;

public class ScenarioRecorder implements EventPreprocessor {
	@Override
	public void preprocessEvent(AWTEvent e) {
		if (e instanceof InputEvent) {
			boolean ignore = false;
			if (e instanceof MouseEvent) {
				if (((MouseEvent) e).paramString().startsWith("MOUSE_MOVED")) {
					ignore = true;
				}
			}
			if (!ignore) {
				System.out.println(">>>>>>>> " + e.getClass().getSimpleName() + " " + e);
			}

		}
	}
}
