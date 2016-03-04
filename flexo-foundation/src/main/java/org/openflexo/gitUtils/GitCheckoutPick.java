package org.openflexo.gitUtils;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.openflexo.foundation.resource.FlexoIOGitDelegate;
import org.openflexo.foundation.resource.FlexoResource;

public class GitCheckoutPick {
	
	public void checkoutPickOperation(Repository gitRepository, Map<FlexoResource<?>,ObjectId> commits,String newBranchName) throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException, MissingObjectException, IncorrectObjectTypeException, IOException{
		Git git = new Git(gitRepository);
		for (FlexoResource<?> resource : commits.keySet()) {
			git.checkout().setStartPoint(commits.get(resource).getName()).addPath(resource.getName()).call();			
		}
		git.close();
	}
	
	public void checkoutPickAndCreateBranch(Repository gitRepository, Map<FlexoResource<?>,ObjectId> commits, String newBranchName) throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException{
		Git git = new Git(gitRepository);
		for (FlexoResource<?> resource : commits.keySet()) {
			git.checkout().setCreateBranch(true).setName(newBranchName).setStartPoint(commits.get(resource).getName()).call();
//			((FlexoIOGitDelegate) resource.getFlexoIODelegate()).save(resource);
		}
		git.close();
	}
	
}
