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
import java.util.Objects;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openflexo.foundation.FlexoServiceManager;
import org.openflexo.foundation.converter.FlexoObjectReferenceConverter;
import org.openflexo.foundation.fml.FMLTechnologyAdapter;
import org.openflexo.foundation.fml.ViewPointRepository;
import org.openflexo.foundation.resource.DirectoryBasedJarIODelegate.DirectoryBasedJarIODelegateImpl;
import org.openflexo.foundation.resource.InJarIODelegate.InJarIODelegateImpl;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.foundation.utils.FlexoObjectReference;
import org.openflexo.model.annotations.Getter;
import org.openflexo.model.annotations.Implementation;
import org.openflexo.model.annotations.ModelEntity;
import org.openflexo.model.annotations.PropertyIdentifier;
import org.openflexo.model.annotations.Setter;
import org.openflexo.model.annotations.XMLAttribute;
import org.openflexo.model.annotations.XMLElement;
import org.openflexo.model.exceptions.ModelDefinitionException;
import org.openflexo.model.factory.ModelFactory;
import org.openflexo.rm.ClasspathResourceLocatorImpl;
import org.openflexo.rm.InJarResourceImpl;
import org.openflexo.rm.JarResourceImpl;
import org.openflexo.rm.Resource;
import org.openflexo.rm.ResourceLocator;
import org.openflexo.toolbox.ClassPathUtils;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;
import org.openflexo.xml.XMLRootElementInfo;
import org.openflexo.xml.XMLRootElementReader;

/**
 * A Jar resource center references a set of resources inside a Jar.
 * 
 * @author Vincent, xtof
 *
 */
