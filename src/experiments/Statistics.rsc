module experiments::Statistics

import Utilities;
import ValueIO;
import DateTime;
import IO;
import resource::versions::Versions;
import resource::versions::cvs::Cvs;
import resource::versions::svn::Svn;
import resource::versions::git::Git;
import resource::versions::Timer;
import DateTime;
import List;
import Map;
import Set;
import Relation;
import Graph;
import String;
import Real;
import Node;

public alias InitVars 
    = tuple[list[Tag] releases, Repository repo, 
	rel[str cat, str dir] catDirs,
	list[ChangeSet] changesets];

public alias MappingVars 
    = tuple[rel[RevisionId child, RevisionId parent] childParents, 
	map[Tag version, ChangeSet changeset] tagChangeset, 
	map[RevisionId revId, ChangeSet cs] revChangeset,
	rel[Tag version, RevisionId revId] versionRevisions,
	rel[Tag version, ChangeSet cs] versionNoMergesChangesets];

public list[int] stats(InitVars initVars, MappingVars maps) {
	domainMap = readTextValueFile(#(map[str domain, str company]), 
		|file:///Users/migod/Rascal/domain-map.txt|);
		// |file:///export/scratch1/shabazi/domain-map.txt|);
	list[int] durations = [];
	Tag lastVersion = initVars.releases[0/*size(initVars.releases) - 1*/];
	//list[Tag] checkVersions = [label("1.0"), label("1.1"), label("1.2"), label("1.3"), label("1.4"), label("1.5"), label("1.5.1")];
	//print("------------------STATS 1------------------");
	//durations += statsOne(maps.tagChangeset, initVars.releases);
	//print("------------------STATS 2------------------");
	//durations += statsTwo(maps.versionRevisions, initVars.releases);
	//print("------------------STATS 3------------------");
	////durations += statsThree(initVars.repo, maps.tagChangeset, [lastVersion]/*initVars.releases*/);
	//print("------------------STATS 4------------------");
	////durations += statsFour(initVars.repo, maps.tagChangeset, [lastVersion]/*initVars.releases*/, initVars.catDirs);
	print("------------------STATS 5------------------");
	durations += statsFive(maps.versionNoMergesChangesets, domainMap, [lastVersion]/*initVars.releases*/);
	
	int totalDuration = 0;
	for(d <- durations) {
		totalDuration += d;
	}
	print("Duration:<totalDuration>");
	return durations;
}

public int statsOne(map[Tag version, ChangeSet cs] tagChangeset, 
	list[Tag] releases) {

    printStartTimer("statsOne");
    int i = 0;
    for (version <- releases, version in tagChangeset) { 
		if (i > 0) {
			prev = releases[i-1];
			print("<version.name> - <daysDiff(tagChangeset[prev].committer.date, tagChangeset[version].committer.date)>");
		}
		i += 1;
    }
    return printStopTimer("statsOne");
}

public int statsTwo(rel[Tag version, RevisionId revision] versionRevisions,
	list[Tag] releases) {

    printStartTimer("statsTwo");
    for (version <- releases, version in versionRevisions.version) {
		print("<version> - <size(versionRevisions[version])>");
    }
    return printStopTimer("statsTwo");
}

public int statsThree(Repository repo, map[Tag version, ChangeSet cs]
	tagChangeset, list[Tag] releases) {

    printStartTimer("statsThree");
    for (version <- releases) {
		checkoutVersion(repo, tagChangeset, version);
		set[WcResource] wcResources = getResources(repo);
		set[Resource resource] resources = {r.resource | r <- wcResources};
		map[Resource file, int lines] fileLines = linesCount(resources);
		int totalLines = 0;
		for(f <- fileLines.file) {
	    	totalLines += fileLines[f];
		}
		print("<version.name> - <size(fileLines.file)> - <totalLines>");
    }
    return printStopTimer("statsThree");
}

public void checkoutVersion(Repository repo, map[Tag version, ChangeSet cs]
	tagChangeset, Tag version) {

    CheckoutUnit cu;
    //little workaround for gits lack of checkout by date
    if (git(_,_,_) := repo) {
		cu = cunit(version);
    } else {
		cu = cunit(tagChangeset[version].committer.date);
    }
    checkoutResources(cu, repo);
}

public int statsFour(Repository repo, map[Tag version, ChangeSet cs]
	tagChangeset, list[Tag] releases, rel[str cat, str dir] catDirs) {

    int duration = 0;
    for (version <- releases) {
		printStartTimer("checkout <version>");
		checkoutVersion(repo, tagChangeset, version);
		duration += printRestartTimer("getResources <version>");
		set[WcResource] wcResources = getResources(repo);
		//set[Resource resource] wcResources = getWCResources(repo);
		duration += printStopTimer("getResources <version>");
		set[Resource resource] resources = {r.resource | r <- wcResources};
		duration += statsFour(repo, resources, catDirs);
		print("statsFour - <version> - <duration>ms");
    }
    return duration;
}

public int statsFour(Repository repo, set[Resource resource] resources,
	rel[str cat, str dir] catDirs) {

    int duration = 0;

    printStartTimer("resourcesByCategory");
    rel[str cat, Resource file] filesByCat = resourcesByCategory(resources, 
	    catDirs<1,0>);
    duration += printStopTimer("resourcesByCategory");
    print("Category - Files - % of kernel");
    int totalFiles = size(filesByCat.file);
    for (c <- filesByCat.cat) {		
		print("<c> - <size(filesByCat[c])> - <size(filesByCat[c])*100/totalFiles>% ");
    }
    
    print("Category - Lines of Code - % of kernel");
    printStartTimer("resourcesByCategory");
    map[Resource file, int lines] fileLines = linesCount(filesByCat.file);
    duration += printStopTimer("resourcesByCategory");
    int totalLines = 0;
    for(f <- fileLines.file) {
		totalLines += fileLines[f];
    }
    for (c <- filesByCat.cat) {
		int catLines = 0;
		for (f <- filesByCat[c], file(_) := f) {
			catLines += fileLines[f];
		}
		print("<c> - <catLines> - <catLines*100/totalLines>%");
    }
    return duration;
}

public rel[str cat, Resource file] resourcesByCategory(set[Resource
	resource] resources, rel[str dir, str cat] dirCategories) {

    rel[str cat, Resource resource] catResources = {};
    for (r <- resources) {
		for(dir <- domain(dirCategories)) {
	    	if (startsWith(r.id.path, dir)) {
				for(cat <- dirCategories[dir]) {
		    		catResources += {<cat, r>};
				}
	    	}
		}
    }
    return catResources;
}

//----------------------------------------
public int statsFive(rel[Tag version, ChangeSet cs] versionChangesets,
	map[str, str] domainMap, list[Tag] releases) {	

    int duration = 0;
    rel[Tag version, str email, str devverName, ChangeSet cs] result = {};
    printStartTimer("calcDevelopers");
    for (version <- releases, version in versionChangesets.version) {
		result += calcDevelopers(version, "Author", 
			{<cs@author ? cs.committer, cs>| cs <- versionChangesets[version]}, 
			false)<0,2,3,4>;
    }
    
    duration += printRestartTimer("calcDevelopers");
    for (version <- releases, version in result.version) {
		map[set[str name] user, set[ChangeSet] cs] userChanges 
			= getUserChangeSets(result[version]);
		map[set[str name] user, int count] userChangesCount 
			= (usr : size(userChanges[usr]) | usr <- domain(userChanges));
		duration += printRestartTimer("<version.name> - <size(userChanges.user)> users");
		printMapOrderedOnRange(userChangesCount, 10);
    }

    startTimer();//quietly restart the timer
    map[str mail, str company] emailCompany = (addr : domainMap[dom] | 
	    dom<-domain(domainMap), addr<-result.email, endsWith(addr, dom));

    duration += printRestartTimer("emailCompany");
    rel[Tag version, str company, ChangeSet cs] versionCompanyChangesets = 	
	    {<version, emailCompany[email] ? "Unknown", cs> 
	    | <Tag version, str email, str devverName, ChangeSet cs> <- result};

    duration += printRestartTimer("versionCompanyChangesets");
    for (version <- releases, version in versionCompanyChangesets.version) {
		rel[str company, ChangeSet cs] companyChangesets 
			= versionCompanyChangesets[version];
		map[str company, int changes] companyChangesCount = (company 
			: size(companyChangesets[company]) 
			| company <- domain(companyChangesets));
		duration += printRestartTimer("\n<version.name> & <size(companyChangesets.company)> companies");
		printMapOrderedOnRange(companyChangesCount, 10);
    }
    duration += printStopTimer("versionCompanyChangesets");
    return duration;
}

public map[set[str name] user, set[ChangeSet] cs] getUserChangeSets(rel[str
	email, str devverName, ChangeSet cs] input) {

    rel[str name, str email] devMail = {<user, email>  | email <- input.email, 
	    email != "", user <- input[email]<0>, user != ""};
    rel[str name, ChangeSet cs] userCs = input[_];
    rel[str email, ChangeSet cs] mailCs = input<0,2>;
    
    devMail = solveBrokenRelations(devMail);
    
    mailDev = devMail<1,0>;
    map[str mail, set[str] userNames] mailUserNames = (email : mailDev[email] 
	    | email <- range(devMail));
    map[set[str] userNames, set[str] mailAdresses] users =invert(mailUserNames);
    
    map[set[str name] user, set[ChangeSet] cs] result 
	= (userNames : domainR(userCs, userNames)<1> 
	    + domainR(mailCs, users[userNames])<1> 
	    | set[str] userNames <- users.userNames);
    
    set[str] processedUsernames = {userName | userName <- users.userNames};
    set[str] usersWithoutMail = domainX(userCs, processedUsernames)<0>;
    result += ({user} : userCs[user]| user <- usersWithoutMail);
    
    return result;
}

/**
* 	Makes sure that each name has a relation with any email known for
* 	the same user, even if alternative usernames are used

*	For example, if the tuple <Linus Torvalds, linus@linux.com> exists
*	in the input along with two other tuples: <Linus Torvalds,
*	linux@linux.com> and <Torvalds, linux@linux.com>, then the resulted
*	set will have the additional tuple: <Torvalds, linus@linux.com>.

*/
public rel[str name, str email] solveBrokenRelations(rel[str name, str email] 
	input) {
    r = input;
    solve(r) {
	r = r o invert(r) o r;
    }
    return r;
} 

/*
* Reachability from set of start nodes with exclusion of certain nodes.
* Another implementation then the one in Graph.rsc, since the later uses 
* the transitive closure and gets out of memory by large amount of data.
*/
public set[&T] reach(Graph[&T] G, set[&T] Start, set[&T] Excl) {
    set[&T] R = Start;
    solve (R) {
	R = R + G[R] - Excl;
    }
    return R;
}

public void printMapOrderedOnRange(map[value,int] content, int topMax) {
    int totalCount = 0;
    for(v <- domain(content)) {
	totalCount += content[v];
    }
    for (int v <- reverse(quickSort(range(content))), 
	    d <- rangeR(content, {v}), topMax >= 0) {
	topMax -= 1;
	print("<d> - <v> - <v*100/totalCount>%");
    }
}

public rel[Tag version, str action, str email, str devverName, ChangeSet cs] 
		calcDevelopers(Tag version, str action, 
		rel[Info info, ChangeSet cs] devvers, bool parseCsMessage) {

    rel[Tag version, str action, str email, str devverName, ChangeSet cs] 
	    results = {};
    for (Info info <- devvers.info) {
	str msg = (parseCsMessage ? (info.message ? "") : "") + " " 
		+ action + "-by: " + info.name;
	for (/\s*<action:[^\s]*>-by:\s*<name:.*>/ := msg) {
	    if (/\s*"?<fname:[^\<"]+>"?\s\<<mail:[^\>]+>\>/ := name) {
		results += {<version, action, toLowerCase(mail), fname, cs> | 
			cs <- devvers[info]};
	    } else if (/\<<mail:[^\>]+>\>/ := name){
		results += {<version, action, toLowerCase(mail), "", cs> | 
			cs <- devvers[info]};
	    } else {
		results += {<version, action, "", name, cs> | 
			cs <- devvers[info]};
	    }
	}
    }
    return results;
} 

