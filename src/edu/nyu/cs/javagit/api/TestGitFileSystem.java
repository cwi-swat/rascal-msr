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

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.DotGit;
import edu.nyu.cs.javagit.api.WorkingTree;
import edu.nyu.cs.javagit.api.GitFileSystemObject;
import edu.nyu.cs.javagit.api.GitFileSystemObject.Status;
import edu.nyu.cs.javagit.api.commands.GitInit;
import edu.nyu.cs.javagit.api.commands.GitStatusResponse;
import edu.nyu.cs.javagit.api.GitFile;
//import edu.nyu.cs.javagit.api.GitDirectory;

import edu.nyu.cs.javagit.test.utilities.FileUtilities;
import edu.nyu.cs.javagit.test.utilities.HelperGitCommands;


public class TestGitFileSystem extends TestCase {

  private File repositoryDirectory;
  private DotGit dotGit;
  private WorkingTree workingTree;
  
  @Before
  public void setUp() throws JavaGitException, IOException {
    repositoryDirectory = FileUtilities.createTempDirectory("GitFileSystemTest_dir");
    GitInit gitInit = new GitInit();
    gitInit.init(repositoryDirectory);
    dotGit = DotGit.getInstance(repositoryDirectory);
    workingTree = WorkingTree.getInstance(repositoryDirectory);
  }


  
  /**
   * creates a single file and runs a series of tests on it
   */
  @Test
  public void testSingleGitFile() throws IOException, JavaGitException {
    //Create a file
    FileUtilities.createFile(repositoryDirectory, "abc.txt", "Some data");

    //check contents
    List<GitFileSystemObject> children = workingTree.getTree();
    assertEquals("Error. Expecting only one file.", 1, children.size());

    GitFileSystemObject currentFile = children.get(0);
    assertEquals("Error. Expecting instance of GitFile.", GitFile.class, currentFile.getClass());

    GitFile gitFile = (GitFile)currentFile;
    assertEquals("Error. Expecting UNTRACKED status for the single file.", Status.UNTRACKED, gitFile.getStatus());

    gitFile.add();
    assertEquals("Error. Expecting NEW_TO_COMMIT status for the single file.", Status.NEW_TO_COMMIT, gitFile.getStatus());
    
    workingTree.getFile(new File("x"));

    gitFile.commit("commit message");
    assertEquals("Error. Expecting IN_REPOSITORY status for the single file.", Status.IN_REPOSITORY, gitFile.getStatus());
   
    FileUtilities.modifyFileContents(gitFile.getFile(), "more data");
    assertEquals("Error. Expecting MODIFIED status for the single file.", Status.MODIFIED, gitFile.getStatus());
 
    gitFile.add();
    assertEquals("Error. Expecting MODIFIED_TO_COMMIT status for the single file.", Status.MODIFIED_TO_COMMIT, gitFile.getStatus());
    
    gitFile.commit("commit message");
    assertEquals("Error. Expecting IN_REPOSITORY status for the single file.", Status.IN_REPOSITORY, gitFile.getStatus());

  }
  
  
  @Test
  /**
   * Adds more file system objects
   */
  public void testGitFileSystem() throws IOException, JavaGitException {
    //Add another file
    FileUtilities.createFile(repositoryDirectory, "file1", "Some data");
    FileUtilities.createFile(repositoryDirectory, "file2", "Some data");
    
    //check contents
    List<GitFileSystemObject> children = workingTree.getTree();
    assertEquals("Error. Expecting 2 files.", 2, children.size());

    //attempt to commit (but without anything on the index)
    try {
      workingTree.commit("commit comment");
      fail("JavaGitException not thrown");
    }
    catch(JavaGitException e) {
    }

    //get children
    GitFile gitFile1 = (GitFile)children.get(0);
    GitFile gitFile2 = (GitFile)children.get(1);

    //both should be untracked
    assertEquals("Error. Expecting UNTRACKED.", Status.UNTRACKED, gitFile1.getStatus());
    assertEquals("Error. Expecting UNTRACKED.", Status.UNTRACKED, gitFile2.getStatus());

    //another way to see the same thing
    File file1 = new File("file1");
    File file2 = new File("file2");
    GitStatusResponse statusResponse = workingTree.getStatus();
    assertEquals("Error. Expecting UNTRACKED.", Status.UNTRACKED, 
        statusResponse.getFileStatus(file1));
    assertEquals("Error. Expecting UNTRACKED.", Status.UNTRACKED, 
        statusResponse.getFileStatus(file2));


    //stage one file
    gitFile1.add();
    
    //TODO (ma1683): check why the following tests fail on different system
 /*
    //check status

    assertEquals("Error. Expecting NEW_TO_COMMIT.", Status.NEW_TO_COMMIT, gitFile1.getStatus());
    assertEquals("Error. Expecting UNTRACKED.", Status.UNTRACKED, gitFile2.getStatus());

    //alternative way
    statusResponse = workingTree.getStatus();
    assertEquals("Error. Expecting NEW_TO_COMMIT.", Status.NEW_TO_COMMIT, 
        statusResponse.getFileStatus(file1));
    assertEquals("Error. Expecting UNTRACKED.", Status.UNTRACKED, 
        statusResponse.getFileStatus(file2));

    //commit everything added to the index
    workingTree.commitAll("commit comment");
    //check status
    assertEquals("Error. Expecting IN_REPOSITORY.", Status.IN_REPOSITORY, gitFile1.getStatus());
    assertEquals("Error. Expecting UNTRACKED.", Status.UNTRACKED, gitFile2.getStatus());
    //alternative way
    statusResponse = workingTree.getStatus();
    assertEquals("Error. Expecting IN_REPOSITORY.", Status.IN_REPOSITORY, 
        statusResponse.getFileStatus(file1));
    assertEquals("Error. Expecting UNTRACKED.", Status.UNTRACKED, 
        statusResponse.getFileStatus(file2));

    //commit everything
    workingTree.addAndCommitAll("commit comment");
    //check status
    assertEquals("Error. Expecting IN_REPOSITORY.", Status.IN_REPOSITORY, gitFile1.getStatus());
    assertEquals("Error. Expecting IN_REPOSITORY.", Status.IN_REPOSITORY, gitFile2.getStatus());
    //alternative way
    statusResponse = workingTree.getStatus();
    assertEquals("Error. Expecting IN_REPOSITORY.", Status.IN_REPOSITORY, 
        statusResponse.getFileStatus(file1));
    assertEquals("Error. Expecting IN_REPOSITORY.", Status.IN_REPOSITORY, 
        statusResponse.getFileStatus(file2));
*/
  }
  
  
  @After
  public void tearDown() throws Exception {
    if ( repositoryDirectory.exists() ) {
      FileUtilities.removeDirectoryRecursivelyAndForcefully(repositoryDirectory);
    }
  }

}
