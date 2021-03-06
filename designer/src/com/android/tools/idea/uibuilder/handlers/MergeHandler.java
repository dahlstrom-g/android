/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.tools.idea.uibuilder.handlers;

import com.android.tools.idea.common.model.NlComponent;
import com.android.tools.idea.uibuilder.api.XmlType;
import com.android.tools.idea.uibuilder.handlers.frame.FrameLayoutHandler;
import com.google.common.collect.ImmutableList;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.android.SdkConstants.ATTR_PARENT_TAG;
import static com.android.SdkConstants.ATTR_SHOW_IN;

/**
 * Handler for the {@code <merge>} tag
 */
public final class MergeHandler extends FrameLayoutHandler {
  @Override
  @NotNull
  public List<String> getInspectorProperties() {
    return ImmutableList.of(ATTR_SHOW_IN, ATTR_PARENT_TAG);
  }

  @Override
  @NotNull
  public String getTitle(@NotNull String tagName) {
    return "<merge>";
  }

  @Override
  @NotNull
  public String getTitle(@NotNull NlComponent component) {
    return "<merge>";
  }

  @Override
  @Language("XML")
  @NotNull
  public String getXml(@NotNull String tagName, @NotNull XmlType xmlType) {
    switch (xmlType) {
      case PREVIEW_ON_PALETTE:
      case DRAG_PREVIEW:
        return NO_PREVIEW;
      default:
        return super.getXml(tagName, xmlType);
    }
  }
}