//gets the changesets and measures the time 
public list[ChangeSet] getChanges(Repository repo) {
    printStartTimer("initChangesets");
    println("");
    changesets = getChangesets(repo);
    printStopTimer("initChangesets");
    println("");
    return changesets;
}

public MappingVars getMappings(InitVars initVars, map[Tag version,
	CheckoutUnit cunit] manualReleases) {

    changesets = initVars.changesets;
    repo = initVars.repo;
    releases = initVars.releases;
    
    int total = 0;
    printStartTimer("childParents");
    childParents={<cs.revision.id,m.parent.id> | cs <-changesets, revision(_, _)
	    := cs.revision, m <- (cs.revision@mergeDetails 
	    ? {mergeParent(cs.revision.parent)})};
    total += printRestartTimer("childParents");
    revChangeset = (cs.revision.id : cs |cs <- changesets);
    total += printRestartTimer("revChangeset");
    tagChangeset = (t : cs | cs <- changesets, t <- (cs.revision@tags ? {}));
    extraTags = (t : revChangeset[rev.id] | t <- manualReleases,
	    cunit(Revision rev) := manualReleases[t]);

    // TODO we currently only support tags, but we might implement support
    // for other checkoutunit types (e.g. date)

    tagChangeset += extraTags;
    if (size(extraTags) < size(manualReleases)) {
	tagChangeset += (t : tagChangeset[symName] | t <- manualReleases, 
		cunit(Tag symName) := manualReleases[t]);
    }
    total += printRestartTimer("tagChangeset");
    
    totalVersionRevisions = getVersionRevisions(repo, childParents, 
	    tagChangeset, releases);
    total += totalVersionRevisions[0];
    versionRevisions = getOnlyUniqueRevisions(totalVersionRevisions[1], 
	    releases);
    total += printRestartTimer("uniqueVersionRevisions");
    rel[Tag version, ChangeSet cs] versionChangesets = versionRevisions 
	    o toRel(revChangeset);
    total += printRestartTimer("versionChangesets");
    versionNoMergesChangesets = {<version, cs> | 
	    version <- versionChangesets.version, 
	    cs <- versionChangesets[version], 
	    "mergeDetails" notin getAnnotations(cs.revision)};

    total += printStopTimer("versionNoMergesChangesets");
    print("Total duration:<total>");
    
    mappingVars = <childParents, tagChangeset, revChangeset, versionRevisions, 
	    versionNoMergesChangesets>;
    return mappingVars;
}


