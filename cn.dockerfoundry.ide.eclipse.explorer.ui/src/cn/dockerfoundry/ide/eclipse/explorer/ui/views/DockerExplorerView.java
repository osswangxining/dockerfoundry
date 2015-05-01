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

package cn.dockerfoundry.ide.eclipse.explorer.ui.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import cn.dockerfoundry.ide.eclipse.explorer.ui.Activator;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerConnectionContainersTreeParent;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerConnectionElement;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerConnectionImagesTreeParent;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerConnectionTreeObject;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerConnectionTreeObject.DockerConnectionContainerTreeObject;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerConnectionTreeObject.DockerConnectionImageTreeObject;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerConnectionTreeParent;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerContainerElement;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerImageElement;
import cn.dockerfoundry.ide.eclipse.explorer.ui.utils.DomainHelper;
import cn.dockerfoundry.ide.eclipse.explorer.ui.wizards.DockerConnectionNewWizard;

import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.Container.PortMapping;
import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.docker.client.messages.Info;
import com.spotify.docker.client.messages.Version;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class DockerExplorerView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "cn.dockerfoundry.ide.eclipse.explorer.ui.views.DockerExplorerView";

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action createDockerConnAction;
	private Action removeDockerConnAction;
	private Action dockerInfoAction;
	private Action dockerVersionAction;
	private Action doubleClickAction;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class ViewContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {
		private DockerConnectionTreeParent invisibleRoot;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot==null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof DockerConnectionTreeObject) {
				return ((DockerConnectionTreeObject)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof DockerConnectionTreeParent) {
				return ((DockerConnectionTreeParent)parent).getChildren();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof DockerConnectionTreeParent)
				return ((DockerConnectionTreeParent)parent).hasChildren();
			return false;
		}
