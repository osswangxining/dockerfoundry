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

package cn.dockerfoundry.ide.eclipse.explorer.ui.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerContainerElement;

import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.Container.PortMapping;

/**
 * @author wangxn
 *
 */
public class DomainHelper {
	public static DockerContainerElement convert(Container container) {
		if (container == null)
			return null;

		String command = container.command();
		String id = container.id();
		String image = container.image();
		List<String> names = container.names();
		Long created = container.created();
		Long sizeRootFs = container.sizeRootFs();
		Long sizeRw = container.sizeRw();
		String status = container.status();
		List<PortMapping> ports = container.ports();

		DockerContainerElement e = new DockerContainerElement();
		e.setCommand(command);
		e.setId(id);
		e.setImage(image);
		e.setNames(names);
		java.util.Date dt = new java.util.Date(created * 1000);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		e.setCreated(sdf.format(dt));
		e.setSizeRootFs(sizeRootFs);
		e.setSizeRw(sizeRw);
		e.setStatus(status);
		e.setPorts(ports);

		return e;
	}

	public static List<DockerContainerElement> convert(
			List<Container> containers) {
		List<DockerContainerElement> containerElements = new ArrayList<DockerContainerElement>();
		if (containers != null) {
			for (Iterator<Container> iterator = containers.iterator(); iterator
					.hasNext();) {
				Container container = iterator.next();
				DockerContainerElement e = convert(container);
				if (e != null)
					containerElements.add(e);
			}
		}
		return containerElements;
	}
}
