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
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import edu.nyu.cs.javagit.client.GitCommitResponseImpl;
import edu.nyu.cs.javagit.api.Ref;
import edu.nyu.cs.javagit.api.commands.GitCommitResponse.AddedOrDeletedFile;
import edu.nyu.cs.javagit.api.commands.GitCommitResponse.CopiedOrMovedFile;

import junit.framework.TestCase;

/**
 * Test class for testing the <code>GitCommitResponse</code> object.
 */
public class TestGitCommitResponse extends TestCase {

  private GitCommitResponseImpl resp;
  private GitCommitResponseImpl respSame;

  @Before
  protected void setUp() {
    resp = new GitCommitResponseImpl(Ref.createSha1Ref("3d3ef1a"), "A Comment");
    respSame = new GitCommitResponseImpl(Ref.createSha1Ref("3d3ef1a"), "A Comment");
  }

  @Test
  public void testGitCommitResponseBasicFunctionality() {
    // These tests use the variables set up in setUp().
    assertEquals("Short hash name is not the same.", Ref.createSha1Ref("3d3ef1a"), resp
        .getCommitShortHashName());
    assertEquals("Short comment is not the same.", "A Comment", resp.getCommitShortComment());

    resp.setFilesChanged(5);
    assertEquals("Number of files changed is not the same.", 5, resp.getFilesChanged());

    resp.setLinesDeleted(403);
    assertEquals("Number of lines deleted is not the same.", 403, resp.getLinesDeleted());

    resp.setLinesInserted(6423);
    assertEquals("Number of lines inserted is not the same.", 6423, resp.getLinesInserted());

    resp.addAddedFile(new File("/some/path/to/file.txt"), "100644");
    resp.addAddedFile(new File("/gumbee/fiddle/friars/whine.txt"), "100777");
    Iterator<AddedOrDeletedFile> adIter = resp.getAddedFilesIterator();
    AddedOrDeletedFile adf = adIter.next();
    assertEquals("Excpected different path to added file", new File("/some/path/to/file.txt"), adf
        .getPathTofile());
    assertEquals("Excpected different mode for added file", "100644", adf.getMode());
    adf = adIter.next();
    assertEquals("Excpected different path to added file", new File(
        "/gumbee/fiddle/friars/whine.txt"), adf.getPathTofile());
    assertEquals("Excpected different mode for added file", "100777", adf.getMode());

    resp.addDeletedFile(new File("/some/path/to/deleted/file.txt"), "100644");
    resp.addDeletedFile(new File("/gumbee/fiddle/friars/del/whine.txt"), "100777");
    adIter = resp.getDeletedFilesIterator();
    adf = adIter.next();
    assertEquals("Excpected different path to deleted file", new File(
        "/some/path/to/deleted/file.txt"), adf.getPathTofile());
    assertEquals("Excpected different mode for deleted file", "100644", adf.getMode());
    adf = adIter.next();
    assertEquals("Excpected different path to deleted file", new File(
        "/gumbee/fiddle/friars/del/whine.txt"), adf.getPathTofile());
    assertEquals("Excpected different mode for deleted file", "100777", adf.getMode());

    resp.addCopiedFile(new File("/starter/path.txt"), new File("/copied/path.txt"), 99);
    resp.addCopiedFile(new File("/filler/filet.txt"), new File("/floppin/fandiggery.txt"), 87);
    Iterator<CopiedOrMovedFile> cmIter = resp.getCopiedFilesIterator();
    CopiedOrMovedFile cmf = cmIter.next();
    assertEquals("Excpected different path for from copied file", new File("/starter/path.txt"),
        cmf.getSourceFilePath());
    assertEquals("Excpected different path for to copied file", new File("/copied/path.txt"), cmf
        .getDestinationFilePath());
    assertEquals("Excpected different percentage for copied file", 99, cmf.getPercentage());
    cmf = cmIter.next();
    assertEquals("Excpected different path for from copied file", new File("/filler/filet.txt"),
        cmf.getSourceFilePath());
    assertEquals("Excpected different path for to copied file",
        new File("/floppin/fandiggery.txt"), cmf.getDestinationFilePath());
    assertEquals("Excpected different percentage for copied file", 87, cmf.getPercentage());

    resp.addRenamedFile(new File("/starter/path.txt"), new File("/renamed/path.txt"), 99);
    resp.addRenamedFile(new File("/filler/filet.txt"), new File("/floppin/fandiggery.txt"), 87);
    cmIter = resp.getRenamedFilesIterator();
    cmf = cmIter.next();
    assertEquals("Excpected different path for from copied file", new File("/starter/path.txt"),
        cmf.getSourceFilePath());
    assertEquals("Excpected different path for to copied file", new File("/renamed/path.txt"), cmf
        .getDestinationFilePath());
    assertEquals("Excpected different percentage for copied file", 99, cmf.getPercentage());
    cmf = cmIter.next();
    assertEquals("Excpected different path for from copied file", new File("/filler/filet.txt"),
        cmf.getSourceFilePath());
    assertEquals("Excpected different path for to copied file",
        new File("/floppin/fandiggery.txt"), cmf.getDestinationFilePath());
    assertEquals("Excpected different percentage for copied file", 87, cmf.getPercentage());

  }

