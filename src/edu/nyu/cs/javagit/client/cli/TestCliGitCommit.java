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
package edu.nyu.cs.javagit.client.cli;

import java.io.File;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.Ref;
import edu.nyu.cs.javagit.client.GitCommitResponseImpl;
import edu.nyu.cs.javagit.client.cli.CliGitCommit;

/**
 * Tests the <code>CliGitCommit</code> class, including <code>CliGitCommit.GitCommitParser</code>.
 */
public class TestCliGitCommit extends TestCase {

  @Before
  protected void setUp() {
  }

  @Test
  public void testGitCommitParserValidInput() {
    CliGitCommit gitcommit = new CliGitCommit();

    // Test a simple commit with updates only, no copy, remove, add or delete
    CliGitCommit.GitCommitParser parser = gitcommit.new GitCommitParser("");
    parser.parseLine("Created commit c18c00f: a change to test committing");
    assertEquals(1, parser.getNumLinesParsed());
    parser.parseLine(" 1 files changed, 6 insertions(+), 1 deletions(-)");
    assertEquals(2, parser.getNumLinesParsed());

    GitCommitResponseImpl response = new GitCommitResponseImpl(Ref.createSha1Ref("c18c00f"),
        "a change to test committing");
    response.setFilesChanged("1");
    response.setLinesInserted("6");
    response.setLinesDeleted("1");
    assertResponsesEqual(parser, response);

    // Test an initial commit.
    parser = gitcommit.new GitCommitParser("");
    parser.parseLine("Created initial commit 21efdb4: initial commit");
    assertEquals(1, parser.getNumLinesParsed());
    parser.parseLine(" 133 files changed, 23679 insertions(+), 0 deletions(-)");
    assertEquals(2, parser.getNumLinesParsed());
    parser.parseLine(" create mode 100644 svnClientAdapter/.classpath");
    assertEquals(3, parser.getNumLinesParsed());
    parser.parseLine(" create mode 100644 svnClientAdapter/.project");
    assertEquals(4, parser.getNumLinesParsed());
    parser.parseLine(" create mode 100644 svnClientAdapter/readme.txt");
    assertEquals(5, parser.getNumLinesParsed());
    parser
        .parseLine(" create mode 100644 svnClientAdapter/src/main/org/tigris/subversion/svnclientadapter/commandline/parser/SvnActionRE.java");
    assertEquals(6, parser.getNumLinesParsed());

    response = new GitCommitResponseImpl(Ref.createSha1Ref("21efdb4"), "initial commit");
    response.setFilesChanged("133");
    response.setLinesInserted("23679");
    response.setLinesDeleted("0");
    response.addAddedFile(new File("svnClientAdapter/.classpath"), "100644");
    response.addAddedFile(new File("svnClientAdapter/.project"), "100644");
    response.addAddedFile(new File("svnClientAdapter/readme.txt"), "100644");
    response
        .addAddedFile(
            new File(
                "svnClientAdapter/src/main/org/tigris/subversion/svnclientadapter/commandline/parser/SvnActionRE.java"),
            "100644");
    assertResponsesEqual(parser, response);

    // Test a commit response with added, removed, copied and renamed files.
    parser = gitcommit.new GitCommitParser("");
    parser.parseLine("Created commit ab238dd: renaming and copying files for commit tests.");
    assertEquals(1, parser.getNumLinesParsed());
    parser.parseLine(" 5 files changed, 1 insertions(+), 1 deletions(-)");
    assertEquals(2, parser.getNumLinesParsed());
    parser.parseLine(" copy another_file.txt => dadum.dot (90%)");
    assertEquals(3, parser.getNumLinesParsed());
    parser.parseLine(" rename testing_idr/hildebrand.bob => hildebrand.bob (100%)");
    assertEquals(4, parser.getNumLinesParsed());
    parser.parseLine(" delete mode 100644 svnClientAdapter/testfile.txt");
    assertEquals(5, parser.getNumLinesParsed());
    parser.parseLine(" create mode 100644 lowerfile.txt");
    assertEquals(6, parser.getNumLinesParsed());
    parser.parseLine(" rename svnClientAdapter/{changelog.txt => CHlog.txt} (100%)");
    assertEquals(7, parser.getNumLinesParsed());

    response = new GitCommitResponseImpl(Ref.createSha1Ref("ab238dd"),
        "renaming and copying files for commit tests.");
    response.setFilesChanged("5");
    response.setLinesInserted("1");
    response.setLinesDeleted("1");
    response.addAddedFile(new File("lowerfile.txt"), "100644");
    response.addCopiedFile(new File("another_file.txt"), new File("dadum.dot"), 90);
    response.addDeletedFile(new File("svnClientAdapter/testfile.txt"), "100644");
    response
        .addRenamedFile(new File("testing_idr/hildebrand.bob"), new File("hildebrand.bob"), 100);
    response.addRenamedFile(new File("svnClientAdapter/changelog.txt"), new File(
        "svnClientAdapter/CHlog.txt"), 100);
    assertResponsesEqual(parser, response);

  }

  private void assertResponsesEqual(CliGitCommit.GitCommitParser parser,
      GitCommitResponseImpl response) {
    try {
      assertTrue("Expected GitCommitResponse not equal to actual GitCommitResponse.", response
          .equals(parser.getResponse()));
    } catch (JavaGitException e) {
      assertTrue("Getting a GitCommitResponse from a CliGitCommit.GitCommitParser instance threw "
          + "an exception when it should not have.", false);
    }
  }

  @Test
  public void testGitCommitParserErrorInput() {
    CliGitCommit gitcommit = new CliGitCommit();

    // Test a spelling mistake
    CliGitCommit.GitCommitParser parser = gitcommit.new GitCommitParser("");
    parser.parseLine("Created commi c18c00f: a change to test committing");
    assertEquals(1, parser.getNumLinesParsed());
    parser.parseLine(" 1 files changed, 6 insertions(+), 1 deletions(-)");
    assertEquals(2, parser.getNumLinesParsed());

    assertExceptionThrownOnResponseRetrieval(parser,
        "410000: Error calling git-commit.  The git-commit error message:  { "
            + "line1=[Created commi c18c00f: a change to test committing], "
            + "line2=[ 1 files changed, 6 insertions(+), 1 deletions(-)] }", 410000);

    // Test a spelling mistake
    parser = gitcommit.new GitCommitParser("");
    parser.parseLine("Error Committing:  some random error");
    assertEquals(1, parser.getNumLinesParsed());

    assertExceptionThrownOnResponseRetrieval(parser,
        "410000: Error calling git-commit.  The git-commit error message:  { "
            + "line1=[Error Committing:  some random error] }", 410000);
  }

  private void assertExceptionThrownOnResponseRetrieval(CliGitCommit.GitCommitParser parser,
      String message, int code) {
    try {
      parser.getResponse();
      assertTrue(
          "Got GitCommitResponse from GitCommitParser when an exception should have been thrown!",
          false);
    } catch (JavaGitException e) {
      assertTrue("Exception message is not correct for thrown exception.", e.getMessage().equals(
          message));
      assertEquals("JavaGitException codeis not correct", e.getCode(), code);
    }
  }

}
