module experiments::FilesInGitCommits

import resource::versions::Versions;
import resource::versions::git::Git;

import List;

import IO;

str gitLoc = "/Users/shahi/Documents/CWI/l1_workspace/pdb.values/";

public void main() {
	// Create a 'Connection' datatype.
	con = fs(gitLoc);
	
	// Reverse so the oldest revision will be on top
	set[LogOption] opt = {reverse()};
	
	// Create a 'Repository' datatype of the git Connection
	repo = git(con, "", opt);
	
	// Get all changesets of this git Repository
	cs = getChangesets(repo);
	
	// Get all CheckoutUnits of the current repo:
	revs = getRevisions(cs);
	cu = getCheckoutUnits(revs);

	// Check out all the revisions in succesion...	
	println("Starting analyzing <size(cu)> CheckoutUnits...");
	int x = 1;
	for(c <- cu) {
		println("CheckoutUnit <x>...");
		x += 1;

		// Checkout a specific CheckoutUnit
		checkoutResources(c, repo);
		
		// Get all resources (e.g. files and folders, see resource.versions.Versions.rsc):
		wcRes = getResources(repo);
		// And create Resources of it:		
		set[Resource resource] res = {r.resource | r <- wcRes};

		for(r <- res) {
			// Now r is a file resource of every java file in the current revision of the project
			if(/\.java/ := "<r.id>") {
  				iprintln("Match: <r.id>");
			}
		}
	}
	// Restore the state to the master branch
	master = cunit(branch("master"));
	checkoutResources(master, repo);
}

/**
 * Create a list of revisions based on a given list of changesets.
 * @param cs A list of changesets.
 * @return list[Revision] A list of revisions.
 */
private list[Revision] getRevisions(list[ChangeSet] cs) {
	return [ x.revision | x <- cs];
}

/**
 * Create a list of CheckoutUnits based on a given list of revisions.
 * @param rev A list of revisions
 * @return list[CheckoutUnit] A list of CheckoutUnits.
 */
private list[CheckoutUnit] getCheckoutUnits(list[Revision] revs) {
	return [ cunit(r) | r <- revs];
}
