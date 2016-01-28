package org.openflexo.foundation.resource;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.openflexo.foundation.technologyadapter.TechnologyAdapter;
import org.openflexo.gitUtils.GitCheckoutPick;
import org.openflexo.gitUtils.GitIODelegateFactory;
import org.openflexo.gitUtils.GitSaveStrategy;
import org.openflexo.gitUtils.GitSaveStrategy.GitSaveStrategyBuilder;
import org.openflexo.toolbox.FlexoVersion;
import org.openflexo.toolbox.IProgress;

import com.google.common.collect.Lists;

public class GitResourceCenter extends FileSystemBasedResourceCenter {

	protected static final Logger logger = Logger.getLogger(GitResourceCenter.class.getPackage().getName());

	private GitSaveStrategy gitSaveStrategy;
	private GitCheckoutPick gitCheckoutPick;

	private Repository gitRepository;

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
		git.commit().setMessage("The first Commit");
		git.close();
	}

	/**
	 * Checkout the old different versions asked of the resources
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

		for (FlexoResource<?> resource : mapResourceVersion.keySet()) {
			Map<FlexoVersion, ObjectId> versionsIds = ((FlexoIOGitDelegate) resource.getFlexoIODelegate())
					.getGitCommitIds();
			ObjectId commitToAdd = versionsIds.get(mapResourceVersion.get(resource));
			commitMap.put(resource, commitToAdd);
		}
		gitCheckoutPick.checkoutPickOperation(gitRepository, commitMap, newBranchName);
	}

	public void loadFlexoEnvironment(Repository gitRepository) throws NoHeadException, GitAPIException, IOException {

		FileTreeIterator iter = new FileTreeIterator(gitRepository);
		while (!iter.eof()) {
			System.out.println("Loading resource :" + iter.getEntryPathString());
			List<TechnologyAdapter> ta = getTechnologyAdapterService().getTechnologyAdapters();
			for (TechnologyAdapter technologyAdapter : ta) {
				FlexoResource<?> resource = technologyAdapter.tryToLookUp(this, iter.getEntryFile());
				if (resource != null) {
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
					
						if (diff.getNewPath().equals(flexoResource.getName())) {
							Map<FlexoVersion, ObjectId> versions = ((FlexoIOGitDelegate) flexoResource
									.getFlexoIODelegate()).getGitCommitIds();
							if (version == null) {
								version = new FlexoVersion("0.1");
							}
							else{
								version = FlexoVersion.versionByIncrementing(version, 0, 1, 0);
							}
							versions.put(version, revCommit.getId());
						}
					}
					flexoResource.setVersion(version);
				}

			}
		}

	}

	@Override
	public Collection<? extends FlexoResource<?>> getAllResources(IProgress progress) {
		// TODO Auto-generated method stub
		return null;
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

}
