module migod

// Libs
import List;
import IO;

// Locals
import Statistics;
import experiments::scm::Scm;
import experiments::scm::git::Git;
import experiments::scm::svn::Svn;
import experiments::scm::cvs::Cvs;

// Some constants
public str homeDir = "/Users/migod";
public str workspace = homeDir + "Documents/workspace";
str linuxVID = "2.6";
public str linuxGitLocation = homeDir + "/Desktop/linux-" + linuxVID;


public void getGitExample () {
	Repository repo = git(fs(linuxGitLocation), "", {});
	// the below crashes
	list[ChangeSet] resourceChanges = getChanges(repo); 
}

public void getSvnExample () {
    Repository repo = svn(ssh("svn+ssh://svn.cwi.nl", "migod", "", |file:///Users/migod/.ssh/migod_rascal_rsa|), "", |file:///Users/migod/Rascal/svnWorkingCopy|,{});
	
	// This seems to work, the number of changes reported is the same 
	// as SVN reports withing Eclipse!
	list[ChangeSet] resourceChanges = getChanges(repo);
	println ("\nNumber of resource changes: <size(resourceChanges)>");
}

public void getCvsExample () {
	// Probably I am not using the right parameters here.
	Connection cvsConnection = 
	    pserver("http://tortoisecvs.cvs.sourceforge.net/viewvc/tortoisecvs/",
	    "TortoiseCVS", "tortoisecvs.cvs.sourceforge.net", "anonymous", " ");
		// pserver(str url, str repname, str host, str username, str password);
	Repository repo = cvs(cvsConnection, "tortoisecvs", 
		|file:///Users/migod/Rascal/cvsWorkingCopy|,{});
	list[ChangeSet] resourceChanges = getChanges(repo); 
	// This returns zero
	println ("\nNumber of resource changes: <size(resourceChanges)>");
}

public void main () {
}
