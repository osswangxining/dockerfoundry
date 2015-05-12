/*******************************************************************************
 * Copyright (c) 2012, 2015 Pivotal Software, Inc. and IBM Corporation 
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
 *     Pivotal Software, Inc. - initial API and implementation
 ********************************************************************************/
package cn.dockerfoundry.ide.eclipse.server.ui.internal.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;

import cn.dockerfoundry.ide.eclipse.server.core.internal.DockerFoundryPlugin;
import cn.dockerfoundry.ide.eclipse.server.core.internal.DockerFoundryServer;
import cn.dockerfoundry.ide.eclipse.server.core.internal.DockerApplicationService;
import cn.dockerfoundry.ide.eclipse.server.core.internal.client.DockerFoundryApplicationModule;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.DockerFoundryImages;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.ICoreRunnable;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.Messages;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.editor.ServiceViewColumn;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.editor.ServiceViewerConfigurator;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.editor.ServiceViewerSorter;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.editor.ServicesTreeLabelProvider;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.editor.TreeContentProvider;

public class DockerFoundryApplicationServicesWizardPage extends PartsWizardPage {

	// This page is optional and can be completed at any time
	private final boolean canFinish = true;

	private final String serverTypeId;

	private final DockerFoundryServer cloudServer;

	private CheckboxTableViewer servicesViewer;

	private static final String DESCRIPTION = Messages.DockerFoundryApplicationServicesWizardPage_TEXT_BIND_DESCRIP;

	/**
	 * Services, either existing or new, that a user has checked for binding.
	 */
	private final Set<String> selectedServicesToBind = new HashSet<String>();

	/**
	 * This is a list of services to add to the CF server. This may not
	 * necessarily match all the services a user has selected to bind to an
	 * application, as a user may add a service, but uncheck it for binding.
	 */
	private final Set<String> servicesToAdd = new HashSet<String>();

	/**
	 * All services both existing and added, used to refresh the input of the
	 * viewer
	 */
	private final Map<String, DockerApplicationService> allServices = new HashMap<String, DockerApplicationService>();

	private final ApplicationWizardDescriptor descriptor;

	public DockerFoundryApplicationServicesWizardPage(DockerFoundryServer cloudServer,
			DockerFoundryApplicationModule module, ApplicationWizardDescriptor descriptor) {
		super(Messages.COMMONTXT_SERVICES, Messages.DockerFoundryApplicationServicesWizardPage_TEXT_SERVICE_SELECTION, null);
		this.cloudServer = cloudServer;
		this.serverTypeId = module.getServerTypeId();
		this.descriptor = descriptor;
	}

	public boolean isPageComplete() {
		return canFinish;
	}

	public void createControl(Composite parent) {

		setDescription(DESCRIPTION);
		ImageDescriptor banner = DockerFoundryImages.getWizardBanner(serverTypeId);
		if (banner != null) {
			setImageDescriptor(banner);
		}

		Composite tableArea = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(tableArea);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableArea);

