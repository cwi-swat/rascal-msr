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
import edu.nyu.cs.javagit.api.Ref;
import edu.nyu.cs.javagit.api.commands.GitAdd;
import edu.nyu.cs.javagit.api.commands.GitBranch;
import edu.nyu.cs.javagit.api.commands.GitBranchOptions;
import edu.nyu.cs.javagit.api.commands.GitCommit;
import edu.nyu.cs.javagit.test.utilities.FileUtilities;
import edu.nyu.cs.javagit.test.utilities.HelperGitCommands;

/**
 * Implements test cases for for GitBranch.
 */
public class TestGitBranch extends TestCase {
  private File repoDirectory;
  private GitCommit commit;
  private GitAdd add;
  private GitBranch branch;
  private Ref branchA = Ref.createBranchRef("branchA");
  private Ref branchB = Ref.createBranchRef("branchB");

  private GitBranchOptions options;

  @Before
  protected void setUp() throws IOException, JavaGitException {
    repoDirectory = FileUtilities.createTempDirectory("GitBranchTestRepo");
    GitInit gitInit = new GitInit();
    gitInit.init(repoDirectory);
    commit = new GitCommit();
    add = new GitAdd();
    branch = new GitBranch();

    options = new GitBranchOptions();
    File testFile = FileUtilities.createFile(repoDirectory, "fileA.txt", "Sameple Contents");
    // Add a file to the repo
    List<File> filesToAdd = new ArrayList<File>();
    filesToAdd.add(testFile);
    add.add(repoDirectory, null, filesToAdd);

    // Call commit
    commit.commit(repoDirectory, "Making a first test commit");
  }

  @After
  protected void tearDown() throws JavaGitException {
    // delete repo dir
    FileUtilities.removeDirectoryRecursivelyAndForcefully(repoDirectory);
  }

  @Test
  public void testCreateBranch() throws IOException, JavaGitException {
    branch.createBranch(repoDirectory, branchA);
    branch.createBranch(repoDirectory, branchB);
  }

  @Test
  public void testDisplayBranch() throws IOException, JavaGitException {
    branch.createBranch(repoDirectory, branchA);
    branch.createBranch(repoDirectory, branchB);

    //Display operation without any option. 
    branch.branch(repoDirectory);

    //Display operation with --verbose option. 
    options.setOptVerbose(true);
    branch.branch(repoDirectory, options);

    //Display operation with --verbose and --no-abbrev options. 
    options.setOptVerbose(true);
    options.setOptNoAbbrev(true);
    branch.branch(repoDirectory, options);
  }

  @Test
  public void testRenameBranch() throws IOException, JavaGitException {
    Ref branchC = Ref.createBranchRef("branchC");
    branch.createBranch(repoDirectory, branchA);
    branch.createBranch(repoDirectory, branchB);

    //Renaming branchA to branchC.
    branch.renameBranch(repoDirectory, false, branchA, branchC);

    //Renaming current branch to branchB.
    branch.renameBranch(repoDirectory, true, branchB);
  }

  @Test
  public void testDeleteBranch() throws IOException, JavaGitException {
    branch.createBranch(repoDirectory, branchA);
    branch.createBranch(repoDirectory, branchB);

    //Deleting a branch.
    branch.deleteBranch(repoDirectory, false, false, branchA);

    Ref branchC = Ref.createBranchRef("branchC");
    Ref branchD = Ref.createBranchRef("branchD");
    Ref branchE = Ref.createBranchRef("branchE");
    branch.createBranch(repoDirectory, branchC);
    branch.createBranch(repoDirectory, branchD);
    branch.createBranch(repoDirectory, branchE);

    List<Ref> branchList = new ArrayList<Ref>();
    branchList.add(branchC);
    branchList.add(branchD);

    //Deleting a list of branches.
    branch.deleteBranch(repoDirectory, false, false, branchList);
  }

  @Test
  public void testCreateBranchBadArugmentPassing() throws IOException, JavaGitException {
    // GitBranch.createBranch(File, Ref);
    assertCreateBranchNPEThrown(null, null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[repository path] }");
    assertCreateBranchNPEThrown(new File("SomePath"), null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[branch name] }");
    assertCreateBranchIllegalArgumentExceptionThrown(new File("SomePath"), new Ref(), new Ref(),
        "100000: Incorrect refType type.  { variableName=[branch name] }");
    assertCreateBranchIllegalArgumentExceptionThrown(new File("SomePath"), Ref
        .createBranchRef("ref"), new Ref(),
        "100000: Incorrect refType type.  { variableName=[start point] }");
    //CreateBranch with options.
    assertCreateBranchWithOptsNPEThrown(null, null, null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[repository path] }");
    assertCreateBranchWithOptsNPEThrown(new File("SomePath"), null, null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[options] }");
    assertCreateBranchWithOptsNPEThrown(new File("SomePath"), new GitBranchOptions(), null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[branch name] }");
    assertCreateBranchOptsWithIllegalArgumentExceptionThrown(new File("SomePath"),
        new GitBranchOptions(), new Ref(),
        "100000: Incorrect refType type.  { variableName=[branch name] }");
    assertCreateBranchIllegalArgumentExceptionThrown(new File("SomePath"), Ref
        .createBranchRef("ref"), new Ref(),
        "100000: Incorrect refType type.  { variableName=[start point] }");
  }

