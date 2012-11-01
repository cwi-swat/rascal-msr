module experiments::Main

import IO;
import List;
import Relation;
import Set;
import Map;
import DateTime;
import ValueIO;
import Node;
import String;

import resource::versions::Versions;
import resource::versions::git::Git;
import resource::versions::Timer;

import experiments::LinuxKernel;

public bool md = true;

str linuxVersion = "2.6";
str gitRepoLoc = "/Users/migod/Desktop/linux-" + linuxVersion;
// str gitRepoLoc = "/export/scratch1/shabazi/linux-" + linuxVersion;


public void statsOne(list[ChangeSet] changesets) {
    print("started at <startTimer()>");
    map[Tag version, ChangeSet cs] versionCs 
	= (t : cs | cs <- changesets, t <- (cs.revision@tags ? {}));
    releases = [label("v2.6.<i>") | i <- [12..21]];
    
    for (i <- [1 .. size(releases)-1]) {
	    prev = releases[i-1];
	    current = releases[i];
	    print("<current.name> & <daysDiff(versionCs[prev].committer.date, versionCs[current].committer.date)> \\");
    }
    print("duration <stopTimer()> ms");
}

//versionRevisions = getVersionRevisions(tagChangesets, versions, childParents);
public void statsTwo(rel[Tag version, RevisionId revision] versionRevisions, 
	list[Tag] releases) {
    for (i <- [1 .. size(releases)-1]) {
	prev = releases[i-1];
	current = releases[i];
	print("<current> & <size(versionRevisions[current] - versionRevisions[prev])> \\\\");
    }
}

//Repository gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", 
//{fileDetails(), mergeDetails(), startUnit(cunit(label("v2.6.12"))), endUnit(cunit(label("v2.6.21")))});
public void statsThree(Repository repo, list[Tag] versions) {
    print("started at <startTimer()>");
    for (version <- versions) {
	checkoutResources(cunit(version), repo);
	wcResources = getResources(repo);
	map[Resource file, int lines] fileLines 
	    = linesCount(wcResources.resources);
	int totalLines = 0;
	for(f <- fileLines.file) {
	    totalLines += fileLines[f];
	}
	print("<version.name> & <size(fileLines.file)> & <totalLines> \\\\");
    }
    print("duration <stopTimer()> ms");
}

//checkoutResources(cunit(label("v2.6.21")), gitRepo);
public void statsFour(Repository repo, set[Resource resource] resources) {
    print("started at <startTimer()>");
    str rootDir = repo.conn.url;
    rel[str cat, str dir] catDirs = {};
    catDirs += {<"core", "<rootDir>/<d>"> | 
	d <- ["init", "block", "ipc", "kernel", "lib", "mm", 
	       "include/linux", "include/keys"]};
    catDirs += {<"drivers", "<rootDir>/<d>"> | d <- ["crypto", "drivers", 
	"sound", "security", "include/acpi", "include/crypto", 
	"include/media", "include/mtd", "include/pcmcia", "include/rdma", 
	"include/rxrpc", "include/scsi", "include/sound", "include/video"]};
    catDirs += {<"architecture", "<rootDir>/<d>"> | d <- ["arch", 
	"include/asm-", "include/math-emu", "include/x"]};
    catDirs += {<"network", "<rootDir>/<d>"> | d <- ["net", "include/net"]};
    catDirs += {<"filesystems", "<rootDir>/<d>"> | d <- ["fs"]};
    catDirs += {<"miscellaneous", "<rootDir>/<d>"> | d <- ["Documentation", 
	"scripts", "usr"]};
    
    rel[str cat, Resource file] filesByCat 
	= resourcesByCategory(resources, catDirs<1,0>);
    print("Category & Files & \\% of kernel\\\\");
    int totalFiles = size(filesByCat.file);
    for (c <- filesByCat.cat) {		
	print("<c> & <size(filesByCat[c])> & <size(filesByCat[c])*100/totalFiles>\\% \\\\");
    }
    print("\n \n");
    print("Category & Lines of Code & \\% of kernel\\\\");
    map[Resource file, int lines] fileLines = linesCount(filesByCat.file);
    int totalLines = 0;
    for(f <- fileLines.file) {
	totalLines += fileLines[f];
    }
    for (c <- filesByCat.cat) {
	int catLines = 0;
	for (f <- filesByCat[c]) {
	    catLines += fileLines[f];
	}
	print("<c> & <catLines> & <catLines*100/totalLines>\\% \\\\");
    }
    print("duration <stopTimer()> ms");
}
//childParents = {<cs.revision.id, m.parent.id> | cs <-changesets, revision(_, _) := cs.revision, m <- (cs.revision@mergeDetails ? {mergeParent(cs.revision.parent)})};
//versionRevisions = getVersionRevisions(tagChangesets, versions, childParents);
//

