module LinuxKernel

import ValueIO;
import IO;
import experiments::scm::Scm;
import experiments::scm::Timer;
import DateTime;
import List;
import Map;
import Set;
import Relation;
import Graph;
import String;
import Real;

data infoActions = anyAction() | actions(set[str] actions) 
	| notActions(set[str] actions);

public void main() {
	
}

/**
*	Kernel size.
*/
public rel[Tag version, Resource file, int lines]
	getLinesOfFiles(Repository repo, set[Tag] versions) {
    print("started at <startTimer()>");
    rel[Tag version, Resource file, int lines] results = {};
    for (version <- versions) {
	checkoutResources(cunit(version), repo);
	wcResources = getResources(repo);
	map[Resource, int] fileLines = linesCount(wcResources.resources);	
	results += {<version, res, fileLines[res]> | res <- domain(fileLines)};
	print("<version.name> - <size(domain(results[version]))> files");
    }
    print("duration <stopTimer()> ms");
    return results;
}

/**
* Table 6: kernel size by files
*/
public rel[str cat, Resource file] resourcesByCategory(set[Resource resource] 
	resources, rel[str dir, str cat] dirCategories) {

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

public map[str cat, int lines] countLines(Tag version, set[str] categories,
	rel[Tag version, Resource file, int lines] resLines, 

    rel[str cat, Resource file] resCategories) {
    rel[Resource, int] resourceLines = resLines[version];
    
    map[str cat, int lines] results = ();
    print("started at <startTimer()>");
    
    for (c <- categories) {
	results[c] = 0;
	for (r <- resCategories[c]) {
	    results[c] += getOneFrom(resourceLines[r]);
	}
    }
    print("duration <stopTimer()> ms");
    return results;
}

public map[str auth, int count] authorInfoCount(rel[str name, str email,
	node n] authorInfo) {

    map[str auth, int count] results = ();
    
    //map[str name, str username, str domain]
    for (str auth <- authorInfo.name) {
	results[auth] = size(authorInfo[auth]);
    }
    
    return results;
}

public list[int count] getSortedCount(map[str auth, int count] authCount) {
    return reverse(quickSort(range(authCount)));
}

public void printTopAuthors(list[int count] topCount, map[str auth, 
	int count] authCount, int maxCount) {

    int total = 0;
    for (auth <- authCount.auth) {
	total += authCount[auth];
    }

    for (int i <- topCount, maxCount >= 0) {
	set[str author] authors = domain(rangeR(authCount, {i}));
	for (auth <- authors) {
	    maxCount -= 1;
	    print("<auth> - <i> - <substring(toString(i*100.00/total), 0, 5)>%");
	    if (maxCount % 10 == 0) {
		print("------------------");
	    }
	}
    }
}

/**
* Developers
*/
public void printDeveloperStatistics(rel[Tag version, str devverName, 
	str email, ChangeSet cs] authorsInfo, map[str, str] domainMap) {

    //Company changesets statistics
    map[str mail, str company] emailCompany = (addr : domainMap[dom] 
	    | dom <- domain(domainMap), 
	    addr <- authorsInfo.email, endsWith(addr, dom) );

    rel[Tag version, str company, ChangeSet cs] versionCompanyChangesets 
	    = {<version, emailCompany[email] ? "Unknown", cs> 
		| <Tag version, str devverName, str email, ChangeSet cs> 
		<- authorsInfo};

    rel[str company, ChangeSet cs] v13CompanyChangesets 
	    = versionCompanyChangesets[label("v2.6.13")];
    
    map[str company, int changes] v13CompanyChangesCount 
	    = (company : size(v13CompanyChangesets[company]) 
		| company <- domain(v13CompanyChangesets));

    printMapOrderedOnRange(v13CompanyChangesCount, 10);
    
    //User changesets statistics
    map[set[str name] user, set[ChangeSet] cs] v13UserChanges 
	= getUserChangeSets(authorsInfo[label("v2.6.13")]);

    map[set[str name] user, int count] v13UserChangesCount 
	    = (usr : size(v13UserChanges[usr]) | usr <- domain(v13UserChanges));
    printMapOrderedOnRange(v13UserChangesCount, 10);
}

public map[Tag version, map[set[str name] user, set[ChangeSet] cs] info]
	getUserStatsPerVersion (rel[Tag version, str devverName, str email,
	ChangeSet cs] authorsInfo, set[Tag version] toProcess) {

	
    map[Tag version, map[set[str name] user, 
	    set[ChangeSet] cs]authorsInfo] results = ();
    print("started getUserStatsPerVersion at <startTimer()>");
    for (version <- toProcess, version in authorsInfo.version) {
	versionUsers = getUserChangeSets(authorsInfo[version]);
	map[set[str name] user, int count] userChangesCount 
		= (usr : size(versionUsers[usr]) | usr <- domain(versionUsers));
	print("Processed <version> <now()>");
	//printMapOrderedOnRange(userChangesCount, 10);
    }
    print("duration getUserStatsPerVersion <stopTimer()> ms");
    //writeBinaryValueFile(|file:///export/scratch1/shabazi/userStatsPerVersionMap.bin|, results); 
    return results;	
}

public void printMapOrderedOnRange(map[value,int] content, int topMax) {
    int totalCount = 0;
    for(v <- domain(content)) {
	totalCount += content[v];
    }
    for (int v <- reverse(quickSort(range(content))), 
	    d <- rangeR(content, {v}), topMax >= 0) {
	topMax -= 1;
	print("<d> & <v> & <v*100/totalCount>\\% \\\\");
    }
}

public rel[str company, ChangeSet cs] getCompanyChangesets(rel[str company,
	ChangeSet cs] versionCompanyChangesets) {

    for(version <- versions) {
	versionCompanyChangesets[version];
    }
}

public rel[Tag version, str action, str devverName, str email, ChangeSet cs] calcDevelopers(
	rel[Tag version, ChangeSet changeset] versionChangesets, bool parseCsMessage) {
		
	print("started at <startTimer()>");
	rel[Tag version, str action, str devverName, str email, ChangeSet cs] results = {};
	for (version <- versionChangesets.version) {
		results += calcDevelopers(version, "Author", {<cs@author, cs>| cs <- versionChangesets[version]}, parseCsMessage);
		results += calcDevelopers(version, "Committer", {<cs.committer, cs>| cs <- versionChangesets[version]}, parseCsMessage);
	}
	print("duration <stopTimer()> ms");
	return results;
}

public rel[Tag version, str action, str devverName, str email, ChangeSet cs] calcDevelopers(Tag version, str action, 
	rel[Info info, ChangeSet cs] devvers, bool parseCsMessage) {
	rel[Tag version, str action, str devverName, str email, ChangeSet cs] results = {};
	int malformed = 0;

	for (Info info <- devvers.info) {
		str msg = (parseCsMessage ? (info.message ? "") : "") + " " + action + "-by: " + info.name;
		for (/\s*<action:[^\s]*>-by:\s*<name:.*>/ := msg) {
			if (/\s*"?<fname:[^\<"]+>"?\s\<<mail:[^\>]+>\>/ := name) {
				results += {<version, action, fname, toLowerCase(mail), cs> | cs <- devvers[info]};
			} else if (/\<<mail:[^\>]+>\>/ := name){
				results += {<version, action, "", toLowerCase(mail), cs> | cs <- devvers[info]};
			} else {
				results += {<version, action, name, "", cs> | cs <- devvers[info]};
				malformed +=1;
			}
		}
	}
	print("<version> - <action> - <size(devvers)> - <size(results)> - malformed: <malformed>");
	return results;
}

