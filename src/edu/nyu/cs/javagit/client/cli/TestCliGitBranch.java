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

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.Ref;
import edu.nyu.cs.javagit.api.commands.GitBranchResponse.BranchRecord;
import edu.nyu.cs.javagit.api.commands.GitBranchResponse.responseType;
import edu.nyu.cs.javagit.client.GitBranchResponseImpl;

public class TestCliGitBranch extends TestCase {
  private CliGitBranch cliGitBranch;
  
  @Before
  protected void setUp() {
  }

  @Test
  public void testGitBranchParserValidInput() {
    cliGitBranch = new CliGitBranch();
    CliGitBranch.GitBranchParser parser = cliGitBranch.new GitBranchParser();
    
    //Testing Deletion of branches.
    parser.parseLine("Deleted branch branchA.");
    parser.parseLine("Deleted branch branchB.");
    GitBranchResponseImpl response = new GitBranchResponseImpl();
    response.setResponseType(responseType.MESSAGE);
    response.addMessages("Deleted branch");
    response.addIntoBranchList(Ref.createBranchRef("branchA"));
    response.addIntoBranchList(Ref.createBranchRef("branchB"));
    assertResponsesEqual(parser, response);

    //Testing git-branch without an option.
    parser = cliGitBranch.new GitBranchParser();
    parser.parseLine("doc-refactor");
    parser.parseLine("master");
    parser.parseLine("refactor");
    parser.parseLine("* xyz");
    response = new GitBranchResponseImpl();
    response.addIntoBranchList(Ref.createBranchRef("doc-refactor"));
    response.addIntoBranchList(Ref.createBranchRef("master"));
    response.addIntoBranchList(Ref.createBranchRef("refactor"));
    response.addIntoBranchList(Ref.createBranchRef("xyz"));
    response.setCurrentBranch(Ref.createBranchRef("xyz"));
    response.setResponseType(responseType.BRANCH_LIST);
    assertResponsesEqual(parser, response);
    
    //Testing git-branch with verbose option.
    parser = cliGitBranch.new GitBranchParser();
    parser.parseLine("doc-refactor 2a66ab6 Committing a document");
    parser.parseLine("master       23d48c1 new file");
    parser.parseLine("refactor     2a66ab6 Committing a document");
    parser.parseLine("* xyz          03529b8 commit");
    
    response = new GitBranchResponseImpl();
    BranchRecord record = new BranchRecord(Ref.createBranchRef("doc-refactor"), 
        Ref.createSha1Ref("2a66ab6"), "Committing a document", false);
    response.addIntoBranchList(Ref.createBranchRef("doc-refactor"));
    response.addIntoListOfBranchRecord(record);   
    record = new BranchRecord(Ref.createBranchRef("master"), Ref.createSha1Ref("23d48c1"),
        "new file", false);
    response.addIntoBranchList(Ref.createBranchRef("master"));
    response.addIntoListOfBranchRecord(record);
    record = new BranchRecord(Ref.createBranchRef("refactor"), Ref.createSha1Ref("2a66ab6"),
        "Committing a document", false);
    response.addIntoBranchList(Ref.createBranchRef("refactor"));
    response.addIntoListOfBranchRecord(record);          
    record = new BranchRecord(Ref.createBranchRef("xyz"), Ref.createSha1Ref("03529b8"),
        "commit", true);
    response.addIntoBranchList(Ref.createBranchRef("xyz"));
    response.addIntoListOfBranchRecord(record);
    response.setCurrentBranch(Ref.createBranchRef("xyz"));
    response.setResponseType(responseType.BRANCH_LIST);
    assertResponsesEqual(parser, response);
  }
  
  private void assertResponsesEqual(CliGitBranch.GitBranchParser parser,
      GitBranchResponseImpl response) {
    try {
      assertTrue("Expected GitBranchResponse not equal to actual GitBranchResponse.", response
          .equals(parser.getResponse()));
    } catch (JavaGitException e) {
      assertTrue("Getting a GitBranchResponse from a CliGitBranch.GitBranchParser instance threw "
          + "an exception when it should not have.", false);
    }
  }
  
  @Test
  public void testGitBranchParserErrorInput() {
    CliGitBranch gitBranch = new CliGitBranch();

    CliGitBranch.GitBranchParser parser = gitBranch.new GitBranchParser();
    parser.parseLine("error: branch 'to_be_deleted' not found.");

    assertExceptionThrownOnResponseRetrieval(parser,
        "404000: Error calling git-branch.   The git-branch error message:  { "
            + "line1=[error: branch 'to_be_deleted' not found.] }", 404000);

    parser = gitBranch.new GitBranchParser();
    parser.parseLine("error: Cannot delete the branch 'xyz' which you are currently on.");

    assertExceptionThrownOnResponseRetrieval(parser,
        "404000: Error calling git-branch.   The git-branch error message:  { "
            + "line1=[error: Cannot delete the branch 'xyz' which you are currently on.] }", 404000);
  }

  private void assertExceptionThrownOnResponseRetrieval(CliGitBranch.GitBranchParser parser,
      String message, int code) {
    try {
      parser.getResponse();
      assertTrue(
          "Got GitBranchResponse from GitBranchParser when an exception should have been thrown!",
          false);
    } catch (JavaGitException e) {
      assertTrue("Exception message is not correct for thrown exception.", e.getMessage().equals(
          message));
      assertEquals("JavaGitException code is not correct", e.getCode(), code);
    }
  }
}
