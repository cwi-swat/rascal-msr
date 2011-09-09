module Stats

import ValueIO;
import DateTime;
import IO;
import experiments::scm::Scm;
import experiments::scm::cvs::Cvs;
import experiments::scm::svn::Svn;
import experiments::scm::git::Git;
import experiments::scm::Timer;
import DateTime;
import List;
import Map;
import Set;
import Relation;
import Graph;
import String;
import Real;
import Node;

public alias InitVars = tuple[list[Tag] releases, Repository repo, rel[str cat, str dir] catDirs, list[ChangeSet] changesets];
public alias MappingVars = tuple[rel[RevisionId child, RevisionId parent] childParents, 
	map[Tag version, ChangeSet changeset] tagChangeset, 
	map[RevisionId revId, ChangeSet cs] revChangeset,
	rel[Tag version, RevisionId revId] versionRevisions,
	rel[Tag version, ChangeSet cs] versionNoMergesChangesets];

//public map[Tag version, RevisionId revId] manualReleases = ();
//public rel[str cat, str dir] catDirs = {};
//public Repository repo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", {});


public void main() {

//public InitVars initVars;
//public MappingVars mappingVars;

	//initVars = setMetaEnvConfig();
	//init();
	//benchmark(100);
	//print(svnStats()[0]);
	//print("-----");
	//print(gitStats()[0]);
}

public tuple[list[int], InitVars, MappingVars] gitStats() {
	gitConfig = getLinuxConfig();
	initVars = <gitConfig.releases, gitConfig.repo, gitConfig.catDirs, getChanges(gitConfig.repo)>;
	MappingVars maps = getMappings(initVars, ());
	return <stats(initVars, maps), initVars, maps>;
}

public tuple[list[int], InitVars, MappingVars] cvsStats() {
	cvsConfig = getCvsConfig();
	list[ChangeSet] resourceChanges = getChanges(cvsConfig.repo);
	transRecovered = recoverChangesets(resourceChanges, 200);
	InitVars initVars = <cvsConfig.releases, cvsConfig.repo, cvsConfig.catDirs, transRecovered[1]>;
	
	MappingVars maps = getMappings(initVars, ());
	return <stats(initVars, maps), initVars, maps>;
}

public tuple[list[int], InitVars, MappingVars] cvsStats(rel[Tag, datetime] manualReleases) {
	cvsConfig = getCvsConfig();
	list[ChangeSet] resourceChanges = getChanges(cvsConfig.repo);
	transRecovered = recoverChangesets(resourceChanges, 200);
	InitVars initVars = <cvsConfig.releases, cvsConfig.repo, cvsConfig.catDirs, transRecovered[1]>;
	
	MappingVars maps = getMappings(initVars, manualReleases);
	return <stats(initVars, maps), initVars, maps>;
}


public tuple[list[int], InitVars, MappingVars] svnStats() {
	svnConfig = getMetaEnvConfig();
	initVars = <svnConfig.releases, svnConfig.repo, svnConfig.catDirs, getChanges(svnConfig.repo)>;
	MappingVars maps = getMappings(initVars, svnConfig.manualReleases);
	return <stats(initVars, maps), initVars, maps>;
}

public list[ChangeSet] createChangesets(rel[Info committer, Resource resource, RevisionChange change, set[Tag] tags] changes) {
	printStartTimer("dateInfo");
	rel[datetime date, Info info] dateInfo = {<info.date,info>|info <- changes.committer};
	printRestartTimer("quickSort dates");
	dates = quickSort(dateInfo.date);
	list[ChangeSet] changesets = [];
	printRestartTimer("create changesets");
	int revisionCounter = 0;
	RevisionId prevId;
	for(date <- dates) {
		for(info <- dateInfo[date]) {
			revisionCounter += 1;
			Revision rev;
			RevisionId revId = id(revisionCounter);
			if(revisionCounter > 1) {
				rev =  revision(revId, revision(prevId));
			} else {
				rev = revision(revId);
			}
			rel[Resource resource, RevisionChange change, set[Tag] tags] tmp = changes[info];
			set[Tag] tags = {t|t <- tmp.tags};
			if (size(tags) > 0) {
				rev@tags = tags;
			}
			changesets += [changeset(rev, tmp<0,1>, info)];
			prevId = revId;
		}
	}
	printStopTimer("create changesets");
	return changesets;
}

public void getChangeSetDifferences() {
	cvsDateCs = {<cs.committer.date, cs> | cs <- cvsSts[1].changesets};
	svnDateCs = {<cs.committer.date, cs> | cs <- svnSts[1].changesets};
	
	cvsDates = quickSort(cvsDateCs<0>);
	svnDates = quickSort(svnDateCs<0>);
}

public set[WcResource] checkoutAndGetWcResources(Repository repo, CheckoutUnit cunit) {
	printStartTimer("checkoutResources");
	checkoutResources(cunit, repo);
	printRestartTimer("getResources");
	set[WcResource] wcResources = getResources(repo);
	printStopTimer("getResources");
	return wcResources;
}