  @Test
  public void testBranchBadArugmentPassing() throws IOException, JavaGitException {
    // GitBranch.branch(File)
    assertBranchNPEThrown(null, "000003: An Object argument was not specified but is required.  "
        + "{ variableName=[repository path] }");
    assertBranchWithOptsNPEThrown(null, null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[repository path] }");
    assertBranchWithOptsNPEThrown(new File("SomePath"), null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[options] }");
  }

  @Test
  public void testRenameBranchBadArugmentPassing() throws IOException, JavaGitException {
    // GitBranch.branch(File)
    assertRenameBranchNPEThrown(null, null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[repository path] }");
    assertRenameBranchNPEThrown(new File("FileOne"), null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[new name] }");
    assertRenameBranchWithOldBranchNPEThrown(null, null, null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[repository path] }");
    assertRenameBranchWithOldBranchNPEThrown(new File("SomePath"), null, null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[old name] }");
    assertRenameBranchWithOldBranchNPEThrown(new File("SomePath"), new Ref(), null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[new name] }");
    assertRenameBranchIllegalArgumentExceptionThrown(new File("SomePath"), new Ref(),
        "100000: Incorrect refType type.  { variableName=[new name] }");
    assertRenameBranchWithOldBranchIllegalArgumentExceptionThrown(new File("SomePath"), new Ref(),
        new Ref(), "100000: Incorrect refType type.  { variableName=[old name] }");
    assertRenameBranchWithOldBranchIllegalArgumentExceptionThrown(new File("SomePath"), Ref
        .createBranchRef("ref"), new Ref(),
        "100000: Incorrect refType type.  { variableName=[new name] }");
  }

  @Test
  public void testDeleteBranchBadArugmentPassing() throws IOException, JavaGitException {
    // GitBranch.branch(File)
    assertDeleteBranchNPEThrown(null, null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[repository path] }");
    assertDeleteBranchNPEThrown(new File("FileOne"), null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[branch name] }");
    assertDeleteBranchIllegalArgumentExceptionThrown(new File("SomePath"), new Ref(),
    "100000: Incorrect refType type.  { variableName=[branch name] }");
    assertDeleteBranchListNPEThrown(null, null,
        "000003: An Object argument was not specified but is required.  "
            + "{ variableName=[repository path] }");
    assertDeleteBranchListNPEThrown(new File("SomePath"), null,
        "000005: An List<?> argument was not specified or is empty but is required.  "
            + "{ variableName=[branch list] }");
    List<Ref> branchList = new ArrayList<Ref>();
    branchList.add(new Ref());
    assertDeleteBranchListIllegalArgumentExceptionThrown(new File("SomePath"), branchList,
        "100000: Incorrect refType type.  { variableName=[branch list] }");
  }

