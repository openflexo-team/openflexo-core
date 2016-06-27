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

package org.openflexo.foundation.resource;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.model.annotations.Import;
import org.openflexo.model.annotations.Imports;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.toolbox.FileUtils;

/**
 * FlexoIOStreamDelegate is a FlexoIODelegate for which the serialization artefact is based on Stream
 * 
 * @author Vincent
 *
 * @param <I>
 */
@ModelEntity(isAbstract = true)
@Imports({ @Import(FileFlexoIODelegate.class), @Import(InJarFlexoIODelegate.class) })
public interface FlexoIOStreamDelegate<I> extends FlexoIODelegate<I> {

	public abstract InputStream getInputStream();

	public abstract void setInputStream(InputStream inputStream);

	public abstract OutputStream getOutputStream();

	public abstract void setOutputStream(OutputStream outsputStream);

	public abstract class FlexoIOStreamDelegateImpl<I> implements FlexoIOStreamDelegate<I> {

		private final Logger logger = Logger.getLogger(FlexoIOStreamDelegate.class.getPackage().getName());

		/**
		 * This constant traduces the delay accepted for the File System to effectively write a file on disk after the date it was
		 * requested. If file is written after this delay, the FlexoEditingContext will interprete it as a concurrent file modification
		 * requiring to be handled properly. In fact, this is not a big problem but resource management may be affected.
		 */
		public static final long ACCEPTABLE_FS_DELAY = 4000;

		/**
		 * This variable is only used to be reset when we have written on disk.
		 */
		private Date _diskLastModifiedDate;

		/**
		 * Flag indicating if resource is currently saving
		 */
		public boolean _isSaving = false;

		/**
		 * This is the date known by Flexo (with milliseconds precision) at which we have written on the disk.
		 */
		private Date _lastWrittenOnDisk;

		@Override
		public void notifyHasBeenWrittenOnDisk() {
			resetDiskLastModifiedDate();
			_isSaving = false;
		}

		public synchronized void _setLastWrittenOnDisk(Date aDate) {
			if (logger.isLoggable(Level.FINE) && aDate != null) {
				logger.fine("Resource " + this + "/" + hashCode() + " declared to be saved on disk on "
						+ new SimpleDateFormat("dd/MM HH:mm:ss SSS").format(aDate));
			}
			_diskLastModifiedDate = null;
			_lastWrittenOnDisk = aDate;
		}

		public final synchronized Date _getLastWrittenOnDisk() {
			return getDiskLastModifiedDate();
		}

		/**
		 * Returns the last modified date of the underlying file that Flexo has computed (or remembered) so that we get milliseconds
		 * precision
		 * 
		 * @return the last modified date known by Flexo with milliseconds precision.
		 */
		public synchronized Date getDiskLastModifiedDate() {
			if ((_diskLastModifiedDate == null || _diskLastModifiedDate.getTime() == 0 || !exists()) && !_isSaving) {
				if (getSerializationArtefact() != null && exists()) {
					_diskLastModifiedDate = FileUtils.getDiskLastModifiedDate((File) getSerializationArtefact());
				}
				else {
					// logger.warning("File "+getFile().getAbsolutePath()+" doesn't exist");
					_diskLastModifiedDate = new Date(0); // means never
					_lastWrittenOnDisk = new Date(0);
				}
				if (_lastWrittenOnDisk == null) {
					_lastWrittenOnDisk = _diskLastModifiedDate;
				}
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM HH:mm:ss SSS");
				if (_diskLastModifiedDate.getTime() > _lastWrittenOnDisk.getTime() + ACCEPTABLE_FS_DELAY) {
					if (_lastWrittenOnDisk.getTime() != 0) {
						// Here we have written on disk, and somehow the disk last modified date is still bigger than the acceptable delay
						// This can happen sometimes if it takes too long to write on disk
						if (logger.isLoggable(Level.INFO)) {
							logger.info("Resource " + this
									+ " : declared lastWrittenOnDisk date is anterior to current effective last modified date: which means that file on disk in newer than expected"
									+ "_diskLastModifiedDate[" + simpleDateFormat.format(_diskLastModifiedDate) + "]"
									+ " > lastWrittenOnDisk["
									+ simpleDateFormat.format(new Date(_lastWrittenOnDisk.getTime() + ACCEPTABLE_FS_DELAY)) + "]");
						}
					}
					// Since we are in this block (diskLastModified was null see the top 'if'), we consider that it is some kind of bug in
					// the
					// FS
					// and we update accordingly so that the resource checking thread won't think that the resource was updated by another
					// application
					_lastWrittenOnDisk = _diskLastModifiedDate;
				}
				else if (_lastWrittenOnDisk.getTime() - _diskLastModifiedDate.getTime() > ACCEPTABLE_FS_DELAY) {
					if (exists()) { // Warn it only if file exists:
						// otherwise it's normal
						logger.warning("Resource " + this
								+ " : declared lastWrittenOnDisk date is posterior to current effective last modified date (with a delay, due to FS date implementation): which means that something strange happened"
								+ "_diskLastModifiedDate[" + simpleDateFormat.format(_diskLastModifiedDate) + "]" + " < lastWrittenOnDisk["
								+ simpleDateFormat.format(_lastWrittenOnDisk) + "]");
						// We should rather go back in time and consider that the information we stored is no longer correct.
						_lastWrittenOnDisk = _diskLastModifiedDate;
					}
				}
			}
			return _lastWrittenOnDisk;
		}

		/**
		 * This method should be used parsimoniously since RM will not detect disk updates. It should only be used in few cases, eg when
		 * converting resources so that RM don't complain about updates in files, or when managing disk update accepting
		 * 
		 */
		protected synchronized void resetDiskLastModifiedDate() {
			if (getSerializationArtefact() == null || !exists()) {
				if (getSerializationArtefact() != null) {
					logger.warning("resetDiskLastModifiedDate() called for non existant file: " + toString());
				}
				else {
					logger.warning("resetDiskLastModifiedDate() called for null file on resource " + this);
				}
				_setLastWrittenOnDisk(null);
			}
			else {
				_setLastWrittenOnDisk(new Date());
			}
		}

		@Override
		public FileWritingLock willWriteOnDisk() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void hasWrittenOnDisk(FileWritingLock lock) {
			// TODO Auto-generated method stub

		}

		@SuppressWarnings("unchecked")
		@Override
		public I getSerializationArtefact() {
			return (I) getInputStream();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void setSerializationArtefact(I artefact) {
			setInputStream((InputStream) artefact);
		}

		@Override
		public String getDeletedProperty() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
