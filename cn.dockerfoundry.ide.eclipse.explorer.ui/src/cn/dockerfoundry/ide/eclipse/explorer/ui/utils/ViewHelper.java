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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerContainerElement;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerImageElement;
import cn.dockerfoundry.ide.eclipse.explorer.ui.views.DockerContainersView;
import cn.dockerfoundry.ide.eclipse.explorer.ui.views.DockerImagesView;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ImageInfo;

/**
 * @author wangxn
 *
 */
public class ViewHelper {
	public static void showConsole(String consoleName, String content) throws IOException, PartInitException{
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		MessageConsole console = ConsoleHelper
				.findConsole(consoleName);
		MessageConsoleStream out = console.newMessageStream();
		out.println(content);
		out.setActivateOnWrite(true);
		out.setColor(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		out.close();
		
		IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
		view.display(console);
	}
	
	public static void showDockerContainers(DockerClient client) {
		try {
			IViewPart propSheet = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.findView(DockerContainersView.ID);

			if (propSheet != null) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().bringToTop(propSheet);
			} else {
				try {
					propSheet = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage()
							.showView(DockerContainersView.ID);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}

			DockerContainersView viewer = (DockerContainersView) propSheet;
			viewer.setClient(client);

			ListContainersParam params = ListContainersParam
					.allContainers(true);
			List<Container> containers = client.listContainers(params);

			List<DockerContainerElement> containerElements = DomainHelper
					.convert(containers);

			viewer.getViewer().setInput(containerElements);
			viewer.getViewer().refresh(true);
		} catch (DockerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void showDockerImages(DockerClient client) {
		try {
			IViewPart propSheet = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.findView(DockerImagesView.ID);

			if (propSheet != null) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().bringToTop(propSheet);
			} else {
				try {
					propSheet = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage()
							.showView(DockerImagesView.ID);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}

			DockerImagesView viewer = (DockerImagesView) propSheet;
			viewer.setClient(client);
			List<com.spotify.docker.client.messages.Image> images = client
					.listImages();

			List<DockerImageElement> imageElements = new ArrayList<DockerImageElement>();
			if (images != null) {
				for (Iterator<com.spotify.docker.client.messages.Image> iterator = images
						.iterator(); iterator.hasNext();) {
					com.spotify.docker.client.messages.Image image = iterator
							.next();
					System.out.println(image.toString());
					String created = image.created();
					String id = image.id();
					String parentId = image.parentId();
					List<String> repoTags = image.repoTags();
					Long size = image.size();
					Long virtualSize = image.virtualSize();
					if (repoTags != null) {
						for (Iterator<String> iterator2 = repoTags.iterator(); iterator2
								.hasNext();) {
							String repoTag = (String) iterator2.next();
							String repo = repoTag.substring(0,
									repoTag.indexOf(":"));
							String tag = repoTag
									.substring(repoTag.indexOf(":") + 1);

							DockerImageElement e = new DockerImageElement();
							e.setCreated(created);
							e.setId(id);
							e.setRepository(repo);
							e.setTag(tag);
							e.setSize(size);
							e.setVirtualSize(virtualSize);
							ImageInfo imageInfo = client.inspectImage(id);
							e.setImageInfo(imageInfo);
							imageElements.add(e);
						}
					}

				}
			}

			viewer.getViewer().setInput(imageElements);
			viewer.getViewer().refresh(true);
		} catch (DockerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
