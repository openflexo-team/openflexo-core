/**
 * 
 * Copyright (c) 2013-2014, Openflexo
 * Copyright (c) 2011-2012, AgileBirds
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

package org.openflexo.application;

import java.awt.AWTEvent;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.ApplicationContext;
import org.openflexo.Flexo;
import org.openflexo.br.SendBugReportServiceTask;
import org.openflexo.foundation.FlexoObject.FlexoObjectImpl;
import org.openflexo.foundation.action.InvalidParametersException;
import org.openflexo.foundation.action.NotImplementedException;
import org.openflexo.help.FlexoHelp;
import org.openflexo.jedit.JEditTextArea;
import org.openflexo.localization.FlexoLocalization;
import org.openflexo.logging.FlexoLoggingManager;
import org.openflexo.utils.CancelException;
import org.openflexo.utils.TooManyFailedAttemptException;
import org.openflexo.view.FlexoDialog;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoController;

/**
 * Represents the current Flexo Application
 * 
 * Contains only one public static method that should be invoked before to do anything else on GUI.
 * 
 * NB: to be portable on other OS than MacOSX, dynamic instanciation of
 * 
 * <pre>
 * com.apple.eawt.Application
 * </pre>
 * 
 * must be performed here.
 * 
 * @author sguerin
 */
public class FlexoApplication {

	protected static final Logger logger = Logger.getLogger(FlexoApplication.class.getPackage().getName());

	private static boolean isInitialized = false;

	public static EventProcessor eventProcessor;

	private static byte[] mem = new byte[1024 * 1024];

	public static void installEventQueue() {
		eventProcessor = new EventProcessor();
	}

	public static void initialize(ApplicationContext applicationContext) {
		if (isInitialized) {
			return;
		}

		isInitialized = true;

		JEditTextArea.DIALOG_FACTORY = FlexoDialog.DIALOG_FACTORY;

		FlexoHelp.configure(applicationContext.getGeneralPreferences().getLanguage().getIdentifier(),
				null/*UserType.getCurrentUserType().getIdentifier()*/);
		FlexoHelp.reloadHelpSet();
		FlexoObjectImpl.setCurrentUserIdentifier(applicationContext.getGeneralPreferences().getUserIdentifier());// Loads the preferences
		// Thread myThread = new Thread(new FocusOwnerDisplayer());
		// myThread.start();
	}

	public static class EventProcessor extends java.awt.EventQueue {
		private EventPreprocessor _preprocessor = null;

		private boolean isReportingBug = false;

		private final Vector<String> exceptions = new Vector<String>();

		public EventPreprocessor getPreprocessor() {
			return _preprocessor;
		}

		public void setPreprocessor(EventPreprocessor preprocessor) {
			_preprocessor = preprocessor;
		}

		public EventProcessor() {
			Toolkit.getDefaultToolkit().getSystemEventQueue().push(this);
		}

		private synchronized boolean testAndSetIsReportingBug() {
			if (isReportingBug) {
				return true;
			}
			else {
				isReportingBug = true;
				return false;
			}
		}

		private synchronized void resetIsReportingBug() {
			isReportingBug = false;
		}

		@Override
		protected void dispatchEvent(AWTEvent e) {
			try {
				if (_preprocessor != null) {
					_preprocessor.preprocessEvent(e);
				}
				super.dispatchEvent(e);

				// if (e instanceof SunDropTargetEvent) { logger.info("dispatchEvent() "+e+" in "+e.getSource()); }

				/*
				 * if (e instanceof KeyEvent) printFocusedComponent();
				 */
			} catch (Throwable exception) {
				// logger.info("ProgressWindow.hasInstance()="+ProgressWindow.hasInstance());
				if (exception instanceof OutOfMemoryError) {
					if (mem != null) {
						mem = null;
					}
				}

				if (exception instanceof CancelException || exception.getCause() instanceof CancelException) {
					return;
				}
				if (exception instanceof TooManyFailedAttemptException || exception.getCause() instanceof TooManyFailedAttemptException) {
					FlexoController.showError(
							FlexoLocalization.getMainLocalizer().localizedForKey("too_many_failed_attempt_to_authenticate_to_proxy"));
					return;
				}

				if (!isIgnorable(exception)) {
					// all uncaught awt thread exceptions will ultimately be
					// caught here
					if (exception instanceof Exception) {
						FlexoLoggingManager.instance().unhandledException((Exception) exception);
					}
					// FlexoLoggingManager.getMainLogger().unhandledException(exception);
					if (logger.isLoggable(Level.SEVERE)) {
						logger.log(Level.SEVERE, "Unexpected exception: " + exception.getClass().getName() + ":" + exception.getMessage(),
								exception);
					}
					String message = "";
					try {
						if (exception instanceof InvalidParametersException) {
							message = "InvalidParametersException: " + exception.getMessage() + ". Edit a bug report ?";
						}
						else if (exception instanceof NotImplementedException) {
							message = FlexoLocalization.getMainLocalizer().localizedForKey("feature_not_implemented:_")
									+ exception.getMessage() + " "
									+ FlexoLocalization.getMainLocalizer().localizedForKey("would_you_like_to_send_a_report");
						}
						else {
							message = FlexoLocalization.getMainLocalizer().localizedForKey("unexpected_exception_occured") + " "
									+ FlexoLocalization.getMainLocalizer().localizedForKey("would_you_like_to_send_a_report");
						}
					} catch (RuntimeException e3) {// This catch is here in case the localization layer has crashed
						e3.printStackTrace();
						if (exception instanceof InvalidParametersException) {
							message = "InvalidParametersException: " + exception.getMessage() + ". Edit a bug report ?";
						}
						else if (exception instanceof NotImplementedException) {
							message = "Feature not implemented: " + exception.getMessage() + ". Edit a bug report ?";
						}
						else {
							message = "Unexpected exception occured: " + exception.getClass().getName() + ". Edit a bug report ?";
						}
					}
					if (exception instanceof Exception) {
						if (!testAndSetIsReportingBug()) {
							try {
								if (FlexoController.confirm(message)) {
									FlexoFrame frame = FlexoFrame.getActiveFrame(false);
									SendBugReportServiceTask sendBugReport = new SendBugReportServiceTask((Exception) exception,
											frame != null ? frame.getModule() : null,
											frame != null ? frame.getController().getProject() : null,
											frame != null ? frame.getController().getApplicationContext() : null);
									frame.getController().getApplicationContext().getTaskManager().scheduleExecution(sendBugReport);

								}
							} catch (HeadlessException e1) {
								e1.printStackTrace();
							} catch (Exception e2) {
								e2.printStackTrace();
							} finally {
								resetIsReportingBug();
							}
						}
						else {
							if (logger.isLoggable(Level.SEVERE)) {
								logger.severe("Already reporting a bug. Ignoring another exception: " + exception);
							}
						}
					}
				}
				else {
					if (logger.isLoggable(Level.INFO)) {
						logger.info("Ignoring exception: " + exception);
					}
				}
			}
		}