/**
* 	Makes sure that each name has a relation with any email known for the same user, even if alternative usernames are used
*	For example, if the tuple <Linus Torvalds, linus@linux.com> exists in the input along with two
*	other tuples: <Linus Torvalds, linux@linux.com> and <Torvalds, linux@linux.com>, then the resulted set will have the 
*	additional tuple: <Torvalds, linus@linux.com>.
*/
public rel[str name, str email] solveBrokenRelations(rel[str name, str email] input) {
	r = input;
	solve(r) {
		r = r o invert(r) o r;
	}
	return r;
}


public map[set[str name] user, set[ChangeSet] cs] getUserChangeSets(rel[str devverName, str email, ChangeSet cs] input) {
	rel[str name, str email] devMail = {<user, mail> | user <- domain(input), user != "", mail <- input[user]<0>, mail != ""};
	rel[str name, ChangeSet cs] userCs = input<0,2>;
	rel[str email, ChangeSet cs] mailCs = input<1,2>;
	
	devMail = solveBrokenRelations(devMail);
	mailDev = devMail<1,0>;
	map[str mail, set[str] userNames] mailUserNames = (email : mailDev[email] | email <- range(devMail));
	map[set[str] userNames, set[str] mailAdresses] users = invert(mailUserNames);
	
	return (userNames : domainR(userCs, userNames)<1> + domainR(mailCs, users[userNames])<1> | set[str] userNames <- users.userNames);

}

