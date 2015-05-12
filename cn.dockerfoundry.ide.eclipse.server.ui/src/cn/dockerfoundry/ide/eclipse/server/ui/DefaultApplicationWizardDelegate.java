/*******************************************************************************
 * Copyright (c) 2013, 2014 Pivotal Software, Inc. 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, 
 * Version 2.0 (the "License�); you may not use this file except in compliance 
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
 *     Pivotal Software, Inc. - initial API and implementation
 ********************************************************************************/
package cn.dockerfoundry.ide.eclipse.server.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;

import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerConnectionElement;
import cn.dockerfoundry.ide.eclipse.explorer.ui.utils.DockerContainerInfo;
import cn.dockerfoundry.ide.eclipse.explorer.ui.utils.DockerDomainHelper;
import cn.dockerfoundry.ide.eclipse.server.core.internal.ApplicationUrlLookupService;
import cn.dockerfoundry.ide.eclipse.server.core.internal.DockerFoundryServer;
import cn.dockerfoundry.ide.eclipse.server.core.internal.client.DockerFoundryApplicationModule;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.wizards.ApplicationWizardDelegate;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.wizards.ApplicationWizardDescriptor;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.wizards.DockerFoundryApplicationEnvVarWizardPage;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.wizards.DockerFoundryApplicationServicesWizardPage;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.wizards.DockerFoundryApplicationWizardPage;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.wizards.DockerFoundryDeploymentWizardPage;

import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.ContainerInfo;

/**
 * Default delegate for any application that uses standard Java web deployment
 * properties for Cloud Foundry. For example, default Java web applications
 * require at least one mapped application URL, therefore the wizard delegate
 * will provide wizard pages that require that at least one mapped application
 * URL is set by the user in the UI.
 * 
 */
public class DefaultApplicationWizardDelegate extends ApplicationWizardDelegate {

	public List<IWizardPage> getWizardPages(ApplicationWizardDescriptor applicationDescriptor,
			DockerFoundryServer cloudServer, DockerFoundryApplicationModule applicationModule) {
		List<IWizardPage> defaultPages = new ArrayList<IWizardPage>();
		
		ApplicationUrlLookupService urllookup = ApplicationUrlLookupService.getCurrentLookup(cloudServer);

		DockerFoundryDeploymentWizardPage deploymentPage = new DockerFoundryDeploymentWizardPage(cloudServer,
				applicationModule, applicationDescriptor, urllookup, this);

		DockerFoundryApplicationWizardPage applicationNamePage = new DockerFoundryApplicationWizardPage(cloudServer,
				deploymentPage, applicationModule, applicationDescriptor);

//		defaultPages.add(applicationNamePage);

//		defaultPages.add(deploymentPage);

		DockerFoundryApplicationServicesWizardPage servicesPage = new DockerFoundryApplicationServicesWizardPage(
				cloudServer, applicationModule, applicationDescriptor);

		defaultPages.add(servicesPage);

		
		String containerId = cloudServer.getDockerContainerId();
		DockerConnectionElement dockerConnElem = cloudServer.getDockerConnElem();
		DockerContainerInfo dockerContainerInfo = new DockerContainerInfo();
		if(containerId != null && dockerConnElem != null){
			DockerClient client;
			try {
				client = dockerConnElem.getDockerClient();
				ContainerInfo containerInfo = client.inspectContainer(containerId);
				dockerContainerInfo = DockerDomainHelper.getDockerInfo(containerInfo);
			}
			catch (DockerCertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (DockerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
			
			
		defaultPages.add(new DockerFoundryApplicationEnvVarWizardPage(cloudServer, applicationDescriptor
				.getDeploymentInfo(), dockerContainerInfo ));
		return defaultPages;
	}

}
