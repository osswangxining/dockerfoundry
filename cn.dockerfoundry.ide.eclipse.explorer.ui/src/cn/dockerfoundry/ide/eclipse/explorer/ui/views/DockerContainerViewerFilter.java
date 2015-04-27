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
import org.eclipse.jface.viewers.ViewerFilter;

import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerContainerElement;

/**
 * @author wangxn
 *
 */
public class DockerContainerViewerFilter extends ViewerFilter {

	private String searchString;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers
	 * .Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (searchString == null || searchString.length() == 0) {
			return true;
		}
		DockerContainerElement p = (DockerContainerElement) element;
		return p.getShortId().matches(searchString)
				|| p.getImage().matches(searchString)
				|| p.getCommand().matches(searchString)
				|| p.getCreated().matches(searchString)
				|| p.getStatus().toString().matches(searchString)
				|| p.getPortsAsString().toString().matches(searchString)
				|| p.getNamesAsString().toString().matches(searchString);
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = ".*" + searchString + ".*";
	}

}
