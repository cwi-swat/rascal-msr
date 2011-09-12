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
package edu.nyu.cs.javagit.test.utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import edu.nyu.cs.javagit.api.JavaGitException;

/**
 * Some simple file utility methods for helping with testing java git functionality.
 */
public class FileUtilities {

  /**
   * Create a temp directory based on the supplied directory base name. The directory will be
   * created under the temp directory for the system running the program.
   * 
   * @param baseDirName
   *          A base name for the directory. If this directory exists, a directory based on this
   *          name will be created.
   * @return A <code>File</code> instance representing the created directory.
   */
  public static File createTempDirectory(String baseDirName) throws IOException {
    // Get the system temp directory location.
    File tmpfile = File.createTempFile("AfileName", "tmp");
    String absPath = tmpfile.getAbsolutePath();
    tmpfile.delete();
    int lastSep = absPath.lastIndexOf(File.separatorChar);
    String tmpDir = absPath.substring(0, lastSep + 1);

    // System.out.println("Temp Directory: tmpDir=["+ tmpDir + "]");

    // Create a temp directory for the caller
    File file = new File(tmpDir + baseDirName);
    int num = 0;
    while (!file.mkdir()) {
      file = new File(tmpDir + baseDirName + Integer.toString(num++));
    }

    return file;
  }

  /**
   * Create a new file and write the contents to it.
   * 
   * @param baseDir
   *          The base directory of the repo.
   * @param filePath
   *          The relative path to the file with respect to the base directory.
   * @param contents
   *          Some contents to write to the file.
   * @return A <code>File</code> object representation of the file.
   * @throws IOException
   *           If there are problems creating the file or writing the contents to the file.
   */
  public static File createFile(File baseDir, String filePath, String contents) throws IOException {
    File file = new File(baseDir.getPath() + File.separator + filePath);
    file.createNewFile();
    FileWriter fw = new FileWriter(file);
    fw.write(contents);
    fw.flush();
    fw.close();
    return new File(filePath);
  }

  /**
   * Recursively deletes the specified file/directory.
   * 
   * @param dirOrFile
   *          The file or directory to delete recursively and forcefully.
   * @throws JavaGitException
   *           Thrown if a file or directory was not deleted.
   */
  public static void removeDirectoryRecursivelyAndForcefully(File dirOrFile)
      throws JavaGitException {
    File[] children = dirOrFile.listFiles();
    if (null != children) {
      for (File f : children) {
        removeDirectoryRecursivelyAndForcefully(f);
      }
    }
    //System.gc();
    if (!dirOrFile.delete()) {
      throw new JavaGitException(-1, "-1:  Unable to delete file/directory.  { path=["
          + dirOrFile.getAbsolutePath() + "] }");
    }
  }

  /**
   * Append some text to an existing file.
   * 
   * @param file
   *          File that will be modified
   * @param appendText
   *          The text that will be appended to the file
   * @throws FileNotFoundException
   *           Exception thrown if the file does not exist.
   * @throws IOException
   *           thrown if the IO operation fails.
   */
  public static void modifyFileContents(File file, String appendText) throws FileNotFoundException,
      IOException {
    if (!file.exists()) {
      throw new FileNotFoundException("File does not exist");
    }
    FileWriter fw = new FileWriter(file);
    fw.append(appendText);
    fw.close();
  }

}
