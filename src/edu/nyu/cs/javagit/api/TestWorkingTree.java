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
import edu.nyu.cs.javagit.api.WorkingTree;
import edu.nyu.cs.javagit.api.DotGit;
import edu.nyu.cs.javagit.api.GitFile;
import edu.nyu.cs.javagit.api.GitDirectory;

/**
 * Test cases for our <code>WorkingTree</code> class.
 */
public class TestWorkingTree extends TestCase {
  // We want two different strings that represent the same place in the filesystem.
  private static final String TEST_DIRNAME = "testdir";
  private static final String TEST_DIRNAME_ALTERNATE = "testdir/../testdir";

  // Make this one totally different than the first two.
  private static final String TEST_DIRNAME_2 = "testdir2";

  @Test
  public void testWorkingTreeEquality() {
    /*
     * First we test two <code>WorkingTree</code> objects that should be equal, using both <code>File</code>/<code>String</code>
     * getInstance methods
     */
    runEqualityTests(TEST_DIRNAME, TEST_DIRNAME_ALTERNATE, true, true);
    runEqualityTests(TEST_DIRNAME, TEST_DIRNAME_ALTERNATE, false, true);

    // Then we test two that should not be equal
    runEqualityTests(TEST_DIRNAME, TEST_DIRNAME_2, true, false);
    runEqualityTests(TEST_DIRNAME, TEST_DIRNAME_2, false, false);
  }

  @Test
  public void testWorkingTreeDotGitEquality() {
    WorkingTree workingTree = WorkingTree.getInstance(TEST_DIRNAME);

    // Check that the <code>WorkingTree</code> path matches its <code>Git</code> path
    assertEquals(workingTree.getPath(), workingTree.getDotGit().getPath());

    // Also test equality not through <code>WorkingTree</code> but using getInstance
    assertEquals(workingTree.getDotGit(), DotGit.getInstance(TEST_DIRNAME));

    // Also check that things are equal a layer deeper
    assertEquals(workingTree, workingTree.getDotGit().getWorkingTree());
  }
  
  @Test
  public void testWorkingTreeGetFile() throws JavaGitException {
    WorkingTree workingTree = WorkingTree.getInstance(TEST_DIRNAME);
    File file = new File("file1");

    GitFile gitFile = workingTree.getFile(file);
    assertEquals(gitFile.getWorkingTree(), workingTree);
    
    GitDirectory gitDirectory = workingTree.getDirectory(file);
    assertEquals(gitDirectory.getWorkingTree(), workingTree);
  }

  private static void runEqualityTests(String path1, String path2, boolean accessViaFileObjects,
      boolean testEquality) {
    WorkingTree workingTree1, workingTree2;

    if (accessViaFileObjects) {
      workingTree1 = WorkingTree.getInstance(new File(path1));
      workingTree2 = WorkingTree.getInstance(new File(path2));
    } else {
      workingTree1 = WorkingTree.getInstance(path1);
      workingTree2 = WorkingTree.getInstance(path2);
    }

    if (testEquality) {
      assertEquals(workingTree1, workingTree2);
      assertEquals(workingTree1.hashCode(), workingTree2.hashCode());
      assertEquals(workingTree1.getDotGit(), workingTree2.getDotGit());
    } else {
      assertNotEquals(workingTree1, workingTree2);
      assertNotEquals(workingTree1.hashCode(), workingTree2.hashCode());
      assertNotEquals(workingTree1.getDotGit(), workingTree2.getDotGit());
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
