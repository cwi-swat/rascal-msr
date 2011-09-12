package edu.nyu.cs.javagit.client.cli;

import java.io.File;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.client.GitMvResponseImpl;

public class TestCliGitMv extends TestCase {
  private CliGitMv cliGitMv;
  
  @Before
  protected void setUp() {
  }
  
  @Test
  public void testGitBranchParserValidInput() {
    cliGitMv = new CliGitMv();
      
    // Checking parsing of git mv with dry-run option.
    GitMvResponseImpl response = new GitMvResponseImpl();
    CliGitMv.GitMvParser parser = cliGitMv.new GitMvParser();
    parser.parseLine("Checking rename of 't1.txt' to 't2.txt'");
    parser.parseLine("Renaming t1.txt to t2.txt");
    parser.parseLine("Adding   : t2.txt");
    parser.parseLine("Deleting : t1.txt");
    response.setSource(new File("t1.txt"));
    response.setDestination(new File("t2.txt"));
    assertResponsesEqual(parser, response);
    
    // Checking parsing of git mv with force option.
    parser.parseLine("Warning: destination exists; will overwrite!");
    response.addComment("Warning: destination exists; will overwrite!");
    assertResponsesEqual(parser, response);
  }
  
  private void assertResponsesEqual(CliGitMv.GitMvParser parser, GitMvResponseImpl response) {
    try {
      assertTrue("Expected GitMvResponse not equal to actual GitMvResponse.", response
          .equals(parser.getResponse()));
    } catch (JavaGitException e) {
      assertTrue("Getting a GitMvResponse from a CliGitBranch.GitBranchParser instance threw "
          + "an exception when it should not have.", false);
    }
  }
  
  @Test
  public void testGitBranchParserErrorInput() {
    cliGitMv = new CliGitMv();
    
    CliGitMv.GitMvParser parser = cliGitMv.new GitMvParser();
    parser.parseLine("fatal: bad source, source=fileOne, destination=fileTwo");
    assertExceptionThrownOnResponseRetrieval(parser,
        "424000: Error calling git-mv.   The git-mv error message:  { "
            + "line1=[fatal: bad source, source=fileOne, destination=fileTwo] }", 424000);
    
    parser = cliGitMv.new GitMvParser();
    parser.parseLine("fatal: destination exists, source=fileOne, destination=fileTwo");
    assertExceptionThrownOnResponseRetrieval(parser,
        "424000: Error calling git-mv.   The git-mv error message:  { "
            + "line1=[fatal: destination exists, source=fileOne, destination=fileTwo] }", 424000);
    
    parser = cliGitMv.new GitMvParser();
    parser.parseLine("fatal: not under version control, source=fileOne, destination=fileTwo");
    assertExceptionThrownOnResponseRetrieval(parser,
        "424000: Error calling git-mv.   The git-mv error message:  { "
            + "line1=[fatal: not under version control, source=fileOne, destination=fileTwo] }", 
            424000);
  }
    
  private void assertExceptionThrownOnResponseRetrieval(CliGitMv.GitMvParser parser,
      String message, int code) {
    try {
      parser.getResponse();
      assertTrue(
          "Got GitMvResponse from GitMvParser when an exception should have been thrown!", false);
    } catch (JavaGitException e) {
      assertEquals("Equal", message, e.getMessage());
      assertTrue("Exception message is not correct for thrown exception.", e.getMessage().equals(
          message));
      assertEquals("JavaGitException code is not correct", e.getCode(), code);
    }
  }

}
