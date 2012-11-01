@license{
  Copyright (c) 2009-2011 CWI
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
}
@contributor{Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI}
@contributor{Waruzjan Shahbazian - waruzjan@gmail.com}
@contributor{Michael W. Godfrey - migod@uwaterloo.ca}
module resource::versions::cvs::Cvs

import resource::versions::Versions;

@doc{
Examples:
<screen>
import resource::versions::cvs::Cvs;
rp = cvs(pserver("/cvsroot/smallsql","smallsql.cvs.sourceforge.net","anonymous",""), "", |tmp:///smallsql|, {});
</screen>
}
data Repository = cvs(Connection conn, str \mod, loc workspace, set[LogOption] options);
	
data Connection 
  = pserver(str url, str repname, str host, str username, str password)
  | pserver(str url, str host, str username, str password)
  ;

data ChangeSet = resource(Resource resource, rel[RevisionChange change, Info committer] revisions, rel[Revision revision, Tag symname] revTags);

data RevisionId	= number(str number);

data WcResource = wcResourceRevisionInfo(Resource resource, Revision revision, Info info);

data CheckoutUnit = cunit(datetime date);

anno loc Connection@logFile;
anno int RevisionChange@linesAdded;
anno int RevisionChange@linesRemoved;