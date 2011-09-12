package edu.nyu.cs.javagit.api;

import junit.framework.TestCase;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import edu.nyu.cs.javagit.api.JavaGitConfiguration;
import edu.nyu.cs.javagit.test.utilities.FileUtilities;

/**
 * Test cases for our <code>WorkingTree</code> class.
 */
public class TestJavaGitConfiguration extends TestCase {

  @Test
  public void testSettingGitPathExceptions() {
    String pathString;
    File pathFile;

    // Attempting to set a null path should generate a NullPointerException.
    pathString = null;
    try {
      JavaGitConfiguration.setGitPath(pathString);
      fail("Invalid path - this should never be reached.");
    } catch (NullPointerException e) {
      // This is fine.
    } catch (Exception e) {
      fail("Null path (String) generated wrong kind of exception: " + e.getClass().getName());
    }

    // Attempting to set an empty path as a String should generate an IllegalArgumentException.
    pathString = "";
    try {
      JavaGitConfiguration.setGitPath(pathString);
      fail("Invalid path - this should never be reached.");
    } catch (IllegalArgumentException e) {
      // This is fine.
    } catch (Exception e) {
      fail("Empty path (String) generated wrong kind of exception: " + e.getClass().getName());
    }

    // Attempting to set an empty path as a File should generate an IOException.
    pathFile = new File("");
    try {
      JavaGitConfiguration.setGitPath(pathFile);
      fail("Invalid path - this should never be reached.");
    } catch (IOException e) {
      // This is fine.
    } catch (Exception e) {
      fail("Empty path (String) generated wrong kind of exception: " + e.getClass().getName());
    }

    // Let's create a temp file and try to set that as the path. Should throw a JavaGitException.
    try {
      pathFile = File.createTempFile("xyz", null);
      JavaGitConfiguration.setGitPath(pathFile);
      fail("Invalid path - this should never be reached.");
    } catch (JavaGitException e) {
      assertEquals(e.getCode(), 020002);
      pathFile.delete();
    } catch (Exception e) {
      fail("Non-directory path (File) generated wrong kind of exception: " + e.getClass().getName());
    }

    /*
     * Let's create a temp dir and try to set that as the path. It won't contain git, so it should
     * throw a JavaGitException.
     */
    try {
      pathFile = FileUtilities.createTempDirectory("abc");
      JavaGitConfiguration.setGitPath(pathFile);
      fail("Invalid path - this should never be reached.");
    } catch (JavaGitException e) {
      assertEquals(e.getCode(), 100002);
      pathFile.delete();
    } catch (Exception e) {
      fail("Temp path not containing git generated wrong kind of exception: "
          + e.getClass().getName());
    }

    /*
     * Set the path using null as the File argument. This is valid - it's saying: wipe out whatever
     * was set before (via previous calls to setGitPath) and just look on the PATH for git.
     */
    pathFile = null;
    try {
      JavaGitConfiguration.setGitPath(pathFile);
    } catch (Exception e) {
      fail("Null path (File) generated exception: " + e.getClass().getName());
    }

    // Grab the version and see if we can get it exception-free.
    String version = "";
    try {
      version = JavaGitConfiguration.getGitVersion();
    } catch (Exception e) {
      fail("Couldn't get git version string - saw \"" + version + "\" and got exception: "
          + e.getClass().getName());
    }
  }
}