/*
 * We will set up a dummy model to initialize tree heararchy.
 * In a real code, you will connect to a real model and
 * expose its hierarchy.
 */
		private void initialize() {
//			DockerConnectionTreeObject to1 = new DockerConnectionTreeObject("Leaf 1");
//			DockerConnectionTreeObject to2 = new DockerConnectionTreeObject("Leaf 2");
//			DockerConnectionTreeObject to3 = new DockerConnectionTreeObject("Leaf 3");
//			DockerConnectionTreeParent p1 = new DockerConnectionTreeParent("Parent 1");
//			p1.addChild(to1);
//			p1.addChild(to2);
//			p1.addChild(to3);
//			
//			DockerConnectionTreeObject to4 = new DockerConnectionTreeObject("Leaf 4");
//			DockerConnectionTreeParent p2 = new DockerConnectionTreeParent("Parent 2");
//			p2.addChild(to4);
//			
//			DockerConnectionTreeParent root = new DockerConnectionTreeParent("Root");
//			root.addChild(p1);
//			root.addChild(p2);
//			
//			invisibleRoot = new DockerConnectionTreeParent("");
//			invisibleRoot.addChild(root);
			invisibleRoot = getInitInput();
		}
	}
	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}
		public Image getImage(Object obj) {
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (obj instanceof DockerConnectionImagesTreeParent)
				imageKey = "icons/images.gif";
			else if (obj instanceof DockerConnectionContainersTreeParent)
				imageKey = "icons/containers.gif";
			else if (obj instanceof DockerConnectionImageTreeObject)
				imageKey = "icons/image.gif";
			else if (obj instanceof DockerConnectionContainerTreeObject)
				imageKey = "icons/container.gif";
			else if (obj instanceof DockerConnectionTreeParent)
				imageKey = "icons/server.gif";
			
			return Activator.getImageDescriptor(imageKey).createImage();
//			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public DockerExplorerView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getInitInput());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "cn.dockerfoundry.ide.eclipse.explorer.ui.viewer");
		makeActions();
		hookContextMenu();
		hookSingleClickAction();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private DockerConnectionTreeParent getInitInput(){
		DockerConnectionTreeParent invisibleRoot = new DockerConnectionTreeParent(
				"invisibleRoot", null);			
		
		Preferences preferences = InstanceScope.INSTANCE
				.getNode(Activator.PLUGIN_ID);
		try {
			String[] childrenNames = preferences.childrenNames();
			if(childrenNames != null){
				for (int i = 0; i < childrenNames.length; i++) {
					Preferences sub1 = preferences.node(childrenNames[i]);
					String name = sub1.get("name", "");
					boolean isUseDefault = sub1.getBoolean("isUseDefault", true);
					boolean isUseUnixSocket = sub1.getBoolean("isUseUnixSocket", false);
					String socketPath = sub1.get("socketPath", "");
					boolean isUseHTTPS = sub1.getBoolean("isUseHTTPS", false);
					String host = sub1.get("host", "");
					boolean isEnableAuth = sub1.getBoolean("isEnableAuth", false);
					String authPath = sub1.get("authPath", "");		
					
					DockerConnectionElement connElem = new DockerConnectionElement();
					connElem.setAuthPath(authPath);
					connElem.setEnableAuth(isEnableAuth);
					connElem.setHost(host);
					connElem.setName(name);
					connElem.setSocketPath(socketPath);
					connElem.setUseDefault(isUseDefault);
					connElem.setUseHTTPS(isUseHTTPS);
					connElem.setUseUnixSocket(isUseUnixSocket);
					
					String connName = name;
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
					
					invisibleRoot.addChild(conn);
				}
			}
		} catch (BackingStoreException e) {
			 e.printStackTrace();
		}
		return invisibleRoot;
	}
	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				DockerExplorerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(createDockerConnAction);
		manager.add(new Separator());
		manager.add(removeDockerConnAction);
		Object selectedElem = getSelectedElement();
		removeDockerConnAction.setEnabled(isDockerConnectionElem(selectedElem));
		manager.add(new Separator());
		manager.add(dockerInfoAction);
		manager.add(dockerVersionAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(createDockerConnAction);
		manager.add(removeDockerConnAction);
		Object selectedElem = getSelectedElement();
		removeDockerConnAction.setEnabled(isDockerConnectionElem(selectedElem));
		manager.add(new Separator());
		manager.add(dockerInfoAction);
		manager.add(dockerVersionAction);
		manager.add(new Separator());
//		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(createDockerConnAction);
		manager.add(removeDockerConnAction);
		Object selectedElem = getSelectedElement();
		removeDockerConnAction.setEnabled(isDockerConnectionElem(selectedElem));
		manager.add(new Separator());
		manager.add(dockerInfoAction);
		manager.add(dockerVersionAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		createDockerConnAction = new Action() {
			public void run() {
				//showMessage("Action 1 executed");
				DockerConnectionNewWizard wizard = new DockerConnectionNewWizard(viewer);
				WizardDialog dialog = new WizardDialog(viewer.getControl().getShell(), wizard);
//				dialog.create();
				dialog.open();
			}
		};
		createDockerConnAction.setText("Create Docker Connection...");
		createDockerConnAction.setToolTipText("Create the connection to talk with Docker...");
		createDockerConnAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJ_ADD));
		
		removeDockerConnAction = new Action() {
			public void run() {				
				if (viewer.getSelection().isEmpty()) {
					return;
				}				
				viewer.getTree().setRedraw(false);
				System.out.println(viewer.getSelection());
				Object selectedElem = getSelectedElement();
				if(isDockerConnectionElem(selectedElem)){
					((DockerConnectionTreeParent)selectedElem).getParent().removeChild((DockerConnectionTreeParent)selectedElem);
				}
				
				viewer.getTree().setRedraw(true);
				viewer.refresh(true);
			}
		};
		removeDockerConnAction.setText("Remove Docker Connection");
		removeDockerConnAction.setToolTipText("Remove the connection to talk with Docker...");
		removeDockerConnAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		
		dockerInfoAction = new Action() {
			public void run() {				
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				if(obj instanceof DockerConnectionTreeParent){
					DockerConnectionTreeParent elem = (DockerConnectionTreeParent)obj;
					if (elem != null && elem.getConn() != null){
						try {
							DockerClient client = elem.getConn().getDockerClient();
							Info info = client.info();
							showMessage(info.toString());
						} catch (DockerCertificateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (DockerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		};
		dockerInfoAction.setText("Docker Info");
		dockerInfoAction.setToolTipText("Display system-wide information - [info]");
		dockerInfoAction.setImageDescriptor(Activator.getImageDescriptor("icons/info.gif"));
		
		dockerVersionAction = new Action() {
			public void run() {				
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				if(obj instanceof DockerConnectionTreeParent){
					DockerConnectionTreeParent elem = (DockerConnectionTreeParent)obj;
					if (elem != null && elem.getConn() != null){
						try {
							DockerClient client = elem.getConn().getDockerClient();
							Version version = client.version();
							showMessage(version.toString());
						} catch (DockerCertificateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (DockerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		};
		dockerVersionAction.setText("Docker Version");
		dockerVersionAction.setToolTipText("Show the Docker version information - [version]");
		dockerVersionAction.setImageDescriptor(Activator.getImageDescriptor("icons/version.gif"));
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();

				if (obj instanceof DockerConnectionImagesTreeParent) {
					DockerConnectionImagesTreeParent elem = (DockerConnectionImagesTreeParent) obj;
					if (elem != null && elem.getConn() != null)
						try {
							showDockerImages(elem.getConn().getDockerClient());
						} catch (DockerCertificateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				} else if (obj instanceof DockerConnectionContainersTreeParent) {					
					DockerConnectionContainersTreeParent elem = (DockerConnectionContainersTreeParent) obj;
					if (elem != null && elem.getConn() != null)
						try {
							showDockerContainers(elem.getConn().getDockerClient());
						} catch (DockerCertificateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				} else if (obj instanceof DockerConnectionTreeParent) {
					
				} else if (obj instanceof DockerConnectionContainerTreeObject) {
					System.out.println(obj);
				} else if (obj instanceof DockerConnectionImageTreeObject) {
					System.out.println(obj);
				}
				if(obj != null){
					if (viewer.getExpandedState(obj)) {
						viewer.collapseToLevel(obj, 2);
					} else {
						viewer.expandToLevel(obj, 2);
					}
				}
			}
		};
	}

	private void showDockerContainers(DockerClient client ){
		try {
			IViewPart propSheet = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.findView(DockerContainersView.ID);

			if (propSheet != null) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().bringToTop(propSheet);
			}else{
				try {
					propSheet = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().showView(DockerContainersView.ID);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
			
			DockerContainersView viewer = (DockerContainersView)propSheet;
			viewer.setClient(client);
			
			ListContainersParam params = ListContainersParam.allContainers(true);
			List<Container> containers = client.listContainers(params);
			
			List<DockerContainerElement> containerElements = DomainHelper.convert(containers);
			
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
	
	private void showDockerImages(DockerClient client ){
		try {
			IViewPart propSheet = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage()
					.findView(DockerImagesView.ID);

			if (propSheet != null) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().bringToTop(propSheet);
			}else{
				try {
					propSheet = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().showView(DockerImagesView.ID);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
			
			DockerImagesView viewer = (DockerImagesView)propSheet;
			viewer.setClient(client);
			List<com.spotify.docker.client.messages.Image> images = client.listImages();
			
			List<DockerImageElement> imageElements = new ArrayList<DockerImageElement>();
			if(images != null){
				for (Iterator<com.spotify.docker.client.messages.Image> iterator = images.iterator(); iterator.hasNext();) {
					com.spotify.docker.client.messages.Image image = iterator.next();
					System.out.println(image.toString());
					String created = image.created();
					String id = image.id();
					String parentId = image.parentId();
					List<String> repoTags = image.repoTags();
					Long size = image.size();
					Long virtualSize = image.virtualSize();
					if(repoTags != null){
						for (Iterator<String> iterator2 = repoTags.iterator(); iterator2
								.hasNext();) {
							String repoTag = (String) iterator2.next();
							String repo = repoTag.substring(0, repoTag.indexOf(":"));
							String tag = repoTag.substring(repoTag.indexOf(":")+1);
							
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
	
	private void hookSingleClickAction() {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				doubleClickAction.run();
				Object selectedElem = getSelectedElement();
				removeDockerConnAction.setEnabled(isDockerConnectionElem(selectedElem));
			}
		});
	}
	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Docker Explorer",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	private Object getSelectedElement() {
		if (viewer.getSelection() == null
				|| !(viewer.getSelection() instanceof IStructuredSelection))
			return null;

		Object obj = ((IStructuredSelection) viewer.getSelection())
				.getFirstElement();
		return obj;
	}

	private boolean isDockerConnectionElem(Object selectedElem) {
		if (selectedElem != null
				&& selectedElem instanceof DockerConnectionTreeParent
				&& !(selectedElem instanceof DockerConnectionContainersTreeParent)
				&& !(selectedElem instanceof DockerConnectionImagesTreeParent)) {
			return true;
		}
		return false;
	}
}