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

package org.openflexo;

import java.awt.Frame;
import java.awt.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.openflexo.application.FlexoApplication;
import org.openflexo.application.Platform;
import org.openflexo.application.PlatformHook;
import org.openflexo.application.PlatformHook.NativeOsCallback;
import org.openflexo.components.AboutDialog;
import org.openflexo.components.RequestLoginDialog;
import org.openflexo.components.SplashWindow;
import org.openflexo.components.WelcomeDialog;
import org.openflexo.foundation.task.FlexoTask.TaskStatus;
import org.openflexo.foundation.utils.OperationCancelledException;
import org.openflexo.foundation.utils.ProjectInitializerException;
import org.openflexo.foundation.utils.ProjectLoadingCancelledException;
import org.openflexo.gina.controller.FIBController.Status;
import org.openflexo.logging.FlexoLoggingFormatter;
import org.openflexo.logging.FlexoLoggingManager;
import org.openflexo.logging.FlexoLoggingManager.LoggingManagerDelegate;
import org.openflexo.module.Module;
import org.openflexo.module.ModuleLoader;
import org.openflexo.module.ModuleLoadingException;
import org.openflexo.project.LoadProjectTask;
import org.openflexo.replay.ScenarioPlayer;
import org.openflexo.replay.ScenarioRecorder;
import org.openflexo.rm.FileSystemResourceLocatorImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.ToolBox;
import org.openflexo.utils.CancelException;
import org.openflexo.utils.TooManyFailedAttemptException;
import org.openflexo.view.FlexoFrame;
import org.openflexo.view.controller.FlexoController;

/**
 * Main class of the Flexo Application Suite
 * 
 * 
 * @author sguerin
 */
public class Flexo {
	protected static final Logger logger = Logger.getLogger(Flexo.class.getPackage().getName());

	public static Platform platform;
	public static PlatformHook platformHook;

	public static boolean isDev = false;

	private static File outLogFile;

	private static File errLogFile;

	private static String fileNameToOpen;

	private static boolean demoMode = false;
	private static boolean recordMode = false;
	private static boolean playMode = false;

	public static boolean isDemoMode() {
		return demoMode;
	}

	protected static String getResourcePath() {
		if (ToolBox.isMacOS()) {

			try {
				Class<?> fileManager = Class.forName("com.apple.eio.FileManager");
				if (fileManager == null) {
					return null;
				}
				Method m = fileManager.getDeclaredMethod("getResource", new Class[] { String.class, String.class });
				String s = (String) m.invoke(null, "English.dict", "Localized");
				s = s.substring(System.getProperty("user.dir").length() + 1);
				s = s.substring(0, s.length() - "Localized/English.dict".length());
				return s;
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				if (e.getCause() instanceof FileNotFoundException) {
					return System.getProperty("user.dir");
				}
				e.printStackTrace();
			}
			return System.getProperty("user.dir");
		}
		else {
			return System.getProperty("user.dir");
		}
	}

