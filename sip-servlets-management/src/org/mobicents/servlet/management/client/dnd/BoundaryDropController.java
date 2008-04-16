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

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.AbsolutePositionDropController;

/**
 * A {@link DropController} for the {@link com.google.gwt.user.client.ui.Panel}
 * which contains a given draggable widget.
 */
public class BoundaryDropController extends AbsolutePositionDropController {

  private boolean allowDroppingOnBoundaryPanel = true;

  public BoundaryDropController(AbsolutePanel dropTarget, boolean allowDroppingOnBoundaryPanel) {
    super(dropTarget);
    dropTarget.addStyleName("dragdrop-boundary");
    this.allowDroppingOnBoundaryPanel = allowDroppingOnBoundaryPanel;
  }

  /**
   * Whether or not dropping on the boundary panel is permitted.
   * 
   * @return <code>true</code> if dropping on the boundary panel is allowed
   */
  public boolean getBehaviorBoundaryPanelDrop() {
    return allowDroppingOnBoundaryPanel;
  }

  public void onPreviewDrop(DragContext context) throws VetoDragException {
    if (!allowDroppingOnBoundaryPanel) {
      throw new VetoDragException();
    }
    super.onPreviewDrop(context);
  }

  /**
   * Set whether or not widgets may be dropped anywhere on the boundary panel.
   * Set to <code>false</code> when you only want explicitly registered drop
   * controllers to accept drops. Defaults to <code>true</code>.
   * 
   * @param allowDroppingOnBoundaryPanel <code>true</code> to allow dropping
   */
  public void setBehaviorBoundaryPanelDrop(boolean allowDroppingOnBoundaryPanel) {
    this.allowDroppingOnBoundaryPanel = allowDroppingOnBoundaryPanel;
  }

  Widget makePositioner(Widget reference) {
    if (allowDroppingOnBoundaryPanel) {
      return super.newPositioner(reference);
    } else {
      return new SimplePanel();
    }
  }
}
