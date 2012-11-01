module experiments::ChangeSetRecovery

import Set;
import IO;
import experiments::Utilities;

import resource::versions::Versions;
import resource::versions::svn::Svn;
import resource::versions::cvs::Cvs;
import resource::versions::Timer;

public tuple[rel[Info committer, Resource resource, RevisionChange change, 
       set[Tag] tags], 
       list[ChangeSet]] recoverChangesets(list[ChangeSet] resourceChanges, 
       int maxSecondsDiff) {

    rel[Info committer, Resource resource, RevisionChange change, 
	set[Tag] tags] trans = recoverTransactions(resourceChanges, maxSecondsDiff);
    return <trans, createChangesets(trans)>;
}

public list[ChangeSet] createChangesets(rel[Info committer, 
	Resource resource, RevisionChange change, set[Tag] tags] changes) {
    printStartTimer("dateInfo");
    rel[datetime date, Info info] dateInfo = {<info.date,info>
	|info <- changes.committer};
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
	    rel[Resource resource, RevisionChange change, set[Tag] tags] tmp 
		    = changes[info];
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

public rel[Info committer, Resource resource, RevisionChange change, 
	set[Tag] tags] recoverTransactions(list[ChangeSet] resourceChanges, 
	int maxSecondsDiff) {
    printStartTimer("authorMessageDateCs");
    rel[str author, str message, datetime date, Resource resource, 
	    RevisionChange change, set[Tag] tags] authorMessageDateRevisions 
	    = {<rev[1].name ? "", rev[1].message ? "", 
		rev[1].date, cs.resource, rev[0], 
		cs.revTags[rev[0].revision]> 
		| cs <- resourceChanges, rev <- cs.revisions};
    printRestartTimer("transactions");
    int msgCounter = 0;

    rel[Info committer, Resource resource, RevisionChange change, 
	set[Tag] tags] transactions = {};
    for(auth <- authorMessageDateRevisions.author) {
	for(msg <- authorMessageDateRevisions[auth]<0>) {
	    msgCounter += 1;
	    set[ChangeSet] processed = {};
	    rel[datetime date, Resource resource, RevisionChange change, 
		    set[Tag] tags] dateResources 
		    = authorMessageDateRevisions[auth][msg];
	    list[datetime] sortedDates = quickSort(dateResources.date);
	    
	    datetime startDate = sortedDates[0];
	    Info info = message(startDate, auth, msg);
	    for(date <- sortedDates) {
		if (minutesDiff(startDate, date) > maxSecondsDiff) {
		    startDate = date;
		    info = message(startDate, auth, msg);
		}
		transactions += {<info, resRev[0], resRev[1], resRev[2]>
			| resRev <- dateResources[date]};
		//TODO resource can't be twice in a changeset
		}
		
	}
	print("Processed [<auth>]s <msgCounter> changeset, transactions <size(transactions.committer)>");
	msgCounter = 0;
    }
    printStopTimer("transactions");
    return transactions;
}
