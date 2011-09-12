
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
import java.util.Iterator;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.client.cli.CliGitStatus;
import edu.nyu.cs.javagit.client.cli.CliGitStatus.GitStatusParser;


public class TestGitStatusResponse extends TestCase {

 GitStatusParser parser;
 CliGitStatus gitStatus;
 private String repositoryDirectory;

 @Before
 public void setUp() throws Exception {
   repositoryDirectory = new String("GitStatusResponseTestRepository" + File.separator);
   parser = new GitStatusParser(repositoryDirectory);
   gitStatus = new CliGitStatus();
 }

 /**
  * Test for JavaGitException is thrown when an invalid switch is passed to 
  * &lt;git-status&gt; 
  */
 @Test
 public void testInvalidSwitchOptionThrowsException() {
   GitStatusParser parser = new GitStatusParser(repositoryDirectory);
   GitStatusResponse response = null;
   try {
     parser.parseLine("error: unknown switch `w'");
     parser.parseLine("usage: git-status [options] [--] <filepattern>...");

     response = parser.getResponse();
     fail("Failed to throw JavaGitException for an invalid switch");
   } catch ( JavaGitException e ) {
     assertEquals("JavaGitException code", 438000, e.getCode());
     assertEquals("response object", null, response);
   }
 }

 /**
  * Test for JavaGitException is thrown if an invalid file name is passed to 
  * &lt;git-status&gt; command.
  */
 @Test
 public void testInvalidFile() {
   GitStatusParser parser = new GitStatusParser(repositoryDirectory);
   GitStatusResponse response = null;
   parser.parseLine("error: pathspec 'foobar' did not match any file(s) known to git.");
   try {
     response = parser.getResponse();
     fail("Failed to throw JavaGitException for an invalid filename");
   } catch ( JavaGitException e ) {
     assertEquals("JavaGitException code", 438000, e.getCode());
     assertEquals("response object", null, response);
   }
 }

 @Test
 public void testModifiedFilePattern() {
   String line = "#  modified:   patttrn06";
   assertTrue(CliGitStatus.Patterns.MODIFIED.matches(line));
   line = "# xyz modified: pattern06";
   assertFalse(CliGitStatus.Patterns.MODIFIED.matches(line));
   // Files with spaces may exist in Windows system
   line = "# modified: xyz pattern06";
   assertTrue(CliGitStatus.Patterns.MODIFIED.matches(line));
   line = "# modified:   dir1/dir2/dir3/foobar07";
   assertTrue(CliGitStatus.Patterns.MODIFIED.matches(line));
 }

 @Test
 public void testNewFilePattern() {
   String line = "#  new file:   foobar03";
   assertTrue(CliGitStatus.Patterns.NEW_FILE.matches(line));
   line = "#       new file:                  foobar03";
   line = "# xyz new file: pattern06";
   assertFalse(CliGitStatus.Patterns.NEW_FILE.matches(line));
   // Files with spaces may exist in Windows systems
   line = "# new file: xyz pattern06";
   assertTrue(CliGitStatus.Patterns.NEW_FILE.matches(line));
   line = "#  new file:   dir1/dir2/dir3/foobar07";
   assertTrue(CliGitStatus.Patterns.NEW_FILE.matches(line));
 }

 /**
  * Test for parsing lines that point to deleted files.
  */
 @Test
 public void testDeletedFilePattern() {
   String line = "# deleted:    foobar03";
   assertTrue(CliGitStatus.Patterns.DELETED.matches(line));
   line = "#       deleted:                  foobar03";
   assertTrue(CliGitStatus.Patterns.DELETED.matches(line));
   line = "# xyz deleted: pattern06";
   assertFalse(CliGitStatus.Patterns.DELETED.matches(line));
   line = "# deleted: xyz pattern06";
   assertTrue(CliGitStatus.Patterns.DELETED.matches(line));
   line = "#  deleted:   dir1/dir2/dir3/foobar07";
   assertTrue(CliGitStatus.Patterns.DELETED.matches(line));
 }

