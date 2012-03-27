/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.allen_sauer.gwt.dnd.client.AbstractDragController;
import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.BoundaryDropController;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.allen_sauer.gwt.dnd.client.util.CoordinateLocation;
import com.allen_sauer.gwt.dnd.client.util.DOMUtil;
import com.allen_sauer.gwt.dnd.client.util.Location;
import com.allen_sauer.gwt.dnd.client.util.WidgetArea;
import com.allen_sauer.gwt.dnd.client.util.WidgetLocation;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * DragController used for drag-and-drop operations where a draggable widget or
 * drag proxy is temporarily picked up and dragged around the boundary panel.
 * Be sure to register a {@link DropController} for each drop target.
 * 
 * @see #registerDropController(DropController)
 */
public class PickupDragController extends AbstractDragController {

  /**
   * Private implementation class to store widget information while dragging.
   */
  private static class SavedWidgetInfo {

    /**
     * The initial draggable index for indexed panel parents.
     */
    int initialDraggableIndex;

    /**
     * Initial draggable CSS margin.
     */
    String initialDraggableMargin;

    /**
     * Initial draggable parent widget.
     */
    Widget initialDraggableParent;

    /**
     * Initial location for absolute panel parents.
     */
    Location initialDraggableParentLocation;
  }

  /**
   * CSS style name applied to movable panels.
   */
  private static final String PRIVATE_CSS_MOVABLE_PANEL = "dragdrop-movable-panel";

  /**
   * CSS style name applied to drag proxies.
   */
  private static final String PRIVATE_CSS_PROXY = "dragdrop-proxy";

  /**
   * The implicit boundary drop controller.
   */
  private BoundaryDropController boundaryDropController;

  private int boundaryOffsetX;

  private int boundaryOffsetY;

  private boolean dragProxyEnabled = false;

  private DropControllerCollection dropControllerCollection;

  private ArrayList dropControllerList = new ArrayList();

  private int dropTargetClientHeight;

  private int dropTargetClientWidth;

  private Widget movablePanel;

  private HashMap savedWidgetInfoMap;

  /**
   * Create a new pickup-and-move style drag controller. Allows widgets or a
   * suitable proxy to be temporarily picked up and moved around the specified
   * boundary panel.
   * 
   * <p>
   * Note: An implicit {@link BoundaryDropController} is created and registered
   * automatically.
   * </p>
   * 
   * @param boundaryPanel the desired boundary panel or <code>RootPanel.get()</code>
   *                      if entire document body is to be the boundary
   * @param allowDroppingOnBoundaryPanel whether or not boundary panel should
   *            allow dropping
   */
  public PickupDragController(AbsolutePanel boundaryPanel, boolean allowDroppingOnBoundaryPanel) {
    super(boundaryPanel);
    boundaryDropController = newBoundaryDropController(boundaryPanel, allowDroppingOnBoundaryPanel);
    registerDropController(boundaryDropController);
    dropControllerCollection = new DropControllerCollection(dropControllerList);
  }

  public void dragEnd() {
    if (context.vetoException != null) {
      if (!getBehaviorDragProxy()) {
        restoreSelectedWidgetsLocation();
      }
    } else {
      context.dropController.onDrop(context);
    }
    context.dropController.onLeave(context);
    context.dropController = null;

    if (!getBehaviorDragProxy()) {
      restoreSelectedWidgetsStyle();
    }
    movablePanel.removeFromParent();
    movablePanel = null;
    super.dragEnd();
  }

  public void dragMove() {
    int desiredLeft = context.desiredDraggableX - boundaryOffsetX;
    int desiredTop = context.desiredDraggableY - boundaryOffsetY;
    if (getBehaviorConstrainedToBoundaryPanel()) {
      desiredLeft = Math.max(0, Math.min(desiredLeft, dropTargetClientWidth
          - context.draggable.getOffsetWidth()));
      desiredTop = Math.max(0, Math.min(desiredTop, dropTargetClientHeight
          - context.draggable.getOffsetHeight()));
    }

    DOMUtil.fastSetElementPosition(movablePanel.getElement(), desiredLeft, desiredTop);

    DropController newDropController = getIntersectDropController(context.mouseX, context.mouseY);
    if (context.dropController != newDropController) {
      if (context.dropController != null) {
        context.dropController.onLeave(context);
      }
      context.dropController = newDropController;
      if (context.dropController != null) {
        context.dropController.onEnter(context);
      }
    }

    if (context.dropController != null) {
      context.dropController.onMove(context);
    }
  }

