@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI}
@contributor{Waruzjan Shahbazian - waruzjan@gmail.com}
module resource::versions::git::Git
import resource::versions::Versions;

data Repository = git(Connection conn, str \mod, set[LogOption] options);

data LogOption 
  = mergeDetails() 
  | fileDetails() 
  | symdiff(CheckoutUnit from, CheckoutUnit to) 
  | onlyMerges() 
  | noMerges() 
  | reverse() 
  | allBranches()
  ;				 

data ChangeSet = changeset(Revision revision, rel[Resource resource, RevisionChange change] resources, Info committer);

data RevisionChange 
  = renamed(Revision revision, Resource origin) 
  | copied(Revision revision, Resource origin)
  ;

data RevisionId	= hash(Sha sha);

data Sha 
  = blob(str sha) 
  | commit(str sha)
  ;

data MergeDetail = mergeResources(Revision parent, rel[Resource resource, RevisionChange change] resources);

data WcResource = wcResource(Resource resource);

data CheckoutUnit 
  = cunit(Revision revision) 
  | cunit(Tag symname)
  ;

anno loc Connection@logFile;
anno Info ChangeSet@author;
anno int RevisionChange@originPercent;
anno int RevisionChange@linesAdded;
anno int RevisionChange@linesRemoved;
anno list[MergeDetail] Revision@mergeDetails;