/**
* Revisions per release version.
*/
public rel[Tag version, RevisionId revision] getVersionRevisions(map[Tag version, ChangeSet changeset] tagChangesets, list[Tag] versions,
	rel[RevisionId child, RevisionId parent] childParents) {
	print("started at <startTimer()>");
	rel[Tag version, RevisionId revision] results = {};
	for (version <- versions, version in tagChangesets) {
		set[RevisionId] reachable = reach(childParents, {tagChangesets[version].revision.id});
		results += {<version, reaching> | reaching <- reachable};
		print("<version> has <size(results[version])> changesets");
	}
	print("duration <stopTimer()> ms");
	return results;
}
/**
* Makes sure that a revisionId is only referenced by one version. So if revision A is part of release 12 and 13, release 13 will
* no longer reference to it in the returned relation.
*/
public rel[Tag version, RevisionId revision] getUniqueRevisions(rel[Tag version, RevisionId revision] versionRevisions, list[Tag] versions) {
	rel[Tag version, RevisionId revision] results = {};
		
	if (size(versions) > 0) {
		results += {<versions[0], rev> | rev <- versionRevisions[versions[0]]};
		for (i <- [1 .. size(versions)-1]) {
			set[RevisionId] revs = versionRevisions[versions[i]] - versionRevisions[versions[i-1]];
			Tag version = versions[i];
			results += {<version, rev> | rev <- revs};
		}
	}
	return results;
}

public map[Tag version, int revisions] countVersionRevisions(rel[Tag version, RevisionId revision] versionRevisions) {
	map[Tag version, int revisions] results = (); 
	for(Tag version <- versionRevisions.version) {
		results[version] = size(versionRevisions[version]);
	}
	return results;
}

/**
* Development time per release.
*/
public map[Tag version, int days] calcDevDays(map[Tag version, ChangeSet changeset] tagChangesets, list[Tag] versions) {
	map[Tag symname, int days] results = ();
	for (i <- [1 .. size(versions)-1]) {
		prev = versions[i-1];
		current = versions[i];
		
		results[current] = daysDiff(tagChangesets[prev].committer.date, tagChangesets[current].committer.date);
	}
	return results;
}


public int sum(list[int] numbers) {
	int total = 0;
	for (number <- numbers) {
		total += number;
	}
	return total;
}



public void printStatistics(map[Tag version, ChangeSet changeset] tagChangesets, list[Tag] versions, list[ChangeSet] changesets) {
	print ("Frequency of kernel releases");
	freqReleases = calcDevDays(tagChangesets, versions);
	for (version <- versions, version in freqReleases) {
		print("<version> - <freqReleases[version]>");
	}
}