  public void dragStart() {
    super.dragStart();

    WidgetLocation currentDraggableLocation = new WidgetLocation(context.draggable,
        context.boundaryPanel);
    /*if (getBehaviorDragProxy()) {
      movablePanel = newDragProxy(context);
      context.boundaryPanel.add(movablePanel, currentDraggableLocation.getLeft(),
          currentDraggableLocation.getTop());
    } else */{
      saveSelectedWidgetsLocationAndStyle();
      AbsolutePanel container = new AbsolutePanel();
      DOM.setStyleAttribute(container.getElement(), "overflow", "visible");

      container.setPixelSize(context.draggable.getOffsetWidth(),
          context.draggable.getOffsetHeight());
      context.boundaryPanel.add(container, currentDraggableLocation.getLeft(),
          currentDraggableLocation.getTop());

      int draggableAbsoluteLeft = context.draggable.getAbsoluteLeft();
      int draggableAbsoluteTop = context.draggable.getAbsoluteTop();
      HashMap widgetLocation = new HashMap();
      for (int q=0; q< context.selectedWidgets.size(); q++) {
    	  Widget widget = (Widget) context.selectedWidgets.get(q);
        widgetLocation.put(widget, new CoordinateLocation(widget.getAbsoluteLeft(),
            widget.getAbsoluteTop()));
      }

      context.dropController = getIntersectDropController(context.mouseX, context.mouseY);
      if (context.dropController != null) {
        context.dropController.onEnter(context);
      }

      for (int q=0; q< context.selectedWidgets.size(); q++) {
    	  Widget widget = (Widget) context.selectedWidgets.get(q);
        Location location = (Location)widgetLocation.get(widget);
        int relativeX = location.getLeft() - draggableAbsoluteLeft;
        int relativeY = location.getTop() - draggableAbsoluteTop;
        container.add(widget, relativeX, relativeY);
      }
      movablePanel = container;
    }
    movablePanel.addStyleName(PRIVATE_CSS_MOVABLE_PANEL);

    // one time calculation of boundary panel location for efficiency during dragging
    Location widgetLocation = new WidgetLocation(context.boundaryPanel, null);
    boundaryOffsetX = widgetLocation.getLeft()
        + DOMUtil.getBorderLeft(context.boundaryPanel.getElement());
    boundaryOffsetY = widgetLocation.getTop()
        + DOMUtil.getBorderTop(context.boundaryPanel.getElement());

    dropTargetClientWidth = DOMUtil.getClientWidth(getBoundaryPanel().getElement());
    dropTargetClientHeight = DOMUtil.getClientHeight(getBoundaryPanel().getElement());
  }

  /**
   * Whether or not dropping on the boundary panel is permitted.
   * 
   * @return <code>true</code> if dropping on the boundary panel is allowed
   */
  public boolean getBehaviorBoundaryPanelDrop() {
    return boundaryDropController.getBehaviorBoundaryPanelDrop();
  }

  /**
   * Determine whether or not this controller automatically creates a drag proxy
   * for each drag operation. Whether or not a drag proxy is used is ultimately
   * determined by the return value of {@link #maybeNewDraggableProxy(Widget)}
   * 
   * @return <code>true</code> if drag proxy behavior is enabled
   */
  public boolean getBehaviorDragProxy() {
    return dragProxyEnabled;
  }

  public void previewDragEnd() throws VetoDragException {
    // Does the DropController allow the drop?
    try {
      context.dropController.onPreviewDrop(context);
      context.finalDropController = context.dropController;
    } catch (VetoDragException ex) {
      context.finalDropController = null;
      throw ex;
    } finally {
      super.previewDragEnd();
    }
  }

