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
package edu.nyu.cs.javagit.api;

import junit.framework.TestCase;

import org.junit.Test;

import edu.nyu.cs.javagit.api.Ref;

/**
 * Test case for the <code>Ref</code> data object.
 */
public class TestRef extends TestCase {

  // TODO (jhl388): add exhaustive tests of the Ref.equals() method
  // TODO (jhl388): add exhaustive tests of the Ref.hashcode() method

  @Test
  public void testStaticVariables() {
    // Checking HEAD
    assertEquals("Expected RefType of HEAD.", Ref.HEAD.getRefType(), Ref.RefType.HEAD);
    assertEquals(Ref.HEAD.getHeadOffset(), 0);

    // Checking HEAD_1
    assertEquals("Expected RefType of HEAD.", Ref.HEAD_1.getRefType(), Ref.RefType.HEAD);
    assertEquals(Ref.HEAD_1.getHeadOffset(), 1);
    assertEquals(Ref.HEAD_1.getName(), null);
  }

  @Test
  public void testCreateHeadRef() {
    // Testing invalid input
    assertIllegalCreateHeadRefArgument(-1,
        "000004: The int argument is not greater than the lower bound (lowerBound < toCheck).  "
            + "{ toCheck=[-1], lowerBound=[-1], variableName=[headOffset] }");
    assertIllegalCreateHeadRefArgument(-23,
        "000004: The int argument is not greater than the lower bound (lowerBound < toCheck).  "
            + "{ toCheck=[-23], lowerBound=[-1], variableName=[headOffset] }");

    // Testing valid input
    Ref ref = Ref.createHeadRef(0);
    assertEquals(ref, Ref.HEAD);
    assertTrue(Ref.HEAD == ref);
    assertEquals(ref.toString(), "HEAD");
    ref.hashCode();

    ref = Ref.createHeadRef(1);
    assertEquals(ref, Ref.HEAD_1);
    assertTrue(Ref.HEAD_1 == ref);
    assertEquals(ref.toString(), "HEAD^1");
    ref.hashCode();

    ref = Ref.createHeadRef(2);
    assertEquals("Expected RefType of HEAD.", ref.getRefType(), Ref.RefType.HEAD);
    assertEquals(ref.getHeadOffset(), 2);
    assertEquals(ref.getName(), null);
    assertEquals(ref.getRepositoryName(), null);
    assertEquals(ref.toString(), "HEAD~2");
    ref.hashCode();

    ref = Ref.createHeadRef(50);
    assertEquals("Expected RefType of HEAD.", ref.getRefType(), Ref.RefType.HEAD);
    assertEquals(ref.getHeadOffset(), 50);
    assertEquals(ref.getName(), null);
    assertEquals(ref.getRepositoryName(), null);
    assertEquals(ref.toString(), "HEAD~50");
    ref.hashCode();
  }

  private void assertIllegalCreateHeadRefArgument(int headOffset, String expectedMessage) {
    try {
      Ref.createHeadRef(headOffset);
      assertTrue("No IllegalArgumentException thrown when one was expected.  Error!", false);
    } catch (IllegalArgumentException e) {
      assertEquals("IllegalArgumentException didn't contain expected message.  Error!",
          expectedMessage, e.getMessage());
    }
  }

  @Test
  public void testCreateBranchRef() {
    // Testing invalid input
    assertCreateBranchRefThrowsNPE(null,
        "000001: A String argument was not specified but is required.  { variableName=[name] }");
    assertCreateBranchRefThrowsIllegalArgException("",
        "000001: A String argument was not specified but is required.  { variableName=[name] }");

    // Testing valid input
    Ref ref = Ref.createBranchRef("master");
    Ref ref2 = Ref.createBranchRef("master");
    assertEquals("Expected RefType of BRANCH.", ref.getRefType(), Ref.RefType.BRANCH);
    assertEquals(ref.getHeadOffset(), -1);
    assertEquals(ref.getName(), "master");
    assertEquals(ref.getRepositoryName(), null);
    assertEquals(ref.toString(), "master");
    assertEquals(ref, ref2);
    assertEquals(ref.hashCode(), ref2.hashCode());

    ref = Ref.createBranchRef("myTestBranch");
    assertTrue(!ref.equals(ref2));

    ref2 = Ref.createBranchRef("myTestBranch");
    assertEquals("Expected RefType of BRANCH.", ref.getRefType(), Ref.RefType.BRANCH);
    assertEquals(ref.getHeadOffset(), -1);
    assertEquals(ref.getName(), "myTestBranch");
    assertEquals(ref.getRepositoryName(), null);
    assertEquals(ref.toString(), "myTestBranch");
    assertEquals(ref, ref2);
    assertEquals(ref.hashCode(), ref2.hashCode());
  }

