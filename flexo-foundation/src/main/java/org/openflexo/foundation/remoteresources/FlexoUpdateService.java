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

package org.openflexo.foundation.remoteresources;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.jar.JarFile;

import org.openflexo.foundation.FlexoServiceImpl;
import org.openflexo.foundation.resource.JarResourceCenter;
import org.openflexo.toolbox.ClassPathUtils;
import org.openflexo.toolbox.FileUtils;

/**
 * The purpose of this service is to manage updates in Openflexo distribution.
 * It adds/removes jars from classloader, and notify implied other flexo services
 * @author Vincent
 *
 */
public class FlexoUpdateService extends FlexoServiceImpl {
	
	private final String USER_PATH = "userResources";
	
	/**
	 * Local paths where are stored new Jars
	 */
	private List<String> localPaths;
	
	/**
	 * The ClassLoader used
	 */
	private URLClassLoader loader;
	
	/**
	 * Add URL method 
	 */
	private Method addURL;
	
	/**
	 * Remove URL method
	 */
	private Method removeURL;
	
	public FlexoUpdateService(){
		super();

	}
	
	/**
	 * Add a set of jars in the class loader
	 * @param urls
	 */
	public void addJarsToClassLoader(URL[] urls){
		for(int i=0;i<urls.length;i++){
			try {
				addURL.invoke(loader, new Object[]{urls[i]});
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Remove a set of jar from the classLoader
	 * @param urls
	 */
	public void removeJarsFromClassLoader(URL[] urls){
		// TODO
	}
	
	/**
	 * Update the current classloader according to new add flexo bundles
	 * @param flexoBundle
	 */
	public void updateFromRemoteBundle(FlexoBundle flexoBundle){
		loadRemoteJars(flexoBundle.getURLs());
		updateFlexoApplication(flexoBundle);
	}
	
	/**
	 * Update flexo application according to new added bundle
	 * @param flexoBundle
	 */
	private void updateFlexoApplication(FlexoBundle flexoBundle){
		if(flexoBundle.getBundleType().equals(FlexoBundle.FlexoBundleType.RESOURCE_CENTER)){
			try {
				for(URL url : flexoBundle.getURLs()){
					String name = url.getFile().substring(url.getFile().lastIndexOf('/') + 1);
					JarFile file = new JarFile(localPaths.get(0) + File.separator+ name);
					JarResourceCenter.addJarFileInResourceCenter(file,getServiceManager().getResourceCenterService());
				}	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(flexoBundle.getBundleType().equals(FlexoBundle.FlexoBundleType.TECHNOLOGY_ADAPTER)){
			loadTechnologyAdapters(loader);
		}
	}
	
	public void loadRemoteJars(List<URL> remoteUrls){
		// Download the files
		ClassPathUtils.downloadJars(remoteUrls, localPaths.get(0));
		// Find the jars in the fileSystem
		URL[] urls = ClassPathUtils.findJarsFromDirectory(new File(localPaths.get(0)));
		// Add jars to classloader
		addJarsToClassLoader(urls);
	}
	
	public URLClassLoader getLoader(){
		return loader;
	}
	
	private void loadTechnologyAdapters(URLClassLoader urlClassLoader){
		getServiceManager().getTechnologyAdapterService().initialize();
	}
	
	/**
	 * Load a service of a certain Class
	 * @param klass
	 * @return
	 */
	public ServiceLoader<?> load(Class<?> klass){
		return ServiceLoader.load(klass, getLoader());
	}
	
	@Override
	public void initialize(){
		// retrieve the class loader
		loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		try {
			addURL = URLClassLoader.class.getDeclaredMethod("addURL", new Class<?>[]{URL.class});
			addURL.setAccessible(true);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File userResourceDirectory = new File(FileUtils.getApplicationDataDirectory(), USER_PATH);
		if(!userResourceDirectory.exists()) {
			// create all directory levels as required (may be more than one)
			if (!userResourceDirectory.mkdirs()) {
				// TODO: report this
				// could not create the directories, could be a permission problem
				// exit now because obviously there would be no jar in this non-existent directory
				return;
			}
		}
		URL[] urls = ClassPathUtils.findJarsFromDirectory(new File(userResourceDirectory.getAbsolutePath()));
		addJarsToClassLoader(urls);
	}
}
