module Scm

data Project = project(Repository configuration, list[ChangeSet] changesets);

data Repository;
data Connection = fs(str url);
data LogOption = startUnit(CheckoutUnit unit) | endUnit(CheckoutUnit unit);

data ChangeSet;
data RevisionChange = added(Revision revision) 
	| modified(Revision revision) | removed(Revision revision);
data Revision = revision(RevisionId id) 
	| revision(RevisionId id, Revision parent);
data RevisionId;
data Info = none(datetime date) | author(datetime date, str name) 
	| message(datetime date, str message) 
	| message(datetime date, str name, str message);
data Resource = file(loc id) | folder(loc id) 
	| folder(loc id, set[Resource] resources);
data WcResource;

data CheckoutUnit = cunit(Revision revision) | cunit(datetime date) 
	| cunit(Tag symname);
data Tag = label(str name) | branch(str name);

anno loc Connection@logFile;
anno set[Tag] Revision@tags;

@javaClass{experiments.scm.Scm}
public java list[ChangeSet]  getChangesets(Repository repository);

@javaClass{experiments.scm.Scm}
public java void  getChangesets(Repository repository, ChangeSet (ChangeSet) callBack);

@javaClass{experiments.scm.Scm}
public java void  checkoutResources(CheckoutUnit unit, Repository repository);

@javaClass{experiments.scm.Scm}
public java set[WcResource]  getResources(Repository repository);

@javaClass{experiments.scm.Scm}
public java map[Resource, int]  linesCount(set[Resource] files);

@javaClass{experiments.scm.Scm}
public java set[Resource]  buildResourceTree(set[Resource] files);
