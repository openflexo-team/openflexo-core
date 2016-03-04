package org.openflexo.foundation.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gitUtils.GitCheckoutPick;
import org.openflexo.gitUtils.GitFile;
import org.openflexo.gitUtils.GitIODelegateFactory;
import org.openflexo.gitUtils.GitSaveStrategy;
import org.openflexo.gitUtils.GitSaveStrategy.GitSaveStrategyBuilder;
import org.openflexo.gitUtils.GitVersion;
import org.openflexo.gitUtils.SerializationArtefactKind;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;

import com.google.common.collect.Lists;


/**
 * Resource center using and manipulating flexo resources that have a git IO delegate 
 * @author kvermeul
 *
 */
public class GitResourceCenter extends FileSystemBasedResourceCenter {

	protected static final Logger logger = Logger.getLogger(GitResourceCenter.class.getPackage().getName());

	private GitSaveStrategy gitSaveStrategy;
	private GitCheckoutPick gitCheckoutPick;

	private Repository gitRepository;

	private List<GitFile> cache = new ArrayList<>();
	
	


	public GitResourceCenter(File resourceCenterDirectory, File gitRepository)
			throws IllegalStateException, IOException, GitAPIException {
		super(resourceCenterDirectory);
		initializeRepositoryGit(gitRepository);
		setDelegateFactory(new GitIODelegateFactory());
	}

	public GitCheckoutPick getGitCheckoutPick() {
		return gitCheckoutPick;
	}

	public void setGitCheckoutPick(GitCheckoutPick gitCheckoutPick) {
		this.gitCheckoutPick = gitCheckoutPick;
	}

	public GitSaveStrategy getGitSaveStrategy() {
		return gitSaveStrategy;
	}

	public void setGitSaveStrategy(GitSaveStrategy gitSaveStrategy) {
		this.gitSaveStrategy = gitSaveStrategy;
	}

	/**
	 * For a first impl, consider that one GitResourceCenter holds one
	 * gitRepository
	 */

	public Repository getGitRepository() {
		return gitRepository;
	}

	public void setGitRepository(Repository gitRepository) {
		this.gitRepository = gitRepository;
	}
	
	
	public List<GitFile> getCache() {
		return cache;
	}

	public void setCache(List<GitFile> cache) {
		this.cache = cache;
	}
	/**
	 * Clone a remote git repository in local
	 * @param whereToclone
	 * @param uri
	 * @throws InvalidRemoteException
	 * @throws TransportException
	 * @throws GitAPIException
	 */
	// Clone the repository where the resource are located
	public void cloneRepo(File whereToclone, String uri)
			throws InvalidRemoteException, TransportException, GitAPIException {
		Git.cloneRepository().setBranch("master").setURI(uri).setGitDir(whereToclone).call();
	}

	
	/**
	 * Initialize the git repo linked with this resource center
	 * @param gitFile
	 * @throws IOException
	 * @throws IllegalStateException
	 * @throws GitAPIException
	 */
	public void initializeRepositoryGit(File gitFile) throws IOException, IllegalStateException, GitAPIException {
		File gitDir = new File(gitFile.getAbsolutePath(), ".git");
		Git.init().setDirectory(gitFile).call();
		setGitCheckoutPick(new GitCheckoutPick());
		GitSaveStrategy saveStrategy = new GitSaveStrategyBuilder().addAdd().addCommit().build();

		setGitSaveStrategy(saveStrategy);

		// Our GitRepository is of type file
		gitRepository = FileRepositoryBuilder.create(gitDir);
		System.out.println("Created a new repository at " + gitRepository.getDirectory());
		// Where files are checked
		System.out.println("Working tree : " + gitRepository.getWorkTree());
		// Where index is checked
		System.out.println("Index File : " + gitRepository.getIndexFile());
		logger.info("New Git Repository Created");
		Git git = new Git(gitRepository);
		git.commit().setMessage("The first Commit").call();
		git.close();
	}

	/**
	 * Checkout the old different versions asked of the resources
	 * @deprecated
	 * 
	 * @throws GitAPIException
	 * @throws CheckoutConflictException
	 * @throws InvalidRefNameException
	 * @throws RefNotFoundException
	 * @throws RefAlreadyExistsException
	 * @throws IOException
	 * @throws IncorrectObjectTypeException
	 * @throws MissingObjectException
	 */
	public void checkoutSeveralVersions(Map<FlexoResource<?>, FlexoVersion> mapResourceVersion, String newBranchName)
			throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, CheckoutConflictException,
			GitAPIException, MissingObjectException, IncorrectObjectTypeException, IOException {

		Map<FlexoResource<?>, ObjectId> commitMap = new HashMap<FlexoResource<?>, ObjectId>();

		// Check if the gitRepository has the version we want to checkout
		Git git = new Git(gitRepository);
		Iterable<RevCommit> commits = git.log().all().call();
		git.close();
		List<RevCommit> commitsList = Lists.newArrayList(commits);
		List<ObjectId> commitIds = new ArrayList<>();
		for (RevCommit revCommit : commitsList) {
			commitIds.add(revCommit.getId());
		}
		for (FlexoResource<?> resource : mapResourceVersion.keySet()) {
			Map<FlexoVersion, ObjectId> versionsIds = ((FlexoIOGitDelegate) resource.getFlexoIODelegate())
					.getGitCommitIds();
			ObjectId commitToAdd = versionsIds.get(mapResourceVersion.get(resource));
			if (commitToAdd != null && commitIds.contains(commitToAdd)) {
				commitMap.put(resource, commitToAdd);
			}
		}
		gitCheckoutPick.checkoutPickOperation(gitRepository, commitMap, newBranchName);
	}

