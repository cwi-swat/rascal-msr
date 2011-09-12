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
package edu.nyu.cs.javagit.client.cli;

import junit.framework.TestCase;

import org.junit.Test;

import edu.nyu.cs.javagit.api.commands.GitAddOptions;
import edu.nyu.cs.javagit.client.cli.CliGitAdd;
import edu.nyu.cs.javagit.client.cli.CliGitAdd.GitAddParser;


public class TestGitAddResponseImpl extends TestCase {

  CliGitAdd gitAdd;
  GitAddParser parser;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gitAdd = new CliGitAdd();
    parser = new CliGitAdd.GitAddParser();
  }

  @Test
  public void testEnclosingQuoteStringParser() {
    String tokenWithSingQuotes = "'foobar'";
    String tokenWithoutSingleQuotes = "foobar";
    assertTrue(parser.enclosedWithSingleQuotes(tokenWithSingQuotes));
    assertFalse(parser.enclosedWithSingleQuotes(tokenWithoutSingleQuotes));
  }
  
  @Test
  public void testFilterFilename() {
    String tokenWithSingleQuotes = "'This_is_a_test'";
    assertEquals("This_is_a_test", parser.filterFileName(tokenWithSingleQuotes));
    // File with spaces in the name
    tokenWithSingleQuotes = "'This is a test'";
    assertEquals("This is a test", parser.filterFileName(tokenWithSingleQuotes));
    // File name with relative path starting with a dot
    tokenWithSingleQuotes = "'./some/relative/path'";
    assertEquals("./some/relative/path", parser.filterFileName(tokenWithSingleQuotes));
    // File name with relative path starting without a dot
    tokenWithSingleQuotes = "'some/relative/path'";
    assertEquals("some/relative/path", parser.filterFileName(tokenWithSingleQuotes));
    // File name with digits in filename
    tokenWithSingleQuotes = "'some1/relative1'";
    assertEquals("some1/relative1", parser.filterFileName(tokenWithSingleQuotes));
    // File name with single char as filename
    tokenWithSingleQuotes = "'A'";
    assertEquals("A", parser.filterFileName(tokenWithSingleQuotes));
    // File with Windows style path  name
    tokenWithSingleQuotes = "'foo\bar'";
    assertEquals("foo\bar", parser.filterFileName(tokenWithSingleQuotes));
    // Windows file without single quotes
    tokenWithSingleQuotes = "foo\bar";
    assertEquals(null, parser.filterFileName(tokenWithSingleQuotes));
  }
  
  @Test
  public void testGitAddResponseObj() {
    GitAddOptions options = new GitAddOptions();
    options.setDryRun(true);
  }
}
