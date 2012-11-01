module CvsStatsExample

import ChangeSetRecovery;
import Utilities;
import Statistics;
import resource::versions::Versions;
import resource::versions::cvs::Cvs;


import DateTime;
import List;
import Map;
import Set;
import Relation;
import Graph;
import String;
import Real;
import Node;

public tuple[list[int], InitVars, MappingVars] cvsStats () {
    cvsConfig = getCvsConfig();
    list[ChangeSet] resourceChanges = getChanges(cvsConfig.repo);
    transRecovered = recoverChangesets(resourceChanges, 200);
    InitVars initVars = <cvsConfig.releases, cvsConfig.repo, cvsConfig.catDirs, transRecovered[1]>;
    
    MappingVars maps = getMappings(initVars, ());
    return <stats(initVars, maps), initVars, maps>;
}

public tuple[list[Tag] releases, Repository repo, rel[str cat, str dir] catDirs] 
		getCvsConfig() {
    repo = cvs(fs("/export/scratch1/shabazi/CVS"),"software_20040603",
    	|file:///export/scratch1/shabazi/cvsWorkingCopy|,{});
    releases = [label("Champagne_Release"), label("RELEASE_0_2"), 
		label("api-release"), label("release_1_4_5"), 
		label("v_1_5_3"), label("release"), label("STABLE-2003-10-08"), 
		label("STABLE-2003-12-18"),label("STABLE-2004-02-15")];
    return <releases, repo, {}>;
}


