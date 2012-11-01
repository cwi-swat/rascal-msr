module experiments::SvnStatsExample

import Utilities;
import Statistics;
import resource::versions::Versions;
import resource::versions::svn::Svn;

import DateTime;
import List;
import Map;
import Set;
import Relation;
import Graph;
import String;
import Real;
import Node;

public tuple[list[int], InitVars, MappingVars] svnStats() {
    svnConfig = getSvnConfig();
    initVars = <svnConfig.releases, svnConfig.repo, svnConfig.catDirs, 
	     getChanges(svnConfig.repo)>;
    MappingVars maps = getMappings(initVars, svnConfig.manualReleases);
    return <stats(initVars, maps), initVars, maps>;
}

public tuple[list[Tag] releases, Repository repo, rel[str cat, str dir] catDirs, 
       map[Tag version, CheckoutUnit cunit] manualReleases] 
       getSvnConfig() {
       
    manualReleases = (label("0.7.3"):cunit(revision(id(4941))),
	    label("0.8"):cunit(revision(id(5585))),
	    label("0.8.1"):cunit(revision(id(5796))),
	    label("1.0"):cunit(revision(id(6117))),
	    label("1.0.1"):cunit(revision(id(6161))),
	    label("1.1"):cunit(revision(id(6576))),
	    label("1.1.1"):cunit(revision(id(6829))),
	    label("1.2"):cunit(revision(id(7411))),
	    label("1.3"):cunit(revision(id(8409))),
	    label("1.4"):cunit(revision(id(10293))),
	    label("1.4.1"):cunit(revision(id(10382))),
	    label("1.5"):cunit(revision(id(13578))),
	    label("1.5.1"):cunit(revision(id(13798))),
	    label("1.5.2"):cunit(revision(id(15255))),
	    label("1.5.3"):cunit(revision(id(15342))),
	    label("2.0-RC1"):cunit(revision(id(18601))),
	    label("2.0-RC2"):cunit(revision(id(21581))),
	    label("2.0-RC3"):cunit(revision(id(23699))),
	    label("2.0.1"):cunit(revision(id(26447))),
	    label("2.0.3"):cunit(revision(id(27005))));
	    
    repo = svn(ssh("svn+ssh://svn.cwi.nl", "waruzjan", "", 
	    |file:///ufs/shahbazi/.ssh/wshahbazian_rascal_rsa|), "",
	    |file:///export/scratch1/shabazi/svnWorkingCopy|,{});
    releases = quickSort(domain(manualReleases));
    
    rootDir = repo.workspace.path;
    rel[str cat, str dir] catDirs = {};
    catDirs += {<"aterm-c", "<rootDir>/<d>"> | d <- ["aterm"]};
    catDirs += {<"aterm-java", "<rootDir>/<d>"> | d <- ["aterm-java", "shared-objects", "JJTraveler"]};
    catDirs += {<"toolbus", "<rootDir>/<d>"> | d <- ["toolbus", "toolbuslib"]};
    catDirs += {<"toolbus-ng", "<rootDir>/<d>"> | d <- ["toolbus-ng", "aterm-java"]};
    
    return <releases, repo, catDirs, manualReleases>;
}
