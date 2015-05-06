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

package cn.dockerfoundry.ide.eclipse.explorer.ui.wizards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;

import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerContainerElement;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerImageElement;
import cn.dockerfoundry.ide.eclipse.explorer.ui.utils.DomainHelper;
import cn.dockerfoundry.ide.eclipse.explorer.ui.utils.ViewHelper;
import cn.dockerfoundry.ide.eclipse.explorer.ui.views.DockerContainersView;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.docker.client.messages.ProgressMessage;

public class DockerCreateContainerWizard extends Wizard {
	DockerCreateContainerWizardPage mainPage;
	DockerImageElement elem;
	DockerClient client;

	public DockerCreateContainerWizard(DockerImageElement elem, DockerClient client) {
		super();
		this.elem = elem;
		this.client = client;
	}

//	public boolean canFinish(){
//		ImageSearchResult searchResult = this.mainPage.getSelectedImage();
//		if (searchResult == null || this.client == null)
//			return false;
//
//		try {
//			String ping = this.client.ping();
//			if (!ping.toLowerCase().equals("ok")) {
//				return false;
//			}
//		} catch (DockerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return false;
//		}
//		
//		return true;
//	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		String name = this.mainPage.getName();
		if (this.client == null || elem == null)
			return false;

	    final ContainerConfig config = ContainerConfig.builder()
	            .image(elem.getId())
	            .build();
	    try {
	    	name = (name == null || name.trim().length() == 0)? null : name;
			ContainerCreation creation = this.client.createContainer(config, name);
			try {
				ViewHelper.showConsole("Docker Create Container", creation.toString());
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (DockerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

	    ViewHelper.showDockerContainers(client);
	    
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Docker Connection"); // NON-NLS-1
		setNeedsProgressMonitor(true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		super.addPages();
		mainPage = new DockerCreateContainerWizardPage("Docker Search", client, elem); // NON-NLS-1
		addPage(mainPage);
	}
}
