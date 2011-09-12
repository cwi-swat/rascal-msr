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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.test.utilities.FileUtilities;
import edu.nyu.cs.javagit.test.utilities.HelperGitCommands;
import edu.nyu.cs.javagit.utilities.ExceptionMessageMap;

public class TestGitMv extends TestCase {
  //Repository path.
  private File repoDirectory;
  private GitCommit commit;
  private GitAdd add;
  private GitMv mv;
  
  private File source;
  private File destination;
  
  private File fileOne;
  private File fileTwo;
  private File fileThree;
  
  private File subDirOne;
   
  @Before
  protected void setUp() throws IOException, JavaGitException {
    repoDirectory = FileUtilities.createTempDirectory("GitMvTestRepo");
    GitInit gitInit = new GitInit();
    gitInit.init(repoDirectory);
    commit = new GitCommit();
    add = new GitAdd();
    mv = new GitMv();

    source = FileUtilities.createFile(repoDirectory, "fileA.txt", "Sample Contents");
    // Add a file to the repository.
    List<File> filesToAdd = new ArrayList<File>();
    filesToAdd.add(source);
    add.add(repoDirectory, null, filesToAdd);

    // Call commit
    commit.commit(repoDirectory, "Committing the source file");
  }
  
  @After
  protected void tearDown() throws JavaGitException {
    // delete repo directory.
    FileUtilities.removeDirectoryRecursivelyAndForcefully(repoDirectory);
  }

  @Test
  public void testRename() throws IOException, JavaGitException {
    // Calling GitMv
    destination = new File("fileB.txt");
    mv.mv(repoDirectory, source, destination);
  }
  
  @Test
  public void testMove() throws IOException, JavaGitException {
    // Calling GitMv
    subDirOne = new File(repoDirectory.getPath() + File.separator + "subDirOne");
    subDirOne.mkdir();
    subDirOne = new File(subDirOne.getName());
    fileOne = FileUtilities.createFile(repoDirectory, "fileOne", "Testfile#1");
    fileTwo = FileUtilities.createFile(repoDirectory, "fileTwo", "Testfile#2");
    fileThree = FileUtilities.createFile(repoDirectory, "fileThree", "Testfile#3");
    // Add files to the repository
    List<File> filesToAdd = new ArrayList<File>();
    filesToAdd.add(fileOne);
    filesToAdd.add(fileTwo);
    filesToAdd.add(fileThree);
    filesToAdd.add(subDirOne);
    add.add(repoDirectory, null, filesToAdd);
    commit.commit(repoDirectory, "Making the commit");
    
    List<File> filesToMove = new ArrayList<File>();
    filesToMove.add(fileOne);
    filesToMove.add(fileTwo);
    filesToMove.add(fileThree);
    destination = subDirOne;
    mv.mv(repoDirectory, filesToMove, destination);
  }
  
  //check if exceptions are thrown below for invalid arguments
  @Test
  public void testGitMvInvalidInput() throws IOException, JavaGitException {
    
    //source file.
    File source = new File("oldFile");

    //destination file.
    File destination = new File("newFile");

    GitMv gitMv = new GitMv();
    try {
      gitMv.mv(null, source, destination);
    } catch (Exception e) {
      assertEquals("Should have null pointer exception or illegal argument exception",
          ExceptionMessageMap.getMessage("000003") + "  { variableName=[repository path] }", e
              .getMessage());
    }
    
    List<File> fileList = new ArrayList<File>();
    try {
      gitMv.mv(repoDirectory, fileList, destination);
    } catch (Exception e) {
      assertEquals("Should have null pointer exception or illegal argument exception",
          ExceptionMessageMap.getMessage("000005") + "  { variableName=[sources] }", 
          e.getMessage());
    }
    
    try {
      gitMv.mv(repoDirectory, null, fileList, destination);
    } catch (Exception e) {
      assertEquals("Should have null pointer exception or illegal argument exception",
          ExceptionMessageMap.getMessage("000003") + "  { variableName=[options] }", 
          e.getMessage());
    }
    
    try {
      gitMv.mv(repoDirectory, source, null);
    } catch (Exception e) {
      assertEquals("Should have null pointer exception or illegal argument exception",
          ExceptionMessageMap.getMessage("000003") + "  { variableName=[destination] }", e
              .getMessage());
    }

    try {
      gitMv.mv(repoDirectory, null, source, destination);
    } catch (Exception e) {
      assertEquals("Should have null pointer exception or illegal argument exception",
          ExceptionMessageMap.getMessage("000003") + "  { variableName=[options] }", 
          e.getMessage());
    }
  }
}
