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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.part.ViewPart;

import cn.dockerfoundry.ide.eclipse.explorer.ui.Activator;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerContainerElement;
import cn.dockerfoundry.ide.eclipse.explorer.ui.utils.ConsoleHelper;
import cn.dockerfoundry.ide.eclipse.explorer.ui.utils.DomainHelper;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerClient.LogsParameter;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;

/**
 * This view  shows the docker containers including all the running and stopped containers.
 * 
 */

public class DockerContainersView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "cn.dockerfoundry.ide.eclipse.explorer.ui.views.DockerContainersView";

	private TableViewer viewer;
	private Table table;
	private DockerContainerViewerFilter searchFilter;
	private DockerContainerViewerComparator comparator;
	private Action startAction;
	private Action stopAction;
	private Action unpauseAction;
	private Action pauseAction;
	private Action renameAction;
	private Action deleteAction;
	private Action inspectAction;
	private Action refreshAction;
	private Action showConsoleAction;
	private Action showEnvAction;
	private Action showLinkAction;
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
				List<DockerContainerElement> images = (List<DockerContainerElement>) parent;
				DockerContainerElement[] elements = new DockerContainerElement[images
						.size()];
				return images.toArray(elements);
			}
			return new DockerContainerElement[] {};
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			if (obj instanceof DockerContainerElement) {
				DockerContainerElement elem = (DockerContainerElement) obj;
				switch (index) {
				case 0:
					return elem.getShortId();
				case 1:
					return elem.getImage();
				case 2:
					return elem.getCommand();
				case 3:
					return elem.getCreated();
				case 4:
					return elem.getStatus();
				case 5:
					return elem.getPortsAsString();
				case 6:
					return elem.getNamesAsString();
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
	public DockerContainersView() {
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

		comparator = new DockerContainerViewerComparator();
		viewer.setComparator(comparator);

		// New to support the search
		searchText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {
				searchFilter.setSearchString(searchText.getText());
				viewer.refresh();
			}

		});
		searchFilter = new DockerContainerViewerFilter();
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
		hookSingleClickAction();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				DockerContainersView.this.fillContextMenu(manager);
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
		DockerContainerElement elem = getSelectedElement();
		String status = null;
		if(elem != null)
			status = elem.getStatus();
		startAction.setEnabled(status != null && status.startsWith("Exited"));
		stopAction.setEnabled(status != null && status.startsWith("Up"));
		unpauseAction.setEnabled(status != null && status.startsWith("Paused"));
		pauseAction.setEnabled(status != null && status.startsWith("Up"));
		showConsoleAction.setEnabled(status != null && status.startsWith("Up"));
		showEnvAction.setEnabled(status != null && status.startsWith("Up"));
		showLinkAction.setEnabled(status != null && status.startsWith("Up"));
		manager.add(startAction);
		manager.add(stopAction);
		manager.add(unpauseAction);
		manager.add(pauseAction);
		manager.add(deleteAction);
		manager.add(inspectAction);
		manager.add(showConsoleAction);
		manager.add(showEnvAction);
		manager.add(showLinkAction);
		manager.add(refreshAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		DockerContainerElement elem = getSelectedElement();
		String status = null;
		if(elem != null)
			status = elem.getStatus();
		startAction.setEnabled(status != null && status.startsWith("Exited"));
		stopAction.setEnabled(status != null && status.startsWith("Up"));
		unpauseAction.setEnabled(status != null && status.startsWith("Paused"));
		pauseAction.setEnabled(status != null && status.startsWith("Up"));
		showConsoleAction.setEnabled(status != null && status.startsWith("Up"));
		showEnvAction.setEnabled(status != null && status.startsWith("Up"));
		showLinkAction.setEnabled(status != null && status.startsWith("Up"));
		manager.add(startAction);
		manager.add(stopAction);
		manager.add(unpauseAction);
		manager.add(pauseAction);
		manager.add(deleteAction);
		manager.add(inspectAction);
		manager.add(showConsoleAction);
		manager.add(showEnvAction);
		manager.add(showLinkAction);
		manager.add(refreshAction);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		DockerContainerElement elem = getSelectedElement();
		String status = null;
		if(elem != null)
			status = elem.getStatus();
		startAction.setEnabled(status != null && status.startsWith("Exited"));
		stopAction.setEnabled(status != null && status.startsWith("Up"));
		unpauseAction.setEnabled(status != null && status.startsWith("Paused"));
		pauseAction.setEnabled(status != null && status.startsWith("Up"));
		showConsoleAction.setEnabled(status != null && status.startsWith("Up"));
		showEnvAction.setEnabled(status != null && status.startsWith("Up"));
		showLinkAction.setEnabled(status != null && status.startsWith("Up"));
		manager.add(startAction);
		manager.add(stopAction);
		manager.add(unpauseAction);
		manager.add(pauseAction);
		manager.add(deleteAction);
		manager.add(inspectAction);
		manager.add(showConsoleAction);
		manager.add(showEnvAction);
		manager.add(showLinkAction);
		manager.add(refreshAction);
	}

	private void makeActions() {
		startAction = new Action() {
			public void run() {
				DockerContainerElement elem = getSelectedElement();
				
				if (elem != null & getClient() != null) {
					String message = "Are you sure you want to start this docker \""
							+ elem.getNames().get(0) + "\"?";
					boolean b = MessageDialog.openQuestion(viewer.getControl()
							.getShell(), "Start a stopped container",
							message);
					if (b) {
						try {
							getClient().startContainer(elem.getId());
							Thread.sleep(5*1000);
							hookRefreshAction();
						} catch (DockerException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}//end for if

			}
		};
		startAction.setText("Start");
		startAction
				.setToolTipText("Start a stopped container");
		startAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/start.gif"));

		stopAction = new Action() {
			public void run() {
				DockerContainerElement elem = getSelectedElement();
				String status = elem.getStatus();
				if (elem != null & getClient() != null) {
					String message = "Are you sure you want to stop this docker \""
							+ elem.getNames().get(0) + "\"?";
					boolean b = MessageDialog.openQuestion(viewer.getControl()
							.getShell(), "Stop a running container",
							message);
					if (b) {
						try {
							getClient().stopContainer(elem.getId(), 100);
							Thread.sleep(5*1000);
							hookRefreshAction();
						} catch (DockerException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}//end for if
			}
		};
		stopAction.setText("Stop");
		stopAction
				.setToolTipText("Stop a running container");
		stopAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/stop.gif"));

		unpauseAction = new Action() {
			public void run() {
				DockerContainerElement elem = getSelectedElement();
				String status = elem.getStatus();
				if (elem != null & getClient() != null) {
					String message = "Are you sure you want to unpause this docker \""
							+ elem.getNames().get(0) + "\"?";
					boolean b = MessageDialog.openQuestion(viewer.getControl()
							.getShell(), "Unpause a paused container",
							message);
					if (b) {
						try {
							getClient().unpauseContainer(elem.getId());
							Thread.sleep(5*1000);
							hookRefreshAction();
						} catch (DockerException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}//end for if
			}
		};
		unpauseAction.setText("Unpause");
		unpauseAction.setToolTipText("Unpause a paused container");
		unpauseAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/unpause.gif"));

		pauseAction = new Action() {
			public void run() {
				DockerContainerElement elem = getSelectedElement();
				String status = elem.getStatus();
				if (elem != null & getClient() != null) {
					String message = "Are you sure you want to pause this docker \""
							+ elem.getNames().get(0) + "\"?";
					boolean b = MessageDialog.openQuestion(viewer.getControl()
							.getShell(), "Pause all processes within a container",
							message);
					if (b) {
						try {
							getClient().pauseContainer(elem.getId());
							Thread.sleep(5*1000);
							hookRefreshAction();
						} catch (DockerException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}//end for if
			}
		};
		pauseAction.setText("Pause");
		pauseAction
				.setToolTipText("Pause all processes within a container");
		pauseAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/pause.gif"));

		renameAction = new Action() {
			public void run() {
				hookRenameAction();
				try {
					Thread.sleep(5*1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				hookRefreshAction();
			}
		};
		renameAction.setText("Rename");
		renameAction
				.setToolTipText("Rename an existing container");
		renameAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/rename.gif"));
		
		deleteAction = new Action() {
			public void run() {
				DockerContainerElement elem = getSelectedElement();
				if (elem != null & getClient() != null) {
					String message = "Are you sure you want to remove this docker \""
							+ elem.getNames().get(0) + "\"?";
					boolean b = MessageDialog.openQuestion(viewer.getControl()
							.getShell(), "Remove one or more containers",
							message);
					if (b) {
						try {
							getClient().removeContainer(elem.getId(), true);
							Thread.sleep(5*1000);
							hookRefreshAction();
						} catch (DockerException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}//end for if
			}
		};
		deleteAction.setText("Remove");
		deleteAction
				.setToolTipText("Remove one or more containers");
		deleteAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));

		inspectAction = new Action() {
			public void run() {
				IViewPart propSheet = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage()
						.findView(IPageLayout.ID_PROP_SHEET);

				if (propSheet != null) {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().bringToTop(propSheet);
				} else {
					try {
						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage()
								.showView(IPageLayout.ID_PROP_SHEET);
					} catch (PartInitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		};
		inspectAction.setText("Inspect");
		inspectAction
				.setToolTipText("Return low-level information on a container");
		inspectAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/inspect.gif"));

		showConsoleAction = new Action() {
			public void run() {
				
				DockerContainerElement elem = getSelectedElement();
				if (elem != null & getClient() != null) {
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					MessageConsole console = ConsoleHelper
							.findConsole("Docker Container - " + elem.getNames().get(0));
					MessageConsoleStream out = console.newMessageStream();
					LogStream logStream = null;
					try {
						logStream = getClient().logs(elem.getId(),
						/* LogsParameter.FOLLOW, */LogsParameter.STDERR,
								LogsParameter.STDOUT, LogsParameter.TIMESTAMPS);

						/*StringBuilder stringBuilder = new StringBuilder();
						int index = 0;
						while (logStream.hasNext()) {
							stringBuilder.append(UTF_8.decode(logStream.next()
									.content()));
							index++;
							if(index > 100)
								break;
						}						
						out.print(stringBuilder.toString());*/
						out.println(logStream.readFully());
						out.setActivateOnWrite(true);
						out.setColor(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
						out.close();
					} catch (DockerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}finally{
						if(logStream != null){
							logStream.close();
						}
					}
					
					try {
						IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
						view.display(console);
						view.setFocus();
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		};
		showConsoleAction.setText("Show Console");
		showConsoleAction.setToolTipText("Show the console for the docker container -  [logs -ft --tail 0]");
		showConsoleAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/console.gif"));
		
		showEnvAction = new Action() {
			public void run() {

				DockerContainerElement elem = getSelectedElement();
				if (elem != null & getClient() != null) {
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					MessageConsole console = ConsoleHelper
							.findConsole("Docker Container - "
									+ elem.getNames().get(0));
					MessageConsoleStream out = console.newMessageStream();
					LogStream logStream = null;
					try {
						String execId = getClient().execCreate(elem.getId(),
								new String[] { "env" },
								DockerClient.ExecParameter.STDOUT,
								DockerClient.ExecParameter.STDERR);

						out.println("execId = " + execId);

						logStream = getClient().execStart(execId);
						String output = logStream.readFully();
						out.println("Result:\n" + output);

						out.setActivateOnWrite(true);
						out.setColor(Display.getDefault().getSystemColor(
								SWT.COLOR_BLUE));
						out.close();
					} catch (DockerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (logStream != null) {
							logStream.close();
						}
					}

					try {
						IConsoleView view = (IConsoleView) page
								.showView(IConsoleConstants.ID_CONSOLE_VIEW);
						view.display(console);
						view.setFocus();
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		};
		showEnvAction.setText("Show Env in Console");
		showEnvAction
				.setToolTipText("Show the environment variables in the console");
		showEnvAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/env.gif"));
		
		showLinkAction = new Action() {
			public void run() {

				DockerContainerElement elem = getSelectedElement();
				if (elem != null & getClient() != null) {
					IWorkbenchPage page = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
					MessageConsole console = ConsoleHelper
							.findConsole("Docker Container - "
									+ elem.getNames().get(0));
					MessageConsoleStream out = console.newMessageStream();
					LogStream logStream = null;
					try {
						ContainerInfo info = getClient().inspectContainer(elem.getId());
						HostConfig hostConfig = info.hostConfig();
						List<String> links;
						if(hostConfig != null && (links = hostConfig.links()) != null && links.size() > 0){
							out.setColor(Display.getDefault().getSystemColor(
									SWT.COLOR_BLUE));
							out.println("Container [" + elem.getName() +"] has the following links:");
							for (Iterator<String>  iterator = links.iterator(); iterator
									.hasNext();) {
								String link = (String) iterator.next();
								out.println(link);
							}
						}else{
							out.setColor(Display.getDefault().getSystemColor(
									SWT.COLOR_RED));
							out.println("Container [" + elem.getName() +"] has not any links.");							
						}
			
						out.setActivateOnWrite(true);						
						out.close();
					} catch (DockerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (logStream != null) {
							logStream.close();
						}
					}

					try {
						IConsoleView view = (IConsoleView) page
								.showView(IConsoleConstants.ID_CONSOLE_VIEW);
						view.display(console);
						view.setFocus();
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		};
		showLinkAction.setText("Show Docker Links in Console");
		showLinkAction
				.setToolTipText("Show the docker links in the console");
		showLinkAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/link.gif"));
		
		refreshAction = new Action() {
			public void run() {
				hookRefreshAction();
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Refresh the docker container list");
		refreshAction.setImageDescriptor(Activator
				.getImageDescriptor("icons/refresh.gif"));

		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				//showMessage("Double-click detected on " + obj.toString());
			}
		};
	}

	private void hookRefreshAction(){
		if(getClient() != null){
			try {
				List<Container> containers = getClient().listContainers(ListContainersParam.allContainers(true));
				List<DockerContainerElement> containerElements = DomainHelper.convert(containers);
				viewer.setInput(containerElements);
			} catch (DockerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		viewer.refresh(true);
	}
	
	private void hookRenameAction() {
		DockerContainerElement elem = getSelectedElement();
		if(elem != null && getClient() != null){
			IInputValidator validator = new IInputValidator() {
				public String isValid(String newText) {
					if (newText.contains("\\") || newText.contains(":") || newText.contains("/")
							)
						return newText + " is not a valid Docker container's name.";
					else
						return null;
				}
			};
			InputDialog dialog = new InputDialog(viewer.getControl().getShell(), "Rename an existing container",
					"New name:", elem.getNames().get(0)+"2",
					validator);
			if (dialog.open() == Window.OK) {
				String newName = dialog.getValue();
				//@ TODO docker client does not provide renaming API now.
			} 
		}
	}
	
	private void hookSingleClickAction() {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent arg0) {
				DockerContainerElement elem = getSelectedElement();
				String status = null;
				if(elem != null)
					status = elem.getStatus();
				startAction.setEnabled(status != null && status.startsWith("Exited"));
				stopAction.setEnabled(status != null && status.startsWith("Up"));
				unpauseAction.setEnabled(status != null && status.startsWith("Paused"));
				pauseAction.setEnabled(status != null && status.startsWith("Up"));
				showConsoleAction.setEnabled(status != null && status.startsWith("Up"));
				showEnvAction.setEnabled(status != null && status.startsWith("Up"));
				showLinkAction.setEnabled(status != null && status.startsWith("Up"));
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
		String[] titles = { "CONTAINER ID", "NAMES", "IMAGE", "COMMAND", "CREATED",
				"STATUS", "PORTS", };
		int[] bounds = { 150, 200, 200, 150, 200, 200, 200 };
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DockerContainerElement elem = (DockerContainerElement) element;
				return elem.getShortId();
			}
		});
		col = createTableViewerColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DockerContainerElement elem = (DockerContainerElement) element;
				return elem.getNamesAsString();
			}
		});
		col = createTableViewerColumn(titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DockerContainerElement elem = (DockerContainerElement) element;
				return elem.getImage();
			}
		});
		col = createTableViewerColumn(titles[3], bounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DockerContainerElement elem = (DockerContainerElement) element;
				return elem.getCommand();
			}
		});
		col = createTableViewerColumn(titles[4], bounds[4], 4);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DockerContainerElement elem = (DockerContainerElement) element;
				return elem.getCreated();
			}
		});
		col = createTableViewerColumn(titles[5], bounds[5], 5);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DockerContainerElement elem = (DockerContainerElement) element;
				return elem.getStatus();
			}
		});
		col = createTableViewerColumn(titles[6], bounds[6], 6);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DockerContainerElement elem = (DockerContainerElement) element;
				return elem.getPortsAsString();
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
	
	private DockerContainerElement getSelectedElement() {
		if (viewer.getSelection() == null
				|| !(viewer.getSelection() instanceof IStructuredSelection))
			return null;

		Object obj = ((IStructuredSelection) viewer.getSelection())
				.getFirstElement();
		if(obj == null || !(obj instanceof DockerContainerElement))
			return null;
		
		return (DockerContainerElement)obj;
	}
}