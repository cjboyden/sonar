/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2012 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.dbcleaner.purges;

import org.junit.Test;
import org.sonar.jpa.test.AbstractDbUnitTestCase;

import java.sql.SQLException;

public class PurgeDisabledResourcesTest extends AbstractDbUnitTestCase {

  @Test
  public void purgeDisabledModule() throws SQLException {
    assertPurge("purgeDisabledModule");
  }

  @Test
  public void purgeDisabledProject() throws SQLException {
    assertPurge("purgeDisabledProject");
  }

  @Test
  public void nothingToPurge() throws SQLException {
    assertPurge("nothingToPurge");
  }

  private void assertPurge(String testName) {
    setupData("sharedFixture", testName);
    new PurgeDisabledResources(getSession()).purge(null);
    checkTables(testName, "snapshots", "project_measures");
  }
}