public tuple[set[WcResource] cvs, set[WcResource] svn] getWcResources() {
	cvsRepo = cvs(fs("/export/scratch1/shabazi/CVS"),"software_20040603",|file:///export/scratch1/shabazi/cvsWorkingCopy|,{});
	svnRepo = svn(ssh("svn+ssh://svn.cwi.nl", "waruzjan", "", |file:///ufs/shahbazi/.ssh/wshahbazian_rascal_rsa|), "", |file:///export/scratch1/shabazi/svnWorkingCopy|,{fileDetails()});
	wcResourcesCvs = checkoutAndGetWcResources(cvsRepo, cunit(createDateTime(2001,11,14,9,35,14,0,1,0)));
	wcResourcesSvn = checkoutAndGetWcResources(svnRepo, cunit(createDateTime(2001,11,14,9,35,14,0,1,0)));
	
	return <wcResourcesCvs, wcResourcesSvn>;
}

public Resource searchResource(loc res, Resource root) {
	set[Resource] childs = root.resources;
	
	solve(childs) {
		locResources = {<r.id,r> | r <- childs};
		if (res in domain(locResources)) {
			fnd = locResources[res];
			for(folder(loc id, set[Resource] resources) <- fnd) {
				return (folder(id, resources));
			}
			return getOneFrom(fnd);
		}
		childs = {r | Resource res <- range(locResources), folder(loc id, set[Resource] resources) := res, r <- resources};
	}
	
	return root;
}

/*
public set[loc] searchResources(str ends, Resource root) {
	set[Resource] childs = root.resources;
	
	solve(childs) {
		locResources = {<r.id,r> | r <- childs};
		if (res in domain(locResources)) {
			fnd = locResources[res];
			for(folder(loc id, set[Resource] resources) <- fnd) {
				return (folder(id, resources));
			}
			return getOneFrom(fnd);
		}
		childs = {r | Resource res <- range(locResources), folder(loc id, set[Resource] resources) := res, r <- resources};
	}
	
	return root;
}*/

public map[Resource c, Resource p] getChildParent(Resource root) {
	map[Resource child, Resource parent] childParent = ();
	
	map[Resource child, Resource parent] childs = (c:root | c <- root.resources);
	childParent += childs;
	solve(childs) {
		map[Resource child, Resource parent] newChilds = ();
		for(c <- childs) {
			if (folder(loc id, set[Resource] resources) := c) {
				newChilds += (r :c | r <- resources);
			}
			childParent[c] = childs[c];
		}
		childs = newChilds;
	}
	return childParent;
}



public map[loc m, loc e] getMissings(map[loc c, loc p] from, map[loc c, loc p] to) {
	map[loc m, loc e] result = ();
	missing = domain(from) - domain(to);
	
	for(m <- missing) {
		parent = from[m];
		solve(parent) {
			if(parent in to) {
				result[m] = parent;
			} else if (parent in from) {
				parent = from[parent];
			}
		}
	}
	return result;
}

public map[loc c, loc p] getChildParent(set[ChangeSet] changesets, bool s) {
	svnResources = {<r.resource, r.change, cs> | cs <- changesets, tuple[Resource resource, RevisionChange change] r <- cs.resources};
	svnRescs = svnResources<0>;
	rel[str, loc] svnBaseOriginLower;
	if (s) {
		svnBaseOriginLower = {<replaceAll(replaceFirst(replaceFirst(replaceFirst(toLowerCase(
			substring(rC.id.path, 40)), "trunk/", ""), "branches/", ""), "tags/", ""), " ", "%20"), rC> | rC <- svnRescs};
	} else {
		svnBaseOriginLower = {<replaceAll(toLowerCase(
			substring(rC.id.path, 58)), " ", "%20"), rC> | rC <- svnRescs};
	}
	svnBaseOriginRscs = { file(|file:///tmp/<p>|) | p <- svnBaseOriginLower<0>};
	svnBaseOriginTree = buildResourceTree(svnBaseOriginRscs);
	svnCP = getChildParent(getOneFrom(svnBaseOriginTree));
	return (c.id:svnCP[c].id | c<- svnCP);
}

//public tuple[map[loc c, loc p] from, map[loc c, loc p] to, map[loc c, loc p] missing] getMissing(set[ChangeSet] c, set[ChangeSet] s) {

//}

public rel[bool res, int date, bool msg, int first, int second] compareResources(
	rel[str author, str msg, ChangeSet cs] oneCs,rel[str author, str msg, ChangeSet cs] twoCs) {
	authors = {"jurgenv"}; //oneCs.author
	for (auth <- authors) {
		rel[str msg, ChangeSet cs] oneChanges = oneCs[auth];
		set[str] oneMsg = oneChanges.msg;
		set[str] twoMsg = twoCs[auth]<0>;
		
		set[str] dff = oneMsg - twoMsg;
		print(size(dff));
		
		set[str] cmn = oneMsg & twoMsg;
		for (s <- cmn) {
			print(s);
		}
		
		rel[str msg, ChangeSet cs] common = domainR(oneChanges, oneMsg & twoMsg);
		
	}
	return {};
}

//compareReslts = compareResources(cvsDateCs, cvsDates, 58, svnDateCs, svnDates, 40, 10, createDateTime(2001,11,1,0,0,0,0), createDateTime(2004,6,3,0,0,0,0));
public rel[bool res, int date, bool msg, int first, int second] compareResources(rel[datetime date, ChangeSet cs] oneCs, list[datetime] oneDates, int fSize,
	rel[datetime date, ChangeSet cs] twoCs, list[datetime] twoDates, int sSize, int max, datetime start, datetime end) {


	rel[bool res, int date, bool msg, int first, int second] result = {};
	bool started = false;
	int i = 0;
	int m = 0;
	while(m < max) {
		date = oneDates[i];
		i = i+1;
		if (!started && date >= start) {
			print("Started!");
			started = true;
		}
		if (started) {
			rslt = compareResources(oneCs, oneDates, fSize, twoCs, twoDates, sSize, i, i+1);
			result += rslt;
			if (!rslt[0]) {
				rslt = compareResources(oneCs, oneDates, fSize, twoCs, twoDates, sSize, i, i);
				result += rslt;
				if (!rslt[0]) {
					rslt = compareResources(oneCs, oneDates, fSize, twoCs, twoDates, sSize, i+1, i);
					result += rslt;
				}
			}
			print("----<i> dont <result[false][_][_][i]>, do <result[true][_][_][i]> ----");
			m += 1;
		}
		
		if(started && date >= end) {
			started = false;
			m = max;
		}
	}
	return result;
}

public rel[bool res, int date, bool msg, int first, int second] tstIt(rel[datetime date, ChangeSet cs] oneCs, list[datetime] oneDates, int fSize,
	rel[datetime date, ChangeSet cs] twoCs, list[datetime] twoDates, int sSize, int max, datetime start, datetime end) {
	printStartTimer("compareResources");
	reslt = compareResources(oneCs, oneDates, fSize, twoCs, twoDates, sSize, max,start,end);
	printStopTimer("compareResources");
	return reslt;
}
public rel[ChangeSet cvs, ChangeSet svn] compareResources(rel[str msg, datetime date, ChangeSet cs] fR, 
	rel[str msg, datetime date, ChangeSet cs] sR) {
	rel[ChangeSet cvs, ChangeSet svn] result = {};
	
	for (m <- fR.msg) {
		rel[datetime date, ChangeSet cs] f = fR[m];
		if (size(f.date) > 1) {
			print("F has <size(f.date)> commits with the msg <m>");
		}
		rel[datetime date, ChangeSet cs] s = sR[m];
		if (size(s.date) > 1) {
			print("S has <size(s.date)> commits with the msg <m>");
		}
		
		for (fcs <- f.cs, scs <- s.cs, size(fcs.resources) == size(scs.resources)) {
			result += <fcs, scs>;
		}
	}
	return result;
}
public tuple[bool res, int date, bool msg] compareResources(ChangeSet fR, ChangeSet sR) {

	int date = 0; 
	bool msg = true;
	if (fR.committer.date != sR.committer.date) {
		if (sR.committer.date > fR.committer.date) {
			date = secondsDiff(fR.committer.date, sR.committer.date);
			//print("<secondsDiff(fR.committer.date, sR.committer.date)>");
		} else {
			date = secondsDiff(sR.committer.date, fR.committer.date);
			//print("<secondsDiff(sR.committer.date, fR.committer.date)>");
		}
		print("<fR.committer.date> vs <sR.committer.date>");
	}
	
	if (fR.committer.message != sR.committer.message) {
		msg = false;
		//print("message mismatch!");
	} else if ((fR.committer.author ? "") != (sR.committer.author ? "")) {
		msg = false;
	}
	resDiff = size(fR.resources) - size(sR.resources);

	return <resDiff == 0, date, msg>;
}

public tuple[bool res, int date, bool msg, int first, int second] compareResources(rel[datetime date, ChangeSet cs] oneCs, list[datetime] oneDates, int fSize,
	rel[datetime date, ChangeSet cs] twoCs, list[datetime] twoDates, int sSize, int first, int second) {
	
	//bool res = true;
	//int date = 0; 
	//bool msg = true;
	 
	
	set[ChangeSet] f = oneCs[oneDates[first]];
	set[ChangeSet] s = twoCs[twoDates[second]];
	if (size(f) > 1) {
		print("First set has <size(f)> items: <f>");
	}
	if (size(s) > 1) {
		print("Second set has <size(s)> items: <s>");
	}
	
	if (size(f) != size(s)) {
		print("First set has <size(f)> items, second has <size(s)> [<first> vs <second>]");
	}
	
	fR = getOneFrom(f);
	sR = getOneFrom(s);
	/*
	if (fR.committer.date != sR.committer.date) {
		if (sR.committer.date > fR.committer.date) {
			date = secondsDiff(fR.committer.date, sR.committer.date);
			//print("<secondsDiff(fR.committer.date, sR.committer.date)>");
		} else {
			date = secondsDiff(sR.committer.date, fR.committer.date);
			//print("<secondsDiff(sR.committer.date, fR.committer.date)>");
		}
	}
	
	if (fR.committer.message != sR.committer.message) {
		msg = false;
		//print("message mismatch!");
	}*/
	
	//fL = {<substring(rC.resource.id.path, fSize), rC.change> | tuple[Resource resource, RevisionChange change] rC <- fR.resources};
	//sL = {<substring(rC.resource.id.path, sSize), rC.change> | tuple[Resource resource, RevisionChange change] rC <- sR.resources};
	
	
	tuple[bool res, int date, bool msg] compRslt = compareResources(fR, sR);
	return <compRslt.res, compRslt.date, compRslt.msg, first, second>;
}

public tuple[rel[Info committer, Resource resource, RevisionChange change, set[Tag] tags], list[ChangeSet]] recoverChangesets(list[ChangeSet] resourceChanges, int maxSecondsDiff) {
	rel[Info committer, Resource resource, RevisionChange change, set[Tag] tags] trans = recoverTransactions(resourceChanges, maxSecondsDiff);
	return <trans, createChangesets(trans)>;
}
//resource(Resource resource, rel[RevisionChange change, Info committer] revisions, rel[Revision revision, Tag symname] revTags)
public rel[Info committer, Resource resource, RevisionChange change, set[Tag] tags] recoverTransactions(
	list[ChangeSet] resourceChanges, int maxSecondsDiff) {
	printStartTimer("authorMessageDateCs");
	//rel[datetime date, str message, ChangeSet cs] dateMessageCs = {<rev[1].date, rev[1].message ? "", cs>|cs <- resourceChanges, rev <- cs.revisions};
	rel[str author, str message, datetime date, Resource resource, RevisionChange change, set[Tag] tags] authorMessageDateRevisions = 
		{<rev[1].name ? "", rev[1].message ? "", rev[1].date, cs.resource, rev[0], cs.revTags[rev[0].revision]> | 
			cs <- resourceChanges, rev <- cs.revisions};
	printRestartTimer("transactions");
	int msgCounter = 0;

	rel[Info committer, Resource resource, RevisionChange change, set[Tag] tags] transactions = {};

	for(auth <- authorMessageDateRevisions.author) {
		for(msg <- authorMessageDateRevisions[auth]<0>) {
			msgCounter += 1;
			set[ChangeSet] processed = {};
			rel[datetime date, Resource resource, RevisionChange change, set[Tag] tags] dateResources = authorMessageDateRevisions[auth][msg];
			list[datetime] sortedDates = quickSort(dateResources.date);
			
			datetime startDate = sortedDates[0];
			Info info = message(startDate, auth, msg);
			for(date <- sortedDates) {
				if (minutesDiff(startDate, date) > maxSecondsDiff) {
					startDate = date;
					info = message(startDate, auth, msg);
				}
				transactions += {<info, resRev[0], resRev[1], resRev[2]>| resRev <- dateResources[date]};
				//TODO resource can't be twice in a changeset
			}
			
		}
		print("Processed [<auth>]s <msgCounter> changeset, transactions <size(transactions.committer)>");
		msgCounter = 0;
	}
	printStopTimer("transactions");
	return transactions;
}
/*
public list[duration] getDurations(list[datetime] sortedDates) {
	list[duration] result = [];
	int i = 0;
	for(date <- sortedDates) {
		if (i > 0) {
			result += [createDuration(sortedDates[i-1], date)];
		}
		i += 1;
	}
	return result;
}*/

public tuple[map[int, list[int]] bench, InitVars initVars, MappingVars mapVars] initStats(int amount) {
	setMetaEnvConfig();
	//setLinuxConfig();
	changes = initChangesets();
	maps = initMappings(releases, repo, changesets, manualReleases);
	map[int, list[int]] bench = benchmark(amount);
	return <bench, changes, maps>;	
}

public tuple[list[Tag] releases, Repository repo, rel[str cat, str dir] catDirs] getCvsConfig() {
	repo = cvs(fs("/export/scratch1/shabazi/CVS"),"software_20040603",|file:///export/scratch1/shabazi/cvsWorkingCopy|,{});
	releases = [label("Champagne_Release"), label("RELEASE_0_2"), 
		label("api-release"), label("release_1_4_5"), 
		label("v_1_5_3"), label("release"), label("STABLE-2003-10-08"), 
		label("STABLE-2003-12-18"),label("STABLE-2004-02-15")];
	return <releases, repo, {}>;
}

public tuple[list[Tag] releases, Repository repo, rel[str cat, str dir] catDirs] getLinuxConfig() {
	repo = git(fs("/export/scratch1/shabazi/linux-2.6"), "", {});	
	releases = [label("v2.6.<i>") | i <- [12..21]];
	
	rootDir = repo.conn.url;
	rel[str cat, str dir] catDirs = {};
	catDirs += {<"core", "<rootDir>/<d>"> | d <- ["init", "block", "ipc", "kernel", "lib", "mm", "include/linux", "include/keys"]};
	catDirs += {<"drivers", "<rootDir>/<d>"> | d <- ["crypto", "drivers", "sound", "security", "include/acpi", "include/crypto", 
		"include/media", "include/mtd", "include/pcmcia", "include/rdma", "include/rxrpc", "include/scsi", "include/sound", "include/video"]};
	catDirs += {<"architecture", "<rootDir>/<d>"> | d <- ["arch", "include/asm-", "include/math-emu", "include/x"]};
	catDirs += {<"network", "<rootDir>/<d>"> | d <- ["net", "include/net"]};
	catDirs += {<"filesystems", "<rootDir>/<d>"> | d <- ["fs"]};
	catDirs += {<"miscellaneous", "<rootDir>/<d>"> | d <- ["Documentation", "scripts", "usr"]};
	
	return <releases, repo, catDirs>;
}

public tuple[list[Tag] releases, Repository repo, rel[str cat, str dir] catDirs, 
	map[Tag version, CheckoutUnit cunit] manualReleases] getMetaEnvConfig() {
	manualReleases = (label("0.7.3"):cunit(revision(id(4941))),
		label("0.8"):cunit(revision(id(5585))),label("0.8.1"):cunit(revision(id(5796))),
		label("1.0"):cunit(revision(id(6117))),label("1.0.1"):cunit(revision(id(6161))),
		label("1.1"):cunit(revision(id(6576))),label("1.1.1"):cunit(revision(id(6829))),
		label("1.2"):cunit(revision(id(7411))),label("1.3"):cunit(revision(id(8409))),
		label("1.4"):cunit(revision(id(10293))),label("1.4.1"):cunit(revision(id(10382))),
		label("1.5"):cunit(revision(id(13578))),label("1.5.1"):cunit(revision(id(13798))),
		label("1.5.2"):cunit(revision(id(15255))),label("1.5.3"):cunit(revision(id(15342))),
		label("2.0-RC1"):cunit(revision(id(18601))),label("2.0-RC2"):cunit(revision(id(21581))),
		label("2.0-RC3"):cunit(revision(id(23699))),label("2.0.1"):cunit(revision(id(26447))),
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

public void setConfiguration(Repository repository, list[Tag] chosenReleases, map[Tag version, CheckoutUnit cunit] extraReleases, rel[str cat, str dir] categories) {
	releases = chosenReleases;
	repo = repository;
	manualReleases = extraReleases;
}

public map[int, list[int]] benchmark(InitVars iVars, MappingVars maps, int amount) {
	map[int, list[int]] results = ();
	for(i <- [0..amount]) {
		results[i] = stats(iVars, maps);
		s = size(results);
		print("<i>) <s> --- results[i]");
		writeTextValueFile(|file:///export/scratch1/shabazi/benchmarkStats<now()>.txt|, results); 
	}
	return results;
}

public list[int] calcAvg(set[list[int]] input) {
	map[int column, int total] totals = ();
	map[int column, int amount] amounts = ();
	
	for(list[int] rslt <- input) {
		int c = 0;
		
		for(v <- rslt) {
			if (c in totals) {
				totals[c] += v;
				amounts[c] += 1;
			} else {
				totals[c] = v;
				amounts[c] = 1;
			}
			c += 1;
		}
	}
	
	return [ totals[c]/amounts[c] | c <- quickSort(totals.column)];
}

//public int statsThree(Repository repo, map[Tag version, ChangeSet cs] tagChangeset, list[Tag] releases) {
//public int statsFour(Repository repo, map[Tag version, ChangeSet cs] tagChangeset, list[Tag] releases, rel[str cat, str dir] catDirs) {
public list[int] stats(InitVars initVars, MappingVars maps) {
	domainMap = readTextValueFile(#(map[str domain, str company]), |file:///export/scratch1/shabazi/domain-map.txt|);
	list[int] durations = [];
	Tag lastVersion = initVars.releases[0/*size(initVars.releases) - 1*/];
	//list[Tag] checkVersions = [label("1.0"), label("1.1"), label("1.2"), label("1.3"), label("1.4"), label("1.5"), label("1.5.1")];
	print("------------------STATS 1------------------");
	durations += statsOne(maps.tagChangeset, initVars.releases);
	print("------------------STATS 2------------------");
	durations += statsTwo(maps.versionRevisions, initVars.releases);
	print("------------------STATS 3------------------");
	durations += statsThree(initVars.repo, maps.tagChangeset, [lastVersion]/*initVars.releases*/);
	print("------------------STATS 4------------------");
	durations += statsFour(initVars.repo, maps.tagChangeset, [lastVersion]/*initVars.releases*/, initVars.catDirs);
	print("------------------STATS 5------------------");
	durations += statsFive(maps.versionNoMergesChangesets, domainMap, [lastVersion]/*initVars.releases*/);
	
	int totalDuration = 0;
	for(d <- durations) {
		totalDuration += d;
	}
	print("Duration:<totalDuration>");
	return durations;
}

public list[int] stats() {
	domainMap = readTextValueFile(#(map[str domain, str company]), |file:///export/scratch1/shabazi/domain-map.txt|);
	list[int] durations = [];
	durations += statsOne(tagChangeset, releases);
	durations += statsTwo(versionRevisions, releases);
	//durations += statsThree(repo, releases);
	
	Tag lastVersion = releases[size(releases)-1];
	//durations += statsThree(repo, tagChangeset, [lastVersion]);
	//durations += statsFour(repo, tagChangeset, [lastVersion], catDirs);
	durations += statsFive(versionNoMergesChangesets, domainMap, [lastVersion]);
/*	
	//durations += statsFour(repo, [lastVersion]);
	printStartTimer("checkout <lastVersion>");
	checkoutResources(cunit(tagChangeset[lastVersion].revision), repo);
	durations += printRestartTimer("getResources <lastVersion>");
	set[Resource resource] wcResources = getWCResources(repo);
	durations += printStopTimer("getResources <lastVersion>");
	
	durations += statsThree(repo, releases);
	durations += statsFour(repo, wcResources);
*/
	
	int totalDuration = 0;
	for(d <- durations) {
		totalDuration += d;
	}
	print("Duration:<totalDuration>");
	
	return durations;
}

public int statsOne(map[Tag version, ChangeSet cs] tagChangeset, list[Tag] releases) {
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

public int statsTwo(rel[Tag version, RevisionId revision] versionRevisions, list[Tag] releases) {
	printStartTimer("statsTwo");
	for (version <- releases, version in versionRevisions.version) {
		print("<version> - <size(versionRevisions[version])>");
	}
	return printStopTimer("statsTwo");
}

public int statsThree(Repository repo, map[Tag version, ChangeSet cs] tagChangeset, list[Tag] releases) {
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

public void checkoutVersion(Repository repo, map[Tag version, ChangeSet cs] tagChangeset, Tag version) {
	CheckoutUnit cu;
	//little workaround for gits lack of checkout by date
	if (git(_,_,_) := repo) {
		cu = cunit(version);
	} else {
		cu = cunit(tagChangeset[version].committer.date);
	}
	checkoutResources(cu, repo);
}

public int statsFour(Repository repo, map[Tag version, ChangeSet cs] tagChangeset, list[Tag] releases, rel[str cat, str dir] catDirs) {
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
/*
public set[Resource resource] getWCResources(Repository repo) {
	wcResources = getResources(repo);
	switch(wcResources) {
		case revisions(map[Resource resource, Revision revision] revisions): return revisions.resource;
		case resources(set[Resource resource] resources): return resources;
		default : throw IllegalArgument("Can\'t get resources from: <wcResources>");
	}
}*/

public int statsFour(Repository repo, set[Resource resource] resources, rel[str cat, str dir] catDirs) {
	int duration = 0;

	printStartTimer("resourcesByCategory");
	rel[str cat, Resource file] filesByCat = resourcesByCategory(resources, catDirs<1,0>);
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

public rel[str cat, Resource file] resourcesByCategory(set[Resource resource] resources, rel[str dir, str cat] dirCategories) {
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
public int statsFive(rel[Tag version, ChangeSet cs] versionChangesets, map[str, str] domainMap, list[Tag] releases) {	
	int duration = 0;
	rel[Tag version, str email, str devverName, ChangeSet cs] result = {};
	printStartTimer("calcDevelopers");
	//for (version <- releases, version in versionChangesets.version) {
	//	result += calcDevelopers(version, "Author", {<cs@author, cs>| cs <- versionChangesets[version]}, false)<0,2,3,4>;
	//}
	
	for (version <- releases, version in versionChangesets.version) {
		result += calcDevelopers(version, "Author", {<cs@author ? cs.committer, cs>| cs <- versionChangesets[version]}, false)<0,2,3,4>;
	}
	
	duration += printRestartTimer("calcDevelopers");
	print(releases);
	print(result.version);
	print(size(result));
	print(size(result.devverName));

	for (version <- releases, version in result.version) {
		map[set[str name] user, set[ChangeSet] cs] userChanges = getUserChangeSets(result[version]);
		map[set[str name] user, int count] userChangesCount = (usr : size(userChanges[usr]) | usr <- domain(userChanges));
		duration += printRestartTimer("<version.name> - <size(userChanges.user)> users");
		printMapOrderedOnRange(userChangesCount, 10);
	}
	startTimer();//quietly restart the timer
	map[str mail, str company] emailCompany = (addr : domainMap[dom] | dom <- domain(domainMap), addr <- result.email, endsWith(addr, dom) );
	duration += printRestartTimer("emailCompany");
	rel[Tag version, str company, ChangeSet cs] versionCompanyChangesets = 	
		{<version, emailCompany[email] ? "Unknown", cs> | <Tag version, str email, str devverName, ChangeSet cs> <- result};
	duration += printRestartTimer("versionCompanyChangesets");
	for (version <- releases, version in versionCompanyChangesets.version) {
		rel[str company, ChangeSet cs] companyChangesets = versionCompanyChangesets[version];
		map[str company, int changes] companyChangesCount = (company : size(companyChangesets[company]) | company <- domain(companyChangesets));
		duration += printRestartTimer("\n<version.name> & <size(companyChangesets.company)> companies");
		printMapOrderedOnRange(companyChangesCount, 10);
	}
	duration += printStopTimer("versionCompanyChangesets");
	return duration;
}

public map[set[str name] user, set[ChangeSet] cs] getUserChangeSets(rel[str email, str devverName, ChangeSet cs] input) {
	rel[str name, str email] devMail = {<user, email>  | email <- input.email, email != "", user <- input[email]<0>, user != ""};
	//rel[str name, str email] devMail = {<user, mail> | user <- domain(input), user != "", mail <- input[user]<0>, mail != ""};
	rel[str name, ChangeSet cs] userCs = input[_];
	rel[str email, ChangeSet cs] mailCs = input<0,2>;
	
	devMail = solveBrokenRelations(devMail);
	
	mailDev = devMail<1,0>;
	map[str mail, set[str] userNames] mailUserNames = (email : mailDev[email] | email <- range(devMail));
	map[set[str] userNames, set[str] mailAdresses] users = invert(mailUserNames);
	
	map[set[str name] user, set[ChangeSet] cs] result = 
		(userNames : domainR(userCs, userNames)<1> + domainR(mailCs, users[userNames])<1> | set[str] userNames <- users.userNames);
	
	set[str] processedUsernames = {userName | userName <- users.userNames};
	set[str] usersWithoutMail = domainX(userCs, processedUsernames)<0>;
	result += ({user} : userCs[user]| user <- usersWithoutMail);
	
	return result;
}

/**
* 	Makes sure that each name has a relation with any email known for the same user, 
*	even if alternative usernames are used
*	For example, if the tuple <Linus Torvalds, linus@linux.com> exists in the input along with two
*	other tuples: <Linus Torvalds, linux@linux.com> and <Torvalds, linux@linux.com>, then the resulted set will 
*	have the additional tuple: <Torvalds, linus@linux.com>.
*/
public rel[str name, str email] solveBrokenRelations(rel[str name, str email] input) {
	r = input;
	solve(r) {
		r = r o invert(r) o r;
	}
	return r;
} 

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
	for (int v <- reverse(quickSort(range(content))), d <- rangeR(content, {v}), topMax >= 0) {
		topMax -= 1;
		print("<d> - <v> - <v*100/totalCount>%");
	}
}

public rel[Tag version, str action, str email, str devverName, ChangeSet cs] calcDevelopers(Tag version, str action, 
	rel[Info info, ChangeSet cs] devvers, bool parseCsMessage) {
	rel[Tag version, str action, str email, str devverName, ChangeSet cs] results = {};
	for (Info info <- devvers.info) {
		str msg = (parseCsMessage ? (info.message ? "") : "") + " " + action + "-by: " + info.name;
		for (/\s*<action:[^\s]*>-by:\s*<name:.*>/ := msg) {
			if (/\s*"?<fname:[^\<"]+>"?\s\<<mail:[^\>]+>\>/ := name) {
				results += {<version, action, toLowerCase(mail), fname, cs> | cs <- devvers[info]};
			} else if (/\<<mail:[^\>]+>\>/ := name){
				results += {<version, action, toLowerCase(mail), "", cs> | cs <- devvers[info]};
			} else {
				results += {<version, action, "", name, cs> | cs <- devvers[info]};
			}
		}
	}
	return results;
} 

//----------------------------------------


public tuple[tuple[list[Tag] releases, Repository repo, list[ChangeSet] changesets],
	tuple[rel[RevisionId child, RevisionId parent] childParents, 
	map[Tag version, ChangeSet changeset] tagChangeset, 
	map[RevisionId revId, ChangeSet cs] revChangeset,
	rel[Tag version, RevisionId revId] versionRevisions,
	rel[Tag version, ChangeSet cs] versionNoMergesChangesets]] init() {
	return <initChangesets(),initMappings(())>;
}

public void setVars(tuple[list[Tag] releases, Repository repo, list[ChangeSet] changesets] initChanges) {
	releases = initChanges.releases;
	repo = initChanges.repo;
	changesets = initChanges.changesets;
}

public void setVars(
tuple[rel[RevisionId child, RevisionId parent] childParents,map[Tag version, ChangeSet changeset] tagChangeset,
	map[RevisionId revId, ChangeSet cs] revChangeset, rel[Tag version, RevisionId revId] versionRevisions,
	rel[Tag version, ChangeSet cs] versionNoMergesChangesets] initMaps) {
	
	childParents = initMaps.childParents;
	tagChangeset = initMaps.tagChangeset;
	revChangeset = initMaps.revChangeset;
	versionRevisions = initMaps.versionRevisions;
	versionNoMergesChangesets = initMaps.versionNoMergesChangesets;
}

public void setVars(tuple[tuple[list[Tag] releases, Repository repo, list[ChangeSet] changesets],
	tuple[rel[RevisionId child, RevisionId parent] childParents, 
	map[Tag version, ChangeSet changeset] tagChangeset, 
	map[RevisionId revId, ChangeSet cs] revChangeset,
	rel[Tag version, RevisionId revId] versionRevisions,
	rel[Tag version, ChangeSet cs] versionNoMergesChangesets]] vars) {
	setVars(vars[0]);
	setVars(vars[1]);
}

public void setVars(tuple[list[Tag] releases, Repository repo, list[ChangeSet] changesets] initChanges,
	tuple[rel[RevisionId child, RevisionId parent] childParents,map[Tag version, ChangeSet changeset] tagChangeset,
	map[RevisionId revId, ChangeSet cs] revChangeset, rel[Tag version, RevisionId revId] versionRevisions,
	rel[Tag version, ChangeSet cs] versionNoMergesChangesets] initMaps) {

	setVars(initChanges);
	setVars(initMaps);
}

public list[ChangeSet] getChanges(Repository repo) {
	printStartTimer("initChangesets");
	changesets = getChangesets(repo);
	//changesets = readBinaryValueFile(#(list[ChangeSet]), |file:///export/scratch1/shabazi/benchmark/initChangesets<now()>.bin|);
	printStopTimer("initChangesets");
	writeBinaryValueFile(|file:///export/scratch1/shabazi/benchmark/initChangesets<now()>.bin|, changesets); 
	
	//changesets = readLinuxKernel("/export/scratch1/shabazi/logs/partial/", "linuxHistory", label(""), label("v2.6.22-rc1"));
	return changesets;
}

public MappingVars getMappings(InitVars initVars, map[Tag version, CheckoutUnit cunit] manualReleases) {
	changesets = initVars.changesets;
	repo = initVars.repo;
	releases = initVars.releases;
	
	int total = 0;
	printStartTimer("childParents");
	childParents = {<cs.revision.id, m.parent.id> | cs <-changesets, revision(_, _) := cs.revision, m <- (cs.revision@mergeDetails ? {mergeParent(cs.revision.parent)})};
	total += printRestartTimer("childParents");
	revChangeset = (cs.revision.id : cs |cs <- changesets);
	total += printRestartTimer("revChangeset");
	tagChangeset = (t : cs | cs <- changesets, t <- (cs.revision@tags ? {}));
	extraTags = (t : revChangeset[rev.id] | t <- manualReleases, cunit(Revision rev) := manualReleases[t]);
	if (size(extraTags) < size(manualReleases)) {
		dateChangeset = (cs.committer.date : cs |cs <- changesets);
		//extraDates = {date | t <- manualReleases, cunit(datetime date) := manualReleases[t]};
		
		//extraTags += (t : dateChangeset[date] | t <- manualReleases, cunit(datetime date) := manualReleases[t]);
	}
	tagChangeset += extraTags;
	if (size(extraTags) < size(manualReleases)) {
		tagChangeset += (t : tagChangeset[symName] | t <- manualReleases, cunit(Tag symName) := manualReleases[t]);
	}
	total += printRestartTimer("tagChangeset");
	
	totalVersionRevisions = getVersionRevisions(repo, childParents, tagChangeset, releases);
	total += totalVersionRevisions[0];
	versionRevisions = getOnlyUniqueRevisions(totalVersionRevisions[1], releases);
	total += printRestartTimer("uniqueVersionRevisions");
	rel[Tag version, ChangeSet cs] versionChangesets = versionRevisions o toRel(revChangeset);
	total += printRestartTimer("versionChangesets");
	versionNoMergesChangesets = {<version, cs> | version <- versionChangesets.version, cs <- versionChangesets[version], "mergeDetails" notin getAnnotations(cs.revision)};
	total += printStopTimer("versionNoMergesChangesets");
	print("Total duration:<total>");
	
	mappingVars = <childParents, tagChangeset, revChangeset, versionRevisions, versionNoMergesChangesets>;
	writeBinaryValueFile(|file:///export/scratch1/shabazi/benchmark/versionRevisions<now()>.bin|, versionRevisions); 
	return mappingVars;
}

public tuple[int total, rel[Tag version, RevisionId revision] versionRevisions] getVersionRevisions(
	Repository repo, rel[RevisionId child, RevisionId parent] childParents, map[Tag version, ChangeSet cs] tagChangeset, list[Tag] releases) {
	int total = 0;
	bool gitRepo = (git(_,_,_) := repo);
	
	rel[Tag version, RevisionId revision] versionRevisions = {};
	for (version <- releases, version in tagChangeset) { 
		set[RevisionId] reachable = {};
		if (gitRepo) {
			reachable = reach(childParents, {tagChangeset[version].revision.id});	
		} else {
			reachable = reach(childParents, {tagChangeset[version].revision.id}, {tagChangeset[ver].revision.id| ver <- tagChangeset.version, ver != version});
		}
		versionRevisions += {<version, reaching> | reaching <- reachable};
		total += printRestartTimer("versionRevision - <version> - <size(reachable)>");
	}
	return <total, versionRevisions>;
}
/**
* Makes sure that a revisionId is only referenced by one version. So if revision A is part of release 12 and 13, release 13 will
* no longer reference to it in the returned relation.
*/
public rel[Tag version, RevisionId revision] getOnlyUniqueRevisions(rel[Tag version, RevisionId revision] versionRevisions, list[Tag] releases) {
	rel[Tag version, RevisionId revision] results = {};
		
	int i = 0;
	for (version <- releases, version in versionRevisions.version) {
		if (i == 0) {
			results += {<version, rev> | rev <- versionRevisions[version]};
		} else {
			prev = versionRevisions[releases[i-1]];
			results += {<version, rev> | rev <- versionRevisions[version], rev notin prev};
		}
		i += 1;
	}
	return results;
}

public datetime printStartTimer(str msg) {
	sTime = startTimer();
	print("started at <sTime> \t[<msg>]");
	return sTime;
}
public int printStopTimer(str msg) {
	dur = stopTimer();
	print("duration <dur> ms \t[<msg>]");
	return dur;
} 
public int printRestartTimer(str msg) {
	dur = stopTimer();
	print("duration <dur> ms \t[<msg>], restarted <startTimer()>");
	return dur;
}

public list[ChangeSet] readLinuxKernel(str directory, str prefix, Tag start, Tag end) {
	list[ChangeSet] changesets = [];
	printStartTimer("readLinuxKernel");
	
	map[int, set[Tag]] index = readBinaryValueFile(#(map[int, set[Tag]]), |file://<directory><prefix>Index.bin|);
	list[int] indexCounter = quickSort(toList(domain(index)));
	int startIndex = 0;
	int endIndex = max(indexCounter);
	for (i <- indexCounter) {
		if (start in index[i]) {
			startIndex = i;
		} else if (end in index[i]) {
			endIndex = i;
		}
	}
	for (i <- indexCounter, i > startIndex && i <= endIndex) {
		changesets += readBinaryValueFile(#(list[ChangeSet]), |file://<directory><prefix><i>.bin|);
		print("Read <index[i]> size: <size(changesets)>");
	}
	printStopTimer("readLinuxKernel");
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