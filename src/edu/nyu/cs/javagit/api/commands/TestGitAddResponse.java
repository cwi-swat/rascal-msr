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

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import edu.nyu.cs.javagit.api.JavaGitException;

public class TestGitAddResponse extends TestCase {

  @Before
  protected void setUp() throws IOException, JavaGitException, Exception {
    super.setUp();
  }
  
  @Test
  public void testVoidTest() {
    assertTrue(true);
  }
}