 /**
  * Test for verifying pattern for lines that only contains a hash(#) in them.
  */
 public void testEmptyHashLinePattern() {
   String line = "#";
   assertTrue(CliGitStatus.Patterns.EMPTY_HASH_LINE.matches(line));
   line = "#      ";
   assertTrue(CliGitStatus.Patterns.EMPTY_HASH_LINE.matches(line));
   line = "# test";
   assertFalse(CliGitStatus.Patterns.EMPTY_HASH_LINE.matches(line));
 }

 /**
  * Test for confirming that lines filenames are parsed correctly and correct filenames are
  * extracted from the line.
  */
 @Test
 public void testGetFilename() {
   String line = "# deleted:    foobar03";
   assertEquals("foobar03", parser.getFilename(line));
   line = "#  new file:   foobar04";
   assertEquals("foobar04", parser.getFilename(line));
   line = "#  new file:   testDir1/foobar04";
   assertEquals("testDir1/foobar04", parser.getFilename(line));
 }

 /**
  * Test for verifying the correct branch name in comment in <code>GitStatusResposne</code>
  */
 @Test
 public void testNothingToCommit() throws JavaGitException {
   GitStatusParser parser = new GitStatusParser(repositoryDirectory);
   parser.parseLine("# On branch master");
   parser.parseLine("nothing to commit (working directory clean)");

   GitStatusResponse response = parser.getResponse();
   assertEquals("Branch name does not match", "master", response.getBranch().getName());
   assertEquals("Status message does not match", "nothing to commit (working directory clean)",
       response.getMessage());
   assertEquals("No. of untracked files", 0, response.getUntrackedFilesSize());
   assertEquals("No. of new filesToCommit", 0, response.getNewFilesToCommitSize());
   assertEquals("No. of deletedFilesToCommit", 0, response.getDeletedFilesToCommitSize());
   assertEquals("No. of modifiedFilesToCommit", 0, response.getModifiedFilesToCommitSize());
   assertEquals("No. of deletedFilesNotUpdated", 0, response.getDeletedFilesNotUpdatedSize());
   assertEquals("No. of modifiedFilesNotUpdated", 0, response.getModifiedFilesNotUpdatedSize());
   assertEquals("No. of errors", 0, response.getErrorCount());
 }

 /**
  * Test for verifying that correct no. of untracked files are created in the
  * <code>GitStatusResponse</code> object when <code>GitStatus</code> is run.
  */
 @Test
 public void testUntrackedFilesAndDircotories() throws JavaGitException {
   GitStatusParser parser = new GitStatusParser(repositoryDirectory);

   parser.parseLine("# On branch master");
   parser.parseLine("# Untracked files:");
   parser.parseLine("#   (use \"git add <file>...\" to include in what will be committed)\"");
   parser.parseLine("#");
   parser.parseLine("# dir/");
   parser.parseLine("# fileA");
   parser.parseLine("# fileB");
   parser.parseLine("# fileC");
   parser.parseLine("nothing added to commit but untracked files present (use \"git add\" to track)");

   GitStatusResponse response = parser.getResponse();
   assertEquals("No. of untracked files", 4, response.getUntrackedFilesSize());
   assertEquals("No. of new filesToCommit", 0, response.getNewFilesToCommitSize());
   assertEquals("No. of deletedFilesToCommit", 0, response.getDeletedFilesToCommitSize());
   assertEquals("No. of modifiedFilesToCommit", 0, response.getModifiedFilesToCommitSize());
   assertEquals("No. of deletedFilesNotUpdated", 0, response.getDeletedFilesNotUpdatedSize());
   assertEquals("No. of modifiedFilesNotUpdated", 0, response.getModifiedFilesNotUpdatedSize());
   assertEquals("No. of errors", 0, response.getErrorCount());

   Iterator<File> iter = response.getUntrackedFiles().iterator();
   assertEquals("FileName", repositoryDirectory + "dir", iter.next().getPath());
   assertEquals("FileName", repositoryDirectory + "fileA", iter.next().getPath());
   assertEquals("FileName", repositoryDirectory + "fileB", iter.next().getPath());
   assertEquals("FileName", repositoryDirectory + "fileC", iter.next().getPath());
 }

