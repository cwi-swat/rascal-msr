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

import java.io.File;
import junit.framework.TestCase;
import org.junit.Test;
import edu.nyu.cs.javagit.api.DotGit;
import edu.nyu.cs.javagit.api.WorkingTree;

/**
 * Test cases for our <code>DotGit</code> class.
 */
public class TestDotGit extends TestCase {
  // We want two different strings that represent the same place in the filesystem.
  private static final String TEST_DIRNAME = "testdir";
  private static final String TEST_DIRNAME_ALTERNATE = "testdir/../testdir";

  // Make this one totally different than the first two.
  private static final String TEST_DIRNAME_2 = "testdir2";

  @Test
  public void testDotGitEquality() {
    /*
     * First we test two <code>DotGit</code> objects that should be equal, using both <code>File</code>/<code>String</code>
     * getInstance methods
     */
    runEqualityTests(TEST_DIRNAME, TEST_DIRNAME_ALTERNATE, true, true);
    runEqualityTests(TEST_DIRNAME, TEST_DIRNAME_ALTERNATE, false, true);

    // Then we test two that should not be equal
    runEqualityTests(TEST_DIRNAME, TEST_DIRNAME_2, true, false);
    runEqualityTests(TEST_DIRNAME, TEST_DIRNAME_2, false, false);
  }

  @Test
  public void testDotGitWorkingTreeValidity() {
    DotGit dotGit = DotGit.getInstance(TEST_DIRNAME);
    
    // Check that the <code>DotGit</code> path matches its <code>WorkingTree</code> path
    assertEquals(dotGit.getPath(), dotGit.getWorkingTree().getPath());
    
    // Also test equality not through <code>DotGit</code> but using getInstance
    assertEquals(dotGit.getWorkingTree(), WorkingTree.getInstance(TEST_DIRNAME));
    
    // Also check that things are equal a layer deeper
    assertEquals(dotGit, dotGit.getWorkingTree().getDotGit());
  }

  private static void runEqualityTests(String path1, String path2, boolean accessViaFileObjects,
      boolean testEquality) {
    DotGit dotgit1, dotgit2;

    if (accessViaFileObjects) {
      dotgit1 = DotGit.getInstance(new File(path1));
      dotgit2 = DotGit.getInstance(new File(path2));
    } else {
      dotgit1 = DotGit.getInstance(path1);
      dotgit2 = DotGit.getInstance(path2);
    }

    if (testEquality) {
      assertEquals(dotgit1, dotgit2);
      assertEquals(dotgit1.hashCode(), dotgit2.hashCode());
      assertEquals(dotgit1.getWorkingTree(), dotgit2.getWorkingTree());
    } else {
      assertNotEquals(dotgit1, dotgit2);
      assertNotEquals(dotgit1.hashCode(), dotgit2.hashCode());
      assertNotEquals(dotgit1.getWorkingTree(), dotgit2.getWorkingTree());
    }
  }

  private static void assertNotEquals(Object obj1, Object obj2) {
    if (obj1.equals(obj2)) {
      fail("Objects were equal - expected unequal");
    }
    if (obj2.equals(obj1)) {
      fail("Objects were equal - expected unequal");
    }
  }
}
