/*
 * Sonar Python Plugin
 * Copyright (C) 2011 Waleri Enns
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package org.sonar.plugins.python;

import java.util.List;

import org.sonar.api.resources.DefaultProjectFileSystem;
import org.sonar.api.resources.Directory;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.WildcardPattern;
import org.apache.commons.lang.StringUtils;

public class PythonFile extends File {
  private static final String INITFILE = "__init__.py";
  
  private String parentPath = null;
  private PythonPackage parent = null;
  
  public PythonFile(String relPath, java.io.File absPath) {
    super(relPath);
    if (isInPythonPackage(absPath)) {
      String parentPathCandidate = parentPathOf(relPath);
      if (!"".equals(parentPathCandidate)) {
        this.parentPath = parentPathOf(relPath);
      }
    }
  }

  /** Creates a File from an io.file and a project */
  public static PythonFile fromIOFile(java.io.File file, Project project) {
    return fromIOFile(file, project.getFileSystem().getSourceDirs());
  }  
  
  /** Creates a File from an io.file and a list of sources directories */
  public static PythonFile fromIOFile(java.io.File file, List<java.io.File> sourceDirs) {
    String relPath = DefaultProjectFileSystem.getRelativePath(file, sourceDirs);
    if (relPath != null) {
      return new PythonFile(relPath, file);
    }
    return null;
  }

  @Override
  public Directory getParent() {
    if (parentPath != null) {
      if (parent == null) {
        parent = PythonPackage.create(parentPath);
      }
      return parent;
    }
    return null;
  }

  @Override
  public Language getLanguage() {
    return Python.INSTANCE;
  }

  private String parentPathOf(String relPath){
    String parentPath = "";
    if (relPath.indexOf("/") != -1) {
      parentPath = StringUtils.substringBeforeLast(relPath, "/");
    }
    
    return parentPath;
  }
  
  private boolean isInPythonPackage(java.io.File absPath){
    return new java.io.File(absPath.getParentFile(), INITFILE).isFile();
  }

  public boolean matchFilePattern(String antPattern) {
    String patternWithoutFileSuffix = StringUtils.substringBeforeLast(antPattern, ".");
    WildcardPattern matcher = WildcardPattern.create(patternWithoutFileSuffix, ".");
    return matcher.match(getKey());
  }
}