public void statsFive(rel[Tag version, ChangeSet cs] versionChangesets, 
	map[str, str] domainMap, list[Tag] releases) {
	
    rel[Tag version, str devverName, str email, ChangeSet cs] result = {};
    print("started parsing authors at <startTimer()>");
    for (version <- releases, version in versionChangesets.version) {
	result += calcDevelopers(version, "Author", 
		{<cs@author, cs>| cs <- versionChangesets[version]}, 
		false) <0,2,3,4>;
    }
    print("duration parsing <stopTimer()> ms");
    print("started calculating dev statistics at <startTimer()>");
    for (version <- releases, version in result.version) {
	map[set[str name] user, set[ChangeSet] cs] userChanges 
	    = getUserChangeSets(result[version]);
	map[set[str name] user, int count] userChangesCount 
	    = (usr : size(userChanges[usr]) | usr <- domain(userChanges));
	print("\n<version.name> & <size(userChanges.user)>");
	printMapOrderedOnRange(userChangesCount, 10);
    }
    print("duration calculating dev statistics <stopTimer()> ms");
    print("started calculating company statistics at <startTimer()>");
    map[str mail, str company] emailCompany = (addr : domainMap[dom] | 
	    dom <- domain(domainMap), 
	    addr <- result.email, 
	    endsWith(addr, dom) );
    rel[Tag version, str company, ChangeSet cs] versionCompanyChangesets 
	= {<version, emailCompany[email] ? "Unknown", cs> | 
	    <Tag version, str devverName, str email, ChangeSet cs> <- result};
    for (version <- releases, version in versionCompanyChangesets.version) {
	rel[str company, ChangeSet cs] companyChangesets 
	    = versionCompanyChangesets[version];
	map[str company, int changes] companyChangesCount 
	    = (company : size(companyChangesets[company]) | 
		    company <- domain(companyChangesets));
	print("\n<version.name> & <size(companyChangesets.company)>");
	printMapOrderedOnRange(companyChangesCount, 10);
    }
    print("duration calculating company statistics <stopTimer()> ms");
}

public void main() {
    println ("Starting Main::main()");
    Repository gitRepo = git(fs(gitRepoLoc), "",     
	    {fileDetails(), mergeDetails(), 
	    startUnit(cunit(label("v2.6.12"))), 
	    endUnit(cunit(label("v2.6.21")))});
    list[ChangeSet] changesets = getChangesets(gitRepo);
    
    childParents = {<cs.revision.id, m.parent.id> | 
	    cs <-changesets, revision(_, _) := cs.revision, 
	    m<-(cs.revision@mergeDetails ? {mergeParent(cs.revision.parent)})};
    releases = [label("v2.6.<i>") | i <- [12..21]];
    map[Tag version, ChangeSet changeset] tagChangesets 
	    = (t : cs | cs <- changesets, t <- (cs.revision@tags ? {}));
    map[RevisionId, ChangeSet] revChangesets 
	    = (cs.revision.id : cs |cs <- changesets); 
    rel[Tag version, RevisionId revId] versionRevisions 
	    = getVersionRevisions(tagChangesets, releases, childParents); 
	//duration 233242 ms
    versionRevisions = getUniqueRevisions(versionRevisions, releases);
    rel[Tag version, ChangeSet revId] versionChangesets 
	    = versionRevisions o toRel(revChangesets);
    
    rel[Tag version, ChangeSet cs] versionNoMergesChangesets 
	    = {<version, cs> | version <- versionChangesets.version, 
	    cs <- versionChangesets[version], 
	    "mergeDetails" notin getAnnotations(cs.revision)};
    
    rel[Tag version, Info auth, ChangeSet cs] input 
	    = {<version, cs@author, cs> | 
	    version <- versionNoMergesChangesets.version, 
	    cs <- versionNoMergesChangesets[version]};

	//print(size(getBetweenTags(cunit(label("v2.6.12")), cunit(label("v2.6.13")))));
	//getTagReleases();
    //print("hehe");
    //print(size(writeLinuxKernel()));
    //print(size(getLinuxKernel()));
    //print("hoi");
    
    //getSaveTagReleases();
    //mergeDetailsTest();
     //Repository gitRepo = git(fs("/export/scratch1/shabazi/gitrepo6"), "", cunit(decrementYears(now(), 10)), cunit(now()));
     //list[ChangeSet] changesets = [];
     //int cntr=0;
     //startExtractingRevisions(gitRepo, ChangeSet (ChangeSet cs) { changesets += [cs]; cntr +=1; if (cntr % 5 == 0) print(cntr); return cs;  });
    
    //revChangesets = (r : c | c <- changesets);
	//tagRevisions = (t : r | r <- domain(revChangesets), t <- (r@tags ? {}));
	
	
	//Repository gitRepo = git(fs("/ufs/shahbazi/Documents/gmeta/testco"), "" ,cunit(decrementYears(now(), 10)), cunit(now()));
	/*
	Repository gitRepo = git(fs("/ufs/shahbazi/Documents/gmeta/testco"), "" , cunit(label("v2.6.17-rc2")), cunit(label("v2.6.34-rc7")));
	
	list[ChangeSet] changesets = [];
	startExtractingRevisions(gitRepo, ChangeSet (ChangeSet cs) {  changesets += [cs]; return cs;  });
	
	for (cs <- changesets) print("<cs.revision> - <(cs.revision@mergeParents ? {})>");
	*/
	
	/*
	firsCommit = revision(hash(commit("06ef66081620e94fe35a518f98624b83a140096e")),revision(hash(commit("fc67b16ecaf6ebde04096030c268adddade023f1"))));
	lastCommit = revision(hash(commit("02b3e4e2d71b6058ec11cc01c72ac651eb3ded2b")),revision(hash(commit("20b1730af3ae05450b0e03f5aed40c4313f65db6"))))[@tags={label("v2.6.13")}];
	
	Repository gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "" ,cunit(decrementYears(now(), 10)), cunit(now()));
	checkoutResources(cunit(firsCommit), gitRepo);
	*/
}

