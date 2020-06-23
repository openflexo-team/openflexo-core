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

package org.openflexo.foundation.nature;

import java.awt.image.BufferedImage;

import org.openflexo.foundation.FlexoException;
import org.openflexo.foundation.FlexoObject;
import org.openflexo.foundation.FlexoService;
import org.openflexo.pamela.annotations.ModelEntity;

/**
 * Implemented by a service that provides screenshot generation features<br>
 * A given Screenshot generation is handled by a {@link ScreenshotServiceDelegate} specific to related object type and nature<br>
 * When no {@link ScreenshotServiceDelegate} is registered, generally throw a {@link CouldNotGenerateScreenshotException}
 * 
 * @author sylvain
 * 
 */
@ModelEntity
public interface ScreenshotService extends FlexoService {

	/**
	 * Register delegate for a given {@link ScreenshotableNature}
	 * 
	 * @param delegate
	 */
	public void registerDelegate(ScreenshotServiceDelegate<?, ?> delegate);

	/**
	 * Un-register delegate
	 * 
	 * @param delegate
	 */
	public void unregisterDelegate(ScreenshotServiceDelegate<?, ?> delegate);

	/**
	 * Return applicable {@link ScreenshotServiceDelegate} for supplied nature class
	 * 
	 * @param natureClass
	 * @return
	 */
	public <T extends FlexoObject, N extends ScreenshotableNature<T>> ScreenshotServiceDelegate<T, N> getDelegate(Class<N> natureClass);

	/**
	 * Generate screenshot for given object and natureClass<br>
	 * Screenshot generation is handled by a {@link ScreenshotServiceDelegate} specific to related object type and nature<br>
	 * When no {@link ScreenshotServiceDelegate} is registered for supplied nature, throw a {@link CouldNotGenerateScreenshotException}
	 * 
	 * @param object
	 * @param natureClass
	 * @return
	 * @throws CouldNotGenerateScreenshotException
	 */
	public <T extends FlexoObject> BufferedImage generateScreenshot(T object, Class<? extends ScreenshotableNature<T>> natureClass)
			throws CouldNotGenerateScreenshotException;

	/**
	 * A delegate which handle screenshot service for a given object type and nature
	 * 
	 * @author sylvain
	 *
	 * @param <T>
	 * @param <N>
	 */
	public static interface ScreenshotServiceDelegate<T extends FlexoObject, N extends ScreenshotableNature<T>> {

		public Class<N> getNatureClass();

		public BufferedImage generateScreenshot(T object);

	}

	@SuppressWarnings("serial")
	public static class CouldNotGenerateScreenshotException extends FlexoException {

	}
}