	protected static void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				FlexoFrame activeFrame = FlexoFrame.getActiveFrame(false);
				if (activeFrame != null) {
					try {
						activeFrame.getController().getModuleLoader()
								.quit(activeFrame.getController().getProjectLoader().someProjectsAreModified());
					} catch (OperationCancelledException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	/**
	 * Launch method to start Flexo in multimodule mode. When in development mode, program args should be: Dev -nosplash For MacOS, you can
	 * also add: -Xdock:name=Flexo -Dapple.laf.useScreenMenuBar=true
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		boolean noSplash = false;

		String localesRelativePath = null;

		if (args.length > 0) {
			// ATTENTION: Argument cannot start with "-D", nor start with "-X", nor start with "-agentlib" since they are reserved keywords
			// for JVM
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-nosplash")) {
					noSplash = true;
				}
				else if (args[i].equalsIgnoreCase("-dev") || args[i].equalsIgnoreCase("DEV")) {
					isDev = true;
				}
				else if (args[i].equalsIgnoreCase("-locales")) {
					if (i < args.length - 1) {
						localesRelativePath = args[i + 1];
					}
				}
				else if (args[i].toLowerCase().contains("-demo")) {
					demoMode = true;
				}
				else if (args[i].toLowerCase().contains("-record")) {
					recordMode = true;
				}
				else if (args[i].toLowerCase().contains("-play")) {
					playMode = true;
				}
			}
		}

		// Pre-initialization of logging
		FlexoLoggingManager.initialize(-1, false, null, Level.INFO, null);

		platform = Platform.determinePlatform();
		logger.info("Platform=" + platform);

		platformHook = Platform.determinePlatform().accept(PlatformHook.CONSTRUCT_FROM_PLATFORM);
		logger.info("platformHook=" + platformHook);

		DefaultNativeOsCallback osCallback = new DefaultNativeOsCallback();

		platformHook.setNativeOsCallback(osCallback);
		// call the really early hook before we do anything else
		platformHook.preStartupHook();

		FlexoApplication.installEventQueue();

		if (recordMode) {
			FlexoApplication.eventProcessor.setPreprocessor(new ScenarioRecorder());
		}

		if (playMode) {
			new ScenarioPlayer();
		}

		platformHook.startupHook(Flexo::askUpdateJava);

		// 1. Very important to initiate first the ResourceLocator. Nothing else. See also issue 463.
		String resourcepath = getResourcePath();

		// TODO : XtoF, Check if this is necessary.... now that Resources are located in classpath

		final FileSystemResourceLocatorImpl fsrl = new FileSystemResourceLocatorImpl();
		fsrl.appendToDirectories(resourcepath);
		fsrl.appendToDirectories(System.getProperty("user.home"));
		ResourceLocator.appendDelegate(fsrl);

		SplashWindow splashWindow = null;
		if (!noSplash) {
			splashWindow = new SplashWindow(FlexoFrame.getActiveFrame());
		}
		final SplashWindow splashWindow2 = splashWindow;

		// First init localization with default location
		// FlexoLocalization.initWith(FlexoMainLocalizer.getInstance());

		final InteractiveApplicationContext applicationContext = new InteractiveApplicationContext(localesRelativePath, true, isDev,
				recordMode, playMode);

		remapStandardOuputs(isDev, applicationContext);

		osCallback.setApplicationContext(applicationContext);

		// Real initialization of logging
		initializeLoggingManager(applicationContext);

		/*final ApplicationContext applicationContext = new ApplicationContext() {
		
			@Override
			public FlexoEditor makeFlexoEditor(FlexoProject project) {
				return new InteractiveFlexoEditor(this, project);
			}
		
			@Override
			protected FlexoProjectReferenceLoader createProjectReferenceLoader() {
				return new InteractiveFlexoProjectReferenceLoader(this);
			}
		
			@Override
			protected FlexoEditor createApplicationEditor() {
				return new InteractiveFlexoEditor(this, null);
			}
		
			@Override
			protected FlexoResourceCenterService createResourceCenterService() {
				return DefaultResourceCenterService.getNewInstance(GeneralPreferences.getLocalResourceCenterDirectory());
			}
		
			@Override
			public ProjectLoadingHandler getProjectLoadingHandler(File projectDirectory) {
				if (UserType.isCustomerRelease() || UserType.isAnalystRelease()) {
					return new BasicInteractiveProjectLoadingHandler(projectDirectory);
				} else {
					return new FullInteractiveProjectLoadingHandler(projectDirectory);
				}
			}
		
			@Override
			protected TechnologyAdapterControllerService createTechnologyAdapterService(FlexoResourceCenterService resourceCenterService) {
				TechnologyAdapterControllerService returned = DefaultTechnologyAdapterControllerService.getNewInstance();
				returned.setFlexoResourceCenterService(resourceCenterService);
				returned.loadAvailableTechnologyAdapters();
				return returned;
			}
		};*/
		// Before starting the UI, we need to initialize localization
		FlexoApplication.initialize(applicationContext);
		SwingUtilities.invokeLater(() -> initFlexo(applicationContext, splashWindow2));
		initProxyManagement(applicationContext);
		logger.info("Starting on " + ToolBox.getPLATFORM() + "... JVM version is " + System.getProperty("java.version"));
		logger.info("Working directory is " + new File(".").getAbsolutePath());
		logger.info("Heap memory is about: " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() / (1024 * 1024) + "Mb");
		logger.info("Launching FLEXO Application Suite version " + FlexoCst.BUSINESS_APPLICATION_VERSION_NAME + "...");
		if (!isDev) {
			registerShutdownHook();
		}
	}

	protected static void initFlexo(InteractiveApplicationContext applicationContext, SplashWindow splashWindow) {
		if (ToolBox.isMacOS()) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("apple.awt.graphics.UseQuartz", "true");
			System.setProperty("apple.awt.rendering", "quality");
		}
		initUILAF(applicationContext.getPresentationPreferences().getLookAndFeelAsString());
		if (isDev) {
			FlexoLoggingFormatter.logDate = false;
		}

		// Disable default resource center installation
		/*if (applicationContext.defaultPackagedResourceCenterIsToBeInstalled()) {
			FIBComponent askRCDirectoryComponent = FIBLibrary.instance().retrieveFIBComponent(
					InstallDefaultPackagedResourceCenterDirectory.FIB_FILE, true);
			InstallDefaultPackagedResourceCenterDirectory installRC = new InstallDefaultPackagedResourceCenterDirectory();
			FIBDialog<InstallDefaultPackagedResourceCenterDirectory> dialog = FIBDialog.instanciateAndShowDialog(askRCDirectoryComponent,
					installRC, null, true, FlexoLocalization.getMainLocalizer());
			if (dialog.getStatus() == Status.VALIDATED) {
				installRC.installDefaultPackagedResourceCenter(applicationContext.getResourceCenterService());
			}
		}*/

		if (fileNameToOpen == null) {
			showWelcomeDialog(applicationContext, splashWindow);
		}
		else {
			try {
				File projectDirectory = new File(fileNameToOpen);
				if (splashWindow != null) {
					splashWindow.setVisible(false);
					splashWindow.dispose();
					splashWindow = null;
				}
				Module<?> module = applicationContext.getModuleLoader()
						.getModuleNamed(applicationContext.getGeneralPreferences().getFavoriteModuleName());
				if (module == null) {
					if (applicationContext.getModuleLoader().getKnownModules().size() > 0) {
						module = applicationContext.getModuleLoader().getKnownModules().iterator().next();
					}
				}
				applicationContext.getModuleLoader().switchToModule(module);

				LoadProjectTask loadProject = applicationContext.getProjectLoader().makeLoadProjectTask(projectDirectory);
				applicationContext.getTaskManager().waitTask(loadProject);
				if (loadProject.getTaskStatus() == TaskStatus.EXCEPTION_THROWN) {
					if (loadProject.getThrownException() instanceof ProjectLoadingCancelledException) {
						// project need a conversion, but user cancelled the conversion.
						showWelcomeDialog(applicationContext, null);
					}
					else if (loadProject.getThrownException() instanceof ProjectInitializerException) {
						loadProject.getThrownException().printStackTrace();
						FlexoController.notify(applicationContext.getLocalizationService().getFlexoLocalizer()
								.localizedForKey("could_not_open_project_located_at") + projectDirectory.getAbsolutePath());
						showWelcomeDialog(applicationContext, null);
					}
				}

			} catch (ModuleLoadingException e) {
				e.printStackTrace();
				FlexoController
						.notify(applicationContext.getLocalizationService().getFlexoLocalizer().localizedForKey("could_not_load_module")
								+ " " + e.getModule());
				showWelcomeDialog(applicationContext, null);
			}
		}
	}