public void printLinesStatistics(map[Tag version, map[Resource file, int lines] fileLines] versionFileLines, list[Tag] versions) {
	for (version <- versions, version in versionFileLines) {
		map[Resource file, int lines] lines = versionFileLines[version];
		print("<version.name> - <size(domain(lines))> files - <countTotalLines(lines)> lines");
	}
}

public map[Tag, rel[str cat, Resource file]] getKernelSize(map[Tag version, map[Resource file, int lines] fileLines] versionFileLines, 
	rel[str cat, str dir] categories, list[Tag] versions) {
	map[Tag, rel[str cat, Resource file]] results = ();
	
	for (version <- versions, version in versionFileLines) {
		results[version] = kernelSizeByCategory(versionFileLines[version], categories);
		print("<version.name> - <size(range(results[version]))> files");
	}
	
	return results;
}



public map[Tag version, map[Resource file, int lines] fileLines] getLinesOfFiles(Repository repo, list[Tag] versions) {
	print("started at <startTimer()>");
	map[Tag version, map[Resource file, int lines] fileLines] results = ();
	for (version <- versions) {
		results[version] = getLinesOfFiles(repo, version);
		print("<version.name> - <size(domain(results[version]))> files");
	}
	print("duration <stopTimer()> ms");
	return results;
}

public map[Resource file, int lines] getLinesOfFiles(Repository repo, Tag version) {
	checkoutResources(cunit(version), repo);
	wcResources = getResources(repo);
	return linesCount(wcResources.resources);	
}

public int countTotalLines( map[Resource file, int lines] fileLines) {
	int total = 0;
	for (f <- domain(fileLines)) {
		total += fileLines[f];
	}
	return total;
}

public map[loc file, int lines] submap(map[Resource file, int lines] fileLines, set[str] dirs) {
	map[Resource file, int lines] results = ();
	for (f <- domain(fileLines)) {
		print();
	}
	return results;
}







public map[Tag version, int days] calcDevDays(map[RevisionId, ChangeSet] revChangesets, rel[Tag version, RevisionId revision] versionRevisions) {
	map[Tag symname, int days] results = ();
	for (t <- domain(versionRevisions)) {
		datetime startMeUp = getOneFrom(range(revChangesets)).committer.date;
		datetime end = startMeUp;
		for (rev <- versionRevisions[t]) {
			d = revChangesets[rev].committer.date;
			if (startMeUp > d) {
				startMeUp = d;
			}
			if (end < d) {
				end = d;
			}
		}
		results[t] = daysDiff(startMeUp, end);
	}
	return results;
}

public void printCalcDevDays(map[Tag version, int days] devDays, list[Tag] versions) {
	for (i <- [1 .. size(versions)-1]) {
		prev = versions[i-1];
		current = versions[i];
		if (prev in devDays && current in devDays) {
			print("<current> <devDays[current] - devDays[prev]>");
		}
	}
}

public list[ChangeSet] readLinuxKernel(str directory, str prefix, Tag startMeUp, Tag end) {
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
	}
	for (i <- indexCounter, i > startIndex && i <= endIndex) {
		changesets += readBinaryValueFile(#(list[ChangeSet]), |file://<directory><prefix><i>.bin|);
		print("Read <index[i]> size: <size(changesets)>");
	}
	print("duration <stopTimer()> ms");
	return changesets;
}


public list[&T] quickSort(set[&T] st) {
	return quickSort(toList(st));
}
public list[&T] quickSort(list[&T] lst)
{
  if(size(lst) <= 1){
  	return lst;
  }
  
  list[&T] less = [];
  list[&T] greater = [];
  &T pivot = lst[0];
  
  <pivot, lst> = takeOneFrom(lst);
  
  for(&T elm <- lst){
     if(elm <= pivot){
       less = [elm] + less;
     } else {
       greater = [elm] + greater;
     }
  }
  
  return quickSort(less) + pivot + quickSort(greater);
}
