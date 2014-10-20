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