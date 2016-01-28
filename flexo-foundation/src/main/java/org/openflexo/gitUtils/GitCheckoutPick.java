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
import org.openflexo.foundation.resource.FlexoResource;

public class GitCheckoutPick {
	
	public void checkoutPickOperation(Repository gitRepository, Map<FlexoResource<?>,ObjectId> commits,String newBranchName) throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException, MissingObjectException, IncorrectObjectTypeException, IOException{
		Git git = new Git(gitRepository);
//		Collections.sort(commits, new Comparator<ObjectId>() {
//
//			@Override
//			public int compare(ObjectId o1, ObjectId o2) {
//				
//				return o1.compareTo(o2);
//			}
//	    });
		for (FlexoResource<?> resource : commits.keySet()) {
			git.checkout().setStartPoint(commits.get(resource).getName()).addPath(resource.getName()).call();
			git.add().addFilepattern(resource.getName());
//			MergeResult merge = git.merge().setStrategy(MergeStrategy.THEIRS).include(objectId).call();	
//			System.out.println("MergeStatus : "+ merge.getMergeStatus());
		}
		
		git.commit().setMessage("Checkout Different Resources with different versions").call();
		//cherryPickCommand.setStrategy(MergeStrategy.THEIRS).call();
		git.close();
	}
	
}