		// Usefull to debug across AWT event queue
		/*
		 * public void postEvent(AWTEvent e) { super.postEvent(e); if ((e instanceof ComponentEvent) && (e.getID() ==
		 * ComponentEvent.COMPONENT_SHOWN)) { logger.info("postEvent: "+e); } }
		 */

		/*
		private static void printFocusedComponent() {
			try {
				Class<?> c = Class.forName("org.openflexo.wkf.view.WKFFrame");
				Field f = c.getField("frame");
				Object o = f.get(null);
				c = o.getClass();
				Method m = c.getMethod("printFocusedComponent", new Class[] {});
				m.invoke(o, new Object[] {});
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		*/

		/**
		 * Determines if exception can be ignored.
		 */
		private boolean isIgnorable(Throwable exception) {
			if (Flexo.isDemoMode()) {
				return true;
			}
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			exception.printStackTrace(pw);
			pw.flush();
			String bug = sw.toString();
			return isIgnorable(exception, bug);
		}

		/**
		 * Determines if the message can be ignored. (Note: this code comes from Gnutella).
		 */
		private boolean isIgnorable(Throwable bug, String msg) {
			// OutOfMemory error should definitely not be ignored. First, they can give a hint on where there is a problem. Secondly,
			// if we ran out of memory, Flexo will not work anymore and bogus behaviour will appear everywhere. So definitely, no, we don't
			// ignore.
			if (bug instanceof OutOfMemoryError) {
				return false;
			}
			// We are going to store 100 exceptions (although it is not going to hold a lot)
			// and we ignore the ones with identical stacktraces
			if (!exceptions.contains(msg)) {
				exceptions.add(msg);
				if (exceptions.size() > 100) {
					exceptions.remove(100);
				}
			}
			else {
				return true;
			}

			// no bug? kinda impossible, but shouldn't report.
			if (msg == null) {
				return true;
			}

			// if the bug came from the FileChooser (Windows or Metal)
			// or the AquaDirectoryModel, ignore it.
			if (bug instanceof NullPointerException && (msg.indexOf("MetalFileChooserUI") != -1 || msg.indexOf("WindowsFileChooserUI") != -1
					|| msg.indexOf("AquaDirectoryModel") != -1)) {
				return true;
			}

			// See Bug DS-016
			if (bug instanceof ArrayIndexOutOfBoundsException && msg.indexOf("SunDisplayChanger") != -1) {
				return true;
			}

			// An other swing known bug !
			if (bug instanceof ClassCastException && msg.indexOf("apple.laf.AquaImageFactory.drawTextBorder") != -1) {
				return true;
			}

			// if we're not somewhere in the bug, ignore it.
			// no need for us to debug sun's internal errors.
			if (msg.indexOf("org.openflexo") == -1) {
				if (logger.isLoggable(Level.WARNING)) {
					logger.warning("Internal JVM Exception occured. See logs for details.");
				}
				bug.printStackTrace();
				return true;
			}
			else {
				// Same for exceptions where org.openflexo appear only as
				// org.openflexo.application.FlexoApplication$EventProcessor.dispatchEvent()
				int index = msg.indexOf("org.openflexo");
				String searchedString = "org.openflexo.application.FlexoApplication$EventProcessor.dispatchEvent";
				if (msg.substring(index, index + searchedString.length()).equals(searchedString)) {
					if (msg.indexOf("org.openflexo", index + 1) == -1) {
						// The only occurence of org.openflexo was in searchedString
						if (logger.isLoggable(Level.WARNING)) {
							logger.warning("Internal JVM Exception occured. See logs for details.");
						}
						bug.printStackTrace();
						return true;
					}
				}
			}

			return false;
		}
	}

	public interface EventPreprocessor {
		public void preprocessEvent(AWTEvent e);
	}
}
