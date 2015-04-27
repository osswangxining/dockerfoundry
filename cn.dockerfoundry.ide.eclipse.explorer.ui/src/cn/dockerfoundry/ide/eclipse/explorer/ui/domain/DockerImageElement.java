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

package cn.dockerfoundry.ide.eclipse.explorer.ui.domain;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

import cn.dockerfoundry.ide.eclipse.explorer.ui.views.property.DockerImageElementPropertySource;

import com.spotify.docker.client.messages.ImageInfo;

/**
 * @author wangxn
 *
 */
public class DockerImageElement implements IAdaptable {
	private String repository;
	private String tag;
	private String id;
	private String shortId;
	private String created;
	private Long size;
	private Long virtualSize;
	private ImageInfo imageInfo;
	
	private DockerImageElementPropertySource propSource;
	
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			propSource = new DockerImageElementPropertySource(this);
			return propSource;
		}
		return null;
	}


	public String getRepository() {
		return repository;
	}


	public void setRepository(String repository) {
		this.repository = repository;
	}


	public String getTag() {
		return tag;
	}


	public void setTag(String tag) {
		this.tag = tag;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getCreated() {
		return created;
	}


	public void setCreated(String created) {
		this.created = created;
	}


	public Long getSize() {
		return size;
	}


	public void setSize(Long size) {
		this.size = size;
	}


	public Long getVirtualSize() {
		return virtualSize;
	}


	public void setVirtualSize(Long virtualSize) {
		this.virtualSize = virtualSize;
	}


	public String getShortId() {
		if(id != null && id.length() > 12){
			return id.substring(0, 12);
		}
		return shortId;
	}


	public ImageInfo getImageInfo() {
		return imageInfo;
	}


	public void setImageInfo(ImageInfo imageInfo) {
		this.imageInfo = imageInfo;
	}
}