public list[ChangeSet] mergeDetailsTest() {
    gitRepo = git(fs(gitRepoLoc), "", {reverse()});
    list[ChangeSet] changesets = getChangesets(gitRepo);
    for (cs <- changesets ) {
	print("<cs.revision> - <cs.revision@mergeDetails ? {}>");
    }
    return changesets;
}

public void findTheCommonChild (rel[RevisionId, RevisionId] childParents, 
	set[RevisionId] v15diff, set[RevisionId] missing) {
	// solve(r) {
	//}
}

public void calcCurrentFiles(map[RevisionId, ChangeSet] revChangesets, 
	RevisionId rev) {
    ChangeSet changeset = revChangesets[rev];
    map[Resource, int] resourceLines = ();
    set[Resource] deleted = {};
    
    for (Resource res <- changeset.resources.resource) {
	print("<res>");
	for (change <- changeset.resources[res]) {
	    print("<changeset.resources[res]>");
	    switch(change) {
		case added(Revision rev): {
		    if ((change@linesAdded ? -1) > 0){
			print("added: <rev> <change@linesAdded>");
		    }
		}
		case renamed(Revision rev): {
		    print("renamed: <rev>");
		}
		case copied(Revision rev): {
		    print("copied: <rev>");
		}
		case removed(Revision rev): {
		    print("removed: <rev>");
		}
		case modified(Revision rev): {
		    print("modified: <rev>");
		}
		case replaced(Revision rev): {
		    print("replaced: <rev>");
		}
	    }
	}
    }
}

public set[RevisionId] findCommonChild(set[RevisionId] searchingParents, 
	rel[RevisionId, RevisionId] childParents, set[RevisionId] v15diff) {
//	hash(commit("654b1536b0927d189526b9063818e0790aa3ea23"))
    rel[RevisionId child, RevisionId parent] childs 
	= rangeR(childParents, searchingParents);
    set[RevisionId] knownChilds = domain(childs) & v15diff;
    
    if (size(knownChilds) > 0) {
	knownChildParents = rangeX(childParents, knownChilds);
	//print(knownChilds);
	return knownChilds;
    } else {
	//reslt = 
	return findCommonChild(domain(childs), childParents, v15diff);
    }
    
    //if (size(common) > 0) {
    //	print("Found <common>");
    //}
    // else {
    //	findCommonChild(parents, childParents, v15diff);
    //}
}

public list[RevisionId] searchMissing(RevisionId search, 
	rel[RevisionId, RevisionId] parentChilds, set[RevisionId] missing, 
	set[RevisionId] v15diff) {
    set[RevisionId] childs = parentChilds[search];
    list[RevisionId] found = [];
    for (ch <- childs) {
	if (ch in missing, size(parentChilds[ch]) > 0) {
	    found += searchMissing(ch, parentChilds, missing);
	} else {
	    print(ch);
	}
    }
    return found;
}

