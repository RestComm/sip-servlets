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
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.allen_sauer.gwt.dnd.client.util.Area;
import com.allen_sauer.gwt.dnd.client.util.CoordinateLocation;
import com.allen_sauer.gwt.dnd.client.util.DOMUtil;
import com.allen_sauer.gwt.dnd.client.util.Location;
import com.allen_sauer.gwt.dnd.client.util.WidgetArea;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Package private helper implementation class for {@link AbstractDragController}
 * to track all relevant {@link DropController DropControllers}.
 */
class DropControllerCollection {

	protected static class Candidate implements Comparable {

		private final DropController dropController;

		private Area targetArea;

		Candidate(DropController dropController) {
			this.dropController = dropController;
			Widget target = dropController.getDropTarget();
			if (!target.isAttached()) {
				throw new IllegalStateException(
						"Unattached drop target. You must call DragController#unregisterDropController for all drop targets not attached to the DOM.");
			}
			targetArea = new WidgetArea(target, null);
		}

		public int compareTo(Object obj) {
			Candidate other = (Candidate) obj;
			Element myElement = getDropTarget().getElement();
			Element otherElement = other.getDropTarget().getElement();
			if (myElement == otherElement) {
				return 0;
			} else if (DOM.isOrHasChild(myElement, otherElement)) {
				return -1;
			} else if (DOM.isOrHasChild(otherElement, myElement)) {
				return 1;
			} else {
				return 0;
			}
		}

		DropController getDropController() {
			return dropController;
		}

		Widget getDropTarget() {
			return dropController.getDropTarget();
		}

		Area getTargetArea() {
			return targetArea;
		}
	}

	private final ArrayList dropControllerList;

	private Candidate[] sortedCandidates = null;

	/**
	 * Default constructor.
	 */
	DropControllerCollection(ArrayList dropControllerList) {
		this.dropControllerList = dropControllerList;
	}

	/**
	 * Determines which DropController represents the deepest DOM descendant
	 * drop target located at the provided location <code>(x, y)</code>.
	 * 
	 * @param x offset left relative to document body
	 * @param y offset top relative to document body
	 * @return a drop controller for the intersecting drop target or <code>null</code> if none
	 *         are applicable
	 */
	DropController getIntersectDropController(int x, int y) {
		Location location = new CoordinateLocation(x, y);
		for (int i = sortedCandidates.length - 1; i >= 0; i--) {
			Candidate candidate = sortedCandidates[i];
			Area targetArea = candidate.getTargetArea();
			if (targetArea.intersects(location)) {
				return candidate.getDropController();
			}
		}
		return null;
	}

	/**
	 * Cache a list of eligible drop controllers, sorted by relative DOM positions
	 * of their respective drop targets. Called at the beginning of each drag operation,
	 * or whenever drop target eligibility has changed while dragging.
	 * 
	 * @param boundaryPanel boundary area for drop target eligibility considerations
	 * @param context the current drag context
	 */
	void resetCache(Panel boundaryPanel, DragContext context) {
		ArrayList list = new ArrayList();

		if (context.draggable != null) {
			WidgetArea boundaryArea = new WidgetArea(boundaryPanel, null);
			for (Iterator iterator = dropControllerList.iterator(); iterator.hasNext();) {
				DropController dropController = (DropController) iterator.next();
				Candidate candidate = new Candidate(dropController);
				if (DOMUtil.isOrContains(context.draggable.getElement(),
						candidate.getDropTarget().getElement())) {
					continue;
				}
				if (candidate.getTargetArea().intersects(boundaryArea)) {
					list.add(candidate);
				}
			}
		}
		Candidate[] sortedCandidates = new Candidate[list.size()];
		for(int q=0; q<sortedCandidates.length; q++) sortedCandidates[q] = (Candidate) list.get(q);
		Arrays.sort(sortedCandidates);
	}
}