package org.openflexo.gitUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.openflexo.foundation.resource.FlexoIOGitDelegate;
import org.openflexo.foundation.resource.FlexoResource;
import org.openflexo.toolbox.FlexoVersion;


/**
 * Set which git actions are to be made when a save on a resource is asked. This strategy is owned by the resource center.
 * @author kvermeul
 *
 */
public class GitSaveStrategy {

	private List<GitOperation> activesOperations = new LinkedList<GitOperation>();

	public List<GitOperation> getActivesOperations() {
		return activesOperations;
	}

	private GitSaveStrategy(GitSaveStrategyBuilder builder) {
		activesOperations = (LinkedList<GitOperation>) builder.getActivesGitOperations();
	}

	public void executeStrategy(Git git, Repository repository, FlexoResource<?> resource)
			throws IncorrectObjectTypeException, IOException {
		List<FlexoResource<?>> list = new LinkedList<>();
		list.add(resource);
		executeStrategy(git, repository, list);
	}
	
	
	/**
	 * Execute the strategy following which git operations are present in the strategy
	 * @param git
	 * @param repository
	 * @param resources
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
	 */
	public void executeStrategy(Git git, Repository repository, List<FlexoResource<?>> resources)
			throws IncorrectObjectTypeException, IOException {
		for (GitOperation gitOperation : activesOperations) {
			if (gitOperation.toString().equals("ADD")) {

				try {
					for (FlexoResource<?> flexoResource : resources) {
						System.out.println("Add ressource : " + flexoResource.getName()
								+ " to Git Index of Repository : " + repository.getBranch());
						//Linux way to have the relative path from the repository, because JGIT Api does not support absolute
						//path in add file pattern method
						String pathName =((FlexoIOGitDelegate) flexoResource.getFlexoIODelegate()).getFile().getAbsolutePath();
						String realtivePath = StringUtils.substringAfter(pathName, repository.getWorkTree().getAbsolutePath()+"/");
						git.add().addFilepattern(realtivePath).call();
						System.out.println("Resource name to add: " + pathName);
					}

				} catch (GitAPIException | IOException e) {
					e.printStackTrace();
				}
			} else if (gitOperation.toString().equals("COMMIT")) {

				try {

					for (FlexoResource<?> flexoResource : resources) {
						String pathName =((FlexoIOGitDelegate) flexoResource.getFlexoIODelegate()).getFile().getAbsolutePath();
						String realtivePath = StringUtils.substringAfter(pathName, repository.getWorkTree().getAbsolutePath()+"/");


						RevCommit commit = git.commit().setMessage("Resource " + pathName + " committed")
								.call();
						ObjectId head = repository.resolve("HEAD^{tree}");
						ObjectId previousHead = repository.resolve("HEAD~^{tree}");
						ObjectReader reader = repository.newObjectReader();
						CanonicalTreeParser oldTree = new CanonicalTreeParser();
						CanonicalTreeParser newTree = new CanonicalTreeParser();
						oldTree.reset(reader, previousHead);
						newTree.reset(reader, head);
						List<DiffEntry> diffs = git.diff().setOldTree(oldTree).setNewTree(newTree).call();
						boolean changed = false;
						for (DiffEntry diffEntry : diffs) {
							if (realtivePath.equals(diffEntry.getNewPath())) {
								changed = true;
								break;
							}
						}
						if (changed) {
							if (flexoResource.getVersion() == null) {
								flexoResource.setVersion(new FlexoVersion("0.1"));
							} else {
								flexoResource.setVersion(
										FlexoVersion.versionByIncrementing(flexoResource.getVersion(), 0, 1, 0));
							}
							FlexoIOGitDelegate gitDelegate = (FlexoIOGitDelegate) flexoResource.getFlexoIODelegate();
							gitDelegate.getGitCommitIds().put(flexoResource.getVersion(), commit.getId());
							gitDelegate.writeVersion(flexoResource, commit.getId());
							//Commit the version file
							git.add().addFilepattern(realtivePath+".version").call();
							git.commit().setMessage("Update of the version file "+pathName);
						}
					}

				} catch (GitAPIException e) {
					e.printStackTrace();
				}
			} else if (gitOperation.toString().equals("PUSH")) {
				try {
					Iterable<PushResult> push = git.push().call();
					for (FlexoResource<?> flexoResource : resources) {
						System.out.println("Push ressource : " + flexoResource.getName() + " in Repository : "
								+ repository.getBranch());
					}
				} catch (GitAPIException | IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Nested Class to build the strategy for saving
	 * @author kvermeul
	 *
	 */
	public static class GitSaveStrategyBuilder {

		List<GitOperation> gitActivesOperations = new LinkedList<>();

		public List<GitOperation> getActivesGitOperations() {
			return gitActivesOperations;
		}

		public GitSaveStrategyBuilder addAdd() {
			GitOperation add = GitOperation.ADD;
			gitActivesOperations.add(add);
			return this;
		}

		public GitSaveStrategyBuilder addCommit() {
			GitOperation commit = GitOperation.COMMIT;
			gitActivesOperations.add(commit);
			return this;
		}

		public GitSaveStrategyBuilder addPush() {
			GitOperation push = GitOperation.PUSH;
			gitActivesOperations.add(push);
			return this;
		}

		public GitSaveStrategy build() {
			return new GitSaveStrategy(this);
		}

	}
}
