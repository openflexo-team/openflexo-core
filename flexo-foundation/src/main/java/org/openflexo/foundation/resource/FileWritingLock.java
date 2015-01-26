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

package org.openflexo.foundation.resource;

import java.io.File;
import java.util.Date;
import java.util.logging.Logger;

import org.openflexo.foundation.resource.FileFlexoIODelegate.FileFlexoIODelegateImpl;
import org.openflexo.toolbox.FileUtils;

public class FileWritingLock extends Thread {
	
	private final Logger logger = Logger.getLogger(FileWritingLock.class.getPackage().getName());

			private Date _previousLastModified;
			
			private final FileFlexoIODelegate delegate;
			
			public static final long ACCEPTABLE_FS_DELAY = 4000;
			
			private FileWritingLock(FileFlexoIODelegate fileDelegate) {
				super("FileWritingLock:" + fileDelegate.getFile().getAbsolutePath());
				this.delegate = fileDelegate;
				if (getFile().exists()) {
					_previousLastModified = FileUtils.getDiskLastModifiedDate(getFile());
					if (new Date().getTime() - _previousLastModified.getTime() < 1000) {
						// Last modified is this second: no way to know that file has been written,
						// Sets to null
						_previousLastModified = null;
					}
				} else {
					_previousLastModified = null;
				}
			}

			private File getFile(){
				return delegate.getFile();
			}
			
			@Override
			public void run() {
				Date startChecking = new Date();
				logger.info("Checking that file " + getFile().getAbsolutePath() + " has been successfully written");

				boolean fileHasBeenWritten = false;

				while (new Date().getTime() <= startChecking.getTime() + ACCEPTABLE_FS_DELAY && !fileHasBeenWritten) {
					if (_previousLastModified == null) {
						fileHasBeenWritten = getFile().exists();
					} else {
						Date currentLastModifiedDate = FileUtils.getDiskLastModifiedDate(getFile());
						fileHasBeenWritten = currentLastModifiedDate.after(_previousLastModified);
					}
					if (!fileHasBeenWritten) {
						logger.info("Waiting file " + getFile().getAbsolutePath() + " to be written, thread " + this);
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				if (!fileHasBeenWritten) {
					logger.warning("TIME-OUT occured while waiting file " + getFile().getAbsolutePath() + " to be written, thread " + this);
				} else {
					logger.info("File " + getFile().getAbsolutePath() + " has been written, thread " + this);
				}

				delegate.notifyHasBeenWrittenOnDisk();
			}

		}