		Composite toolBarArea = new Composite(tableArea, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(toolBarArea);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(toolBarArea);

		Label label = new Label(toolBarArea, SWT.NONE);
		GridDataFactory.fillDefaults().grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(label);
		label.setText(Messages.DockerFoundryApplicationServicesWizardPage_LABEL_SELECT_SERVICE);

		Table table = new Table(tableArea, SWT.BORDER | SWT.SINGLE | SWT.CHECK);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(table);

		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar bar = toolBarManager.createControl(toolBarArea);
		GridDataFactory.fillDefaults().align(SWT.END, SWT.BEGINNING).grab(true, false).applyTo(bar);

		servicesViewer = new CheckboxTableViewer(table);

		servicesViewer.setContentProvider(new TreeContentProvider());
		servicesViewer.setLabelProvider(new ServicesTreeLabelProvider(servicesViewer) {

			protected Image getColumnImage(DockerApplicationService service, ServiceViewColumn column) {
				
				return null;
			}

		});
		servicesViewer.setSorter(new ServiceViewerSorter(servicesViewer) {

			@Override
			protected int compare(DockerApplicationService service1, DockerApplicationService service2, ServiceViewColumn sortColumn) {
			
				return super.compare(service1, service2, sortColumn);
			}

		});

		new ServiceViewerConfigurator().enableAutomaticViewerResizing().configureViewer(servicesViewer);

		servicesViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				Object[] services = servicesViewer.getCheckedElements();
				if (services != null) {
					selectedServicesToBind.clear();
					for (Object obj : services) {
						DockerApplicationService service = (DockerApplicationService) obj;
						selectedServicesToBind.add(service.getName());
					}
					setServicesToBindInDescriptor();
				}
			}
		});

		Action addServiceAction = new Action(Messages.COMMONTXT_ADD_SERVICE, DockerFoundryImages.NEW_SERVICE) {

			public void run() {
				// Do not create the service right away.
				boolean deferAdditionOfService = true;
				
				DockerFoundryServiceWizard wizard = new DockerFoundryServiceWizard(cloudServer, deferAdditionOfService);
				WizardDialog dialog = new WizardDialog(getShell(), wizard);
				wizard.setParent(dialog);
				dialog.setPageSize(900, 600);
				dialog.setBlockOnOpen(true);
				
				if (dialog.open() == Window.OK) {
					// This cloud service does not yet exist. It will be created
					// outside of the wizard
					List<List<ServiceInstance>> addedService = wizard.getServices();
					if (addedService != null) {
						List<DockerApplicationService> _addedServiceList = new ArrayList<DockerApplicationService>();
						for (List<ServiceInstance> list : addedService) {
							for (ServiceInstance serviceInstance : list) {								
								DockerApplicationService e = new DockerApplicationService();
								e.setName(serviceInstance.getName());
								e.setLinkName(serviceInstance.getUserDefinedName());
								_addedServiceList.add(e );
							}
						}
						addServices(_addedServiceList);
					}
				}
			}

			public String getToolTipText() {
				return Messages.DockerFoundryApplicationServicesWizardPage_TEXT_TOOLTIP;
			}
		};
		toolBarManager.add(addServiceAction);

		toolBarManager.update(true);

		setControl(tableArea);
		setInput();
	}

	/**
	 * Also automatically selects the added service to be bound to the
	 * application.
	 * @param service that was added and will also be automatically selected to
	 * be bound to the application.
	 */
	protected void addServices(List<DockerApplicationService> services) {
		if (services == null || services.size() == 0) {
			return;
		}
		
		for(DockerApplicationService service : services) {
				allServices.put(service.getName(), service);

				servicesToAdd.add(service.getName());

				selectedServicesToBind.add(service.getName());			
		}

		setServicesToBindInDescriptor();
		setServicesToCreateInDescriptor();
		setBoundServiceSelectionInUI();
	}

	protected void setInput() {

		ICoreRunnable runnable = new ICoreRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {

				try {
					List<DockerApplicationService> existingServices = cloudServer.getBehaviour().getServices(monitor);

					// Clear only after retrieving an update list without errors
					allServices.clear();
					servicesToAdd.clear();
					selectedServicesToBind.clear();

					// Only populate from the existing deployment info if
					// retrieving list of existing services was successful.
					// That way the services in the deployment info can be
					// verified if they exist, or if they need to be created.
					populateServicesFromDeploymentInfo();

					// Update the mapping with existing Cloud Services. Local
					// services
					// (services that have not yet been created) will be
					// unaffected by this.
					if (existingServices != null) {
						for (DockerApplicationService actualService : existingServices) {
							if (actualService != null) {
								allServices.put(actualService.getName(), actualService);
							}
						}
					}

					// At this stage, since the existing Cloud Service mapping
					// has been updated
					// above, any remaining Local cloud services can be assumed
					// to not exist and
					// will require being created. Only create services IF they
					// are to be bound to the app.
					for (String name : selectedServicesToBind) {
						DockerApplicationService service = allServices.get(name);
						if (service instanceof DockerApplicationService) {
							servicesToAdd.add(name);
						}
					}

					setServicesToCreateInDescriptor();

					// Refresh UI
					Display.getDefault().asyncExec(new Runnable() {

						public void run() {
							// Clear any info in the dialogue
							setMessage(null);
							update(false, Status.OK_STATUS);

							setBoundServiceSelectionInUI();
						}

					});
				}
				catch (final CoreException e) {

					Display.getDefault().asyncExec(new Runnable() {

						public void run() {

							update(false,
									DockerFoundryPlugin
											.getErrorStatus(
													NLS.bind(Messages.DockerFoundryApplicationServicesWizardPage_ERROR_VERIFY_SERVICE,
															e.getMessage()), e));

						}
					});
				}
			}
		};
		runAsynchWithWizardProgress(runnable, Messages.DockerFoundryApplicationServicesWizardPage_TEXT_VERIFY_SERVICE_PROGRESS);

	}

	protected void populateServicesFromDeploymentInfo() {

//		List<DockerApplicationService> servicesToBind = descriptor.getDeploymentInfo().getServices();
//
//		if (servicesToBind != null) {
//			for (DockerApplicationService service : servicesToBind) {
//				allServices.put(service.getName(), service);
//
//				selectedServicesToBind.add(service.getName());
//			}
//		}
//		setServicesToBindInDescriptor();
	}

	protected void setBoundServiceSelectionInUI() {
		servicesViewer.setInput(allServices.values().toArray(new DockerApplicationService[] {}));
		List<DockerApplicationService> checkedServices = getServicesToBindAsCloudServices();
		servicesViewer.setCheckedElements(checkedServices.toArray());
	}

	protected List<DockerApplicationService> getServicesToBindAsCloudServices() {
		List<DockerApplicationService> servicesToBind = new ArrayList<DockerApplicationService>();
		for (String serviceName : selectedServicesToBind) {
			DockerApplicationService service = allServices.get(serviceName);
			if (service != null) {
				servicesToBind.add(service);
			}
		}
		return servicesToBind;
	}

	protected void setServicesToBindInDescriptor() {
		List<DockerApplicationService> servicesToBind = getServicesToBindAsCloudServices();

//		descriptor.getDeploymentInfo().setServices(servicesToBind);
	}

	protected void setServicesToCreateInDescriptor() {
		List<DockerApplicationService> toCreate = new ArrayList<DockerApplicationService>();
		for (String serviceName : servicesToAdd) {
			DockerApplicationService service = allServices.get(serviceName);
			if (service != null) {
				toCreate.add(service);
			}
		}

//		descriptor.setCloudServicesToCreate(toCreate);
	}

	public void setErrorText(String newMessage) {
		// Clear the message
		setMessage(""); //$NON-NLS-1$
		super.setErrorMessage(newMessage);
	}

	public void setMessageText(String newMessage) {
		setErrorMessage(""); //$NON-NLS-1$
		super.setMessage(newMessage);
	}

}