public void getSaveTagReleases() {
    getBetweenTags(cunit(label("v2.6.12")), cunit(label("v2.6.13")));
    getBetweenTags(cunit(label("v2.6.13")), cunit(label("v2.6.14")));
    getBetweenTags(cunit(label("v2.6.14")), cunit(label("v2.6.15")));
    getBetweenTags(cunit(label("v2.6.15")), cunit(label("v2.6.16")));
    getBetweenTags(cunit(label("v2.6.16")), cunit(label("v2.6.17")));
    getBetweenTags(cunit(label("v2.6.17")), cunit(label("v2.6.18")));
    getBetweenTags(cunit(label("v2.6.19")), cunit(label("v2.6.20")));
    getBetweenTags(cunit(label("v2.6.20")), cunit(label("v2.6.21")));
}

public void getTagReleases() {
    //list[ChangeSet] v2_6_12 = getBetweenTags(cunit(label("v2.6.11")), cunit(label("v2.6.12")));
    list[ChangeSet] v2_6_13 = getBetweenTags(cunit(label("v2.6.12")), cunit(label("v2.6.13")));
    list[ChangeSet] v2_6_14 = getBetweenTags(cunit(label("v2.6.13")), cunit(label("v2.6.14")));
    list[ChangeSet] v2_6_15 = getBetweenTags(cunit(label("v2.6.14")), cunit(label("v2.6.15")));
    list[ChangeSet] v2_6_16 = getBetweenTags(cunit(label("v2.6.15")), cunit(label("v2.6.16")));
    list[ChangeSet] v2_6_17 = getBetweenTags(cunit(label("v2.6.16")), cunit(label("v2.6.17")));
    list[ChangeSet] v2_6_18 = getBetweenTags(cunit(label("v2.6.17")), cunit(label("v2.6.18")));
    list[ChangeSet] v2_6_19 = getBetweenTags(cunit(label("v2.6.18")), cunit(label("v2.6.19")));
    list[ChangeSet] v2_6_20 = getBetweenTags(cunit(label("v2.6.19")), cunit(label("v2.6.20")));
    list[ChangeSet] v2_6_21 = getBetweenTags(cunit(label("v2.6.20")), cunit(label("v2.6.21")));
}