	public static void showWelcomeDialog(final ApplicationContext applicationContext, final SplashWindow splashWindow) {
		WelcomeDialog welcomeDialog = new WelcomeDialog(applicationContext);
		welcomeDialog.pack();
		welcomeDialog.center();
		if (splashWindow != null) {
			splashWindow.setVisible(false);
			splashWindow.dispose();
		}
		welcomeDialog.setVisible(true);
		welcomeDialog.toFront();
	}

	public static void initUILAF(String value) {
		if (UIManager.getLookAndFeel().getClass().getName().equals(value)) {
			return;
		}
		try {
			UIManager.setLookAndFeel(value);
			for (Frame frame : Frame.getFrames()) {
				for (Window window : frame.getOwnedWindows()) {
					SwingUtilities.updateComponentTreeUI(window);
				}
				SwingUtilities.updateComponentTreeUI(frame);
			}
			// prints the stacktrace but don't let Exception bothered initialization of L&F as it does not actually matter
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	protected static void initProxyManagement(final ApplicationContext applicationContext) {
		applicationContext.getAdvancedPrefs().applyProxySettings();
		final ProxySelector defaultSelector = ProxySelector.getDefault();
		ProxySelector.setDefault(new ProxySelector() {
			@Override
			public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
				System.err.println("Failed " + uri + " sa " + sa);
				defaultSelector.connectFailed(uri, sa, ioe);
			}

			@Override
			public List<Proxy> select(URI uri) {
				List<Proxy> proxies = new ArrayList<Proxy>(defaultSelector.select(uri));
				if (!proxies.contains(Proxy.NO_PROXY)) {
					proxies.add(Proxy.NO_PROXY);
				}
				return proxies;
			}
		});
		Authenticator.setDefault(new Authenticator() {
			private URL previous;
			int count = 0;

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				if (getRequestingHost().equals(applicationContext.getAdvancedPrefs().getProxyHost())
						&& applicationContext.getAdvancedPrefs().getProxyPort().equals(getRequestingPort())
						|| getRequestingHost().equals(applicationContext.getAdvancedPrefs().getSProxyHost())
								&& applicationContext.getAdvancedPrefs().getSProxyPort().equals(getRequestingPort())) {
					try {
						if (previous == getRequestingURL()) {
							count++;
						}
						if (previous != getRequestingURL() && applicationContext.getAdvancedPrefs().getProxyLogin() != null
								&& applicationContext.getAdvancedPrefs().getProxyPassword() != null) {
							count = 0;
						}
						else {
							if (count < 3) {
								RequestLoginDialog dialog = new RequestLoginDialog(applicationContext);
								dialog.setVisible(true);
								if (dialog.getStatus() == Status.VALIDATED) {
									applicationContext.getAdvancedPrefs().setProxyLogin(dialog.getData().login);
									applicationContext.getAdvancedPrefs().setProxyPassword(dialog.getData().password);
									// AdvancedPrefs.save();
								}
								else {
									throw new CancelException();
								}
							}
							else {
								if (logger.isLoggable(Level.WARNING)) {
									logger.warning("Too many attempts (3) failed. Throwing exception to prevent locking.");
								}
								throw new TooManyFailedAttemptException();
							}
						}
						return new PasswordAuthentication(applicationContext.getAdvancedPrefs().getProxyLogin(),
								applicationContext.getAdvancedPrefs().getProxyPassword().toCharArray());
					} finally {
						previous = getRequestingURL();
					}
				}
				else {
					return null;
				}
			}
		});
	}

