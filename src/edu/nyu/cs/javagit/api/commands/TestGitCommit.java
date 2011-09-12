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
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.commands.GitAdd;
import edu.nyu.cs.javagit.api.commands.GitCommit;
import edu.nyu.cs.javagit.api.commands.GitCommitOptions;
import edu.nyu.cs.javagit.test.utilities.FileUtilities;
import edu.nyu.cs.javagit.test.utilities.HelperGitCommands;

/**
 * Implements test cases for for GitCommit.
 */
public class TestGitCommit extends TestCase {

  /*
   * TODO (jhl388): Create tests for the following:
   * 
   * commands -- add, move(same dir, different dir in same tree, totally different dir), delete,
   * copy (same as for move), just edits
   * 
   * methods -- all methods in GitCommit, thus all the types of commits.
   * 
   * errors -- test errors states returned by git-commit (?)
   */

  private File repoDirectory;
  private GitCommit commit;
  private GitAdd add;

  private GitCommitOptions options;
  private List<File> paths;

  @Before
  protected void setUp() throws IOException, JavaGitException {
    repoDirectory = FileUtilities.createTempDirectory("GitCommitTestRepo");
    GitInit gitInit = new GitInit();
    gitInit.init(repoDirectory);
    commit = new GitCommit();
    add = new GitAdd();

    options = new GitCommitOptions();
    paths = new ArrayList<File>();
  }

  @After
  protected void tearDown() throws JavaGitException {
    // delete repo dir
    FileUtilities.removeDirectoryRecursivelyAndForcefully(repoDirectory);
  }

  @Test
  public void testCommit() throws IOException, JavaGitException {
    File testFile = FileUtilities.createFile(repoDirectory, "fileA.txt", "Sameple Contents");

    // Add a file to the repo
    List<File> filesToAdd = new ArrayList<File>();
    // filesToAdd.add(new File("fileA.txt"));
    filesToAdd.add(testFile);
    add.add(repoDirectory, null, filesToAdd);

    // Call commit
    GitCommitResponse resp = commit.commit(repoDirectory, "Making a first test commit");
    resp.getFilesChanged();
    assertEquals("Short comment not as expected", resp.getCommitShortComment(),
        "Making a first test commit");
    assertEquals("", resp.getFilesChanged(), 1);
    assertEquals("", resp.getLinesDeleted(), 0);
    assertEquals("", resp.getLinesInserted(), 1);
    // Can't assert the short hash because it changes with the date.
    // assertEquals("", resp.getCommitShortHashName(), "5f8e2d7");

    // TODO (jhl388): check number of each file type
    // TODO (jhl388): check the files themselves.
  }

  @Test
  public void testBadArugmentPassing() throws IOException, JavaGitException {
    // GitCommit.commitAll(String, String);
    assertCommitAllNPEThrown(null, null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[repository] }");
    assertCommitAllNPEThrown(new File("SomePath"), null,
        "000001: A String argument was not specified but is required.  { variableName=[message] }");

    assertCommitAllIllegalArgumentExceptionThrown(new File("SomePath"), "",
        "000001: A String argument was not specified but is required.  { variableName=[message] }");

    // GitCommit.commit(String, GitCommitOptions, String);
    assertCommitWithOptsNPEThrown(null, null, null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[repository] }");
    assertCommitWithOptsNPEThrown(new File("something"), null, null,
        "000001: A String argument was not specified but is required.  { variableName=[message] }");
    assertCommitWithOptsNPEThrown(new File("/some/path"), null, "A message",
        "000003: An Object argument was not specified but is required.  { variableName=[options] }");

    assertCommitWithOptsIllegalArgumentExceptionThrown(new File("c:\\path\\to somewhere"), null,
        "",
        "000001: A String argument was not specified but is required.  { variableName=[message] }");

    // GitCommit.commit(String, GitCommitOptions, String, List<String>);
    assertCommitAllParametersNeededNPEThrown(null, null, null, null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[repository] }");
    assertCommitAllParametersNeededNPEThrown(new File("something"), null, null, null,
        "000001: A String argument was not specified but is required.  { variableName=[message] }");
    assertCommitAllParametersNeededNPEThrown(new File("/some/path"), null, "A message", null,
        "000003: An Object argument was not specified but is required.  { variableName=[options] }");
    assertCommitAllParametersNeededNPEThrown(new File("/some/path"), options, "A message", null,
        "000005: An List<?> argument was not specified or is empty but is required.  "
            + "{ variableName=[paths] }");
    paths.add(null);
    assertCommitAllParametersNeededNPEThrown(new File("something"), options, "test msg", paths,
        "000003: An Object argument was not specified but is required.  { variableName=[paths] }");

    assertCommitAllParametersNeededIllegalArgumentExceptionThrown(
        new File("c:\\path\\to somewhere"), null, "", null,
        "000001: A String argument was not specified but is required.  { variableName=[message] }");
    paths.clear();
    assertCommitAllParametersNeededIllegalArgumentExceptionThrown(new File("/some/path"), options,
        "A message", paths,
        "000005: An List<?> argument was not specified or is empty but is required.  "
            + "{ variableName=[paths] }");

    // GitCommit.commit(String, String);
    assertCommitNPEThrown(null, null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[repository] }");
    assertCommitNPEThrown(new File("SomePath"), null,
        "000001: A String argument was not specified but is required.  { variableName=[message] }");

    assertCommitIllegalArgumentExceptionThrown(new File("SomePath"), "",
        "000001: A String argument was not specified but is required.  { variableName=[message] }");

    // GitCommit.commitOnly(String, String, List<String>);
    assertCommitOnlyNPEThrown(null, null, null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[repository] }");
    assertCommitOnlyNPEThrown(new File("something"), null, null,
        "000001: A String argument was not specified but is required.  { variableName=[message] }");
    assertCommitOnlyNPEThrown(new File("/some/path"), "A message", null,
        "000005: An List<?> argument was not specified or is empty but is required.  "
            + "{ variableName=[paths] }");
    paths.clear();
    paths.add(null);
    assertCommitOnlyNPEThrown(new File("something"), "test msg", paths,
        "000003: An Object argument was not specified but is required.  { variableName=[paths] }");

    assertCommitOnlyIllegalArgumentExceptionThrown(new File("c:\\path\\to somewhere"), "", null,
        "000001: A String argument was not specified but is required.  { variableName=[message] }");
    paths.clear();
    assertCommitOnlyIllegalArgumentExceptionThrown(new File("/some/path"), "A message", paths,
        "000005: An List<?> argument was not specified or is empty but is required.  "
            + "{ variableName=[paths] }");

  }

