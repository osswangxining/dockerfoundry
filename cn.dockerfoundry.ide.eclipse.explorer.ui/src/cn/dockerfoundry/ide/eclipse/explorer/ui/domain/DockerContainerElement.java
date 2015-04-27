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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

import cn.dockerfoundry.ide.eclipse.explorer.ui.views.property.DockerContainerElementPropertySource;

import com.spotify.docker.client.messages.Container.PortMapping;

/**
 * @author wangxn
 *
 */
public class DockerContainerElement implements IAdaptable {
	private String name;
	private List<String> names;
//	private String namesAsString;
	private String image;
	private String id;
	private String shortId;
	private String created;
	private String command;
	private String status;
	private List<PortMapping> ports;
//	private String portsAsString;
	private Long sizeRootFs;
	private Long sizeRw;
	
	private DockerContainerElementPropertySource propSource;

//	public static class PortMapping {
//		private int privatePort;
//		private int publicPort;
//		private String type;
//		private String ip;
//		public int getPrivatePort() {
//			return privatePort;
//		}
//		public void setPrivatePort(int privatePort) {
//			this.privatePort = privatePort;
//		}
//		public int getPublicPort() {
//			return publicPort;
//		}
//		public void setPublicPort(int publicPort) {
//			this.publicPort = publicPort;
//		}
//		public String getType() {
//			return type;
//		}
//		public void setType(String type) {
//			this.type = type;
//		}
//		public String getIp() {
//			return ip;
//		}
//		public void setIp(String ip) {
//			this.ip = ip;
//		}
//	}

	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			propSource = new DockerContainerElementPropertySource(this);
			return propSource;
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
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

	public String getShortId() {
		if (id != null && id.length() > 12) {
			return id.substring(0, 12);
		}
		return shortId;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<PortMapping> getPorts() {
		return ports;
	}

	public void setPorts(List<PortMapping> ports) {
		this.ports = ports;
	}
	
	public void addPort(PortMapping portElem){
		if(this.ports == null){
			this.ports = new ArrayList<PortMapping>();
		}
		this.ports.add(portElem);
	}

	public List<String> getNames() {
		return names;
	}

	public void setNames(List<String> names) {
		this.names = names;
	}
	
	public void addName(String name){
		if(this.names == null){
			this.names = new ArrayList<String>();
		}
		this.names.add(name);
	}

	public Long getSizeRootFs() {
		return sizeRootFs;
	}

	public void setSizeRootFs(Long sizeRootFs) {
		this.sizeRootFs = sizeRootFs;
	}

	public Long getSizeRw() {
		return sizeRw;
	}

	public void setSizeRw(Long sizeRw) {
		this.sizeRw = sizeRw;
	}

	public String getNamesAsString() {
		List<String> names = getNames();
		StringBuilder sbNames = new StringBuilder();
		if (names != null && names.size() > 0) {
			for (Iterator<String> iterator = names
					.iterator(); iterator.hasNext();) {
				String name = (String) iterator
						.next();
				sbNames.append(name);
				if (iterator.hasNext()) {
					sbNames.append("\n");
				}
			}
		}
		return sbNames.toString();
	}

	public String getPortsAsString() {
		List<PortMapping> portMappings = getPorts();
		StringBuilder sbPortMappings = new StringBuilder();
		if (portMappings != null && portMappings.size() > 0) {
			for (Iterator<PortMapping> iterator = portMappings
					.iterator(); iterator.hasNext();) {
				PortMapping portMapping = (PortMapping) iterator
						.next();
				sbPortMappings.append(portMapping.getIp()).append(":")
						.append(portMapping.getPublicPort())
						.append("->")
						.append(portMapping.getPrivatePort())
						.append("/").append(portMapping.getType());
				if (iterator.hasNext()) {
					sbPortMappings.append("\n");
				}
			}
		}
		return sbPortMappings.toString();
	}
}
