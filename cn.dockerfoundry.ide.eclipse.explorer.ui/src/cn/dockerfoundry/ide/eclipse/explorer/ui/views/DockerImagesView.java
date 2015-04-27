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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import cn.dockerfoundry.ide.eclipse.explorer.ui.Activator;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerImageElement;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.RemovedImage;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class DockerImagesView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "cn.dockerfoundry.ide.eclipse.explorer.ui.views.DockerImagesView";

	private TableViewer viewer;
	private Table table;
	private DockerImageViewerFilter searchFilter;
	private DockerImageViewerComparator comparator;
	private Action pullImageAction;
	private Action pushmageAction;
	private Action createImageAction;
	private Action createContainerAction;
	private Action deleteImageAction;
	private Action inspectAction;
	private Action refreshAction;
	private Action doubleClickAction;
	private DockerClient client;

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			System.out.println(parent);
			if (parent != null && parent instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<DockerImageElement> images = (List<DockerImageElement>) parent;
				DockerImageElement[] elements = new DockerImageElement[images
						.size()];
				return images.toArray(elements);
			}
			return new DockerImageElement[] {};
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			if (obj instanceof DockerImageElement) {
				DockerImageElement elem = (DockerImageElement) obj;
				switch (index) {
				case 0:
					return elem.getRepository();
				case 1:
					return elem.getTag();
				case 2:
					return elem.getShortId();
				case 3:
					return elem.getCreated();
				case 4:
					return elem.getVirtualSize().toString();
				default:
					break;
				}
			}
			return null;
		}

		public Image getColumnImage(Object obj, int index) {
			return null;
			// return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public DockerImagesView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		composite.setLayoutData(fileSelectionData);

		GridLayout fileSelectionLayout = new GridLayout();
		fileSelectionLayout.numColumns = 1;
		fileSelectionLayout.makeColumnsEqualWidth = false;
		fileSelectionLayout.marginWidth = 0;
		fileSelectionLayout.marginHeight = 0;
		composite.setLayout(fileSelectionLayout);

		final Text searchText = new Text(composite, SWT.BORDER | SWT.SEARCH);
		searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));

		viewer = new TableViewer(composite, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 1;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);

		comparator = new DockerImageViewerComparator();
		viewer.setComparator(comparator);

		// New to support the search
		searchText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {
				searchFilter.setSearchString(searchText.getText());
				viewer.refresh();
			}

		});
		searchFilter = new DockerImageViewerFilter();
		viewer.addFilter(searchFilter);

		table = viewer.getTable();
		table.setLinesVisible(false);
		table.setHeaderVisible(true);
	    createColumns(parent, viewer);

	    getSite().setSelectionProvider(viewer);
		// Create the help context id for the viewer's control
		PlatformUI
				.getWorkbench()
				.getHelpSystem()
				.setHelp(viewer.getControl(),
						"cn.dockerfoundry.ide.eclipse.explorer.ui.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				DockerImagesView.this.fillContextMenu(manager);
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
		manager.add(pullImageAction);
		manager.add(new Separator());
		manager.add(inspectAction);
		manager.add(createImageAction);
		manager.add(createContainerAction);
		manager.add(deleteImageAction);
		manager.add(inspectAction);
		manager.add(refreshAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		if(viewer.getSelection() instanceof IStructuredSelection){
			IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
			if(selection.size() <1)
				return;
			Object obj = selection.getFirstElement();
			System.out.println(obj);
			if(obj instanceof DockerImageElement){
				DockerImageElement elem = (DockerImageElement)obj;
			}
		}
		manager.add(pullImageAction);
		manager.add(pushmageAction);
		manager.add(createImageAction);
		manager.add(createContainerAction);
		manager.add(deleteImageAction);
		manager.add(inspectAction);
		manager.add(refreshAction);
		
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(pullImageAction);
		manager.add(pushmageAction);
		manager.add(createImageAction);
		manager.add(createContainerAction);
		manager.add(deleteImageAction);
		manager.add(inspectAction);
		manager.add(refreshAction);
	}

	private void makeActions() {
		pullImageAction = new Action() {
			public void run() {
				DefaultDockerClient client = DefaultDockerClient.builder()
				        .uri(DefaultDockerClient.DEFAULT_UNIX_ENDPOINT).build();
//				client.searchImages(term)
				showMessage("Action 1 executed");
				
			}
		};
		pullImageAction.setText("Pull Image");
		pullImageAction.setToolTipText("Pull an image or a repository from a Docker registry server");
		pullImageAction.setImageDescriptor(Activator.getImageDescriptor("icons/import.gif"));

		pushmageAction = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		pushmageAction.setText("Push Image");
		pushmageAction.setToolTipText("Push an image or a repository to a Docker registry server");
		pushmageAction.setImageDescriptor(Activator.getImageDescriptor("icons/export.gif"));
		
		createImageAction = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		createImageAction.setText("Build Image");
		createImageAction.setToolTipText("Building images from a Dockerfile");
		createImageAction.setImageDescriptor(Activator.getImageDescriptor("icons/image.gif"));
		
		createContainerAction = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		createContainerAction.setText("Create Container");
		createContainerAction.setToolTipText("Creating a container from an image");
		createContainerAction.setImageDescriptor(Activator.getImageDescriptor("icons/container.gif"));
		
		deleteImageAction = new Action() {
			public void run() {
				DefaultDockerClient client = DefaultDockerClient.builder()
				        .uri(DefaultDockerClient.DEFAULT_UNIX_ENDPOINT).build();
				DockerImageElement elem = getSelection();
				
				if(elem != null){
					try {
						List<RemovedImage> removedImages = client.removeImage(elem.getId());
						if(removedImages != null && removedImages.size() > 0){
							StringBuilder sb = new StringBuilder();
							for (Iterator<RemovedImage> iterator = removedImages.iterator(); iterator
									.hasNext();) {
								RemovedImage removedImage = (RemovedImage) iterator
										.next();
								String imageId = removedImage.imageId();
								//removedImage.type();
								sb.append(imageId);
								if(iterator.hasNext())
									sb.append(",");
							}
							showMessage("The following images have been removed:\n" + sb.toString());
						}
						
					} catch (DockerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		deleteImageAction.setText("Remove Image");
		deleteImageAction.setToolTipText("Pull an image or a repository from a Docker registry server");
		deleteImageAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		
		inspectAction = new Action() {
			public void run() {
				IViewPart propSheet = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.findView(IPageLayout.ID_PROP_SHEET);

				if (propSheet != null) {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().bringToTop(propSheet);
				}else{
					try {
						PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage().showView(IPageLayout.ID_PROP_SHEET);
					} catch (PartInitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		};
		inspectAction.setText("Inspect");
		inspectAction.setToolTipText("Inspect the information on a docker image");
		inspectAction.setImageDescriptor(Activator.getImageDescriptor("icons/inspect.gif"));
		
		refreshAction = new Action() {
			public void run() {
				viewer.refresh(true);
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Refresh images list");
		refreshAction.setImageDescriptor(Activator.getImageDescriptor("icons/refresh.gif"));
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				showMessage("Double-click detected on " + obj.toString());
			}
		};
	}

	private DockerImageElement getSelection(){
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection) selection)
				.getFirstElement();
		if(obj != null && obj instanceof DockerImageElement)
			return (DockerImageElement)obj;
		return null;
	}
	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				"Docker Images", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public TableViewer getViewer() {
		return viewer;
	}

	private void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { "REPOSITORY", "TAG", "IMAGE ID", "CREATED",
				"VIRTUAL SIZE" };
		int[] bounds = { 400, 200, 200, 200, 200 };
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DockerImageElement elem = (DockerImageElement) element;
				return elem.getRepository();
			}
		});
		col = createTableViewerColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DockerImageElement elem = (DockerImageElement) element;
				return elem.getTag();
			}
		});
		col = createTableViewerColumn(titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DockerImageElement elem = (DockerImageElement) element;
				return elem.getShortId();
			}
		});
		col = createTableViewerColumn(titles[3], bounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DockerImageElement elem = (DockerImageElement) element;
				return elem.getCreated();
			}
		});
		col = createTableViewerColumn(titles[4], bounds[4], 4);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DockerImageElement elem = (DockerImageElement) element;
				return elem.getVirtualSize().toString();
			}
		});
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound,
			final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(getSelectionAdapter(column, colNumber));
		return viewerColumn;
	}

	private SelectionAdapter getSelectionAdapter(final TableColumn column,
			final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column);
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}

	public DockerClient getClient() {
		return client;
	}

	public void setClient(DockerClient client) {
		this.client = client;
	}
}