  private void assertCreateBranchRefThrowsNPE(String name, String message) {
    try {
      Ref.createBranchRef(name);
      assertTrue("NPE exception not thrown when one was expected!", false);
    } catch (NullPointerException e) {
      assertEquals("NPE did not contain the expected message.", message, e.getMessage());
    }
  }

  private void assertCreateBranchRefThrowsIllegalArgException(String name, String message) {
    try {
      Ref.createBranchRef(name);
      assertTrue("IllegalArgumentException not thrown when one was expected!", false);
    } catch (IllegalArgumentException e) {
      assertEquals("IllegalArgumentException did not contain the expected message.", message, e
          .getMessage());
    }
  }

  @Test
  public void testCreateRemoteRef() {
    // Testing invalid input
    assertCreateRemoteRefThrowsNPE(null, null,
        "000001: A String argument was not specified but is required.  { variableName=[name] }");
    assertCreateRemoteRefThrowsIllegalArgException(null, "",
        "000001: A String argument was not specified but is required.  { variableName=[name] }");

    // Testing valid input
    Ref ref = Ref.createRemoteRef(null, "newFeatureDev");
    Ref ref2 = Ref.createRemoteRef(null, "newFeatureDev");
    assertEquals("Expected RefType of REMOTE.", ref.getRefType(), Ref.RefType.REMOTE);
    assertEquals(ref.getHeadOffset(), -1);
    assertEquals(ref.getName(), "newFeatureDev");
    assertEquals(ref.getRepositoryName(), null);
    assertEquals(ref.toString(), "newFeatureDev");
    assertEquals(ref, ref2);
    assertEquals(ref.hashCode(), ref2.hashCode());

    ref = Ref.createRemoteRef(null, "review0");
    assertTrue(!ref.equals(ref2));

    ref2 = Ref.createRemoteRef("", "review0");
    assertEquals("Expected RefType of REMOTE.", ref.getRefType(), Ref.RefType.REMOTE);
    assertEquals(ref.getHeadOffset(), -1);
    assertEquals(ref.getName(), "review0");
    assertEquals(ref.getRepositoryName(), null);
    assertEquals(ref.toString(), "review0");
    assertEquals(ref, ref2);
    assertEquals(ref.hashCode(), ref2.hashCode());

    ref = Ref.createRemoteRef("", "nextReview");
    assertTrue(!ref.equals(ref2));

    ref2 = Ref.createRemoteRef("", "nextReview");
    assertEquals("Expected RefType of REMOTE.", ref.getRefType(), Ref.RefType.REMOTE);
    assertEquals(ref.getHeadOffset(), -1);
    assertEquals(ref.getName(), "nextReview");
    assertEquals(ref.getRepositoryName(), null);
    assertEquals(ref.toString(), "nextReview");
    assertEquals(ref, ref2);
    assertEquals(ref.hashCode(), ref2.hashCode());

    ref = Ref.createRemoteRef("origin", "stable");
    assertTrue(!ref.equals(ref2));

    ref2 = Ref.createRemoteRef("origin", "stable");
    assertEquals("Expected RefType of REMOTE.", ref.getRefType(), Ref.RefType.REMOTE);
    assertEquals(ref.getHeadOffset(), -1);
    assertEquals(ref.getName(), "stable");
    assertEquals(ref.getRepositoryName(), "origin");
    assertEquals(ref.toString(), "origin/stable");
    assertEquals(ref, ref2);
    assertEquals(ref.hashCode(), ref2.hashCode());

    ref = Ref.createRemoteRef("jhlRepo", "stable");
    assertTrue(!ref.equals(ref2));

    ref2 = Ref.createRemoteRef("jhlRepo", "stable");
    assertEquals("Expected RefType of REMOTE.", ref.getRefType(), Ref.RefType.REMOTE);
    assertEquals(ref.getHeadOffset(), -1);
    assertEquals(ref.getName(), "stable");
    assertEquals(ref.getRepositoryName(), "jhlRepo");
    assertEquals(ref.toString(), "jhlRepo/stable");
    assertEquals(ref, ref2);
    assertEquals(ref.hashCode(), ref2.hashCode());
  }

  private void assertCreateRemoteRefThrowsNPE(String repositoryName, String name, String message) {
    try {
      Ref.createRemoteRef(repositoryName, name);
      assertTrue("NPE exception not thrown when one was expected!", false);
    } catch (NullPointerException e) {
      assertEquals("NPE did not contain the expected message.", message, e.getMessage());
    }
  }

  private void assertCreateRemoteRefThrowsIllegalArgException(String repositoryName, String name,
      String message) {
    try {
      Ref.createRemoteRef(repositoryName, name);
      assertTrue("IllegalArgumentException not thrown when one was expected!", false);
    } catch (IllegalArgumentException e) {
      assertEquals("IllegalArgumentException did not contain the expected message.", message, e
          .getMessage());
    }
  }

