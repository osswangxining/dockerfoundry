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

package cn.dockerfoundry.ide.eclipse.explorer.ui.views.property;

import java.util.Date;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerImageElement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ImageInfo;

/**
 * @author wangxn
 *
 */
public class DockerImageElementPropertySource implements IPropertySource {

	private IPropertyDescriptor[] propertyDescriptors;
	private DockerImageElement dockerImageElement;

	public DockerImageElementPropertySource(DockerImageElement element) {
		this.dockerImageElement = element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
	 */
	@Override
	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors()
	 */
	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		if (propertyDescriptors == null) {
			final ImageInfo imageInfo = dockerImageElement.getImageInfo();
			// @JsonProperty("Id") private String id;
			// @JsonProperty("Parent") private String parent;
			// @JsonProperty("Comment") private String comment;
			// @JsonProperty("Created") private Date created;
			// @JsonProperty("Container") private String container;
			// @JsonProperty("ContainerConfig") private ContainerConfig
			// containerConfig;
			// @JsonProperty("DockerVersion") private String dockerVersion;
			// @JsonProperty("Author") private String author;
			// @JsonProperty("Config") private ContainerConfig config;
			// @JsonProperty("Architecture") private String architecture;
			// @JsonProperty("Os") private String os;
			// @JsonProperty("Size") private Long size;

			// Create a descriptor and set a category
			PropertyDescriptor idDescriptor = new PropertyDescriptor("Id", "Id");
			idDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					System.out.println("::"+element);
					return imageInfo.id();
				}
			});
			PropertyDescriptor parentDescriptor = new PropertyDescriptor(
					"Parent", "Parent");
			parentDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return ((ImageInfo) element).parent();
				}
			});
			PropertyDescriptor commentDescriptor = new PropertyDescriptor(
					"Comment", "Comment");
			commentDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return ((ImageInfo) element).comment();
				}
			});
			PropertyDescriptor createdDescriptor = new PropertyDescriptor(
					"Created", "Created");
			createdDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return ((ImageInfo) element).created().toString();
				}
			});
			PropertyDescriptor containerDescriptor = new PropertyDescriptor(
					"Container", "Container");
			containerDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return ((ImageInfo) element).container();
				}
			});

			// @JsonProperty("Hostname") private String hostname;
			// @JsonProperty("Domainname") private String domainname;
			// @JsonProperty("User") private String user;
			// @JsonProperty("Memory") private Long memory;
			// @JsonProperty("MemorySwap") private Long memorySwap;
			// @JsonProperty("CpuShares") private Long cpuShares;
			// @JsonProperty("Cpuset") private String cpuset;
			// @JsonProperty("AttachStdin") private Boolean attachStdin;
			// @JsonProperty("AttachStdout") private Boolean attachStdout;
			// @JsonProperty("AttachStderr") private Boolean attachStderr;
			// @JsonProperty("PortSpecs") private ImmutableList<String>
			// portSpecs;
			// @JsonProperty("ExposedPorts") private ImmutableSet<String>
			// exposedPorts;
			// @JsonProperty("Tty") private Boolean tty;
			// @JsonProperty("OpenStdin") private Boolean openStdin;
			// @JsonProperty("StdinOnce") private Boolean stdinOnce;
			// @JsonProperty("Env") private ImmutableList<String> env;
			// @JsonProperty("Cmd") private ImmutableList<String> cmd;
			// @JsonProperty("Image") private String image;
			// @JsonProperty("Volumes") private ImmutableSet<String> volumes;
			// @JsonProperty("WorkingDir") private String workingDir;
			// @JsonProperty("Entrypoint") private ImmutableList<String>
			// entrypoint;
			// @JsonProperty("NetworkDisabled") private Boolean networkDisabled;
			// @JsonProperty("OnBuild") private ImmutableList<String> onBuild;
			final ContainerConfig containerConfig = imageInfo.containerConfig();
			PropertyDescriptor containerConfigHostnameDescriptor = new PropertyDescriptor(
					"Hostname", "Hostname");
			containerConfigHostnameDescriptor
					.setLabelProvider(new LabelProvider() {
						public String getText(Object element) {
							return containerConfig.hostname();
						}
					});
			containerConfigHostnameDescriptor.setCategory("ContainerConfig");

			PropertyDescriptor dockerVersionDescriptor = new PropertyDescriptor(
					"DockerVersion", "DockerVersion");
			dockerVersionDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return ((ImageInfo) element).dockerVersion();
				}
			});

			PropertyDescriptor authorDescriptor = new PropertyDescriptor(
					"Author", "Author");
			authorDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return ((ImageInfo) element).author();
				}
			});
			
			final ContainerConfig config = imageInfo.config();
			PropertyDescriptor configHostnameDescriptor = new PropertyDescriptor(
					"Config.Hostname", "Hostname");
			configHostnameDescriptor
					.setLabelProvider(new LabelProvider() {
						public String getText(Object element) {
							return config.hostname();
						}
					});
			configHostnameDescriptor.setCategory("Config");
			
			PropertyDescriptor architectureDescriptor = new PropertyDescriptor(
					"Architecture", "Architecture");
			architectureDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return ((ImageInfo) element).architecture();
				}
			});
			
			PropertyDescriptor osDescriptor = new PropertyDescriptor(
					"Os", "Os");
			osDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return ((ImageInfo) element).os();
				}
			});
			
			PropertyDescriptor sizeDescriptor = new PropertyDescriptor(
					"Size", "Size");
			sizeDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return ((ImageInfo) element).size().toString();
				}
			});
			propertyDescriptors = new IPropertyDescriptor[] { idDescriptor,
					parentDescriptor, commentDescriptor, createdDescriptor,
					containerDescriptor, containerConfigHostnameDescriptor,
					dockerVersionDescriptor, authorDescriptor,configHostnameDescriptor,  
					architectureDescriptor, osDescriptor, sizeDescriptor};
		}
		return propertyDescriptors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(java
	 * .lang.Object)
	 */
	@Override
	public Object getPropertyValue(Object arg0) {
		ImageInfo info = dockerImageElement.getImageInfo();
		return info;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.properties.IPropertySource#isPropertySet(java.lang
	 * .Object)
	 */
	@Override
	public boolean isPropertySet(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue(java
	 * .lang.Object)
	 */
	@Override
	public void resetPropertyValue(Object arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(java
	 * .lang.Object, java.lang.Object)
	 */
	@Override
	public void setPropertyValue(Object arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

}
