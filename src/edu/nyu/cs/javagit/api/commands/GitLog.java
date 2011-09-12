/*
 * ====================================================================
 * Copyright (c) 2008 JavaGit Project.  All rights reserved.
 *
 * This software is licensed using the GNU LGPL v2.1 license.  A copy
 * of the license is included with the distribution of this source
 * code in the LICENSE.txt file.  The text of the license can also
 * be obtained at:
 *
 *   http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *
 * For more information on the JavaGit project, see:
 *
 *   http://www.javagit.com
 * ====================================================================
 */
package edu.nyu.cs.javagit.api.commands;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.commands.GitLogResponse.Commit;
import edu.nyu.cs.javagit.client.ClientManager;
import edu.nyu.cs.javagit.client.IClient;
import edu.nyu.cs.javagit.client.IGitLog;
import edu.nyu.cs.javagit.utilities.CheckUtilities;
import experiments.scm.ScmLogEntryHandler;
/**
 * 	<code>GitLog</code> provides an interface to retrieve logs from a git
 * 	repository
 */
public final class GitLog {
	
	/**
	 * 
	 * @param repositoryPath	
	 *			A <code>File</code> instance for the path to the repository root (the parent
	 *          directory of the .git directory) or a sub-directory in the working tree of the
	 *          repository to move/rename against. This argument must represent the absolute path to 
	 *          the desired directory as returned by the <code>File.getPath()</code> method. If null 
	 *          is passed, a <code>NullPointerException</code> will be thrown.
	 *
	 * @param options	Specify option used to collect logs, see <code>GitLogOptions</code>
	 * @param handler	Gets called for every entry in the log
	 * @return A List of Commit objects
	 * @throws JavaGitException
	 * 			Thrown when there is an error executing git log.
	 * @throws IOException
	 * 			Thrown when there is an error executing git log.
	 */
	public List<Commit> log(File repositoryPath, GitLogOptions options, ScmLogEntryHandler<Commit> handler) throws JavaGitException, IOException{
		CheckUtilities.checkNullArgument(repositoryPath, "repository");
	    
	    IClient client = ClientManager.getInstance().getPreferredClient();
	    IGitLog gitLog = client.getGitLogInstance();
	    return gitLog.log(repositoryPath, options, handler);
		
	}
	
	/**
	 * 
	 * @param repositoryPath	
	 *			A <code>File</code> instance for the path to the repository root (the parent
	 *          directory of the .git directory) or a sub-directory in the working tree of the
	 *          repository to move/rename against. This argument must represent the absolute path to 
	 *          the desired directory as returned by the <code>File.getPath()</code> method. If null 
	 *          is passed, a <code>NullPointerException</code> will be thrown.
	 *
	 * @param options	Specify option used to collect logs, see <code>GitLogOptions</code>
	 * @return A List of Commit objects
	 * @throws JavaGitException
	 * 			Thrown when there is an error executing git log.
	 * @throws IOException
	 * 			Thrown when there is an error executing git log.
	 */
	public List<Commit> log(File repositoryPath, GitLogOptions options) throws JavaGitException, IOException{
		CheckUtilities.checkNullArgument(repositoryPath, "repository");
	    
	    IClient client = ClientManager.getInstance().getPreferredClient();
	    IGitLog gitLog = client.getGitLogInstance();
	    return gitLog.log(repositoryPath,options);
		
	}
	
	/**
	 * 
	 * @param repositoryPath	
	 *			A <code>File</code> instance for the path to the repository root (the parent
	 *          directory of the .git directory) or a sub-directory in the working tree of the
	 *          repository to move/rename against. This argument must represent the absolute path to 
	 *          the desired directory as returned by the <code>File.getPath()</code> method. If null 
	 *          is passed, a <code>NullPointerException</code> will be thrown.
	 *
	 * @return A List of Commit objects
	 * @throws JavaGitException
	 * 			Thrown when there is an error executing git log.
	 * @throws IOException
	 * 			Thrown when there is an error executing git log.
	 */
	public List<Commit> log(File repositoryPath) throws JavaGitException, IOException{
		CheckUtilities.checkNullArgument(repositoryPath, "repository");
	    
	    IClient client = ClientManager.getInstance().getPreferredClient();
	    IGitLog gitLog = client.getGitLogInstance();
	    return gitLog.log(repositoryPath);
		
	}

}