  private void assertCommitAllNPEThrown(File repoPath, String message, String expectedMessage) {
    try {
      commit.commitAll(repoPath, message);
      fail("No NullPointerException thrown when one was expected.  Error!");
    } catch (NullPointerException e) {
      assertEquals("The message from the caught NPE is not what was expected!", expectedMessage, e
          .getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      fail("Caught Throwable when none was expected.  Error!");
    }
  }

  private void assertCommitAllIllegalArgumentExceptionThrown(File repoPath, String message,
      String expectedMessage) {
    try {
      commit.commitAll(repoPath, message);
      fail("No IllegalArgumentException thrown when one was expected.  Error!");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "The message from the caught IllegalArgumentException is not what was expected!",
          expectedMessage, e.getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      fail("Caught Throwable when none was expected.  Error!");
    }
  }

  private void assertCommitWithOptsNPEThrown(File repoPath, GitCommitOptions options,
      String message, String expectedMessage) {
    try {
      commit.commit(repoPath, options, message);
      fail("No NullPointerException thrown when one was expected.  Error!");
    } catch (NullPointerException e) {
      assertEquals("The message from the caught NPE is not what was expected!", expectedMessage, e
          .getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      fail("Caught Throwable when none was expected.  Error!");
    }
  }

  private void assertCommitWithOptsIllegalArgumentExceptionThrown(File repoPath,
      GitCommitOptions options, String message, String expectedMessage) {
    try {
      commit.commit(repoPath, options, message);
      fail("No IllegalArgumentException thrown when one was expected.  Error!");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "The message from the caught IllegalArgumentException is not what was expected!",
          expectedMessage, e.getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      fail("Caught Throwable when none was expected.  Error!");
    }
  }

  private void assertCommitAllParametersNeededNPEThrown(File repoPath, GitCommitOptions options,
      String message, List<File> paths, String expectedMessage) {
    try {
      commit.commit(repoPath, options, message, paths);
      fail("No NullPointerException thrown when one was expected.  Error!");
    } catch (NullPointerException e) {
      assertEquals("The message from the caught NPE is not what was expected!", expectedMessage, e
          .getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      fail("Caught Throwable when none was expected.  Error!");
    }
  }

  private void assertCommitAllParametersNeededIllegalArgumentExceptionThrown(File repoPath,
      GitCommitOptions options, String message, List<File> paths, String expectedMessage) {
    try {
      commit.commit(repoPath, options, message, paths);
      fail("No IllegalArgumentException thrown when one was expected.  Error!");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "The message from the caught IllegalArgumentException is not what was expected!",
          expectedMessage, e.getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      fail("Caught Throwable when none was expected.  Error!");
    }
  }

  private void assertCommitNPEThrown(File repoPath, String message, String expectedMessage) {
    try {
      commit.commit(repoPath, message);
      fail("No NullPointerException thrown when one was expected.  Error!");
    } catch (NullPointerException e) {
      assertEquals("The message from the caught NPE is not what was expected!", expectedMessage, e
          .getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      fail("Caught Throwable when none was expected.  Error!");
    }
  }

  private void assertCommitIllegalArgumentExceptionThrown(File repoPath, String message,
      String expectedMessage) {
    try {
      commit.commit(repoPath, message);
      fail("No IllegalArgumentException thrown when one was expected.  Error!");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "The message from the caught IllegalArgumentException is not what was expected!",
          expectedMessage, e.getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      fail("Caught Throwable when none was expected.  Error!");
    }
  }

  private void assertCommitOnlyNPEThrown(File repoPath, String message, List<File> paths,
      String expectedMessage) {
    try {
      commit.commitOnly(repoPath, message, paths);
      fail("No NullPointerException thrown when one was expected.  Error!");
    } catch (NullPointerException e) {
      assertEquals("The message from the caught NPE is not what was expected!", expectedMessage, e
          .getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      fail("Caught Throwable when none was expected.  Error!");
    }
  }

  private void assertCommitOnlyIllegalArgumentExceptionThrown(File repoPath, String message,
      List<File> paths, String expectedMessage) {
    try {
      commit.commitOnly(repoPath, message, paths);
      fail("No IllegalArgumentException thrown when one was expected.  Error!");
    } catch (IllegalArgumentException e) {
      assertEquals(
          "The message from the caught IllegalArgumentException is not what was expected!",
          expectedMessage, e.getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      fail("Caught Throwable when none was expected.  Error!");
    }
  }

}
