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
package resource.versions;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IListWriter;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.rascalmpl.interpreter.result.RascalFunction;

public abstract class AbstractScmLogEntryHandler<E> implements ScmLogEntryHandler<E> {

	protected final IConstructor repository;
	protected final RascalFunction factExtractor;
	protected final IListWriter logEntriesWriter;
//	protected IList logEntries;
	
	protected AbstractScmLogEntryHandler(IConstructor repository, RascalFunction factExtractor, IListWriter logEntriesWriter) {
		this.repository = repository;
		this.factExtractor = factExtractor;
		this.logEntriesWriter = logEntriesWriter; //ScmTypes.VF.listWriter(ScmTypes.AbstractDataType.RESOURCE.getType());
	}
	
	/**
	 * Calls back to the factextractor function script in Rascal.
	 * @param changeSet containing the changeSet to send to the rascal function
	 * @return the changeSet returned by the rascal function
	 */
	protected IConstructor callBack(IConstructor changeSet) {
		if (logEntriesWriter != null) {
			logEntriesWriter.append(changeSet);
		}
		if (factExtractor != null) {
			Type argumentTypes = factExtractor.getFunctionType().getArgumentTypes();
	    	Type fieldType = argumentTypes.getFieldType(0);
	    	changeSet = (IConstructor) factExtractor.call(new Type[] {fieldType}, new IValue[] {changeSet}, null).getValue();
		}
    	
    	return changeSet;
	}
	
	/*protected void addLogEntry(IConstructor logEntry) {
		logEntriesWriter.append(logEntry);
	}
	
	public IList done() {
		if (logEntries != null) {
			throw new IllegalStateException("LogEntries is already finalized:" + logEntries);
		}
		logEntries = logEntriesWriter.done();
		return logEntries;
	}
	
	public IList getLogEntries() {
		if (logEntries == null) {
			throw new IllegalStateException("LogEntries is not finalized, please call done() first!");
		}
		return logEntries;
	}
	*/
	/**
	 * 
	 * @param workspace should end with a '/'
	 * @param file should not start with a '/'
	 * @return
	
	public static URI createFileURI(String workspace, String file) {
		try {
			return new URI("file", workspace + file, null);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	} */
	
	/*
	protected URI createPathURI(String schema, String host, String repositoryUrl, String repositoryFilename, String workspacePath, String revision) {
		String path = repositoryFilename.substring(repositoryUrl.length());
		return createPathURI(schema, host, workspacePath + path, revision);
	}
	
	protected URI createPathURI(String schema, String host, String path, String query) {
		try {
			return new URI(schema, host, path.startsWith("/") ? path : "/" + path, query, null);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}*/
}
