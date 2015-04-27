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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerContainerElement;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ImageInfo;

/**
 * @author wangxn
 *
 */
public class DockerContainerElementPropertySource implements IPropertySource {

	private IPropertyDescriptor[] propertyDescriptors;
	private DockerContainerElement dockerContainerElement;

	public DockerContainerElementPropertySource(DockerContainerElement element) {
		this.dockerContainerElement = element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
	 */
	@Override
	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return this.dockerContainerElement;
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
			final DockerContainerElement elem = dockerContainerElement;
//			  @JsonProperty("Id") private String id;
//			  @JsonProperty("Names") private ImmutableList<String> names;
//			  @JsonProperty("Image") private String image;
//			  @JsonProperty("Command") private String command;
//			  @JsonProperty("Created") private Long created;
//			  @JsonProperty("Status") private String status;
//			  @JsonProperty("Ports") private ImmutableList<PortMapping> ports;
//			  @JsonProperty("SizeRw") private Long sizeRw;
//			  @JsonProperty("SizeRootFs") private Long sizeRootFs;

			// Create a descriptor and set a category
			PropertyDescriptor idDescriptor = new PropertyDescriptor("container.Id", "Id");
			idDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					System.out.println("::"+element);
					return elem.getId();
				}
			});
			
//			List<String> names = elem.getNames();
//			if(names != null){
//				for (Iterator<String> iterator = names.iterator(); iterator.hasNext();) {
//					String name = (String) iterator.next();
//					
//				}
//			}
			PropertyDescriptor namesDescriptor = new PropertyDescriptor(
					"container.Names", "Names");
			namesDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return elem.getNamesAsString();
				}
			});
			PropertyDescriptor imageDescriptor = new PropertyDescriptor(
					"container.Image", "Image");
			imageDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return elem.getImage();
				}
			});
			PropertyDescriptor commandDescriptor = new PropertyDescriptor(
					"container.Command", "Command");
			commandDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return elem.getCommand();
				}
			});
			PropertyDescriptor createdDescriptor = new PropertyDescriptor(
					"container.Created", "Created");
			createdDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return elem.getCreated();
				}
			});
			PropertyDescriptor statusDescriptor = new PropertyDescriptor(
					"container.Status", "Status");
			statusDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return elem.getStatus();
				}
			});

		
//			final ContainerConfig containerConfig = imageInfo.containerConfig();
//			PropertyDescriptor containerConfigHostnameDescriptor = new PropertyDescriptor(
//					"Hostname", "Hostname");
//			containerConfigHostnameDescriptor
//					.setLabelProvider(new LabelProvider() {
//						public String getText(Object element) {
//							return containerConfig.hostname();
//						}
//					});
//			containerConfigHostnameDescriptor.setCategory("ContainerConfig");

			PropertyDescriptor portsDescriptor = new PropertyDescriptor(
					"container.Ports", "Ports");
			portsDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return elem.getPortsAsString();
				}
			});

			PropertyDescriptor sizeRwDescriptor = new PropertyDescriptor(
					"container.SizeRw", "SizeRw");
			sizeRwDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return elem.getSizeRw() == null ?"": elem.getSizeRw().toString();	
				}
			});
			
			PropertyDescriptor sizeRootFsDescriptor = new PropertyDescriptor(
					"container.SizeRootFs", "SizeRootFs");
			sizeRootFsDescriptor.setLabelProvider(new LabelProvider() {
				public String getText(Object element) {
					return elem.getSizeRootFs() == null ?"": elem.getSizeRootFs().toString();	
				}
			});
			
//			final ContainerConfig config = imageInfo.config();
//			PropertyDescriptor configHostnameDescriptor = new PropertyDescriptor(
//					"Config.Hostname", "Hostname");
//			configHostnameDescriptor
//					.setLabelProvider(new LabelProvider() {
//						public String getText(Object element) {
//							return config.hostname();
//						}
//					});
//			configHostnameDescriptor.setCategory("Config");
			
//			PropertyDescriptor architectureDescriptor = new PropertyDescriptor(
//					"Architecture", "Architecture");
//			architectureDescriptor.setLabelProvider(new LabelProvider() {
//				public String getText(Object element) {
//					return ((ImageInfo) element).architecture();
//				}
//			});
//			
//			PropertyDescriptor osDescriptor = new PropertyDescriptor(
//					"Os", "Os");
//			osDescriptor.setLabelProvider(new LabelProvider() {
//				public String getText(Object element) {
//					return ((ImageInfo) element).os();
//				}
//			});
//			
//			PropertyDescriptor sizeDescriptor = new PropertyDescriptor(
//					"Size", "Size");
//			sizeDescriptor.setLabelProvider(new LabelProvider() {
//				public String getText(Object element) {
//					return ((ImageInfo) element).size().toString();
//				}
//			});
			idDescriptor.setCategory("Container");
			namesDescriptor.setCategory("Container");
			imageDescriptor.setCategory("Container");
			commandDescriptor.setCategory("Container");
			createdDescriptor.setCategory("Container");
			statusDescriptor.setCategory("Container");
			portsDescriptor.setCategory("Container");
			sizeRwDescriptor.setCategory("Container");
			sizeRootFsDescriptor.setCategory("Container");
			
			propertyDescriptors = new IPropertyDescriptor[] { idDescriptor,
					namesDescriptor, imageDescriptor, commandDescriptor, createdDescriptor,
					statusDescriptor, portsDescriptor,
					sizeRwDescriptor, sizeRootFsDescriptor};
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
		final DockerContainerElement newElem = this.dockerContainerElement;
		return newElem;
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
