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

import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerImageElement;

/**
 * @author wangxn
 *
 */
public class DockerImageViewerComparator extends ViewerComparator {
	private int propertyIndex;
	private static final int DESCENDING = 1;
	private int direction = DESCENDING;

	public DockerImageViewerComparator() {
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
		DockerImageElement p1 = (DockerImageElement) e1;
		DockerImageElement p2 = (DockerImageElement) e2;
		int rc = 0;
		switch (propertyIndex) {
		case 0:
			rc = p1.getRepository().compareTo(p2.getRepository());
			break;
		case 1:
			rc = p1.getTag().compareTo(p2.getTag());
			break;
		case 2:
			rc = p1.getShortId().compareTo(p2.getShortId());
			break;
		case 3:
			rc = p1.getCreated().compareTo(p2.getCreated());
			break;
		case 4:
			rc = p1.getVirtualSize().intValue()
					- p2.getVirtualSize().intValue();
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