  @Test
  public void testCreateSha1Ref() {
    // Testing invalid input
    assertCreateSha1RefThrowsNPE(null,
        "000001: A String argument was not specified but is required.  { variableName=[name] }");
    assertCreateSha1RefThrowsIllegalArgException("",
        "000001: A String argument was not specified but is required.  { variableName=[name] }");

    // Testing valid input
    Ref ref = Ref.createSha1Ref("a");
    Ref ref2 = Ref.createSha1Ref("a");
    assertEquals("Expected RefType of SHA1.", ref.getRefType(), Ref.RefType.SHA1);
    assertEquals(ref.getHeadOffset(), -1);
    assertEquals(ref.getName(), "a");
    assertEquals(ref.getRepositoryName(), null);
    assertEquals(ref.toString(), "a");
    assertEquals(ref, ref2);
    assertEquals(ref.hashCode(), ref2.hashCode());

    ref = Ref.createSha1Ref("ab238dd4c9fa4d8eabe03715c3e8b212f9532013");
    assertTrue(!ref.equals(ref2));

    ref2 = Ref.createSha1Ref("ab238dd4c9fa4d8eabe03715c3e8b212f9532013");
    assertEquals("Expected RefType of SHA1.", ref.getRefType(), Ref.RefType.SHA1);
    assertEquals(ref.getHeadOffset(), -1);
    assertEquals(ref.getName(), "ab238dd4c9fa4d8eabe03715c3e8b212f9532013");
    assertEquals(ref.getRepositoryName(), null);
    assertEquals(ref.toString(), "ab238dd4c9fa4d8eabe03715c3e8b212f9532013");
    assertEquals(ref, ref2);
    assertEquals(ref.hashCode(), ref2.hashCode());
  }

  private void assertCreateSha1RefThrowsNPE(String sha1Name, String message) {
    try {
      Ref.createSha1Ref(sha1Name);
      assertTrue("NPE exception not thrown when one was expected!", false);
    } catch (NullPointerException e) {
      assertEquals("NPE did not contain the expected message.", message, e.getMessage());
    }
  }

  private void assertCreateSha1RefThrowsIllegalArgException(String sha1Name, String message) {
    try {
      Ref.createSha1Ref(sha1Name);
      assertTrue("IllegalArgumentException not thrown when one was expected!", false);
    } catch (IllegalArgumentException e) {
      assertEquals("IllegalArgumentException did not contain the expected message.", message, e
          .getMessage());
    }
  }

  @Test
  public void testCreateTagRef() {
    // Testing invalid input
    assertCreateTagRefThrowsNPE(null,
        "000001: A String argument was not specified but is required.  { variableName=[name] }");
    assertCreateTagRefThrowsIllegalArgException("",
        "000001: A String argument was not specified but is required.  { variableName=[name] }");

    // Testing valid input
    Ref ref = Ref.createTagRef("v2.1");
    Ref ref2 = Ref.createTagRef("v2.1");
    assertEquals("Expected RefType of TAG.", ref.getRefType(), Ref.RefType.TAG);
    assertEquals(ref.getHeadOffset(), -1);
    assertEquals(ref.getName(), "v2.1");
    assertEquals(ref.getRepositoryName(), null);
    assertEquals(ref.toString(), "v2.1");
    assertEquals(ref, ref2);
    assertEquals(ref.hashCode(), ref2.hashCode());

    ref = Ref.createTagRef("Release-5.5.2");
    assertTrue(!ref.equals(ref2));

    ref2 = Ref.createTagRef("Release-5.5.2");
    assertEquals("Expected RefType of TAG.", ref.getRefType(), Ref.RefType.TAG);
    assertEquals(ref.getHeadOffset(), -1);
    assertEquals(ref.getName(), "Release-5.5.2");
    assertEquals(ref.getRepositoryName(), null);
    assertEquals(ref.toString(), "Release-5.5.2");
    assertEquals(ref, ref2);
    assertEquals(ref.hashCode(), ref2.hashCode());
  }

  private void assertCreateTagRefThrowsNPE(String name, String message) {
    try {
      Ref.createTagRef(name);
      assertTrue("NPE exception not thrown when one was expected!", false);
    } catch (NullPointerException e) {
      assertEquals("NPE did not contain the expected message.", message, e.getMessage());
    }
  }

  private void assertCreateTagRefThrowsIllegalArgException(String name, String message) {
    try {
      Ref.createTagRef(name);
      assertTrue("IllegalArgumentException not thrown when one was expected!", false);
    } catch (IllegalArgumentException e) {
      assertEquals("IllegalArgumentException did not contain the expected message.", message, e
          .getMessage());
    }
  }

}
