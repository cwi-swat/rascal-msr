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

@SuppressWarnings("serial")
public class ScmProviderException extends Exception {

	public ScmProviderException() {
		super();
	}
	
    public ScmProviderException(String message) {
        super(message);
    }
    
    public ScmProviderException(String message, Throwable cause) {
        super(message, cause);
    }
 
}
