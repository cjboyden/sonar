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
package org.sonar.core.resource;

final class ResourceIndexerQuery {
  private Integer rootProjectId = null;
  private String[] scopes = null;
  private String[] qualifiers = null;
  private boolean nonIndexedOnly=false;

  private ResourceIndexerQuery() {
  }

  public static ResourceIndexerQuery create() {
    return new ResourceIndexerQuery();
  }

  public String[] getScopes() {
    return scopes;
  }

  public String[] getQualifiers() {
    return qualifiers;
  }

  public ResourceIndexerQuery setScopes(String[] scopes) {
    this.scopes = scopes;
    return this;
  }

  public ResourceIndexerQuery setQualifiers(String[] qualifiers) {
    this.qualifiers = qualifiers;
    return this;
  }

  public Integer getRootProjectId() {
    return rootProjectId;
  }

  public ResourceIndexerQuery setRootProjectId(Integer i) {
    this.rootProjectId = i;
    return this;
  }

  public boolean isNonIndexedOnly() {
    return nonIndexedOnly;
  }

  public ResourceIndexerQuery setNonIndexedOnly(boolean b) {
    this.nonIndexedOnly = b;
    return this;
  }
}
