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
package edu.nyu.cs.javagit.test.utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.nyu.cs.javagit.api.JavaGitConfiguration;
import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.commands.CommandResponse;
import edu.nyu.cs.javagit.client.cli.IParser;
import edu.nyu.cs.javagit.client.cli.ProcessUtilities;

/**
 * This class contains methods that implement some git commands that aren't coded into javagit yet
 * but which are required to test other commands already written.
 */
public class HelperGitCommands {

  /**
   * Initialize a git repository.
   * 
   * @param repoDirectory
   *          The root directory of the repository.
   * @throws IOException
   *           If IO errors happen.
   * @throws JavaGitException
   *           If errors happen while initializing the repo.
   */
  public static void initRepo(File repoDirectory) throws IOException, JavaGitException {
    List<String> cmdLine = new ArrayList<String>();

    cmdLine.add(JavaGitConfiguration.getGitCommand());
    cmdLine.add("init");

    ProcessUtilities.runCommand(repoDirectory, cmdLine, new IParser() {
      public CommandResponse getResponse() {
        return null;
      }

      public void processExitCode(int code) {
      }
      
      public void parseLine(String line) {
      }
    });
  }

}
