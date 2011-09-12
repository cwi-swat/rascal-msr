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
import java.io.IOException;
import java.util.List;
import java.util.Iterator;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.DotGit;
import edu.nyu.cs.javagit.api.WorkingTree;
import edu.nyu.cs.javagit.api.GitFileSystemObject;
import edu.nyu.cs.javagit.api.GitFile;
import edu.nyu.cs.javagit.api.commands.GitInit;

import edu.nyu.cs.javagit.test.utilities.FileUtilities;
import edu.nyu.cs.javagit.test.utilities.HelperGitCommands;


public class TestBranching extends TestCase {

  private File repositoryDirectory;
  private DotGit dotGit;
  private WorkingTree workingTree;
  
  @Before
  public void setUp() throws JavaGitException, IOException {
    repositoryDirectory = FileUtilities.createTempDirectory("TestGitBranching_dir");
    GitInit gitInit = new GitInit();
    gitInit.init(repositoryDirectory);
    dotGit = DotGit.getInstance(repositoryDirectory);
    workingTree = WorkingTree.getInstance(repositoryDirectory);
  }


  
  /**
   * 
   */
  @Test
  public void testGitApiBranch() throws IOException, JavaGitException {
    //Create a file
    FileUtilities.createFile(repositoryDirectory, "file1.txt", "Some data");

    //check contents
    List<GitFileSystemObject> children = workingTree.getTree();
    assertEquals("Error. Expecting only one file.", 1, children.size());

    GitFileSystemObject file = children.get(0);
    file.commit("some comment");

    //now we can create new branch
    Ref createdBranch = dotGit.createBranch("branch1");

    Ref currentBranch = workingTree.getCurrentBranch();
    assertEquals("Error. Expecting master branch.", 
        Ref.createBranchRef("master"), currentBranch);

    //iterate through branches
    boolean found = false;
    Iterator<Ref> branches = dotGit.getBranches();
    while(branches.hasNext()) {
      //check
      Ref branch = branches.next();
      if(branch.equals(createdBranch)) {
        found = true;
      }

      //test checkout
      if(!branch.equals(currentBranch)) {
        workingTree.checkout(branch);
        currentBranch = workingTree.getCurrentBranch();
        assertEquals("Error. Expecting new current branch.", branch, currentBranch);
      }
    }
    
    assertEquals("Error. Expecting created branch to appear.", true, found);

    //test branch rename
    String newName = new String("branch2");
    Ref renamedBranch = dotGit.renameBranch(Ref.createBranchRef("branch1"), newName, true);
    assertEquals("Error. Expecting renamed branch.", newName, renamedBranch.getName());
    
    found = false;
    branches = dotGit.getBranches();
    while(branches.hasNext()) {
      Ref branch = branches.next();
      if(branch.equals(renamedBranch)) {
        found = true;
      }
    }
    assertEquals("Error. Expecting renamed branch to appear.", true, found);
    
    //test branch delete
    dotGit.deleteBranch(renamedBranch, true);
    found = false;
    branches = dotGit.getBranches();
    while(branches.hasNext()) {
      Ref branch = branches.next();
      if(branch.equals(renamedBranch)) {
        found = true;
      }
    }
    assertEquals("Error. Expecting deleted branch to disappear.", false, found);
  }
  
  
  
  @After
  public void tearDown() throws Exception {
    if ( repositoryDirectory.exists() ) {
      FileUtilities.removeDirectoryRecursivelyAndForcefully(repositoryDirectory);
    }
  }

}
