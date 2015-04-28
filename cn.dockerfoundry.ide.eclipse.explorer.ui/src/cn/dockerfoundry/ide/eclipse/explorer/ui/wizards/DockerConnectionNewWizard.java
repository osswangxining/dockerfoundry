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

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.ViewSite;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import cn.dockerfoundry.ide.eclipse.explorer.ui.Activator;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerConnectionContainersTreeParent;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerConnectionElement;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerConnectionImagesTreeParent;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerConnectionTreeParent;
import cn.dockerfoundry.ide.eclipse.explorer.ui.views.DockerExplorerView;

public class DockerConnectionNewWizard extends Wizard {

	DockerConnectionNewWizardPage mainPage;

	TreeViewer viewer;

	public DockerConnectionNewWizard(TreeViewer viewer) {
		super();
		this.viewer = viewer;
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
		DockerConnectionElement connElem = mainPage.getDockerConnection();
		if (connElem == null)
			return false;

		String connName = connElem.getName();

		if (connElem.isUseDefault()) {
			connName += "[Default]";
		} else if (connElem.isUseHTTPS()) {
			connName += "[" + connElem.getHost() + "]";
		}
		DockerConnectionTreeParent conn = new DockerConnectionTreeParent(
				connName, connElem);

		DockerConnectionTreeParent containersTreeParent = new DockerConnectionContainersTreeParent(
				"Containers", connElem);
		conn.addChild(containersTreeParent);
		DockerConnectionTreeParent imagesTreeParent = new DockerConnectionImagesTreeParent(
				"Images", connElem);
		conn.addChild(imagesTreeParent);

//		DockerClient client = null;
//		try {
//			client = connElem.getDockerClient();
//		} catch (DockerCertificateException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		if (client == null) {
//			return false;
//		}
//		try {
//			List<Image> images = client.listImages();
//			if (images != null) {
//				for (Iterator<Image> iterator = images.iterator(); iterator
//						.hasNext();) {
//					Image image = (Image) iterator.next();
//					List<String> repoTags = image.repoTags();
//					if(repoTags != null && repoTags.size() > 0){
//						for (Iterator<String> iterator2 = repoTags.iterator(); iterator2
//								.hasNext();) {
//							String repoTag = (String) iterator2.next();
//							String label = repoTag +"(" + image.id().substring(0, 12)+")";
//							DockerConnectionTreeObject to1 = new DockerConnectionImageTreeObject(
//									label, connElem);
//							imagesTreeParent.addChild(to1);
//						}
//					}//end for if					
//				}
//			}
//			ListContainersParam params = ListContainersParam.allContainers(true);
//			List<Container> containers = client.listContainers(params );
//			if (containers != null) {
//				for (Iterator<Container> iterator = containers.iterator(); iterator
//						.hasNext();) {
//					Container container = (Container) iterator.next();
//					StringBuilder sb = new StringBuilder();
//					if(container.names() == null || container.names().isEmpty()){
//						sb.append(container.id());
//					}else{
//						String _name = container.names().get(0);
//						String label = _name.startsWith("/")?_name.substring(1): _name;
//						sb.append(label);
//					}
//					sb.append("(").append(container.image()).append(")");
//					DockerConnectionTreeObject to1 = new DockerConnectionContainerTreeObject(
//							sb.toString(), connElem);
//					containersTreeParent.addChild(to1);
//				}
//			}
//		} catch (DockerException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		Object input = this.viewer.getInput();
		System.out.println(input instanceof DockerExplorerView);
		if (input == null || !(input instanceof DockerConnectionTreeParent)) {
			DockerConnectionTreeParent root = new DockerConnectionTreeParent(
					"Root", connElem);
			root.addChild(conn);
			this.viewer.setInput(root);
//		} else if(input instanceof ViewSite){
//			if(((ViewSite)input).getPart() != null && ((ViewSite)input).getPart() instanceof DockerExplorerView){
//				DockerExplorerView view = (DockerExplorerView) ((ViewSite)input).getPart();
//				view.get
//			}
		}else {
			DockerConnectionTreeParent parent = (DockerConnectionTreeParent) input;
			parent.addChild(conn);
		}
		this.viewer.refresh(true);
		this.viewer.expandToLevel(3);
		
		persist(connElem);
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
	 * org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("File Import Wizard"); // NON-NLS-1
		setNeedsProgressMonitor(true);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	public void addPages() {
		super.addPages();
		mainPage = new DockerConnectionNewWizardPage("Docker Connection"); // NON-NLS-1
		addPage(mainPage);
	}

	private void persist(DockerConnectionElement connElem) {
		Preferences preferences = InstanceScope.INSTANCE
				.getNode(Activator.PLUGIN_ID);
		
		if(connElem != null){
			String name = connElem.getName();
			String authPath = connElem.getAuthPath();
			String host = connElem.getHost();
			boolean isUseDefault = connElem.isUseDefault();
			boolean isUseUnixSocket = connElem.isUseUnixSocket();
			boolean isUseHTTPS = connElem.isUseHTTPS();
			boolean isEnableAuth = connElem.isEnableAuth();
			String socketPath = connElem.getSocketPath();
			Preferences sub1 = preferences.node(name);
			sub1.put("name", name);
			sub1.putBoolean("isUseDefault", isUseDefault);
			sub1.putBoolean("isUseUnixSocket", isUseUnixSocket);
			sub1.put("socketPath", socketPath);
			sub1.putBoolean("isUseHTTPS", isUseHTTPS);
			sub1.put("host", host);
			sub1.putBoolean("isEnableAuth", isEnableAuth);
			sub1.put("authPath", authPath);			
		}
	
		try {
			// forces the application to save the preferences
			preferences.flush();
		} catch (BackingStoreException e2) {
			e2.printStackTrace();
		}
	}
}
