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


public void getLinuxStats () {
	Repository repo = git(fs(linuxGitLocation), "", {});
    list[ChangeSet] resourceChanges = getChanges(repo); // this crashes
}

public void getRascalStats () {
    Repository repo = svn(ssh("svn+ssh://svn.cwi.nl", "migod", "", 
	    |file:///Users/migod/.ssh/migod_rascal_rsa|), "",
	    |file:///Users/migod/Rascal/svnWorkingCopy|,{});
	list[ChangeSet] resourceChanges = getChanges(repo); // this seems to work!
	println ("\nNumber of resource changes: <size(resourceChanges)>");
}

public void getXmlCommonsStats () {
	Connection cvsConnection = 
	    pserver( "", "xml-commons", "cvs.apache.org", "", "anoncvs");
		// pserver(str url, str repname, str host, str username, str password);
	Repository repo = cvs(cvsConnection, "flurble", 
		|file:///Users/migod/Rascal/cvsWorkingCopy|,{});
	list[ChangeSet] resourceChanges = getChanges(repo); 
	// This returns zero
	println ("\nNumber of resource changes: <size(resourceChanges)>");
}



public void main () {
}
