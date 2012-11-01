/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   * Waruzjan Shahbazian - waruzjan@gmail.com
 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
*******************************************************************************/
package resource.versions.svn;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IDateTime;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISetWriter;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.wc.ISVNInfoHandler;
import org.tmatesoft.svn.core.wc.SVNInfo;

import resource.versions.ScmTypes;
import resource.versions.Versions;
import resource.versions.ScmTypes.Info;
import resource.versions.ScmTypes.Resource;
import resource.versions.ScmTypes.Revision;
import resource.versions.ScmTypes.RevisionId;
import resource.versions.ScmTypes.WcResource;

public class SvnWcInfoHandler implements ISVNInfoHandler {
	
	private final ISetWriter resourceFilesWriter;
	
	private ISet results;
	
	public SvnWcInfoHandler() {
		resourceFilesWriter = ScmTypes.VF.setWriter(WcResource.getAbstractType());
	}
	
	public void handleInfo(SVNInfo info) throws SVNException {
		long revNumber = info.getCommittedRevision().getNumber();
		boolean isDir = (info.getKind() == SVNNodeKind.DIR);
		
		Resource resType = isDir ? Resource.FOLDER : Resource.FILE;
		
		ISourceLocation id = ScmTypes.VF.sourceLocation(Versions.encodePath(Versions.encodePath(info.getFile().getAbsolutePath())));
		IDateTime datetime = ScmTypes.VF.datetime(info.getCommittedDate().getTime());
		IConstructor revision = Revision.REVISION.make(RevisionId.ID.make(revNumber));
		
		resourceFilesWriter.insert(WcResource.RESOURCE_REVISION_INFO.make(resType.make(id), revision, Info.AUTHOR.make(datetime, info.getAuthor())));
	}
	
	/**
	 * Finalizes the info handler and gets the resources in the working copy.
	 * @return a set of resources as an {@link WcResource} with optionally {@link Revision} and {@link Info} fields.
	 */
	public ISet done() {
		if (results != null) {
			throw new IllegalStateException("Can't call done twice:" + results);
		}
		results = resourceFilesWriter.done();
		return results;
	} 
	
	
	public ISet getResults() {
		if (results == null) {
			throw new IllegalStateException("Please call done() first to finalize the fileresources writer");
		}
		return results;
	}
}