 /**
  * Test verifies the names and no. of untracked files & new files that will be committed next time
  * &lt;git-commit&gt; is run.
  */
 @Test
 public void testNewlyAddedFiles() throws JavaGitException {
   GitStatusParser parser = new GitStatusParser(repositoryDirectory);
   parser.parseLine("# On branch master");
   parser.parseLine("# Changes to be committed:");
   parser.parseLine("#   (use \"git reset HEAD <file>...\" to unstage)");
   parser.parseLine("#");
   parser.parseLine("# new file:   dir/fileD");
   parser.parseLine("# new file:   fileA");
   parser.parseLine("#");
   parser.parseLine("# Untracked files:");
   parser.parseLine("#   (use \"git add <file>...\" to include in what will be committed)");
   parser.parseLine("#");
   parser.parseLine("# fileB");
   parser.parseLine("# fileC");

   GitStatusResponse response = parser.getResponse();
   assertEquals("Branch Name", "master", response.getBranch().getName());
   assertEquals("No. of untracked files", 2, response.getUntrackedFilesSize());
   assertEquals("No. of new filesToCommit", 2, response.getNewFilesToCommitSize());
   assertEquals("No. of deletedFilesToCommit", 0, response.getDeletedFilesToCommitSize());
   assertEquals("No. of modifiedFilesToCommit", 0, response.getModifiedFilesToCommitSize());
   assertEquals("No. of deletedFilesNotUpdated", 0, response.getDeletedFilesNotUpdatedSize());
   assertEquals("No. of modifiedFilesNotUpdated", 0, response.getModifiedFilesNotUpdatedSize());
   assertEquals("No. of errors", 0, response.getErrorCount());

   Iterator<File> iter = response.getNewFilesToCommit().iterator();
   assertEquals("File Name", repositoryDirectory + "dir" + File.separator + "fileD", 
       iter.next().getPath());
   assertEquals("File Name", repositoryDirectory + "fileA", iter.next().getPath());

   iter = response.getUntrackedFiles().iterator();
   assertEquals("File Name", repositoryDirectory + "fileB", iter.next().getPath());
   assertEquals("File Name", repositoryDirectory + "fileC", iter.next().getPath());
 }

 /**
  * Test simulates parsing an output generated by &lt;git-status&gt; command and verifies that no.
  * of files parsed for newFilesToCommit, deletedFilesToCommit, deletedFilesNotUpdated and
  * modifiedFilesNotUpdated are correct and match.
  */
 @Test
 public void testModifiedAndDeletedFiles() throws JavaGitException {
   GitStatusParser parser = new GitStatusParser(repositoryDirectory);

   parser.parseLine("# On branch master");
   parser.parseLine("# Changes to be committed:");
   parser.parseLine("#   (use \"git reset HEAD <file>...\" to unstage)");
   parser.parseLine("#");
   parser.parseLine("# new file:   dir/fileD");
   parser.parseLine("# deleted:    fileC");
   parser.parseLine("#");
   parser.parseLine("# Changed but not updated:");
   parser.parseLine("#   (use \"git add <file>...\" to update what will be committed)");
   parser.parseLine("#");
   parser.parseLine("# modified:   fileA");
   parser.parseLine("# deleted:    fileB");
   parser.parseLine("#");

   GitStatusResponse response = parser.getResponse();
   assertEquals("No. of untracked files", 0, response.getUntrackedFilesSize());
   assertEquals("No. of new filesToCommit", 1, response.getNewFilesToCommitSize());
   assertEquals("No. of deletedFilesToCommit", 1, response.getDeletedFilesToCommitSize());
   assertEquals("No. of modifiedFilesToCommit", 0, response.getModifiedFilesToCommitSize());
   assertEquals("No. of deletedFilesNotUpdated", 1, response.getDeletedFilesNotUpdatedSize());
   assertEquals("No. of modifiedFilesNotUpdated", 1, response.getModifiedFilesNotUpdatedSize());
   assertEquals("No. of errors", 0, response.getErrorCount());

   assertEquals("FileName", repositoryDirectory + "dir" + File.separator + "fileD", 
       response.getNewFilesToCommit().iterator().next().getPath());
   assertEquals("FileName", repositoryDirectory + "fileC", 
       response.getDeletedFilesToCommit().iterator().next().getPath());
   assertEquals("FileName", repositoryDirectory + "fileA", 
       response.getModifiedFilesNotUpdated().iterator().next().getPath());
   assertEquals("FileName", repositoryDirectory + "fileB", 
       response.getDeletedFilesNotUpdated().iterator().next().getPath());
 }

