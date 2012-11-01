module Migod

// Libs
import List;
import Set;
import IO;

// Locals
import experiments::Statistics;
import resource::versions::Versions;
import resource::versions::git::Git;
import resource::versions::svn::Svn;
import resource::versions::cvs::Cvs;

// Some constants
public str homeDir = "/Users/migod";
public str workspace = homeDir + "/Documents/workspace";
str linuxVID = "2.6";
public str linuxGitLocation = homeDir + "/Desktop/linux-" + linuxVID;
public list[ChangeSet] migodChanges;

// Below line should probably get put into experiment::scm::cvs
data Connection = pserver(str url, str host, str username, str password);

public void getGitExample () {
	println (linuxGitLocation);
	Repository repo = git(fs(linuxGitLocation), "", {});
	list[ChangeSet] resourceChanges = getChanges(repo); 
	println(size(resourceChanges));  // returns 263602 after about 30 secs
	ChangeSet cs = getOneFrom(resourceChanges);
	print(cs);
	// print(resourceChanges);  // Too many elements, it dies
}

public void getSvnExample () {
    Repository repo = svn(ssh("svn+ssh://svn.cwi.nl", 
    	"migod",
    	"", 
    	|file:///Users/migod/.ssh/migod_rascal_rsa|), "", 
    	|file:///Users/migod/Rascal/svnWorkingCopy|,
    	{fileDetails()});
	// This seems to work, the number of changes reported is the same 
	// as SVN reports withing Eclipse.
	list[ChangeSet] resourceChanges = getChanges(repo);
	println ("\nNumber of resource changes: <size(resourceChanges)>");
	// print (resourceChanges);
	migodChanges = {cs | cs <- resourceChanges, cs.committer.name=="migod"};
	ChangeSet cs = getOneFrom(migodChanges);
	print (cs);
}

public void getSvnExample2 () {
    Repository repo = svn(
    	ssh("svn+ssh://sulphur.cs.uvic.ca/var/svn/papersJulius/2011/msrJese2011paper", 
    	"migod", 
    	"", 
    	|file:///Users/migod/.ssh/migod_rascal_rsa|), "", 
    	|file:///Users/migod/Rascal/svnWorkingCopy|,
    	{
    	fileDetails()
    	//,
    	// mergeDetails()
    	});
	list[ChangeSet] resourceChanges = getChanges(repo);
	println ("\nNumber of resource changes: <size(resourceChanges)>");
	// print (resourceChanges);
	migodChanges = {cs | cs <- resourceChanges, cs.committer.name=="migod"};
	// ChangeSet cs = getOneFrom(migodChanges);
	ChangeSet cs = getOneFrom(resourceChanges);
	
	print (cs);
	// print (migodChanges);
}

// This doesn't work yet!
public void getCvsExample () {
	// Probably I am not using the right parameters here.
    // pserver(str url, str repname, str host, str username, str password);
    // Connection cvsConnection = pserver("tortoisecvs.cvs.sourceforge.net/TortoiseCVS", "TortoiseCVS",
    //    "tortoisecvs.cvs.sourceforge.net", "anonymous", " ");
       	
    // data Connection = pserver(str url, str host, str username, str password);
    Connection cvsConnection = pserver(
    	"tortoisecvs.cvs.sourceforge.net:/cvsroot/tortoisecvs",
    	"tortoisecvs.cvs.sourceforge.net",
       	"anonymous", 
       	"");
    Repository repo = cvs(cvsConnection, 
    	"TortoiseCVS", 
    	|file:///Users/migod/Rascal/cvsWorkingCopy|,
    	{});
    list[ChangeSet] resourceChanges = getChanges(repo);
    // This returns zero, but shouldn't
    println ("\nNumber of resource changes: <size(resourceChanges)>");
}

// This doesn't work yet!
public void getCvsExample2 () {
	// data Connection = pserver(str url, str host, str username, str password);
    Connection cvsConnection = pserver(
    	"pserver.samba.org/cvsroot",
    	"pserver.samba.org",
    	"cvs", 
    	"cvs");
    Repository repo = cvs(
    	cvsConnection, 
    	"SambaCVS", 
    	|file:///Users/migod/Rascal/cvsWorkingCopy|,
    	{});
    list[ChangeSet] resourceChanges = getChanges(repo);
    // This returns zero, but shouldn't
    println ("\nNumber of resource changes: <size(resourceChanges)>");
}

public void main () {
}