public class JarResourceCenter extends ResourceRepository<FlexoResource<?>, InJarResourceImpl>
		implements FlexoResourceCenter<InJarResourceImpl> {

	protected static final Logger logger = Logger.getLogger(ResourceRepository.class.getPackage().getName());

	/**
	 * A jar file the resource center might interpret
	 */
	private final JarFile jarFile;

	/**
	 * A string that is used to identify the JarRC and build uri of resources included in the RC
	 * 
	 */
	private String rcBaseUri;

	/**
	 * A JarResource is the main element of a JarResource center. It contains a set of InJarResource elements.
	 */
	private JarResourceImpl jarResourceImpl;

	private final FlexoResourceCenterService rcService;

	// private final Map<TechnologyAdapter, ResourceRepository<?>> globalRepositories = new HashMap<>();

	/**
	 * Contructor based on a given JarResource
	 * 
	 * @param jarResourceImpl
	 */
	public JarResourceCenter(JarResourceImpl jarResourceImpl, FlexoResourceCenterService rcService) {
		super(null, jarResourceImpl.getRootEntry());
		this.rcService = rcService;
		this.jarFile = jarResourceImpl.getJarfile();
		this.jarResourceImpl = jarResourceImpl;

	}

	/**
	 * Constructor based on a given jarFile
	 * 
	 * @param jarFile
	 */
	public JarResourceCenter(JarFile jarFile, FlexoResourceCenterService rcService) {
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
	}

	@Override
	public JarResourceCenter getResourceCenter() {
		return this;
	}

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

	public FlexoResourceCenterService getFlexoResourceCenterService() {
		return rcService;
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

	private final HashMap<TechnologyAdapter, HashMap<Class<? extends ResourceRepository<?, InJarResourceImpl>>, ResourceRepository<?, InJarResourceImpl>>> repositories = new HashMap<>();

	private HashMap<Class<? extends ResourceRepository<?, InJarResourceImpl>>, ResourceRepository<?, InJarResourceImpl>> getRepositoriesForAdapter(
			TechnologyAdapter technologyAdapter, boolean considerEmptyRepositories) {
		if (considerEmptyRepositories) {
			technologyAdapter.ensureAllRepositoriesAreCreated(this);
		}
		HashMap<Class<? extends ResourceRepository<?, InJarResourceImpl>>, ResourceRepository<?, InJarResourceImpl>> map = repositories
				.get(technologyAdapter);
		if (map == null) {
			map = new HashMap<Class<? extends ResourceRepository<?, InJarResourceImpl>>, ResourceRepository<?, InJarResourceImpl>>();
			repositories.put(technologyAdapter, map);
		}
		return map;
	}

	@SuppressWarnings({ "hiding", "unchecked" })
	@Override
	public final <R extends ResourceRepository<?, InJarResourceImpl>> R retrieveRepository(Class<? extends R> repositoryType,
			TechnologyAdapter technologyAdapter) {
		HashMap<Class<? extends ResourceRepository<?, InJarResourceImpl>>, ResourceRepository<?, InJarResourceImpl>> map = getRepositoriesForAdapter(
				technologyAdapter, false);
		return (R) map.get(repositoryType);
	}

	@SuppressWarnings("hiding")
	@Override
	public final <R extends ResourceRepository<?, InJarResourceImpl>> void registerRepository(R repository,
			Class<? extends R> repositoryType, TechnologyAdapter technologyAdapter) {
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
	public Collection<ResourceRepository<?, InJarResourceImpl>> getRegistedRepositories(TechnologyAdapter technologyAdapter,
			boolean considerEmptyRepositories) {
		return getRepositoriesForAdapter(technologyAdapter, considerEmptyRepositories).values();
	}

	/**
	 * Register global repository for this resource center<br>
	 * It is stated that the global repository contains all resources which supplied technology adapter has discovered and may interpret<br>
	 * This is the resource repository which is generally given in GUIs (such as browsers) to display the contents of a resource center for
	 * a given technology
	 * 
	 * @param repository
	 * @param technologyAdapter
	 */
	/*@Override
	public final void registerGlobalRepository(ResourceRepository<?> repository, TechnologyAdapter technologyAdapter) {
		if (repository != null && technologyAdapter != null) {
			globalRepositories.put(technologyAdapter, repository);
		}
	}*/

	/**
	 * Return the global repository for this resource center and for supplied technology adapter<br>
	 * It is stated that the global repository contains all resources which supplied technology adapter has discovered and may interpret<br>
	 * This is the resource repository which is generally given in GUIs (such as browsers) to display the contents of a resource center for
	 * a given technology
	 * 
	 * @param technologyAdapter
	 * @return
	 */
	/*@Override
	public ResourceRepository<?> getGlobalRepository(TechnologyAdapter technologyAdapter) {
		if (technologyAdapter != null) {
			return globalRepositories.get(technologyAdapter);
		}
		return null;
	}*/

	@Override
	public <T extends ResourceData<T>> List<FlexoResource<T>> retrieveResource(String uri, Class<T> type, IProgress progress) {
		// TODO: provide support for class and version
		FlexoResource<T> uniqueResource = retrieveResource(uri, null, null, progress);
		return Collections.singletonList(uniqueResource);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ResourceData<T>> FlexoResource<T> retrieveResource(String uri, FlexoVersion version, Class<T> type,
			IProgress progress) {
		// TODO: provide support for class and version
		return (FlexoResource<T>) retrieveResource(uri, progress);
	}

	@Override
	public FlexoResource<?> retrieveResource(String uri, IProgress progress) {
		return getResource(uri);
	}

	@Override
	public <R extends FlexoResource<?>> R getResource(InJarResourceImpl resourceArtifact, Class<R> resourceClass) {
		try {
			// searches for parent folder.
			RepositoryFolder<?, InJarResourceImpl> folder = getParentRepositoryFolder(resourceArtifact, false);
			if (folder == null) { return null; }

			for (FlexoResource<?> r : folder.getResources()) {
				if (Objects.equals(r.getIODelegate().getSerializationArtefact(), resourceArtifact)) {
					if (resourceClass.isInstance(r)) {
						return resourceClass.cast(r);
					}
					logger.warning("Found resource matching file " + resourceArtifact + " but not of desired type: " + r.getClass() + " instead of " + resourceClass);
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

	/**
	 * Add all the jars from the class path to resource centers
	 * 
	 * @param rcService
	 */
	public static void addAllJarFromClassPath(FlexoResourceCenterService rcService) {
		for (JarFile file : ClassPathUtils.getClassPathJarFiles()) {
			addJarFile(file, rcService);
		}
	}

	/**
	 * Add the first jar from the class path found with this name Example : path of the jar in the class path :
	 * c:/a/b/c/org/openflexo/myjar.jar Name : org.openflexo.myjar Return the c:/a/b/c/org/openflexo/myjar.jar
	 * 
	 * @param rcService
	 */
	@SuppressWarnings("rawtypes")
	public static JarResourceCenter addNamedJarFromClassPath(FlexoResourceCenterService rcService, String name) {
		JarResourceCenter rc = null;
		for (JarFile file : ClassPathUtils.getClassPathJarFiles()) {
			if ((file.getName().endsWith(name + ".jar")) || (name.endsWith(".jar") && file.getName().endsWith(name))) {
				rc = addJarFile(file, rcService);
				break;
			}
		}
		return rc;
	}

	/**
	 * Add a resource center from a jar file
	 * 
	 * @param jarFile
	 * @param rcService
	 */
	@SuppressWarnings("rawtypes")
	public static JarResourceCenter addJarFile(JarFile jarFile, FlexoResourceCenterService rcService) {
		logger.info("Try to create a resource center from a jar file : " + jarFile.getName());
		JarResourceCenter rc = new JarResourceCenter(jarFile, rcService);

		rc.setDefaultBaseURI(jarFile.getName());
		rcService.addToResourceCenters(rc);
		rcService.storeDirectoryResourceCenterLocations();
		return rc;
	}

	@Override
	public Collection<? extends FlexoResource<?>> getAllResources(IProgress progress) {
		return getAllResources();
	}

	@Override
	public void publishResource(FlexoResource<?> resource, FlexoVersion newVersion, IProgress progress) throws Exception {
		// TODO Not yet implemented
	}

	@Override
	public void update() throws IOException {
		// TODO Not yet implemented
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
				JarFile jarFile;
				try {
					jarFile = new JarFile(getFile());
					return new JarResourceCenter(jarFile, rcService);
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

	@Override
	public String getDefaultBaseURI() {
		return rcBaseUri;
	}

	@Override
	public void setDefaultBaseURI(String defaultBaseURI) {
		rcBaseUri = defaultBaseURI;

	}

	@Override
	public boolean isIgnorable(InJarResourceImpl artefact, TechnologyAdapter technologyAdapter) {
		// Trivial implementation
		return false;
	}

	// TODO Remove this
	@Override
	public ViewPointRepository<?> getViewPointRepository() {
		if (getServiceManager() != null) {
			FMLTechnologyAdapter vmTA = getServiceManager().getTechnologyAdapterService().getTechnologyAdapter(FMLTechnologyAdapter.class);
			return vmTA.getViewPointRepository(this);
		}
		return null;
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
		return true;
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
	public InJarIODelegate makeFlexoIODelegate(InJarResourceImpl serializationArtefact, FlexoResourceFactory<?, ?, ?> resourceFactory) {
		return InJarIODelegateImpl.makeInJarFlexoIODelegate(serializationArtefact, resourceFactory);
	}

	@Override
	public FlexoIODelegate<InJarResourceImpl> makeDirectoryBasedFlexoIODelegate(InJarResourceImpl serializationArtefact,
			String directoryExtension, String fileExtension, FlexoResourceFactory<?, ?, ?> resourceFactory) {

		String baseName = retrieveName(serializationArtefact).substring(0,
				retrieveName(serializationArtefact).length() - directoryExtension.length());
		return DirectoryBasedJarIODelegateImpl.makeDirectoryBasedFlexoIODelegate(serializationArtefact.getContainer(), baseName,
				directoryExtension, fileExtension, this, resourceFactory);
	}

	@Override
	public XMLRootElementInfo getXMLRootElementInfo(InJarResourceImpl serializationArtefact) {
		XMLRootElementReader reader = new XMLRootElementReader();
		try {
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
				InputStream is = propertiesJarEntry.openInputStream();
				try {
					returned.load(is);
				} finally {
					is.close();
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
			// TODO Auto-generated catch block
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
		if (!getRootFolder().getChildren().contains(resource)) {
			List<String> pathTo = new ArrayList<String>();
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
		else {
			return null;
		}
	}

	/*
	 * ReferenceOwner default implementation => does nothing
	 * @see org.openflexo.foundation.utils.FlexoObjectReference.ReferenceOwner#notifyObjectLoaded(org.openflexo.foundation.utils.FlexoObjectReference)
	 */

	@Override
	public void notifyObjectLoaded(FlexoObjectReference<?> reference) {
		// logger.warning("TODO: implement this");
	}

	@Override
	public void objectCantBeFound(FlexoObjectReference<?> reference) {
		logger.warning("TODO: implement this");
	}

	@Override
	public void objectSerializationIdChanged(FlexoObjectReference<?> reference) {
		setChanged();
	}

	@Override
	public void objectDeleted(FlexoObjectReference<?> reference) {
		logger.warning("TODO: implement this");
	}

	/**
	 * access to ObjectReference Converter used to translate strings to ObjectReference
	 */

	protected FlexoObjectReferenceConverter objectReferenceConverter = new FlexoObjectReferenceConverter(this);

	@Override
	public FlexoObjectReferenceConverter getObjectReferenceConverter() {
		return objectReferenceConverter;
	}

	@Override
	public void setObjectReferenceConverter(FlexoObjectReferenceConverter objectReferenceConverter) {
		this.objectReferenceConverter = objectReferenceConverter;
	}

	@Override
	public String getDisplayableName() {
		if (jarResourceImpl != null) {
			return jarResourceImpl.getJarFileName();
		}
		return "???";
	}
}
