@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI}
@contributor{Waruzjan Shahbazian - waruzjan@gmail.com}
module resource::versions::Versions

data Project = project(Repository configuration, list[ChangeSet] changesets);

data Repository;

data Connection = fs(str url);

data LogOption 
  = startUnit(CheckoutUnit unit) 
  | endUnit(CheckoutUnit unit)
  ;

data ChangeSet;

data RevisionChange 
  = added(Revision revision) 
  | modified(Revision revision) 
  | removed(Revision revision)
  ;
	
data Revision 
  = revision(RevisionId id) 
  | revision(RevisionId id, Revision parent)
  ;
  
data RevisionId;

data Info 	
  = none(datetime date) 
  | author(datetime date, str name) 
  | message(datetime date, str message) 
  | message(datetime date, str name, str message);
  
data Resource 
  = file(loc id) 
  | folder(loc id) 
  | folder(loc id, set[Resource] resources)
  ;
  
data WcResource;

data CheckoutUnit;

data Tag = label(str name) | branch(str name);

anno set[Tag] Revision@tags;

@javaClass{resource.versions.Versions}
public java list[ChangeSet] getChangesets(Repository repository);

@javaClass{resource.versions.Versions}
public java void getChangesets(Repository repository, ChangeSet (ChangeSet) callBack);

@javaClass{resource.versions.Versions}
public java void checkoutResources(CheckoutUnit unit, Repository repository);

@javaClass{resource.versions.Versions}
public java set[WcResource] getResources(Repository repository);

@javaClass{resource.versions.Versions}
public java map[Resource, int] linesCount(set[Resource] files);

@javaClass{resource.versions.Versions}
public java set[Resource] buildResourceTree(set[Resource] files);