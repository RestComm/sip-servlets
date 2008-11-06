package org.mobicents.servlet.management.client.dnd;
/*
 * Copyright 2008 Fred Sauer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import com.google.gwt.user.client.ui.IndexedPanel;
import com.google.gwt.user.client.ui.Widget;

import com.allen_sauer.gwt.dnd.client.drop.IndexedDropController;

/**
 * IndexedDropController that disallows dropping after the last child, which is
 * assumed to be dummy spacer widget preventing parent collapse.
 */
public class NoInsertAtEndIndexedDropController extends IndexedDropController {

  private IndexedPanel dropTarget;

  public NoInsertAtEndIndexedDropController(IndexedPanel dropTarget) {
    super(dropTarget);
    this.dropTarget = dropTarget;
  }

  protected void insert(Widget widget, int beforeIndex) {
    if (beforeIndex == dropTarget.getWidgetCount()) {
      beforeIndex--;
    }
    super.insert(widget, beforeIndex);
  }
}
