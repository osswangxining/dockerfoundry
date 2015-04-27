/*******************************************************************************
 * Copyright (c) 2015 www.DockerFoundry.cn
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 *  Contributors:
 *     Xi Ning Wang
 ********************************************************************************/

package cn.dockerfoundry.ide.eclipse.explorer.ui.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerContainerElement;

/**
 * @author wangxn
 *
 */
public class DockerContainerViewerComparator extends ViewerComparator {
	private int propertyIndex;
	private static final int DESCENDING = 1;
	private int direction = DESCENDING;

	public DockerContainerViewerComparator() {
		this.propertyIndex = 0;
		direction = 1 - DESCENDING;
	}

	public int getDirection() {
		return direction == 1 ? SWT.DOWN : SWT.UP;
	}

	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do an ascending sort
			this.propertyIndex = column;
			direction = DESCENDING;
		}
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		DockerContainerElement p1 = (DockerContainerElement) e1;
		DockerContainerElement p2 = (DockerContainerElement) e2;
		int rc = 0;
		switch (propertyIndex) {
		case 0:
			rc = p1.getShortId().compareTo(p2.getShortId());
			break;
		case 1:
			rc = p1.getImage().compareTo(p2.getImage());
			break;
		case 2:
			rc = p1.getCommand().compareTo(p2.getCommand());
			break;
		case 3:
			rc = p1.getCreated().compareTo(p2.getCreated());
			break;
		case 4:
			rc = p1.getStatus().compareTo(p2.getStatus());
			break;
		case 5:
			rc = p1.getPortsAsString().compareTo(p2.getPortsAsString());
			break;
		case 6:
			rc = p1.getNamesAsString().compareTo(p2.getNamesAsString());
			break;	
		default:
			rc = 0;
		}
		// If descending order, flip the direction
		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}
}