	/**
	 * Synchronizes git reposiory with the wanted (resource, version) in the workspace
	 * @param mapResourceVersion
	 * @param newBranchName
	 * @return
	 * @throws RefAlreadyExistsException
	 * @throws RefNotFoundException
	 * @throws InvalidRefNameException
	 * @throws CheckoutConflictException
	 * @throws GitAPIException
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
	 */
	public Map<FlexoResource<?>, GitVersion> checkoutPick(Map<FlexoResource<?>, FlexoVersion> mapResourceVersion,
			String newBranchName) throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException,
					CheckoutConflictException, GitAPIException, MissingObjectException, IncorrectObjectTypeException,
					IOException {

		// First retrieve the files matching the resources in the working tree
		// repository
		List<String> resourcesNames = new ArrayList<>();
		for (FlexoResource<?> resource : mapResourceVersion.keySet()) {
			resourcesNames.add(resource.getName());
			resourcesNames.add(resource.getName() + ".version");
		}
		FileTreeIterator iter = new FileTreeIterator(gitRepository);
		List<File> keys = new ArrayList<>();
		List<File> values = new ArrayList<>();
		while (!iter.eof()) {
			if (resourcesNames.contains(iter.getEntryFile().getName())) {
				if (iter.getEntryFile().getPath().endsWith(".version")) {
					values.add(iter.getEntryFile());
				} else {
					keys.add(iter.getEntryFile());
				}
			}
			iter.next(1);
		}

		Map<FlexoResource<?>, GitVersion> returned = new HashMap<FlexoResource<?>, GitVersion>();
		Map<FlexoResource<?>, ObjectId> commitMap = new HashMap<FlexoResource<?>, ObjectId>();
		// Then put the version we get in this repository thanks to the .version
		// file
		for (File key : keys) {
			Map<FlexoVersion, ObjectId> versionsInRepo = new HashMap<>();
			String resourceName = key.getName();
			for (File value : values) {
				if (value.getPath().equals(key.getPath() + ".version")) {
					BufferedReader stream = new BufferedReader(new FileReader(value));
					String content = null;
					while ((content = stream.readLine()) != null) {
						String[] split = content.split(",");
						// split 0 is version, split 1 id of commit
						FlexoVersion versionInFile = new FlexoVersion(split[0]);
						versionsInRepo.put(versionInFile, ObjectId.fromString(split[1]));
					}
					stream.close();
				}
			}
			// Put the versions to synchronize git repository in the map to
			// checkout
			for (FlexoResource<?> resource : mapResourceVersion.keySet()) {
				if (resource.getName().equals(resourceName)) {
					for (FlexoVersion version : versionsInRepo.keySet()) {
						if (version.equals(mapResourceVersion.get(resource))) {
							GitVersion versionToReturn = new GitVersion(mapResourceVersion.get(resource),gitRepository);
							returned.put(resource, versionToReturn);
							commitMap.put(resource, versionsInRepo.get(version));
							break;
						}
					}
				}
			}
		}
		// Finally call the checkout to synchronize git repository
		gitCheckoutPick.checkoutPickOperation(gitRepository, commitMap, newBranchName);

		return returned;

	}
	
