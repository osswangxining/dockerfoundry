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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;

import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerConnectionTreeParent;
import cn.dockerfoundry.ide.eclipse.explorer.ui.utils.ViewHelper;
import cn.dockerfoundry.ide.eclipse.explorer.ui.views.DockerExplorerView;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.messages.ProgressMessage;

public class DockerfileWizard extends Wizard {

	DockerfileWizardPage mainPage;
	DockerClient client;

	public DockerfileWizard(DockerClient client) {
		super();
		this.client = client;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		// IFile file = mainPage.createNewFile();
		// if (file == null)
		// return false;
		String dockerDirectory = mainPage.getDockerfile();
		if (dockerDirectory == null || this.client == null)
			return false;

		String t = mainPage.getTargetImageName();
		final List<ProgressMessage> messages = new ArrayList<ProgressMessage>();
		try {
			String returnedImageId = this.client.build(Paths.get(dockerDirectory).getParent(), t, new ProgressHandler() {
				@Override
				public void progress(ProgressMessage message)
						throws DockerException {
					messages.add(message);
					try {
						ViewHelper.showConsole("Dockerfile", message.toString());
					} catch (PartInitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			
			ViewHelper.showConsole("Dockerfile", "Image "+returnedImageId + " has been built successfully.");
		} catch (DockerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ViewHelper.showDockerImages(client);

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
		mainPage = new DockerfileWizardPage("Dockerfile"); // NON-NLS-1
		addPage(mainPage);
	}
}
