module GitLinuxStatsExample

import Utilities;
import Statistics;
import experiments::scm::Scm;
import experiments::scm::git::Git;

import DateTime;
import List;
import Map;
import Set;
import Relation;
import Graph;
import String;
import Real;
import Node;
import IO;

// str gitLoc = "/export/scratch1/shabazi/linux-2.6"
str gitLoc = "/Users/migod/Desktop/linux-2.6";

public tuple[list[int], InitVars, MappingVars] gitStats() {
    gitConfig = getGitConfig();
    initVars = <gitConfig.releases, gitConfig.repo, gitConfig.catDirs, 
	     getChanges(gitConfig.repo)>;
    MappingVars maps = getMappings(initVars, ());
    return <stats(initVars, maps), initVars, maps>;
}

public tuple[list[Tag] releases, Repository repo, rel[str cat, str dir] catDirs]
		getGitConfig() {
    repo = git(fs(gitLoc), "", {});	
    // releases = [label("v2.6.<i>") | i <- [12..21]];
    releases = [label("v2.6.<i>") | i <- [12..14]];
    

    rootDir = repo.conn.url;
    rel[str cat, str dir] catDirs = {};
    catDirs += {<"core", "<rootDir>/<d>"> | d <- ["init", "block", "ipc", 
		"kernel", "lib", "mm", "include/linux", "include/keys"]};
    // commented out by migod
    // catDirs += {<"drivers", "<rootDir>/<d>"> | d <- ["crypto", "drivers", 
		// "sound", "security", "include/acpi", "include/crypto", 
		// "include/media", "include/mtd", "include/pcmcia", "include/rdma", 
		// "include/rxrpc", "include/scsi", "include/sound", "include/video"]};
    // catDirs += {<"architecture", "<rootDir>/<d>"> 
		// | d <- ["arch", "include/asm-", "include/math-emu", "include/x"]};
    // catDirs += {<"network", "<rootDir>/<d>"> | d <- ["net", "include/net"]};
    // catDirs += {<"filesystems", "<rootDir>/<d>"> | d <- ["fs"]};
    // catDirs += {<"miscellaneous", "<rootDir>/<d>"> | 
		// d <- ["Documentation", "scripts", "usr"]};

    return <releases, repo, catDirs>;
}

// add by migod
public void main () {
	println ("hi mom");
	tuple[list[int], InitVars, MappingVars] temp = gitStats();
	println ("hi again mom");
	// t = getOneFrom(temp);
	// println (t);
}