 /**
  * Test for verifying renamed files that will be committed next time git commit command is run.
  * @throws IOException
  * @throws JavaGitException
  */
 @Test
 public void testRenamedFilesToCommitFromCommandOutput() throws IOException, JavaGitException {
	GitStatusParser parser = new GitStatusParser(repositoryDirectory);
	parser.parseLine("# On branch master");
	parser.parseLine("# Changes to be committed:");
	parser.parseLine("#   (use \"git reset HEAD <file>...\" to unstage)");
	parser.parseLine("#");
	parser.parseLine("#       renamed:    file2 -> file1");
	parser.parseLine("#       renamed:    file5 -> file6");
	parser.parseLine("#");	
	GitStatusResponse response = parser.getResponse();
	Iterable<File> renamedFiles = response.getRenamedFilesToCommit();
	assertEquals("Renamed File name does not match", repositoryDirectory + "file1", 
	    renamedFiles.iterator().next().getPath());
	assertEquals("Renamed File name does not match", repositoryDirectory + "file6", 
	    renamedFiles.iterator().next().getPath());
	assertEquals("No. of renamed files", 2, response.getRenamedFilesToCommitSize());
 }
 
 /**
  * Test for verifying that modified staged and un-staged files are parsed properly by
  * <code>GitStatusParser</code>
  */
 @Test
 public void testModifiedFilesFromCommandOutput() throws IOException, JavaGitException {

   GitStatusParser parser = new GitStatusParser(repositoryDirectory);

   parser.parseLine("# On branch master");
   parser.parseLine("# Changes to be committed:");
   parser.parseLine("#   (use \"git reset HEAD <file>...\" to unstage");
   parser.parseLine("#");
   parser.parseLine("#       modified:   src/Modified.java");
   parser.parseLine("#       deleted:    src/Removed.java");
   parser.parseLine("#       deleted:    src/RemovedNotStaged.java");
   parser.parseLine("#");
   parser.parseLine("# Changed but not updated:");
   parser.parseLine("#   (use \"git add <file>...\" to update what will be committed)");
   parser.parseLine("#");
   parser.parseLine("#       modified:   src/ModifiedNotStaged.java");
   parser.parseLine("#");
   parser.parseLine("# Untracked files:");
   parser.parseLine("#   (use \"git add <file>...\" to include in what will be committed)");
   parser.parseLine("#");
   parser.parseLine("#       .classpath");
   parser.parseLine("#       .project");
   parser.parseLine("#       bin/");

   GitStatusResponse response = parser.getResponse();
   assertEquals(1, response.getModifiedFilesNotUpdatedSize());
   assertEquals("ModifiedNotStaged.java", response.getModifiedFilesNotUpdated().iterator().next()
       .getName());
   assertEquals(1, response.getModifiedFilesToCommitSize());
   assertEquals("Modified.java", response.getModifiedFilesToCommit().iterator().next().getName());
 }
 
}
