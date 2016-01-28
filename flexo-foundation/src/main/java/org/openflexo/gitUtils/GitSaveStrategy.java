package org.openflexo.gitUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.PushResult;
import org.openflexo.foundation.resource.FlexoIOGitDelegate;
import org.openflexo.foundation.resource.FlexoResource;

public class GitSaveStrategy {

	private List<GitOperation> activesOperations = new LinkedList<GitOperation>();

	public List<GitOperation> getActivesOperations() {
		return activesOperations;
	}

	private GitSaveStrategy(GitSaveStrategyBuilder builder) {
		activesOperations = (LinkedList<GitOperation>) builder.getActivesGitOperations();
	}
	
	public void executeStrategy(Git git, Repository repository,FlexoResource<?> resource) {
		List<FlexoResource<?>> list = new LinkedList<>();
		list.add(resource);
		executeStrategy(git, repository, list);
	}
	
	public void executeStrategy(Git git, Repository repository,List<FlexoResource<?>> resources) {
		for (GitOperation gitOperation : activesOperations) {
			if (gitOperation.toString().equals("ADD")) {
				try {
					for (FlexoResource<?> flexoResource : resources) {
						System.out.println("Add ressource : "+flexoResource.getName()+" to Git Index of Repository : "+repository.getBranch() );
						DirCache inIndex = git.add().addFilepattern(flexoResource.getName()).call();
						FlexoIOGitDelegate gitDelegate = (FlexoIOGitDelegate) flexoResource.getFlexoIODelegate();
						ObjectId addId = inIndex.getEntry(flexoResource.getName()).getObjectId();
						System.out.println("Resource name to add: "+ flexoResource.getName());
						gitDelegate.getGitObjectIds().add(addId);
					}

				} catch (GitAPIException | IOException e) {
					e.printStackTrace();
				}
			}
			if (gitOperation.toString().equals("COMMIT")) {
				
				try {
					RevCommit commit = git.commit().setMessage("Resource committed").call();

					for (FlexoResource<?> flexoResource : resources) {
						FlexoIOGitDelegate gitDelegate = (FlexoIOGitDelegate) flexoResource.getFlexoIODelegate();
						gitDelegate.getGitCommitIds().put(flexoResource.getVersion(), commit.getId());
						System.out.println("Commit ressource : "+flexoResource.getName()+" in Repository : "+repository.getBranch() );
						
					}
				} catch (GitAPIException | IOException e) {
					e.printStackTrace();
				}
			}
			if (gitOperation.toString().equals("PUSH")) {
				try {
					Iterable<PushResult> push = git.push().call();
					for (FlexoResource<?> flexoResource : resources) {
						System.out.println("Push ressource : "+flexoResource.getName()+" in Repository : "+repository.getBranch() );
					}
				} catch (GitAPIException | IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

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
