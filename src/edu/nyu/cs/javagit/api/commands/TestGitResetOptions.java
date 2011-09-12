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

import org.junit.Test;

import edu.nyu.cs.javagit.api.Ref;
import edu.nyu.cs.javagit.api.commands.GitResetOptions;

/**
 * Tests the <code>GitResetOptions</code> class.
 */
public class TestGitResetOptions extends TestCase {

  // TODO (jhl388): add exhaustive tests of the GitResetOptions.equals() method
  // TODO (jhl388): add exhaustive tests of the GitResetOptions.hashcode() method

  @Test
  public void testInvalidConstruction() {
    try {
      new GitResetOptions((Ref) null);
      assertTrue("No NPE thrown when one was expected.  Error!", false);
    } catch (NullPointerException e) {
      assertEquals("000003: An Object argument was not specified but is required.  "
          + "{ variableName=[commitName] }", e.getMessage());
    }

    try {
      new GitResetOptions((GitResetOptions.ResetType) null);
      assertTrue("No NPE thrown when one was expected.  Error!", false);
    } catch (NullPointerException e) {
      assertEquals("000003: An Object argument was not specified but is required.  "
          + "{ variableName=[resetType] }", e.getMessage());
    }

    assertbla(null, null, "000003: An Object argument was not specified but is required.  "
        + "{ variableName=[resetType] }");
    assertbla(GitResetOptions.ResetType.MIXED, null, "000003: An Object argument was not "
        + "specified but is required.  { variableName=[commitName] }");
  }

  private void assertbla(GitResetOptions.ResetType resetType, Ref commitName,
      String expecteMessage) {
    try {
      new GitResetOptions(resetType, commitName);
      assertTrue("No NPE thrown when one was expected.  Error!", false);
    } catch (NullPointerException e) {
      assertEquals(expecteMessage, e.getMessage());
    }
  }

  @Test
  public void testValidConstruction() {
    GitResetOptions grOpts = new GitResetOptions();
    assertEquals(Ref.HEAD, grOpts.getCommitName());
    assertEquals(GitResetOptions.ResetType.MIXED, grOpts.getResetType());
    assertTrue(!grOpts.isQuiet());
    assertEquals(grOpts.toString(), "--mixed HEAD");
    grOpts.hashCode();

    grOpts = new GitResetOptions(Ref.HEAD_1);
    assertEquals(Ref.HEAD_1, grOpts.getCommitName());
    assertEquals(GitResetOptions.ResetType.MIXED, grOpts.getResetType());
    assertTrue(!grOpts.isQuiet());
    assertEquals(grOpts.toString(), "--mixed HEAD^1");
    grOpts.hashCode();

    grOpts = new GitResetOptions(GitResetOptions.ResetType.HARD);
    assertEquals(Ref.HEAD, grOpts.getCommitName());
    assertEquals(GitResetOptions.ResetType.HARD, grOpts.getResetType());
    assertTrue(!grOpts.isQuiet());
    assertEquals(grOpts.toString(), "--hard HEAD");
    grOpts.hashCode();

    grOpts = new GitResetOptions(GitResetOptions.ResetType.HARD, Ref.HEAD_1);
    assertEquals(Ref.HEAD_1, grOpts.getCommitName());
    assertEquals(GitResetOptions.ResetType.HARD, grOpts.getResetType());
    assertTrue(!grOpts.isQuiet());
    assertEquals(grOpts.toString(), "--hard HEAD^1");
    grOpts.hashCode();

    grOpts.setCommitName(Ref.HEAD);
    assertEquals(Ref.HEAD, grOpts.getCommitName());
    assertEquals(grOpts.toString(), "--hard HEAD");
    grOpts.hashCode();

    grOpts.setQuiet(true);
    assertTrue(grOpts.isQuiet());
    assertEquals(grOpts.toString(), "--hard -q HEAD");
    grOpts.hashCode();

    grOpts.setResetType(GitResetOptions.ResetType.SOFT);
    assertEquals(GitResetOptions.ResetType.SOFT, grOpts.getResetType());
    assertEquals(grOpts.toString(), "--soft -q HEAD");
    grOpts.hashCode();
  }

}