  private void assertCreateBranchNPEThrown(File repoPath, Ref branchName, String expectedMessage) {
    try {
      branch.createBranch(repoPath, branchName);
      assertTrue("No NullPointerException thrown when one was expected.  Error!", false);
    } catch (NullPointerException e) {
      assertEquals("The message from the caught NPE is not what was expected!", expectedMessage, e
          .getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      assertTrue("Caught Throwable when none was expected.  Error!", false);
    }
  }

  private void assertCreateBranchIllegalArgumentExceptionThrown(File repoPath, Ref branchName,
      Ref startPoint, String expectedMessage) {
    try {
      branch.createBranch(repoPath, branchName, startPoint);
      assertTrue("No IllegalArgumentException thrown when one was expected.  Error!", false);
    } catch (IllegalArgumentException e) {
      assertEquals(
          "The message from the caught IllegalArgumentException is not what was expected!",
          expectedMessage, e.getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      assertTrue("Caught Throwable when none was expected.  Error!", false);
    }
  }

  private void assertCreateBranchWithOptsNPEThrown(File repoPath, GitBranchOptions options,
      Ref branchName, String expectedMessage) {
    try {
      branch.createBranch(repoPath, options, branchName);
      assertTrue("No NullPointerException thrown when one was expected.  Error!", false);
    } catch (NullPointerException e) {
      assertEquals("The message from the caught NPE is not what was expected!", expectedMessage, e
          .getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      assertTrue("Caught Throwable when none was expected.  Error!", false);
    }
  }

  private void assertCreateBranchOptsWithIllegalArgumentExceptionThrown(File repoPath,
      GitBranchOptions options, Ref branchName, String expectedMessage) {
    try {
      branch.createBranch(repoPath, options, branchName);
      assertTrue("No IllegalArgumentException thrown when one was expected.  Error!", false);
    } catch (IllegalArgumentException e) {
      assertEquals(
          "The message from the caught IllegalArgumentException is not what was expected!",
          expectedMessage, e.getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      assertTrue("Caught Throwable when none was expected.  Error!", false);
    }
  }

  private void assertBranchNPEThrown(File repoPath, String expectedMessage) {
    try {
      branch.branch(repoPath);
      assertTrue("No NullPointerException thrown when one was expected.  Error!", false);
    } catch (NullPointerException e) {
      assertEquals("The message from the caught NPE is not what was expected!", expectedMessage, e
          .getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      assertTrue("Caught Throwable when none was expected.  Error!", false);
    }
  }

  private void assertBranchWithOptsNPEThrown(File repoPath, GitBranchOptions options,
      String expectedMessage) {
    try {
      branch.branch(repoPath, options);
      assertTrue("No NullPointerException thrown when one was expected.  Error!", false);
    } catch (NullPointerException e) {
      assertEquals("The message from the caught NPE is not what was expected!", expectedMessage, e
          .getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      assertTrue("Caught Throwable when none was expected.  Error!", false);
    }
  }

  private void assertRenameBranchNPEThrown(File repoPath, Ref newName, String expectedMessage) {
    try {
      branch.renameBranch(repoPath, false, newName);
      assertTrue("No NullPointerException thrown when one was expected.  Error!", false);
    } catch (NullPointerException e) {
      assertEquals("The message from the caught NPE is not what was expected!", expectedMessage, e
          .getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      assertTrue("Caught Throwable when none was expected.  Error!", false);
    }
  }

  private void assertRenameBranchWithOldBranchNPEThrown(File repoPath, Ref oldBranch,
      Ref newBranch, String expectedMessage) {
    try {
      branch.renameBranch(repoPath, false, oldBranch, newBranch);
      assertTrue("No NullPointerException thrown when one was expected.  Error!", false);
    } catch (NullPointerException e) {
      assertEquals("The message from the caught NPE is not what was expected!", expectedMessage, e
          .getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      assertTrue("Caught Throwable when none was expected.  Error!", false);
    }
  }

  private void assertRenameBranchIllegalArgumentExceptionThrown(File repoPath, Ref newBranch,
      String expectedMessage) {
    try {
      branch.renameBranch(repoPath, false, newBranch);
      assertTrue("No IllegalArgumentException thrown when one was expected.  Error!", false);
    } catch (IllegalArgumentException e) {
      assertEquals(
          "The message from the caught IllegalArgumentException is not what was expected!",
          expectedMessage, e.getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      assertTrue("Caught Throwable when none was expected.  Error!", false);
    }
  }

  private void assertRenameBranchWithOldBranchIllegalArgumentExceptionThrown(File repoPath,
      Ref oldBranch, Ref newBranch, String expectedMessage) {
    try {
      branch.renameBranch(repoPath, false, oldBranch, newBranch);
      assertTrue("No IllegalArgumentException thrown when one was expected.  Error!", false);
    } catch (IllegalArgumentException e) {
      assertEquals(
          "The message from the caught IllegalArgumentException is not what was expected!",
          expectedMessage, e.getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      assertTrue("Caught Throwable when none was expected.  Error!", false);
    }
  }

  private void assertDeleteBranchNPEThrown(File repoPath, Ref branchName, String expectedMessage) {
    try {
      branch.deleteBranch(repoPath, false, false, branchName);
      assertTrue("No NullPointerException thrown when one was expected.  Error!", false);
    } catch (NullPointerException e) {
      assertEquals("The message from the caught NPE is not what was expected!", expectedMessage, e
          .getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      assertTrue("Caught Throwable when none was expected.  Error!", false);
    }
  }

  private void assertDeleteBranchListNPEThrown(File repoPath, List<Ref> branchList,
      String expectedMessage) {
    try {
      branch.deleteBranch(repoPath, false, false, branchList);
      assertTrue("No NullPointerException thrown when one was expected.  Error!", false);
    } catch (NullPointerException e) {
      assertEquals("The message from the caught NPE is not what was expected!", expectedMessage, e
          .getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      assertTrue("Caught Throwable when none was expected.  Error!", false);
    }
  }

  private void assertDeleteBranchIllegalArgumentExceptionThrown(File repoPath,
      Ref branchName, String expectedMessage) {
    try {
      branch.deleteBranch(repoPath, false, false, branchName);
      assertTrue("No IllegalArgumentException thrown when one was expected.  Error!", false);
    } catch (IllegalArgumentException e) {
      assertEquals(
          "The message from the caught IllegalArgumentException is not what was expected!",
          expectedMessage, e.getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      assertTrue("Caught Throwable when none was expected.  Error!", false);
    }
  }
  
  private void assertDeleteBranchListIllegalArgumentExceptionThrown(File repoPath,
      List<Ref> branchList, String expectedMessage) {
    try {
      branch.deleteBranch(repoPath, false, false, branchList);
      assertTrue("No IllegalArgumentException thrown when one was expected.  Error!", false);
    } catch (IllegalArgumentException e) {
      assertEquals(
          "The message from the caught IllegalArgumentException is not what was expected!",
          expectedMessage, e.getMessage());
    } catch (Throwable e) {
      e.printStackTrace();
      assertTrue("Caught Throwable when none was expected.  Error!", false);
    }
  }
}