  /**
   * Register a new DropController, representing a new drop target, with this
   * drag controller.
   * 
   * @see #unregisterDropController(DropController)
   * 
   * @param dropController the controller to register
   */
  public void registerDropController(DropController dropController) {
    dropControllerList.add(dropController);
  }

  public void resetCache() {
    super.resetCache();
    dropControllerCollection.resetCache((Panel)getBoundaryPanel(), context);
  }

  /**
   * Set whether or not widgets may be dropped anywhere on the boundary panel.
   * Set to <code>false</code> when you only want explicitly registered drop
   * controllers to accept drops. Defaults to <code>true</code>.
   * 
   * @param allowDroppingOnBoundaryPanel <code>true</code> to allow dropping
   */
  public void setBehaviorBoundaryPanelDrop(boolean allowDroppingOnBoundaryPanel) {
    boundaryDropController.setBehaviorBoundaryPanelDrop(allowDroppingOnBoundaryPanel);
  }

  /**
   * Set whether or not this controller should automatically create a drag proxy
   * for each drag operation. Whether or not a drag proxy is used is ultimately
   * determined by the return value of {@link #maybeNewDraggableProxy(Widget)}.
   * 
   * @param dragProxyEnabled <code>true</code> to enable drag proxy behavior
   */
  public void setBehaviorDragProxy(boolean dragProxyEnabled) {
    this.dragProxyEnabled = dragProxyEnabled;
  }

  /**
   * Unregister a DropController from this drag controller.
   * 
   * @see #registerDropController(DropController)
   * 
   * @param dropController the controller to register
   */
  public void unregisterDropController(DropController dropController) {
    dropControllerList.remove(dropController);
  }

  /**
   * Create a new BoundaryDropController to manage our boundary panel as a drop
   * target. To ensure that draggable widgets can only be dropped on registered
   * drop targets, set <code>allowDroppingOnBoundaryPanel</code> to <code>false</code>.
   *
   * @param boundaryPanel the panel to which our drag-and-drop operations are constrained
   * @param allowDroppingOnBoundaryPanel whether or not dropping is allowed on the boundary panel
   * @return the new BoundaryDropController
   */
  protected BoundaryDropController newBoundaryDropController(AbsolutePanel boundaryPanel,
      boolean allowDroppingOnBoundaryPanel) {
    return new BoundaryDropController(boundaryPanel, allowDroppingOnBoundaryPanel);
  }

  /**
   * Called by {@link PickupDragController#dragStart(Widget)} to allow subclasses to
   * provide their own drag proxies.
   * 
   * @param context the current drag context
   * @return a new drag proxy
   */
  protected Widget newDragProxy(DragContext context) {
    AbsolutePanel container = new AbsolutePanel();
    DOM.setStyleAttribute(container.getElement(), "overflow", "visible");

    WidgetArea draggableArea = new WidgetArea(context.draggable, null);
    for (int q=0; q< context.selectedWidgets.size(); q++) {
  	  Widget widget = (Widget) context.selectedWidgets.get(q);
      WidgetArea widgetArea = new WidgetArea(widget, null);
      Widget proxy = new SimplePanel();
      proxy.setPixelSize(widget.getOffsetWidth(), widget.getOffsetHeight());
      proxy.addStyleName(PRIVATE_CSS_PROXY);
      container.add(proxy, widgetArea.getLeft() - draggableArea.getLeft(), widgetArea.getTop()
          - draggableArea.getTop());
    }

    return container;
  }

