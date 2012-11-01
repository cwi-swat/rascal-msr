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
import org.eclipse.imp.pdb.facts.ISet;
import org.rascalmpl.interpreter.result.RascalFunction;

import resource.versions.ScmTypes.Info;
import resource.versions.ScmTypes.Repository;
import resource.versions.ScmTypes.Revision;
import resource.versions.ScmTypes.WcResource;

public interface ScmProvider<E extends ScmLogEntryHandler<?>> {
	
	public E createLogEntryHandler(IConstructor repository, RascalFunction factExtractor, IListWriter logEntriesWriter);
	public E extractLogs(IConstructor repository, RascalFunction factExtractor, IListWriter logEntriesWriter) throws ScmProviderException;
	public void extractLogs(IConstructor repository, E handler) throws ScmProviderException;
	
	/**
	 * Checkout the resources of the given repository according to the checkoutUnit.
	 * @param checkoutUnit of type {@link ScmTypes.CheckoutUnit} containing information about the resources to checkout
	 * @param repository of type {@link Repository} containing information about the repository to checkout from.
	 */
	public void checkoutResources(IConstructor checkoutUnit, IConstructor repository) throws ScmProviderException;

	/**
	 * Gets a set of the resources on the workspace specified by the repository configuration.
	 * @param repository of type {@link Repository} containing information about the repository workspace to get
	 * the resources from.
	 * @return a set of resources as an {@link WcResource} with optionally {@link Revision} and {@link Info} fields.
	 * @throws ScmProviderException when something goes wrong during the listing of the resources.
	 */
	public ISet getResources(IConstructor repository) throws ScmProviderException;
}