  @Test
  public void testGitCommitResponseEqualsMethod() {
    // These tests use the variables set up in setUp().
    assertEquals("GitCommitResponse objects are not equal when they should be", resp, respSame);

    resp.setFilesChanged(5);
    assertTrue("GitCommitResponseObjects are equal when they should not be.", !resp
        .equals(respSame));
    respSame.setFilesChanged(5);
    assertEquals("GitCommitResponse objects are not equal when they should be", resp, respSame);

    resp.setLinesDeleted(403);
    assertTrue("GitCommitResponseObjects are equal when they should not be.", !resp
        .equals(respSame));
    respSame.setLinesDeleted(403);
    assertEquals("GitCommitResponse objects are not equal when they should be", resp, respSame);

    resp.setLinesInserted(6423);
    assertTrue("GitCommitResponseObjects are equal when they should not be.", !resp
        .equals(respSame));
    respSame.setLinesInserted(6423);
    assertEquals("GitCommitResponse objects are not equal when they should be", resp, respSame);

    resp.addAddedFile(new File("/some/path/to/file.txt"), "100644");
    assertTrue("GitCommitResponseObjects are equal when they should not be.", !resp
        .equals(respSame));
    respSame.addAddedFile(new File("/some/path/to/file.txt"), "100644");
    assertEquals("GitCommitResponse objects are not equal when they should be", resp, respSame);

    resp.addDeletedFile(new File("/another/path/to/file.txt"), "100644");
    assertTrue("GitCommitResponseObjects are equal when they should not be.", !resp
        .equals(respSame));
    respSame.addDeletedFile(new File("/another/path/to/file.txt"), "100644");
    assertEquals("GitCommitResponse objects are not equal when they should be", resp, respSame);

    resp.addCopiedFile(new File("/from/this/path.txt"), new File("/to/this/altered-path.txt"), 56);
    assertTrue("GitCommitResponseObjects are equal when they should not be.", !resp
        .equals(respSame));
    respSame.addCopiedFile(new File("/from/this/path.txt"), new File("/to/this/altered-path.txt"),
        56);
    assertEquals("GitCommitResponse objects are not equal when they should be", resp, respSame);

    resp.addRenamedFile(new File("/from/another/path.txt"),
        new File("/to/another/altered-path.txt"), 23);
    assertTrue("GitCommitResponseObjects are equal when they should not be.", !resp
        .equals(respSame));
    respSame.addRenamedFile(new File("/from/another/path.txt"), new File(
        "/to/another/altered-path.txt"), 23);
    assertEquals("GitCommitResponse objects are not equal when they should be", resp, respSame);

  }

  @Test
  public void testAddedOrDeletedFile() {
    GitCommitResponseImpl.AddedOrDeletedFile addDel = new AddedOrDeletedFile(new File(
        "/a/path/to/add/del/file.txt"), "100644");
    GitCommitResponseImpl.AddedOrDeletedFile addDelSame = new AddedOrDeletedFile(new File(
        "/a/path/to/add/del/file.txt"), "100644");
    GitCommitResponseImpl.AddedOrDeletedFile addDelDiff1 = new AddedOrDeletedFile(new File(
        "/another/path/to/add/del/file.txt"), "100644");
    GitCommitResponseImpl.AddedOrDeletedFile addDelDiff2 = new AddedOrDeletedFile(new File(
        "/a/path/to/add/del/file.txt"), "100777");

    assertEquals("AddedOrDeletedFile instances not equal when they should be equal", addDel,
        addDelSame);

    assertTrue("AddedOrDeletedFile instances equal when they should not be equal", !(addDel
        .equals(addDelDiff1)));
    assertTrue("AddedOrDeletedFile instances equal when they should not be equal", !addDel
        .equals(addDelDiff2));

    assertEquals("AddedOrDeletedFile hashcodes not equal when they should be equal.", addDel
        .hashCode(), addDelSame.hashCode());
  }

  @Test
  public void testCopiedOrMovedFile() {
    GitCommitResponseImpl.CopiedOrMovedFile copyMove = new CopiedOrMovedFile(new File(
        "c:\\path\\1\\txt.txt"), new File("c:\\other\\path\\bob.txt"), 32);
    GitCommitResponseImpl.CopiedOrMovedFile copyMoveSame = new CopiedOrMovedFile(new File(
        "c:\\path\\1\\txt.txt"), new File("c:\\other\\path\\bob.txt"), 32);
    GitCommitResponseImpl.CopiedOrMovedFile copyMoveDiff1 = new CopiedOrMovedFile(new File(
        "c:\\path\\1\\notSame.txt"), new File("c:\\other\\path\\bob.txt"), 32);
    GitCommitResponseImpl.CopiedOrMovedFile copyMoveDiff2 = new CopiedOrMovedFile(new File(
        "c:\\path\\1\\txt.txt"), new File("c:\\path\\1\\bob.txt"), 32);
    GitCommitResponseImpl.CopiedOrMovedFile copyMoveDiff3 = new CopiedOrMovedFile(new File(
        "c:\\path\\1\\txt.txt"), new File("c:\\other\\path\\bob.txt"), 83);

    assertEquals("CopiedOrMovedFile instances not equal when they should be equal", copyMove,
        copyMoveSame);

    assertTrue("CopiedOrMovedFile instances equal when they should not be equal", !copyMove
        .equals(copyMoveDiff1));
    assertTrue("CopiedOrMovedFile instances equal when they should not be equal", !copyMove
        .equals(copyMoveDiff2));
    assertTrue("CopiedOrMovedFile instances equal when they should not be equal", !copyMove
        .equals(copyMoveDiff3));

    assertEquals("CopiedOrMovedFile hashcodes not equal when they should be equal.", copyMove
        .hashCode(), copyMoveSame.hashCode());
  }

}