  /**
   * Restore the selected widgets to their original location.
   * @see #saveSelectedWidgetsLocationAndStyle()
   * @see #restoreSelectedWidgetsStyle()
   */
  protected void restoreSelectedWidgetsLocation() {
      for (int q=0; q< context.selectedWidgets.size(); q++) {
    	  Widget widget = (Widget) context.selectedWidgets.get(q);
      SavedWidgetInfo info = (SavedWidgetInfo) savedWidgetInfoMap.get(widget);

      // TODO simplify after enhancement for issue 1112 provides InsertPanel interface
      // http://code.google.com/p/google-web-toolkit/issues/detail?id=1112
      if (info.initialDraggableParent instanceof AbsolutePanel) {
        ((AbsolutePanel) info.initialDraggableParent).add(widget,
            info.initialDraggableParentLocation.getLeft(),
            info.initialDraggableParentLocation.getTop());
      } else if (info.initialDraggableParent instanceof HorizontalPanel) {
        ((HorizontalPanel) info.initialDraggableParent).insert(widget, info.initialDraggableIndex);
      } else if (info.initialDraggableParent instanceof VerticalPanel) {
        ((VerticalPanel) info.initialDraggableParent).insert(widget, info.initialDraggableIndex);
      } else if (info.initialDraggableParent instanceof FlowPanel) {
        ((FlowPanel) info.initialDraggableParent).insert(widget, info.initialDraggableIndex);
      } else if (info.initialDraggableParent instanceof SimplePanel) {
        ((SimplePanel) info.initialDraggableParent).setWidget(widget);
      } else {
        throw new RuntimeException("Unable to handle initialDraggableParent "
            + info.initialDraggableParent.toString());
      }
    }
  }

  /**
   * Restore the selected widgets with their original style.
   * @see #saveSelectedWidgetsLocationAndStyle()
   * @see #restoreSelectedWidgetsLocation()
   */
  protected void restoreSelectedWidgetsStyle() {
      for (int q=0; q< context.selectedWidgets.size(); q++) {
    	  Widget widget = (Widget) context.selectedWidgets.get(q);
      SavedWidgetInfo info = (SavedWidgetInfo) savedWidgetInfoMap.get(widget);
      DOM.setStyleAttribute(widget.getElement(), "margin", info.initialDraggableMargin);
    }
  }

  /**
   * Save the selected widgets' current location in case they much
   * be restored due to a cancelled drop.
   * @see #restoreSelectedWidgetsLocation()
   */
  protected void saveSelectedWidgetsLocationAndStyle() {
    savedWidgetInfoMap = new HashMap();
    for (int q=0; q< context.selectedWidgets.size(); q++) {
  	  Widget widget = (Widget) context.selectedWidgets.get(q);
      SavedWidgetInfo info = new SavedWidgetInfo();
      info.initialDraggableParent = widget.getParent();

      // TODO simplify after enhancement for issue 1112 provides InsertPanel interface
      // http://code.google.com/p/google-web-toolkit/issues/detail?id=1112
      if (info.initialDraggableParent instanceof AbsolutePanel) {
        info.initialDraggableParentLocation = new WidgetLocation(widget,
            info.initialDraggableParent);
      } else if (info.initialDraggableParent instanceof HorizontalPanel) {
        info.initialDraggableIndex = ((HorizontalPanel) info.initialDraggableParent).getWidgetIndex(widget);
      } else if (info.initialDraggableParent instanceof VerticalPanel) {
        info.initialDraggableIndex = ((VerticalPanel) info.initialDraggableParent).getWidgetIndex(widget);
      } else if (info.initialDraggableParent instanceof FlowPanel) {
        info.initialDraggableIndex = ((FlowPanel) info.initialDraggableParent).getWidgetIndex(widget);
      } else if (info.initialDraggableParent instanceof SimplePanel) {
        // save nothing
      } else {
        throw new RuntimeException(
            "Unable to handle 'initialDraggableParent instanceof "
                + info.initialDraggableParent.toString()
                + "'; Please create your own DragController and override saveDraggableLocationAndStyle() and restoreDraggableLocation()");
      }

      info.initialDraggableMargin = DOM.getStyleAttribute(widget.getElement(), "margin");
      DOM.setStyleAttribute(widget.getElement(), "margin", "0px");
      savedWidgetInfoMap.put(widget, info);
    }
  }

  private DropController getIntersectDropController(int x, int y) {
    DropController dropController = dropControllerCollection.getIntersectDropController(x, y);
    return dropController != null ? dropController : boundaryDropController;
  }
}