public tuple[int total, rel[Tag version, RevisionId revision] versionRevisions] 
	getVersionRevisions( Repository repo, rel[RevisionId child, 
	RevisionId parent] childParents, map[Tag version, ChangeSet cs]
	tagChangeset, list[Tag] releases) {

    int total = 0;

    rel[Tag version, RevisionId revision] versionRevisions = {};
    for (version <- releases, version in tagChangeset) { 
	set[RevisionId] reachable = {};
	if (git(_,_,_) := repo) {
	    reachable =reach(childParents, {tagChangeset[version].revision.id});
	} else {
	    reachable = reach(childParents, {tagChangeset[version].revision.id},		    {tagChangeset[ver].revision.id | 
		    ver <- tagChangeset.version, ver != version});
	}
	versionRevisions += {<version, reaching> | reaching <- reachable};
	total += printRestartTimer("versionRevision - <version> - <size(reachable)>");
    }
    return <total, versionRevisions>;
}


/**
* Makes sure that a revisionId is only referenced by one version. So if
* revision A is part of release 12 and 13, release 13 will no longer
* reference to it in the returned relation.
*/

public rel[Tag version, RevisionId revision] getOnlyUniqueRevisions(rel[Tag
	version, RevisionId revision] versionRevisions, list[Tag] releases){

    rel[Tag version, RevisionId revision] results = {};
	    
    int i = 0;
    for (version <- releases, version in versionRevisions.version) {
	if (i == 0) {
	    results += {<version, rev> | rev <- versionRevisions[version]};
	} else {
	    prev = versionRevisions[releases[i-1]];
	    results += {<version, rev> | rev <- versionRevisions[version], 
		rev notin prev};
	}
	i += 1;
    }
    return results;
}
