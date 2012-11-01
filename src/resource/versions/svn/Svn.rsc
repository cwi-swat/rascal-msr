@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI}
@contributor{Waruzjan Shahbazian - waruzjan@gmail.com}
module resource::versions::svn::Svn

import resource::versions::Versions;

@doc{
Examples:
<screen>
import resource::versions::cvs::Cvs;
rp = svn(ssh("svn+ssh://svn.cwi.nl","jurgenv", "", |file:///Users/jurgenv/.ssh/id_rsa_svn_glt|),"", |tmp://svnexp|, {fileDetails()});
</screen>
}
data Repository = svn(Connection conn, str \mod, loc workspace, set[LogOption] options);

data Connection 
  = ssh(str url, str username, str password) 
  | ssh(str url, str username, str password, loc privateKey)
  ;

data LogOption = mergeDetails() | fileDetails();

data ChangeSet
  = changeset(Revision revision, rel[Resource resource, RevisionChange change] resources, Info committer); 

data RevisionChange 
  = added(Revision revision, Resource origin) 
  | replaced(Revision revision) 
  | replaced(Revision revision, Resource origin)
  ;

data RevisionId	= id(int id);

data MergeDetail = mergeParent(Revision parent);

data WcResource = wcResourceRevisionInfo(Resource resource, Revision revision, Info info);
	
data CheckoutUnit 
  = cunit(datetime date) 
  | cunit(Revision revision)
  ;

anno list[MergeDetail] Revision@mergeDetails;