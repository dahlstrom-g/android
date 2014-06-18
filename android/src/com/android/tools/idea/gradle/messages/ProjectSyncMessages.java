/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.gradle.messages;

import com.android.tools.idea.gradle.service.notification.GradleNotificationExtension;
import com.android.tools.idea.gradle.service.notification.NotificationHyperlink;
import com.android.tools.idea.gradle.service.notification.SearchInBuildFilesHyperlink;
import com.android.tools.idea.gradle.util.Projects;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.externalSystem.service.notification.ExternalSystemNotificationManager;
import com.intellij.openapi.externalSystem.service.notification.NotificationCategory;
import com.intellij.openapi.externalSystem.service.notification.NotificationData;
import com.intellij.openapi.externalSystem.service.notification.NotificationSource;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.util.GradleConstants;

import java.util.Collection;

import static com.android.tools.idea.gradle.messages.CommonMessageGroupNames.UNRESOLVED_ANDROID_DEPENDENCIES;
import static com.android.tools.idea.gradle.messages.CommonMessageGroupNames.UNRESOLVED_DEPENDENCIES;

/**
 * Service that collects and displays, in the "Messages" tool window, post-sync project setup messages (errors, warnings, etc.)
 */
public class ProjectSyncMessages {
  @NotNull private final Project myProject;
  @NotNull private final ExternalSystemNotificationManager myNotificationManager;

  @NotNull
  public static ProjectSyncMessages getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, ProjectSyncMessages.class);
  }

  public int getErrorCount() {
    return myNotificationManager.getMessageCount(
      null, NotificationSource.PROJECT_SYNC, NotificationCategory.ERROR, GradleConstants.SYSTEM_ID);
  }

  public ProjectSyncMessages(@NotNull Project project, @NotNull ExternalSystemNotificationManager manager) {
    myProject = project;
    myNotificationManager = manager;
  }

  public void removeMessages(@NotNull String groupName) {
    myNotificationManager.clearNotifications(groupName, NotificationSource.PROJECT_SYNC, GradleConstants.SYSTEM_ID);
  }

  public int getMessageCount(@NotNull String groupName) {
    return myNotificationManager.getMessageCount(groupName, NotificationSource.PROJECT_SYNC, null, GradleConstants.SYSTEM_ID);
  }

  public void add(@NotNull final Message message, @NotNull NotificationHyperlink... hyperlinks) {
    Navigatable navigatable = message.getNavigatable();
    String title = message.getGroupName();
    String errorMsg = StringUtil.join(message.getText(), "\n");

    VirtualFile file = message.getFile();
    String filePath = file != null ? FileUtil.toSystemDependentName(file.getPath()) : null;

    NotificationData notification =
      new NotificationData(title, errorMsg, NotificationCategory.convert(message.getType().getValue()), NotificationSource.PROJECT_SYNC,
                           filePath, message.getLine(), message.getColumn(), false);
    notification.setNavigatable(navigatable);

    if (hyperlinks.length > 0) {
      GradleNotificationExtension.updateNotification(notification, myProject, title, errorMsg, hyperlinks);
    }

    myNotificationManager.showNotification(GradleConstants.SYSTEM_ID, notification);
  }

  public boolean isEmpty() {
    return myNotificationManager.getMessageCount(NotificationSource.PROJECT_SYNC, null, GradleConstants.SYSTEM_ID) == 0;
  }

  public void reportUnresolvedDependencies(@NotNull Collection<String> unresolvedDependencies) {
    for (String dep : unresolvedDependencies) {
      String group = dep.startsWith("com.android.support:") ? UNRESOLVED_ANDROID_DEPENDENCIES : UNRESOLVED_DEPENDENCIES;
      Message msg = new Message(group, Message.Type.ERROR, AbstractNavigatable.NOT_NAVIGATABLE, dep);

      add(msg, new SearchInBuildFilesHyperlink(dep));
    }

    if (!unresolvedDependencies.isEmpty()) {
      myProject.putUserData(Projects.HAS_UNRESOLVED_DEPENDENCIES, true);
    }
  }
}
