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

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.client.cli.CliGitCheckout;
import edu.nyu.cs.javagit.client.cli.CliGitCheckout.GitCheckoutParser;

public class TestGitCheckoutResponse extends TestCase {

  GitCheckoutParser parser;

  @Before
  public void setUp() throws Exception {
    //CliGitCheckout gitCheckout = new CliGitCheckout();
    parser = new CliGitCheckout.GitCheckoutParser();
  }

  /**
   * Test for confirming that <code>JavaGitException</code> is thrown while trying to checkout a
   * non-existent branch from the repository. Here the error flag should be set and the branch
   * should be null as the given branch does not exist.
   */
  @Test
  public void testGitCheckoutResponseForNonExistingBranch() throws JavaGitException {
    GitCheckoutResponse response = null;
    try {
      parser.parseLine("error: pathspec 'foo03' did not match any file(s) known to git.");
      response = parser.getResponse();
      fail("Failed to throw JavaGitException");
    } catch (JavaGitException e) {
      assertEquals("Response object", null, response);
      assertEquals("Error Code", 406000, e.getCode());
    }
  }

  /**
   * Test for parsing fatal error generated while trying to create a new branch based on a
   * non-existent base-branch.
   */
  @Test
  public void testGitCheckoutResponseForCreatingBranchOnNonExistentBranch() throws JavaGitException {
    GitCheckoutResponse response = null;
    try {
      parser
          .parseLine("fatal: git checkout: updating paths is incompatible with switching branches/forcing");
      parser.parseLine("Did you intend to checkout 'foo04' which can not be resolved as commit?");
      response = parser.getResponse();
      fail("Failed to throw JavaGitException - testGitCheckoutResponseForCreatingBranchOnNonExistentBranch()");
    } catch (JavaGitException e) {
      assertEquals("Response object", null, response);
      assertEquals("Error Code", 406000, e.getCode());
    }
  }

  /**
   * Test for parsing the output line that contains the output with "Switched to branch" and the
   * name of the branch in double quotes. The output indicates that repository has been switched to
   * an already existing another branch.
   */
  @Test
  public void testGitCheckoutSwitchToBranch() throws JavaGitException {
    parser.parseLine("Switched to branch \"foo01\"");
    GitCheckoutResponse response = parser.getResponse();
    assertEquals("Switching to branch foo01", "\"foo01\"", response.getBranch().getName());
  }

  /**
   * Test for parsing the output line that contains the output with "Switched to a new branch". The
   * output indicates that the new branch with the new name within quotes has been created and
   * switched to.
   */
  @Test
  public void testGitCheckoutResponseSwitchToNewBranch() throws JavaGitException {
    parser.parseLine("Switched to a new branch \"foo02\"");
    GitCheckoutResponse response = parser.getResponse();
    assertEquals("Switching to New branch foo02", "\"foo02\"", response.getNewBranch().getName());
  }

  /**
   * Test for checking that <code>JavaGitException</code> is thrown when an invalid switch is
   * passed to &lt;git-checkout&gt; command. <code>GitCheckoutParser</code> should parse the error
   * message properly and save it into error object of <code>GitCheckoutResponse</code> object.
   */
  @Test
  public void testGitCheckoutErrorMessageResponse() throws JavaGitException {
    parser.parseLine("error: unknown switch `v'");
    parser.parseLine("usage: git checkout [options] <branch>");
    parser.parseLine("   or: git checkout [options] [<branch>] -- <file>...");
    parser.parseLine("");
    parser.parseLine("    -q, --quiet           be quiet");
    parser.parseLine("    -b <new branch>       branch");
    parser.parseLine("    -l                    log for new branch");
    parser.parseLine("    --track               track");
    parser.parseLine("    -f                    force");
    parser.parseLine("    -m                    merge");

    GitCheckoutResponse response = null;
    try {
      response = parser.getResponse();
      fail("Failed to throw JavaGitException in : testGitCheckoutErrorMessageResponse()");
    } catch (JavaGitException e) {
      assertEquals("Response Object should be NULL", null, response);
      assertEquals("Error Code", 406000, e.getCode());
    }

  }

  /**
   * Test for checking the list of files that are added, modified or deleted but have not been
   * committed to the branch in repository.
   */
  @Test
  public void testGitCheckoutResponseAddedModifiedDeletedFiles() throws JavaGitException {
    parser.parseLine("M foobar01");
    parser.parseLine("M foobar05");
    parser.parseLine("M foobar06");
    parser.parseLine("A foobar02");
    parser.parseLine("A foobar07");
    parser.parseLine("D foobar03");
    GitCheckoutResponse response = parser.getResponse();
    assertEquals("No of files in addedFilesList", 2, response.getNumberOfAddedFiles());
    assertEquals("No of files in modifiedFileslist", 3, response.getNumberOfModifiedFiles());
    assertEquals("No of files in deletedFileslist", 1, response.getNumberOfDeletedFiles());
    assertEquals("First location - modifiedFileslist", "foobar01", response.getModifiedFile(0)
        .getName());
    assertEquals("First location - deletedFileslist", "foobar03", response.getDeletedFile(0)
        .getName());
    assertEquals("First location - addedFilesList", "foobar02", response.getAddedFile(0).getName());
  }

  @After
  public void tearDown() throws Exception {
  }

}
