// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.roots.ui;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.roots.ui.util.*;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.ex.http.HttpFileSystem;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FileAppearanceServiceImpl extends FileAppearanceService {
  private static final CellAppearanceEx EMPTY = new CellAppearanceEx() {
    @Override
    public void customize(@NotNull SimpleColoredComponent component) { }

    @Override
    public @NotNull String getText() { return ""; }
  };

  @Override
  public @NotNull CellAppearanceEx empty() {
    return EMPTY;
  }

  @Override
  public @NotNull CellAppearanceEx forVirtualFile(final @NotNull VirtualFile file) {
    if (!file.isValid()) {
      return forInvalidUrl(file.getPresentableUrl());
    }

    final VirtualFileSystem fileSystem = file.getFileSystem();
    if (fileSystem.getProtocol().equals(JarFileSystem.PROTOCOL)) {
      return new JarSubfileCellAppearance(file);
    }
    if (fileSystem instanceof HttpFileSystem) {
      return new HttpUrlCellAppearance(file);
    }
    if (file.isDirectory()) {
      return SimpleTextCellAppearance.regular(file.getPresentableUrl(), PlatformIcons.FOLDER_ICON);
    }
    return new ValidFileCellAppearance(file);
  }

  @Override
  public @NotNull CellAppearanceEx forIoFile(final @NotNull File file) {
    final String absolutePath = file.getAbsolutePath();
    if (!file.exists()) {
      return forInvalidUrl(absolutePath);
    }

    if (file.isDirectory()) {
      return SimpleTextCellAppearance.regular(absolutePath, PlatformIcons.FOLDER_ICON);
    }

    final String name = file.getName();
    final FileType fileType = FileTypeManager.getInstance().getFileTypeByFileName(name);
    final File parent = file.getParentFile();
    final CompositeAppearance appearance = CompositeAppearance.textComment(name, parent.getAbsolutePath());
    appearance.setIcon(fileType.getIcon());
    return appearance;
  }

  @Override
  public @NotNull CellAppearanceEx forInvalidUrl(final @NotNull String text) {
    return SimpleTextCellAppearance.invalid(text, PlatformIcons.INVALID_ENTRY_ICON);
  }
}