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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openflexo.foundation.FlexoProject;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.fml.CompilationUnitRepository;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTTechnologyAdapter;
import org.openflexo.foundation.fml.rt.FMLRTVirtualModelInstanceRepository;
import org.openflexo.foundation.project.FlexoProjectResource;
import org.openflexo.foundation.resource.DirectoryBasedJarIODelegate.DirectoryBasedJarIODelegateImpl;
import org.openflexo.foundation.resource.InJarIODelegate.InJarIODelegateImpl;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.Implementation;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.PropertyIdentifier;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.XMLAttribute;
import org.openflexo.pamela.annotations.XMLElement;
import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.rm.ClasspathResourceLocatorImpl;
import org.openflexo.rm.InJarResourceImpl;
import org.openflexo.rm.JarResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.JarUtils;
import org.openflexo.xml.XMLRootElementInfo;
import org.openflexo.xml.XMLRootElementReader;

/**
 * A Jar resource center references a set of resources inside a Jar.
 * 
 * @author Vincent, xtof
 *
 */
@ModelEntity
@ImplementationClass(JarResourceCenter.JarResourceCenterImpl.class)
public interface JarResourceCenter extends FlexoResourceCenter<InJarResourceImpl> {

	public static JarResourceCenter instanciateNewJarResourceCenter(JarFile jarFile, FlexoResourceCenterService rcService)
			throws IOException {
		JarResourceCenterImpl.logger.info("Instanciate JarResourceCenter from " + jarFile);
		ModelFactory factory;
		try {
			factory = new ModelFactory(JarResourceCenter.class);
			JarResourceCenter jarResourceCenter = factory.newInstance(JarResourceCenter.class);
			jarResourceCenter.setFlexoResourceCenterService(rcService);
			jarResourceCenter.setJarFile(jarFile);
			jarResourceCenter.update();
			return jarResourceCenter;
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Add all the jars from the class path to resource centers
	 * 
	 * @param rcService
	 * @throws IOException
	 */
	public static void addAllJarFromClassPath(FlexoResourceCenterService rcService) throws IOException {
		for (JarFile file : getClassPathJarFiles()) {
			addJarFile(file, rcService);
		}
	}

	/**
	 * Add the first jar from the class path found with this name Example : path of the jar in the class path :
	 * c:/a/b/c/org/openflexo/myjar.jar Name : org.openflexo.myjar Return the c:/a/b/c/org/openflexo/myjar.jar
	 * 
	 * @param rcService
	 * @throws IOException
	 */
	public static JarResourceCenter addNamedJarFromClassPath(FlexoResourceCenterService rcService, String name) throws IOException {
		JarResourceCenter rc = null;
		for (JarFile file : getClassPathJarFiles()) {
			if ((file.getName().endsWith(name + ".jar")) || (name.endsWith(".jar") && file.getName().endsWith(name))) {
				rc = addJarFile(file, rcService);
				break;
			}
		}
		return rc;
	}

	public static List<JarFile> getClassPathJarFiles() {
		List<JarFile> result = new ArrayList<>();
		// Get the files in the class path
		List<File> files = new ArrayList<>();
		StringTokenizer string = new StringTokenizer(System.getProperty("java.class.path"), Character.toString(File.pathSeparatorChar));
		while (string.hasMoreTokens()) {
			files.add(new File(string.nextToken()));
		}
		for (File jar : files) {
			if (isJarFile(jar)) {
				try {
					// TODO: resource leak, here we cannot close the JarFile in the scope, is it close somewhere?
					result.add(new JarFile(jar));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public static boolean isJarFile(File jar) {
		return jar.getName().endsWith(".jar");
	}

	/**
	 * Add a resource center from a jar file
	 * 
	 * @param jarFile
	 * @param rcService
	 * @throws IOException
	 */
	public static JarResourceCenter addJarFile(JarFile jarFile, FlexoResourceCenterService rcService) throws IOException {
		JarResourceCenterImpl.logger.info("Try to create a resource center from a jar file : " + jarFile.getName());
		// JarResourceCenter rc = new JarResourceCenter(jarFile, rcService);
		JarResourceCenter rc = instanciateNewJarResourceCenter(jarFile, rcService);
		rc.setDefaultBaseURI(jarFile.getName());
		rcService.addToResourceCenters(rc);
		rcService.storeDirectoryResourceCenterLocations();
		return rc;
	}

	public JarFile getJarFile();

	public void setJarFile(JarFile jarFile);

	public JarResourceImpl getJarResourceImpl();

	public static abstract class JarResourceCenterImpl extends ResourceRepositoryImpl<FlexoResource<?>, InJarResourceImpl>
			implements JarResourceCenter {

		protected static final Logger logger = Logger.getLogger(ResourceRepositoryImpl.class.getPackage().getName());

		/**
		 * A jar file the resource center might interpret
		 */
		private JarFile jarFile;

		/**
		 * A string that is used to identify the JarRC and build uri of resources included in the RC
		 * 
		 */
		private String rcBaseUri;

		/**
		 * A JarResource is the main element of a JarResource center. It contains a set of InJarResource elements.
		 */
		private JarResourceImpl jarResourceImpl;

		private FlexoResourceCenterService rcService;

		/**
		 * Contructor based on a given JarResource
		 * 
		 * @param jarResourceImpl
		 */
		/*public JarResourceCenterImpl(JarResourceImpl jarResourceImpl, FlexoResourceCenterService rcService) {
			super(null, jarResourceImpl.getRootEntry());
			this.rcService = rcService;
			this.jarFile = jarResourceImpl.getJarfile();
			this.jarResourceImpl = jarResourceImpl;
		}*/

		/**
		 * Constructor based on a given jarFile
		 * 
		 * @param jarFile
		 */
		/*public JarResourceCenterImpl(JarFile jarFile, FlexoResourceCenterService rcService) {
			super(null, null);
			this.rcService = rcService;
			ClasspathResourceLocatorImpl locator = (ClasspathResourceLocatorImpl) ResourceLocator
					.getInstanceForLocatorClass(ClasspathResourceLocatorImpl.class);
			jarResourceImpl = (JarResourceImpl) locator.locateResource(jarFile.getName());
			if (jarResourceImpl == null) {
				try {
					jarResourceImpl = new JarResourceImpl(ResourceLocator.getInstanceForLocatorClass(ClasspathResourceLocatorImpl.class),
							jarFile);
					setBaseArtefact(jarResourceImpl.getRootEntry());
				} catch (MalformedURLException e) {
					logger.warning("Unable to create a Jar Resource Center for jar " + jarFile.getName());
				}
			}
			this.jarFile = jarFile;
			locator.getJarResourcesList().put(jarResourceImpl.getRelativePath(), jarResourceImpl);
		}*/

		/**
		 * Return {@link FlexoResourceCenterService} managing this {@link FlexoResourceCenter}
		 * 
		 * @return
		 */
		@Override
		public FlexoResourceCenterService getFlexoResourceCenterService() {
			return rcService;
		}

		/**
		 * Sets {@link FlexoResourceCenterService} managing this {@link FlexoResourceCenter}
		 * 
		 * @return
		 */
		@Override
		public void setFlexoResourceCenterService(FlexoResourceCenterService rcService) {
			this.rcService = rcService;
		}

		@Override
		public JarResourceCenter getResourceCenter() {
			return this;
		}

		@Override
		public Class<InJarResourceImpl> getSerializationArtefactClass() {
			return InJarResourceImpl.class;
		}

		@Override
		public JarResourceImpl getJarResourceImpl() {
			return jarResourceImpl;
		}

		@Override
		public String toString() {
			return super.toString() + " jar=" + (jarResourceImpl != null ? jarResourceImpl.toString() : null);
		}

		@Override
		public FlexoServiceManager getServiceManager() {
			if (getFlexoResourceCenterService() == null) {
				return super.getServiceManager();
			}
			return getFlexoResourceCenterService().getServiceManager();
		}

		/**
		 * Returns an iterator over contained InJarResources
		 */
		@SuppressWarnings("unchecked")
		@Override
		public Iterator<InJarResourceImpl> iterator() {
			return (Iterator<InJarResourceImpl>) getJarResourceImpl().getContents().iterator();
		}

		@Override
		public String getName() {
			if (jarFile != null) {
				return jarFile.getName();
			}
			return "unset";
		}

		private final Map<TechnologyAdapter<?>, HashMap<Class<? extends ResourceRepository<?, InJarResourceImpl>>, ResourceRepository<?, InJarResourceImpl>>> repositories = new HashMap<>();

		private HashMap<Class<? extends ResourceRepository<?, InJarResourceImpl>>, ResourceRepository<?, InJarResourceImpl>> getRepositoriesForAdapter(
				TechnologyAdapter<?> technologyAdapter, boolean considerEmptyRepositories) {
			if (considerEmptyRepositories) {
				technologyAdapter.ensureAllRepositoriesAreCreated(this);
			}
			HashMap<Class<? extends ResourceRepository<?, InJarResourceImpl>>, ResourceRepository<?, InJarResourceImpl>> map = repositories
					.get(technologyAdapter);
			if (map == null) {
				map = new HashMap<>();
				repositories.put(technologyAdapter, map);
			}
			return map;
		}

		@SuppressWarnings("unchecked")
		@Override
		public final <R extends ResourceRepository<?, InJarResourceImpl>> R retrieveRepository(Class<? extends R> repositoryType,
				TechnologyAdapter<?> technologyAdapter) {
			HashMap<Class<? extends ResourceRepository<?, InJarResourceImpl>>, ResourceRepository<?, InJarResourceImpl>> map = getRepositoriesForAdapter(
					technologyAdapter, false);
			return (R) map.get(repositoryType);
		}

		@Override
		public final <R extends ResourceRepository<?, InJarResourceImpl>> void registerRepository(R repository,
				Class<? extends R> repositoryType, TechnologyAdapter<?> technologyAdapter) {
			HashMap<Class<? extends ResourceRepository<?, InJarResourceImpl>>, ResourceRepository<?, InJarResourceImpl>> map = getRepositoriesForAdapter(
					technologyAdapter, false);
			if (map.get(repositoryType) == null) {
				map.put(repositoryType, repository);
			}
			else {
				logger.warning("Repository already registered: " + repositoryType + " for " + repository);
			}
		}

		@Override
		public Collection<ResourceRepository<?, InJarResourceImpl>> getRegistedRepositories(TechnologyAdapter<?> technologyAdapter,
				boolean considerEmptyRepositories) {
			return getRepositoriesForAdapter(technologyAdapter, considerEmptyRepositories).values();
		}

		/**
		 * Register global repository for this resource center<br>
		 * It is stated that the global repository contains all resources which supplied technology adapter has discovered and may
		 * interpret<br>
		 * This is the resource repository which is generally given in GUIs (such as browsers) to display the contents of a resource center
		 * for a given technology
		 * 
		 * @param repository
		 * @param technologyAdapter
		 */
		/*@Override
		public final void registerGlobalRepository(ResourceRepositoryImpl<?> repository, TechnologyAdapter technologyAdapter) {
			if (repository != null && technologyAdapter != null) {
				globalRepositories.put(technologyAdapter, repository);
			}
		}*/

		/**
		 * Return the global repository for this resource center and for supplied technology adapter<br>
		 * It is stated that the global repository contains all resources which supplied technology adapter has discovered and may
		 * interpret<br>
		 * This is the resource repository which is generally given in GUIs (such as browsers) to display the contents of a resource center
		 * for a given technology
		 * 
		 * @param technologyAdapter
		 * @return
		 */
		/*@Override
		public ResourceRepositoryImpl<?> getGlobalRepository(TechnologyAdapter technologyAdapter) {
			if (technologyAdapter != null) {
				return globalRepositories.get(technologyAdapter);
			}
			return null;
		}*/

		@Override
		public <T extends ResourceData<T>> List<FlexoResource<T>> retrieveResource(String uri, Class<T> type) {
			// TODO: provide support for class and version
			FlexoResource<T> uniqueResource = retrieveResource(uri, null, null);
			return Collections.singletonList(uniqueResource);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends ResourceData<T>> FlexoResource<T> retrieveResource(String uri, FlexoVersion version, Class<T> type) {
			// TODO: provide support for class and version
			return (FlexoResource<T>) retrieveResource(uri);
		}

		@Override
		public FlexoResource<?> retrieveResource(String uri) {
			return getResource(uri);
		}

		@Override
		public <R extends FlexoResource<?>> R getResource(InJarResourceImpl resourceArtifact, Class<R> resourceClass) {
			try {
				// searches for parent folder.
				RepositoryFolder<?, InJarResourceImpl> folder = getParentRepositoryFolder(resourceArtifact, false);

				// When not found
				// It might be a resource artefact encoded as a directory based, try to find parent folder
				if (folder == null) {
					folder = getParentRepositoryFolder(resourceArtifact.getContainer(), false);
				}

				if (folder == null) {
					return null;
				}

				for (FlexoResource<?> r : folder.getResources()) {
					if (Objects.equals(r.getIODelegate().getSerializationArtefact(), resourceArtifact)) {
						if (resourceClass.isInstance(r)) {
							return resourceClass.cast(r);
						}
						logger.warning("Found resource matching file " + resourceArtifact + " but not of desired type: " + r.getClass()
								+ " instead of " + resourceClass);
						return null;
					}
				}

				// Cannot find the resource
				return null;

			} catch (IOException e) {
				logger.log(Level.WARNING, "Error while getting parent folder for " + resourceArtifact, e);
				return null;
			}
		}

		@Override
		public void publishResource(FlexoResource<?> resource, FlexoVersion newVersion) throws Exception {
			// TODO Not yet implemented
		}

		@Override
		public void update() throws IOException {
			// TODO Not yet implemented
		}

		@Override
		public String getDefaultBaseURI() {
			return rcBaseUri;
		}

		@Override
		public void setDefaultBaseURI(String defaultBaseURI) {
			rcBaseUri = defaultBaseURI;

		}

		@Override
		public Resource getBaseArtefactAsResource() {
			return getBaseArtefact();
		}

		@Override
		public boolean isIgnorable(InJarResourceImpl artefact, TechnologyAdapter<?> technologyAdapter) {
			// Trivial implementation
			return false;
		}

		// TODO Remove this
		@Override
		public CompilationUnitRepository<InJarResourceImpl> getVirtualModelRepository() {
			if (getServiceManager() != null) {
				FMLTechnologyAdapter vmTA = getServiceManager().getTechnologyAdapterService()
						.getTechnologyAdapter(FMLTechnologyAdapter.class);
				return vmTA.getVirtualModelRepository(this);
			}
			return null;
		}

		@Override
		public FMLRTVirtualModelInstanceRepository<InJarResourceImpl> getVirtualModelInstanceRepository() {
			if (getServiceManager() != null) {
				FMLRTTechnologyAdapter vmRTTA = getServiceManager().getTechnologyAdapterService()
						.getTechnologyAdapter(FMLRTTechnologyAdapter.class);
				return vmRTTA.getVirtualModelInstanceRepository(this);
			}
			return null;
		}

		@Override
		public JarFile getJarFile() {
			return jarFile;
		}

		@Override
		public void setJarFile(JarFile jarFile) {
			ClasspathResourceLocatorImpl locator = (ClasspathResourceLocatorImpl) ResourceLocator
					.getInstanceForLocatorClass(ClasspathResourceLocatorImpl.class);
			jarResourceImpl = (JarResourceImpl) locator.locateResource(jarFile.getName());
			if (jarResourceImpl == null) {
				try {
					jarResourceImpl = new JarResourceImpl(ResourceLocator.getInstanceForLocatorClass(ClasspathResourceLocatorImpl.class),
							jarFile);
					setBaseArtefact(jarResourceImpl.getRootEntry());
				} catch (MalformedURLException e) {
					logger.warning("Unable to create a Jar Resource Center for jar " + jarFile.getName());
				}
			}
			this.jarFile = jarFile;
			locator.getJarResourcesList().put(jarResourceImpl.getRelativePath(), jarResourceImpl);
		}

		private JarResourceCenterEntry entry;

		@Override
		public ResourceCenterEntry<?> getResourceCenterEntry() {
			if (entry == null) {
				try {
					ModelFactory factory = new ModelFactory(JarResourceCenterEntry.class);
					entry = factory.newInstance(JarResourceCenterEntry.class);
					entry.setFile(new File(getJarResourceImpl().getRelativePath()));
				} catch (ModelDefinitionException e) {
					e.printStackTrace();
				}
			}
			return entry;
		}

		/**
		 * Stops the Resource Center (When needed)
		 */
		@Override
		public void stop() {
			// Nothing to do for now
		}

		/**
		 * Compute and return a default URI for supplied resource<br>
		 * If resource does not provide URI support, this might be delegated to the {@link FlexoResourceCenter} through this method
		 * 
		 * @param resource
		 * @return
		 */
		@Override
		public <R extends FlexoResource<?>> String getDefaultResourceURI(R resource) {
			String defaultBaseURI = getDefaultBaseURI();
			if (!defaultBaseURI.endsWith("/")) {
				defaultBaseURI = defaultBaseURI + "/";
			}
			String lastPath = resource.getName();
			String relativePath = "";
			if (resource.getIODelegate() != null) {
				InJarResourceImpl serializationArtefact = (InJarResourceImpl) resource.getIODelegate().getSerializationArtefact();
				if (resource.getIODelegate() instanceof DirectoryBasedJarIODelegate) {
					serializationArtefact = ((DirectoryBasedJarIODelegate) resource.getIODelegate()).getDirectory();
				}
				if (serializationArtefact != null) {
					InJarResourceImpl f = serializationArtefact.getContainer();
					while (f != null && !(f.equals(getRootFolder().getSerializationArtefact()))) {
						relativePath = f.getName() + "/" + relativePath;
						f = f.getContainer();
					}
				}
			}

			return defaultBaseURI + relativePath + lastPath;
		}

		@Override
		public String retrieveName(InJarResourceImpl serializationArtefact) {
			if (serializationArtefact != null) {
				String returned = serializationArtefact.getURL().getFile();
				if (returned.endsWith("!")) {
					returned = returned.substring(0, returned.length() - 1);
				}
				if (returned.endsWith("/")) {
					returned = returned.substring(0, returned.length() - 1);
				}
				returned = returned.substring(returned.lastIndexOf("/") + 1);
				return returned;
			}
			return getName();
		}

		@Override
		public InJarResourceImpl rename(InJarResourceImpl serializationArtefact, String newName) {
			// Not applicable
			return null;
		}

		@Override
		public InJarResourceImpl delete(InJarResourceImpl serializationArtefact) {
			// Not applicable
			return null;
		}

		/**
		 * Return serialization artefact containing supplied serialization artefact (parent directory)
		 * 
		 * @param serializationArtefact
		 * @return
		 */
		@Override
		public InJarResourceImpl getContainer(InJarResourceImpl serializationArtefact) {
			if (serializationArtefact == null) {
				return null;
			}
			Resource container = serializationArtefact.getContainer();
			if (container instanceof InJarResourceImpl) {
				return (InJarResourceImpl) container;
			}
			return null;
		}

		/**
		 * Return list of serialization actefacts contained in supplied serialization actifact<br>
		 * Return empty list if supplied serialization artefact has no contents
		 * 
		 * @param serializationArtefact
		 * @return
		 */
		@Override
		public List<InJarResourceImpl> getContents(InJarResourceImpl serializationArtefact) {
			return serializationArtefact.getContents(false);
		}

		@Override
		public boolean isDirectory(InJarResourceImpl serializationArtefact) {
			return serializationArtefact.isContainer();
		}

		@Override
		public boolean exists(InJarResourceImpl serializationArtefact) {
			return serializationArtefact != null;
		}

		@Override
		public boolean canRead(InJarResourceImpl serializationArtefact) {
			return true;
		}

		@Override
		public InJarResourceImpl createDirectory(String name, InJarResourceImpl parentDirectory) {
			// Not applicable
			return null;
		}

		/**
		 * Get container serialization artefact, with supplied name and parent serialization artefact
		 * 
		 * @param name
		 * @param parentDirectory
		 * @return
		 */
		@Override
		public InJarResourceImpl getDirectory(String name, InJarResourceImpl parentDirectory) {
			for (InJarResourceImpl r : parentDirectory.getContents(false)) {
				// System.out.println(" * " + r.getName() + " " + r);
				if (name.equals(r.getName())) {
					return r;
				}
			}
			return null;
		}

		@Override
		public InJarResourceImpl createEntry(String name, InJarResourceImpl parentDirectory) {
			// Not applicable
			return null;
		}

		@Override
		public InJarResourceImpl getEntry(String name, InJarResourceImpl parentDirectory) {
			for (InJarResourceImpl r : parentDirectory.getContents(false)) {
				// System.out.println(" * " + r.getName() + " " + r);
				if (name.equals(r.getName())) {
					return r;
				}
			}
			return null;
		}

		@Override
		public InJarIODelegate makeFlexoIODelegate(InJarResourceImpl serializationArtefact, FlexoResourceFactory<?, ?> resourceFactory) {
			return InJarIODelegateImpl.makeInJarFlexoIODelegate(serializationArtefact, resourceFactory);
		}

		@Override
		public FlexoIODelegate<InJarResourceImpl> makeDirectoryBasedFlexoIODelegate(InJarResourceImpl serializationArtefact,
				String directoryExtension, String fileExtension, FlexoResourceFactory<?, ?> resourceFactory) {

			String baseName = retrieveName(serializationArtefact).substring(0,
					retrieveName(serializationArtefact).length() - directoryExtension.length());

			InJarResourceImpl directory = getDirectory(baseName + directoryExtension, getContainer(serializationArtefact));
			InJarResourceImpl file = getEntry(baseName + fileExtension, directory);

			return makeDirectoryBasedFlexoIODelegate(directory, file, resourceFactory);
		}

		@Override
		public FlexoIODelegate<InJarResourceImpl> makeDirectoryBasedFlexoIODelegate(InJarResourceImpl directory, InJarResourceImpl file,
				FlexoResourceFactory<?, ?> resourceFactory) {
			return DirectoryBasedJarIODelegateImpl.makeDirectoryBasedFlexoIODelegate(directory, file, resourceFactory);
		}

		@Override
		public XMLRootElementInfo getXMLRootElementInfo(InJarResourceImpl serializationArtefact) {
			return getXMLRootElementInfo(serializationArtefact, false, null);
		}

		@Override
		public XMLRootElementInfo getXMLRootElementInfo(InJarResourceImpl serializationArtefact, boolean parseFirstLevelElements,
				String firstLevelElementName) {
			if (serializationArtefact == null) {
				return null;
			}
			XMLRootElementReader reader = new XMLRootElementReader(parseFirstLevelElements, firstLevelElementName);
			try {
				System.out.println("Load "+serializationArtefact+" in "+jarFile+" resource: "+getJarResourceImpl());
				return reader.readRootElement(serializationArtefact);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}

		}

		/**
		 * Return properties stored in supplied directory<br>
		 * Find the first entry whose name ends with .properties and analyze it as a {@link Properties} serialization
		 * 
		 * @return
		 * @throws IOException
		 */
		@Override
		public Properties getProperties(InJarResourceImpl directory) throws IOException {
			// System.out.println("Reading properties from JarEntry " + directory);
			Properties returned = null;
			if (isDirectory(directory)) {
				InJarResourceImpl propertiesJarEntry = null;
				for (InJarResourceImpl content : getContents(directory)) {
					if (retrieveName(content).endsWith(".properties")) {
						propertiesJarEntry = content;
						break;
					}
				}
				if (propertiesJarEntry != null) {
					returned = new Properties();
					try (InputStream is = propertiesJarEntry.openInputStream()) {
						returned.load(is);
					}
				}
			}
			// System.out.println("Return properties: " + returned);
			return returned;
		}

		@Override
		public <R extends FlexoResource<?>> RepositoryFolder<R, InJarResourceImpl> getRepositoryFolder(
				FlexoIODelegate<InJarResourceImpl> ioDelegate, ResourceRepository<R, InJarResourceImpl> resourceRepository) {

			InJarResourceImpl candidateFile = null;
			if (ioDelegate instanceof DirectoryBasedJarIODelegate) {
				candidateFile = ((DirectoryBasedJarIODelegate) ioDelegate).getDirectory();
			}
			else if (ioDelegate instanceof InJarIODelegate) {
				candidateFile = ((InJarIODelegate) ioDelegate).getInJarResource();
			}
			try {

				// System.out.println("Folder for " + ioDelegate.getSerializationArtefact() + " is "
				// + resourceRepository.getRepositoryFolder(candidateFile, true));

				return resourceRepository.getParentRepositoryFolder(candidateFile, true);
			} catch (IOException e) {
				e.printStackTrace();
				return resourceRepository.getRootFolder();
			}
		}

		/**
		 * Get the set of path in the case of InJarResource
		 * 
		 * @param resource
		 * @return
		 */
		@Override
		public List<String> getPathTo(InJarResourceImpl resource) {
			// TODO FD4SG la ligne ci-dessous est vraiment bizarre (getChildren renvoie une liste de RepositoryFolder alors que resource est
			// une ressource
			if (!getRootFolder().getChildren().contains(resource)) {
				List<String> pathTo = new ArrayList<>();
				StringTokenizer string = new StringTokenizer(/*resource.getURI()*/resource.getEntry().getName(),
						Character.toString(ClasspathResourceLocatorImpl.PATH_SEP.toCharArray()[0]));
				while (string.hasMoreTokens()) {
					String next = string.nextToken();
					if (string.hasMoreTokens()) {
						pathTo.add(next);
					}
				}
				return pathTo;
			}
			return null;
		}

		@Override
		public String getDisplayableName() {
			if (jarResourceImpl != null) {
				return jarResourceImpl.getJarFileName();
			}
			return "???";
		}

		/**
		 * Called to register a resource relatively to its serialization artefact
		 * 
		 * @param resource
		 * @param serializationArtefact
		 */
		@Override
		public void registerResource(FlexoResource<?> resource, InJarResourceImpl serializationArtefact) {
			registerResource(resource);
		}

		/**
		 * Called to register a resource relatively to its serialization artefact
		 * 
		 * @param resource
		 * @param serializationArtefact
		 */
		@Override
		public void unregisterResource(FlexoResource<?> resource, InJarResourceImpl serializationArtefact) {
			unregisterResource(resource);
		}

		private FlexoProjectResource<InJarResourceImpl> delegatingProjectResource;

		/**
		 * Returns project which delegates it's FlexoResourceCenter to this<br>
		 * Returns null if this {@link FlexoResourceCenter} is not acting as a delegate for a {@link FlexoProject}
		 * 
		 * @return
		 */
		@Override
		public FlexoProjectResource<InJarResourceImpl> getDelegatingProjectResource() {
			return delegatingProjectResource;
		}

		/**
		 * Sets project which delegates it's FlexoResourceCenter to this<br>
		 * 
		 * @return
		 */
		@Override
		public void setDelegatingProjectResource(FlexoProjectResource<InJarResourceImpl> delegatingProjectResource) {
			this.delegatingProjectResource = delegatingProjectResource;
		}

		@Override
		public boolean containsArtefact(InJarResourceImpl serializationArtefact) {
			// TODO
			return false;
		}

		@Override
		public String relativePath(InJarResourceImpl serializationArtefact) {
			return JarUtils.makePathRelativeTo(serializationArtefact, getBaseArtefact());
		}

		@Override
		public String getDisplayableStatus() {
			return "[uri=\"" + getDefaultBaseURI() + "\" jarFile=\"" + getJarFile() + "\"] with " + getAllResources().size() + " resources";
		}

	}

	@ModelEntity
	@XMLElement
	public static interface JarResourceCenterEntry extends ResourceCenterEntry<JarResourceCenter> {
		@PropertyIdentifier(type = File.class)
		public static final String JAR_KEY = "jar";

		@Getter(JAR_KEY)
		@XMLAttribute
		public File getFile();

		@Setter(JAR_KEY)
		public void setFile(File jar);

		@Implementation
		public static abstract class JarResourceCenterEntryImpl implements JarResourceCenterEntry {
			@Override
			public JarResourceCenter makeResourceCenter(FlexoResourceCenterService rcService) {
				// Euh pas l'air de causer des soucis
				JarFile jarFile;
				try {
					jarFile = new JarFile(getFile());
					return instanciateNewJarResourceCenter(jarFile, rcService);
				} catch (IOException e) {
					return null;
				}
			}

			@Override
			public boolean isSystemEntry() {
				// For now, jarRC are only added from ClassPath
				return true;
			}

			@Override
			public void setIsSystemEntry(boolean isSystemEntry) {
				// Does Nothing
			}
		}

	}

}