	/**
	 *
	 */
	public static void remapStandardOuputs(boolean outputToConsole, final ApplicationContext applicationContext) {
		try {
			// First let's see if we will be able to write into the log directory
			File outputDir = applicationContext.getPreferencesService().getLoggingPreferences().getLogDirectory();

			logger.info("Mapping standard and errors outputs to " + outputDir);

			if (!outputDir.exists()) {
				outputDir.mkdirs();
			}
			if (outputDir.isFile()) {
				if (logger.isLoggable(Level.SEVERE)) {
					logger.severe("Can not write to " + outputDir.getAbsolutePath() + " because it is already a file.");
				}
			}
			if (!outputDir.canWrite()) {
				if (logger.isLoggable(Level.SEVERE)) {
					logger.severe("Can not write to " + outputDir.getAbsolutePath() + " because access is denied by the file system");
				}
			}
			String outString = outputDir.getAbsolutePath() + "/Openflexo.out";
			String errString = outputDir.getAbsolutePath() + "/Openflexo.err";

			outLogFile = getOutputFile(outString);
			if (outLogFile != null) {
				PrintStream ps1 = new PrintStream(outLogFile);
				if (outputToConsole) {
					System.setOut(new PrintStream(new DoublePrintStream(ps1, System.out)));
				}
				else {
					System.setOut(ps1);
				}
			}

			errLogFile = getOutputFile(errString);
			if (errLogFile != null) {
				PrintStream ps1 = new PrintStream(errLogFile);
				if (outputToConsole) {
					System.setErr(new PrintStream(new DoublePrintStream(ps1, System.err)));
				}
				else {
					System.setErr(ps1);
				}
			}
		} catch (Exception e) {
			if (logger.isLoggable(Level.SEVERE)) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	public static File getErrLogFile() {
		return errLogFile;
	}

	public static File getOutLogFile() {
		return outLogFile;
	}

	private static class DoublePrintStream extends OutputStream {

		private final PrintStream ps1;
		private final PrintStream ps2;

		public DoublePrintStream(PrintStream ps1, PrintStream ps2) {
			this.ps1 = ps1;
			this.ps2 = ps2;
		}

		@Override
		public void write(int b) throws IOException {
			ps1.print((char) b);
			ps2.print((char) b);
		}

		@Override
		public void flush() throws IOException {
			ps1.flush();
			ps2.flush();
		}
	}

	private static File getOutputFile(String outString) throws IOException {
		int attempt = 0;
		File out = null;
		while (out == null && attempt < 100) {
			// Get a file channel for the file
			File file = new File(outString + (attempt == 0 ? "" : "." + attempt) + ".log");
			File lock = new File(outString + (attempt == 0 ? "" : "." + attempt) + ".log.lck");
			if (!file.exists()) {
				try {
					boolean done = file.createNewFile();
					if (done) {
						out = file;
					}
				} catch (RuntimeException e1) {
					e1.printStackTrace();
				}
			}
			if (!file.canWrite()) {
				out = null;
				attempt++;
				continue;
			}
			if (lock.exists()) {
				lock.delete();
			}
			if (lock.exists()) {
				out = null;
				attempt++;
				continue;
			}
			else {
				try {
					lock.createNewFile();
					boolean lockAcquired = false;
					FileOutputStream fos = new FileOutputStream(lock);
					try {
						FileLock fileLock = fos.getChannel().lock();
						lockAcquired = true;
						System.out.println("locked " + fileLock);
					} catch (IOException e) {
					} finally {
						if (!lockAcquired) {
							fos.close();
						}
					}
					lock.deleteOnExit();
				} catch (RuntimeException e) {
					e.printStackTrace();
				}
			}
			out = file;
			attempt++;
		}
		return out;
	}

	public static FlexoLoggingManager initializeLoggingManager(final ApplicationContext applicationContext) {
		try {
			// FlexoProperties properties = FlexoProperties.instance();
			String loggingFileName = System.getProperty("java.util.logging.config.file");
			Resource loggingFile = null;
			if (loggingFileName == null) {
				if (isDev) {
					loggingFile = ResourceLocator.locateResource("Config/logging_INFO.properties");
				}
				else {
					loggingFile = ResourceLocator.locateResource("Config/logging_WARNING.properties");
				}
				if (loggingFile != null) {
					loggingFileName = loggingFile.getURI();
				}
				else {
					logger.severe("Unable to find default logging File");
				}
			}
			else {
				loggingFile = ResourceLocator.locateResource(loggingFileName);
			}
			if (loggingFile != null) {
				logger.info("Default logging config file " + loggingFileName);
				if (applicationContext.getLoggingPreferences().getDefaultLoggingLevel() == null) {
					applicationContext.getLoggingPreferences().setDefaultLoggingLevel(Level.INFO);
				}
				return FlexoLoggingManager.initialize(applicationContext.getLoggingPreferences().getMaxLogCount(),
						applicationContext.getLoggingPreferences().getIsLoggingTrace(),
						applicationContext.getLoggingPreferences().getCustomLoggingFile() != null
								? applicationContext.getLoggingPreferences().getCustomLoggingFile()
								: loggingFile,
						applicationContext.getLoggingPreferences().getDefaultLoggingLevel(), new LoggingManagerDelegate() {
							@Override
							public void setMaxLogCount(Integer maxLogCount) {
								applicationContext.getLoggingPreferences().setMaxLogCount(maxLogCount);
							}

							@Override
							public void setKeepLogTrace(boolean logTrace) {
								applicationContext.getLoggingPreferences().setIsLoggingTrace(logTrace);
							}

							@Override
							public void setDefaultLoggingLevel(Level lev) {
								String fileName = "SEVERE";
								if (lev == Level.SEVERE) {
									fileName = "SEVERE";
								}
								if (lev == Level.WARNING) {
									fileName = "WARNING";
								}
								if (lev == Level.INFO) {
									fileName = "INFO";
								}
								if (lev == Level.FINE) {
									fileName = "FINE";
								}
								if (lev == Level.FINER) {
									fileName = "FINER";
								}
								if (lev == Level.FINEST) {
									fileName = "FINEST";
								}
								Resource loggingFile = ResourceLocator.locateResource("Config/logging_" + fileName + ".properties");
								reloadLoggingFile(loggingFile);
								applicationContext.getLoggingPreferences().setLoggingFileName(null);
							}

							@Override
							public void setConfigurationFileLocation(Resource configurationFile) {
								reloadLoggingFile(configurationFile);
								applicationContext.getLoggingPreferences().setLoggingFileName(configurationFile.getRelativePath());
							}
						});
			}
			else {
				logger.severe("cannot read logging configuration file : " + System.getProperty("java.util.logging.config.file"));
				return null;

			}
		} catch (SecurityException e) {
			logger.severe("cannot read logging configuration file : " + System.getProperty("java.util.logging.config.file")
					+ "\nIt seems the file has read access protection.");
			e.printStackTrace();
			return null;
		}
	}

	private static boolean reloadLoggingFile(Resource rsc) {
		logger.info("reloadLoggingFile with " + rsc.getURI());
		try {
			LogManager.getLogManager().readConfiguration(rsc.openInputStream());
			;
		} catch (SecurityException e) {
			logger.warning("The specified logging configuration file can't be read (not enough privileges).");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			logger.warning("The specified logging configuration file cannot be read.");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static void setFileNameToOpen(String filename) {
		Flexo.fileNameToOpen = filename;
	}

	private static class DefaultNativeOsCallback implements NativeOsCallback {

		private ApplicationContext applicationContext;

		@Override
		public void openFiles(List<File> files) {
			logger.info("Not implemented: open " + files);
		}

		@Override
		public boolean handleQuitRequest() {
			try {
				getModuleLoader().quit(true);
				return true;
			} catch (OperationCancelledException e) {
				return false;
			}
		}

		@Override
		public void handleAbout() {
			new AboutDialog();
		}

		@Override
		public void handlePreferences() {
			getApplicationContext().getPreferencesService().showPreferences();
		}

		public ApplicationContext getApplicationContext() {
			return applicationContext;
		}

		public void setApplicationContext(ApplicationContext applicationContext) {
			this.applicationContext = applicationContext;
		}

		public ModuleLoader getModuleLoader() {
			return applicationContext.getModuleLoader();
		}

	}

	/**
	 * Asks user to update its version of Java.
	 * 
	 * @param updVersion
	 *            target update version
	 * @param url
	 *            download URL
	 * @param major
	 *            true for a migration towards a major version of Java (8:9), false otherwise
	 * @param eolDate
	 *            the EOL/expiration date
	 * @since 12270
	 */
	public static void askUpdateJava(String updVersion, String url, String eolDate, boolean major) {
		// TODO one day if required
		/*ExtendedDialog ed = new ExtendedDialog(
		        Main.parent,
		        tr("Outdated Java version"),
		        tr("OK"), tr("Update Java"), tr("Cancel"));
		// Check if the dialog has not already been permanently hidden by user
		if (!ed.toggleEnable("askUpdateJava"+updVersion).toggleCheckState()) {
		    ed.setButtonIcons("ok", "java", "cancel").setCancelButton(3);
		    ed.setMinimumSize(new Dimension(480, 300));
		    ed.setIcon(JOptionPane.WARNING_MESSAGE);
		    StringBuilder content = new StringBuilder(tr("You are running version {0} of Java.",
		            "<b>"+System.getProperty("java.version")+"</b>")).append("<br><br>");
		    if ("Sun Microsystems Inc.".equals(System.getProperty("java.vendor")) && !platform.isOpenJDK()) {
		        content.append("<b>").append(tr("This version is no longer supported by {0} since {1} and is not recommended for use.",
		                "Oracle", eolDate)).append("</b><br><br>");
		    }
		    content.append("<b>")
		           .append(major ?
		                tr("JOSM will soon stop working with this version; we highly recommend you to update to Java {0}.", updVersion) :
		                tr("You may face critical Java bugs; we highly recommend you to update to Java {0}.", updVersion))
		           .append("</b><br><br>")
		           .append(tr("Would you like to update now ?"));
		    ed.setContent(content.toString());
		
		    if (ed.showDialog().getValue() == 2) {
		        try {
		            platform.openUrl(url);
		        } catch (IOException e) {
		            Logging.warn(e);
		        }
		    }
		}*/
	}

}