	/**
	 * load resource from a git repository and set the io delegate with the good versions
	 * @param gitRepository
	 * @throws IOException
	 */
	public void initializeResources(Repository gitRepository) throws IOException{
		FileTreeIterator iter = new FileTreeIterator(gitRepository);
		List<FlexoResource<?>> resourcesFoundInRepo = new ArrayList<>();
		List<File> versionsFiles = new ArrayList<>();
		while(!iter.eof()){	
			List<TechnologyAdapter> ta = getTechnologyAdapterService().getTechnologyAdapters();
			for (TechnologyAdapter technologyAdapter : ta) {
				FlexoResource<?> resource = technologyAdapter.tryToLookUp(this, iter.getEntryFile());
				if (resource != null) {
					System.out.println("Loaded resource :" + iter.getEntryPathString());
					resourcesFoundInRepo.add(resource);
					registerResource(resource);
				}
			}
			iter.next(1);
		}
		for (FlexoResource<?> resource : resourcesFoundInRepo) {
			GitIODelegateFactory factory = new GitIODelegateFactory();
			FlexoIOGitDelegate  gitDelegate= (FlexoIOGitDelegate) factory.makeIODelegateNewInstance(resource,SerializationArtefactKind.FILE);
			for (File file : versionsFiles) {
				if(file.getName().equals(resource.getName()+".version")){					
					BufferedReader stream = new BufferedReader(new FileReader(file));
					String content = null;
					while ((content = stream.readLine()) != null) {
						String[] split = content.split(",");
						// split 0 is version, split 1 id of commit
						FlexoVersion versionInFile = new FlexoVersion(split[0]);
						ObjectId commitId = ObjectId.fromString(split[1]);
						gitDelegate.getGitCommitIds().put(versionInFile, commitId);
						resource.setVersion(versionInFile);
					}
					stream.close();
				}
			}
			resource.setFlexoIODelegate(gitDelegate);
		}
		
	}
	public void loadFlexoEnvironment(Repository gitRepository) throws NoHeadException, GitAPIException, IOException {

		FileTreeIterator iter = new FileTreeIterator(gitRepository);
		while (!iter.eof()) {
			List<TechnologyAdapter> ta = getTechnologyAdapterService().getTechnologyAdapters();
			for (TechnologyAdapter technologyAdapter : ta) {
				FlexoResource<?> resource = technologyAdapter.tryToLookUp(this, iter.getEntryFile());
				if (resource != null) {
					System.out.println("Loaded resource :" + iter.getEntryPathString());
					registerResource(resource);
				}
			}
			iter.next(1);
		}

		Git git = new Git(gitRepository);
		Iterable<RevCommit> commits = git.log().all().call();
		git.close();
		LinkedList<RevCommit> listCommits = Lists.newLinkedList(commits);
		// Put the commits from the first to the last
		Collections.reverse(listCommits);
		Collection<FlexoResource<?>> resources = this.getAllResources();
		ObjectReader reader = gitRepository.newObjectReader();
		for (RevCommit revCommit : listCommits) {
			CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
			if (!listCommits.getFirst().equals(revCommit)) {
				oldTreeIter.reset(reader, revCommit.getParent(0).getTree());
				CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
				newTreeIter.reset(reader, revCommit.getTree());
				List<DiffEntry> diffs = git.diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).call();
				for (FlexoResource<?> flexoResource : resources) {
					FlexoVersion version = flexoResource.getVersion();

					for (DiffEntry diff : diffs) {

						if (diff.getNewPath().contains(flexoResource.getName())
								&& !diff.getNewPath().endsWith(".version")) {
							if (version == null) {
								version = new FlexoVersion("0.1");
							} else {
								version = FlexoVersion.versionByIncrementing(version, 0, 1, 0);
							}
							((FlexoIOGitDelegate) flexoResource.getFlexoIODelegate()).getGitCommitIds().put(version,
									revCommit.getId());
						}
					}
					flexoResource.setVersion(version);
				}

			}
		}
		setGitRepository(gitRepository);
	}

	
	/**
	 * Push the local cloned repository to the remote repository
	 */
	public void pushLocalRepository() {
		Git git = new Git(gitRepository);
		try {
			if (git.status().call().isClean()) {
				try {
					git.pull().call();
				} catch (GitAPIException e) {
					logger.log(Level.SEVERE, "Could not Pull the repository" + e.getMessage());
				}
				try {
					git.push().call();
				} catch (GitAPIException e) {
					logger.log(Level.SEVERE, "Could not push the repository" + e.getMessage());
				}
			}
		} catch (NoWorkTreeException | GitAPIException e1) {
			logger.log(Level.WARNING, "Dirty working tree : " + e1.getMessage());
		}
		git.close();
	}

	/**
	 * Retrieve a certain version of a resource and put in the cache of the resource center
	 * @param resourceName
	 * @param version
	 */
	
	public void putVersionInCache(String resourceName, FlexoVersion version){
		Map<FlexoResource<?>,FlexoVersion> resourceFoundInGitResourceCenter = new HashMap<>();
		for (FlexoResource<?> resource : this.getAllResources()) {
			if (resourceName.equals(resource.getName())){
				resourceFoundInGitResourceCenter.put(resource, version);
			}
		}
		try {
			this.checkoutPick(resourceFoundInGitResourceCenter, null);
		} catch (GitAPIException | IOException e) {
			e.printStackTrace();
		}
		FileTreeIterator iter = new FileTreeIterator(gitRepository);
		while(!iter.eof()){
			if(iter.getEntryFile().getName().equals(resourceName)){
				cache.add(new GitFile(version,iter.getEntryFile()));
			}
			try {
				iter.next(1);
			} catch (CorruptObjectException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public void publishResource(FlexoResource<?> resource, FlexoVersion newVersion, IProgress progress)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void update() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDefaultBaseURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends FlexoResource<?>> getAllResources(IProgress progress) {
		// TODO Auto-generated method stub
		return null;
	}

}