public list[ChangeSet] getBetweenTags(CheckoutUnit from, CheckoutUnit to) {
    logOpt = {symdiff(from, to), fileDetails(), reverse()};
    str pt = "/export/scratch1/shabazi/logs/ranges/";
    if (md) {
	logOpt += {mergeDetails()};
	pt += "md/";
	print(logOpt);
    }
    
    Repository gitRepo = git(fs(gitRepoLoc), "", logOpt);
    // gitRepo = git(fs("/ufs/shahbazi/Documents/gmeta/testco"), "", from, to);
    map[int, set[Tag]] tags = ();
    int cntr=0;
    list[ChangeSet] changesets = [];
    
    startMeUp = startTimer();
    print("started <from> to <to> at <startMeUp>");
    
    //changesets = getChangesets(gitRepo);
    getChangesets(gitRepo, ChangeSet (ChangeSet cs) { 
	changesets += [cs]; 
	cntr +=1; 
	if (cntr % 500 == 0) {
	    print("<cntr> commits processed");
	}
	if (size(cs.revision@tags ? {}) > 0) {
	    tags[cntr] = cs.revision@tags;
	    print("<cntr> commits processed until <cs.revision@tags>");
	    loc l = |file://<pt>linuxHistory<to.symname.name><cntr>.bin|;
	    writeBinaryValueFile(l, changesets);
	    writeBinaryValueFile(|file://<pt>linuxHistory<to.symname.name>Index.bin|, tags);
	    print("<now()>) commits are saved at <l>");
	    changesets = [];
	}
	return cs;  
    });
    
    end = stopTimer();
    print("duration <end> ms");
    
    writeBinaryValueFile(|file://<pt>linuxHistory<to.symname.name>.bin|, changesets);
    
    return changesets;
}

public set[RevisionId] getParentsRevisionsIds(set[RevisionId] parents, 
	map[RevisionId child, list[RevisionId] parents] childParents, 
	set[RevisionId] result, map[RevisionId, set[Tag]] cst) { 
	
    set[RevisionId] grands = {p |RevisionId parent <- parents, 
	p <- [grand |RevisionId grand <- childParents[parent]]};
    print("<[ cst[p] ? label("root") 
	    | p <- parents]> \t <[ cst[p] ? label("root") | p <- grands]> - ");
    if (size(grands) > 0) {
	return getParentsRevisionsIds(grands,childParents,parents+result, cst);
    } else {
	return parents + result;
    }
}


public set[RevisionId] getParentsRevisionsIds(set[RevisionId] parents, 
	map[RevisionId child, list[RevisionId] parents] childParents, 
	map[RevisionId, set[Tag]] cst) { 
	
    set[RevisionId] grands = {p |RevisionId parent <- parents, 
	    p <- [grand |RevisionId grand <- childParents[parent]]};
    print("<[ cst[p] ? label("root") 
	    | p <- parents]> \t <[ cst[p] ? label("root") | p <- grands]> - ");
    if (size(grands) > 0) {
	return parents + getParentsRevisionsIds(grands, childParents, cst);
    } else {
	return parents;
    }
}

public list[RevisionId] getParentsRevisionsIds(list[RevisionId] parents, 
	map[RevisionId child, list[RevisionId] parents] childParents, 
	map[RevisionId, set[Tag]] cst, set[RevisionId] rslt, int max) { 
    //print("<max>");
    set[RevisionId] grands = {p |RevisionId parent <- parents, 
	    p <- [grand |RevisionId grand <- (childParents[parent] ? {})]};
    //print("<[ cst[p] ? label("root") | p <- parents]> \t <[ cst[p] ? label("root") | p <- grands]> - ");
    if (size(grands) > 0 && max > 0) {
	return parents + getParentsRevisionsIds(toList(grands - rslt), 
		childParents, cst, rslt+grands, max-1);
    } else {
	return parents;
    }
}

public set[RevisionId] getParentsRevisions(set[RevisionId] childIds, 
	rel[RevisionId child, RevisionId parent] childParents,
	set[RevisionId] ancestors, int max) {
	
    set[RevisionId] parents = {p | RevisionId child <- childIds, 
	    p <- (childParents[child] ? {})};

    if (size(parents) > 0 && max > 0) {
	return childIds + getParentsRevisions(parents - ancestors, 
		childParents, ancestors + parents, max-1);
    } 
    
    //else {
    //	print(childIds);
	return childIds;
    //}
}

public set[RevisionId] getParentsRevisionIds(set[RevisionId] childIds, 
	map[RevisionId child, list[RevisionId] parents] childParents,
	set[RevisionId] ancestors, int max) {
	
    set[RevisionId] parents = {p |RevisionId child <- childIds, 
	    p <- [parent | RevisionId parent <- (childParents[child] ? {})]};
    if (hash(commit("12725675e26d52c39e856d341035b94bf7802458")) in childIds) {
	print("<size(childIds)> childs have <size(parents)> parents, 
	right parent: <hash(commit("af6f5e3247a68074e384ef93c0b4bce1b73c9d80"))
		in parents>"); 
    } 
    if (size(parents) > 0 && max > 0) {
	return childIds + getParentsRevisionIds(parents - ancestors, 
		childParents, ancestors + parents, max-1);
    } else {
	return childIds;
    }
}

public rel[RevisionId child, RevisionId parents] solveParentsRevisionIds(rel[RevisionId child, RevisionId parents] childParents, RevisionId child) {
    startMeUp = startTimer();
    print("started at <startMeUp>");
    r = domainR(childParents, {child});
    //int maxSize = 0;
    //int i = 0;
    solve(r) {
	r += r o childParents;
	//i += 1;
	//print("<i>) <size(r)>");
	//if (i > maxSize) {
	//	maxSize = i;
	//}
    }
    end = stopTimer();
    print("duration <end> ms");
    return r;
}

public list[RevisionId] getParentsRevisionsIds(RevisionId child, 
	map[RevisionId child, list[RevisionId] parents] childParents, 
	list[RevisionId] result, map[RevisionId, set[Tag]] cst) { 
	
    parentIds = result;
    list[RevisionId] prnts = childParents[child];
    if (size(prnts) > 0 && prnts[0] in cst) {
	parentIds += getParentsRevisionsIds(prnts[0], childParents, 
		parentIds, cst);
    }
    print("<cst[child] ? label("root")> has <size(prnts)> <[ cst[p] 
	    ? label("root") | p <- prnts]>");
    return [child] + parentIds;
}


public list[RevisionId] getParentsRevisionIds(RevisionId child, 
	map[RevisionId child, list[RevisionId] parents] childParents, 
	list[RevisionId] result, map[RevisionId, set[Tag]] cst) { 
    if (child in result) {
	print("!!<cst[child] ? label("root")>");
    }
    parentIds = result + [child];
    list[RevisionId] prnts = childParents[child];
    print("<cst[child] ? label("root")> had <size(prnts)> <[ p | p <- prnts]>");
    for (p <- prnts, p in childParents, p notin result) {
	parentIds += getParentsRevisionIds(p, childParents, parentIds, cst);
    }
    print("\t\t<cst[child] ? label("root")> 
	    has <size(parentIds-result)> <[ cst[p] ? label("root") 
	    | p <- parentIds-result]>");
    return parentIds;
}

public set[RevisionId] getParentRevisions(RevisionId id, 
	rel[RevisionId child, RevisionId parent] childParents) {
    parents = {id};
    for (p <- childParents[id]) {
	parents += getParentRevisions(p, childParents);
    }
	
    return parents;
} 

public set[RevisionId] resultedIn(RevisionId commit, 
	rel[RevisionId child, RevisionId parent] childParents) {
	set[RevisionId] result = {commit};
    print("Commit:  <commit> -- <size(childParents[commit])>");
    for (RevisionId parent <- childParents[commit]) {
	if (parent in result) {
	    print("<parent> already in list!");
	    return result;
	}
	result += resultedIn(parent, childParents);
    }
    return result;
}

public list[ChangeSet] timeLinuxKernel() {
    startMeUp = startTimer();
    print("started at <startMeUp>");
    
    changesets = getLinuxKernel();
    
    end = stopTimer();
    print("duration <end> ms");
    return changesets;
}



public list[ChangeSet] getLinuxKernel() {
	/*Repository gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", cunit(decrementYears(now(), 10)), cunit(now()));
	//gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", cunit(label("v2.6.14")), cunit(label("v2.6.16")));
	//gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", cunit(createDateTime(2005, 10, 27, 17, 0, 0, 0)), cunit(createDateTime(2006, 3, 19, 21, 54, 0, 0)));
	//gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", cunit(createDateTime(2005, 10, 27, 17, 0, 0, 0)), cunit(createDateTime(2007, 4, 25, 20, 10, 0, 0)));
	//gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", cunit(createDateTime(2005, 8, 28, 16, 41, 0, 0)), cunit(createDateTime(2005, 10, 17, 02, 10, 0, 0)));
	
	//gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", cunit(createDateTime(2005, 6, 17, 00, 00, 0, 0)), cunit(createDateTime(2007, 07, 9, 0, 0, 0, 0)));
	gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", cunit(createDateTime(2005, 6, 17, 00, 00, 0, 0)), cunit(createDateTime(2005, 10, 27, 17, 2, 10, 0)));
	gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", cunit(decrementYears(now(), 10)), cunit(createDateTime(2005, 6, 18, 00, 00, 0, 0)));
	gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", cunit(decrementYears(now(), 10)), cunit(createDateTime(2006, 1, 19, 20, 2, 10, 0)));
	gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", cunit(createDateTime(2006, 1, 19, 20, 2, 10, 0)), cunit(createDateTime(2006, 6, 17, 18, 50, 10, 0)));
	gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", cunit(createDateTime(2006, 6, 17, 18, 50, 10, 0)), cunit(createDateTime(2006, 7, 8, 16, 33, 10, 0)));
	
	gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", cunit(decrementYears(now(), 10)), cunit(createDateTime(2005, 10, 27, 17, 2, 8, 0, -7, 0)));
	
	gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "",  cunit(createDateTime(2005, 10, 27, 17, 2, 8, 0, -7, 0)), cunit(createDateTime(2006, 3, 19, 21, 53, 29, 0, -8, 0)));
	
	//from start till 21: Wed Apr 25 20:08:32 2007 -0700
	gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", cunit(decrementYears(now(), 10)), cunit(createDateTime(2007, 4, 25, 20, 8, 32, 0, -7, 0)));
	
	gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", cunit(decrementYears(now(), 10)), cunit(now()));*/
	
	//gitRepo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", cunit(decrementYears(now(), 10)), cunit(createDateTime(2007, 07, 9, 0, 0, 0, 0)));
	//loc lgs = |file:///export/scratch1/shabazi/linux-2.6/v12.txt|;
	
    Repository gitRepo = git(fs(gitRepoLoc), "", {fileDetails(), mergeDetails(), reverse()});
    map[int, set[Tag]] tags = ();
    list[ChangeSet] changesets = [];
    //startExtractingLogsFromFile(gitRepo, lgs, ChangeSet (ChangeSet cs) {  changesets += [cs]; return cs;  });
    int cntr=0;
    getChangesets(gitRepo, ChangeSet (ChangeSet cs) {
	changesets += [cs];
	cntr +=1; 
	if (cntr % 500 == 0) {
	    print("<cntr> commits processed");
	}
	if (size(cs.revision@tags ? {}) > 0) {
	    tags[cntr] = cs.revision@tags;
	    print("<cntr> commits processed until <cs.revision@tags>");
	    loc l = |file:///export/scratch1/shabazi/logs/partial/linuxHistory<cntr>.bin|;
	    writeBinaryValueFile(l, changesets);
	    writeBinaryValueFile(|file:///export/scratch1/shabazi/logs/partial/linuxHistoryIndex.bin|, tags);
	    print("<now()>) commits are saved at <l>");
	    changesets = [];
	}
	return cs;
    });
    //if (size(cs.revision@tags ? {}) == 0) { //the rest
    writeBinaryValueFile(|file:///export/scratch1/shabazi/logs/partial/linuxHistoryLast.bin|, changesets);
    
    getSaveTagReleases();
    md = false;
    getSaveTagReleases();
    
    
    //}
    list[ChangeSet] allChangesets = [];
    for (i <- domain(tags)) {
	allChangesets += readBinaryValueFile(#(list[ChangeSet]), 
	    |file:///export/scratch1/shabazi/logs/partial/linuxHistory<i>.bin|);
    }
    allChangesets += changesets;
    return allChangesets;
}

public list[ChangeSet] readLinuxKernel(str directory, str prefix, 
	Tag startMeUp, Tag end) {
    list[ChangeSet] changesets = [];
    print("started at <startTimer()>");
    
    map[int, set[Tag]] index = readBinaryValueFile(#(map[int, set[Tag]]), |file://<directory><prefix>Index.bin|);
    list[int] indexCounter = quickSort(toList(domain(index)));
    int startIndex = 0;
    int endIndex = max(indexCounter);
    for (i <- indexCounter) {
	if (startMeUp in index[i]) {
	    startIndex = i;
	} else if (end in index[i]) {
	    endIndex = i;
	}
	if (i > startIndex && i <= endIndex) {
	    changesets += readBinaryValueFile(#(list[ChangeSet]), 
		    |file://<directory><prefix><i>.bin|);
	    print("Read <i> size: <size(changesets)>");
	}
    }
    print("duration <stopTimer()> ms");
    return changesets;
}

private list[ChangeSet] readLinxKernel(int cntr) {
    print("<now()>) Reading <cntr>");
    return readBinaryValueFile(#(list[ChangeSet]), 
	    |file:///export/scratch1/shabazi/logs/linuxHistory<cntr>.bin|);
}

public list[ChangeSet] readAndComposeLinuxKernel() {
    
    list[ChangeSet] changesets = [];
    
    startMeUp = startTimer();
    print("started at <startMeUp>");
    
    changesets += readLinxKernel(10000);
    changesets += readLinxKernel(20000);
    changesets += readLinxKernel(30000);
    changesets += readLinxKernel(40000);
    changesets += readLinxKernel(50000);
    changesets += readLinxKernel(52297);
    
    end = stopTimer();
    print("duration <end> ms");
    
    return changesets;
}

public list[ChangeSet] writeLinuxKernel() {
    changesets = timeLinuxKernel();
    writeBinaryValueFile(
	|file:///export/scratch1/shabazi/logs/linuxHistory.bin|, changesets);
    return changesets;
}

public tuple[list[ChangeSet],list[ChangeSet], list[ChangeSet]] readLinuxKernel() {
    startMeUp = startTimer();
    print("<startMeUp>) Reading till 15");
    
    changes15 = readBinaryValueFile(#(list[ChangeSet]), 
	    |file:///export/scratch1/shabazi/logs/linuxHistoryTill.15.bin|);
    print("<now()>) Reading till 17");
    changes17 = readBinaryValueFile(#(list[ChangeSet]), 
	    |file:///export/scratch1/shabazi/logs/linuxHistoryTill.17.bin|);
    print("<now()>) Reading till 22");
    changes22 = readBinaryValueFile(#(list[ChangeSet]), 
	    |file:///export/scratch1/shabazi/logs/linuxHistoryTill.22.bin|);
    
    end = stopTimer();
    print("duration <end> ms");
    return <changes15, changes17, changes22>;
} 


public tuple[list[Tag] tagsToProcess, map[Tag, ChangeSet] tagChangesets, 
	list[datetime] dates, rel[datetime, ChangeSet] datesAndChangeSets] 
	splitData(list[ChangeSet] changesets) {
	
    list[Tag] tagsToProcess = [label("tag: refs/tags/v2.6.14"), 
	label("tag: refs/tags/v2.6.15"), label("tag: refs/tags/v2.6.16"), 
	label("tag: refs/tags/v2.6.17"), label("tag: refs/tags/v2.6.18"), 
	label("tag: refs/tags/v2.6.19"), label("tag: refs/tags/v2.6.20"), 
	label("tag: refs/tags/v2.6.21")];
	    
    tagChangesets = (t : cs | cs <- changesets, t <- (cs.revision@tags ? {}));
    //dates = readBinaryValueFile(#(list[datetime]), |file:///export/scratch1/shabazi/logs/dates.bin|);
    
    datesAndChangeSets = {<cs.committer.date, cs> | cs <- changesets};
    return <tagsToProcess, tagChangesets, toList(domain(datesAndChangeSets)), 
	   datesAndChangeSets>;
}

public map[Tag symname, real changes] calculateMetrics(map[Tag symname, 
	list[ChangeSet] changeset] changesets, 
	map[Tag, ChangeSet] tagChangesets) {
	
    tags = quickSort(toList(domain(changesets)));
    
    map[Tag symname, real changes] results = ();
    for (i <- [1 .. size(tags)-1]) {
	prev = tags[i-1];
	current = tags[i];  
	
	duration = hoursDiff(tagChangesets[prev].committer.date, 
		tagChangesets[current].committer.date) + 0.0;
	results[current] = size(changesets[current])/duration;
	
	days = daysDiff(tagChangesets[prev].committer.date, 
		tagChangesets[current].committer.date) + 0.0;
	print("<current> at <tagChangesets[current].committer.date> has <size(changesets[current])> changes in <duration> hours or <days> days, avg: <results[current]>");
    }
    return results;
}

public map[Tag symname, real changes] calculateMetrics(map[Tag symname, 
	list[ChangeSet] changeset] changesets) {
    tags = quickSort(toList(domain(changesets)));
    
    for(t <- tags) {
	changes = changesets[t];
	int s = size(changes);
	
	datetime startMeUp = getOneFrom(changes).committer.date;
	datetime end = startMeUp;
	
	for (cs <- changes) {
	    if (cs.committer.date < startMeUp) {
		startMeUp = cs.committer.date;
	    }
	    if (cs.committer.date > end) {
		end = cs.committer.date;
	    }
	}
	//real days = daysDiff(startMeUp, end) + 0.0; //otherwise the avg calculation will result in an int.
	//duration dur = createDuration(startMeUp, end);

	//otherwise the avg calculation will result in an int.
	real hours = hoursDiff(startMeUp, end) + 0.0;
	print("<t> has <s> changes in <hours> hours");
	print("\t from <startMeUp> untill <end> average of <s/hours> changes");
    }
    
    return ();
}


public map[Tag symname, list[ChangeSet] changeset] calculateMetrics
	(list[Tag] tagsToProcess, map[Tag, ChangeSet] tagChangesets, 
	list[datetime] datesOfChangeSetsSorted,
	rel[datetime, ChangeSet] datesAndChangeSets) {
    bool first = true;
    map[Tag symname, list[ChangeSet] changeset] result = ();
    for (i <- [1 .. size(tagsToProcess)-1]) {
	current = tagsToProcess[i]; 
	prev = tagsToProcess[i-1]; 
	
	startDate = (tagChangesets[prev]@author).date;
	endDate = (tagChangesets[current]@author).date;
	intervalDates = [d | d <- datesOfChangeSetsSorted, 
		d > startDate && d < endDate];
	intervalCommits = domainR(datesAndChangeSets, toSet(intervalDates));
	
	print("<i>) <current> - <size(domain(intervalCommits))> -\> <size(range(intervalCommits))>"); 
	result[current] = toList(range(intervalCommits));
	/*
	if (first) {
	    mDcommitts = [d | d <- domain(intervalCommits), 
		    size(intervalCommits[d]) > 1];
	    print("\t\t <size(domain(mDcommitts))>"); 
	    d = getOneFrom(mDcommitts);
	    cd = intervalCommits[d];
	    print(" \t \t \t <d> -\> <size(cd)> -\> <cd>");
	    first = false;
	    return;
	}*/
    }
    return result;
}

public list[ChangeSet] visitTags(list[ChangeSet] changesets) {
    startMeUp = startTimer();
    changedSets = visit(changesets) {case label(/tag: <name:.*>/) 
	    => label(name)}
    end = stopTimer();
    print("duration <end> ms");
    return changedSets;
}

