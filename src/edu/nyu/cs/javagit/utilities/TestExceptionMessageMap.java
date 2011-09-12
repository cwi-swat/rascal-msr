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
package edu.nyu.cs.javagit.utilities;

import org.junit.Before;
import org.junit.Test;

import edu.nyu.cs.javagit.utilities.ExceptionMessageMap;

import junit.framework.TestCase;

/**
 * Test class for edu.nyu.cs.javagit.utiltites.ExceptionMessageMap.
 */
public class TestExceptionMessageMap extends TestCase {

  @Before
  protected void setUp() {
  }

  @Test
  public void testGetMessage() {
    assertGetMessageValid(null, "NO MESSAGE FOR ERROR CODE. { code=[null] }");
    assertGetMessageValid("", "NO MESSAGE FOR ERROR CODE. { code=[] }");
    assertGetMessageValid("0", "NO MESSAGE FOR ERROR CODE. { code=[0] }");
    assertGetMessageValid("000001", "000001: A String argument was not specified but is required.");
  }

  private void assertGetMessageValid(String code, String expectedMessage) {
    String actualMessage = ExceptionMessageMap.getMessage(code);
    assertEquals("Expected message was not received from ExcpetionMessageMap.getMessage()",
        expectedMessage, actualMessage);
  }

}
