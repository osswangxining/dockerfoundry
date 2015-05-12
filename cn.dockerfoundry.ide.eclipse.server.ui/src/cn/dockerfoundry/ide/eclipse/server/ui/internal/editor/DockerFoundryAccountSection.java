/*******************************************************************************
 * Copyright (c) 2012, 2014 Pivotal Software, Inc. 
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
package cn.dockerfoundry.ide.eclipse.server.ui.internal.editor;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

import cn.dockerfoundry.ide.eclipse.server.core.internal.DockerFoundryBrandingExtensionPoint;
import cn.dockerfoundry.ide.eclipse.server.core.internal.DockerFoundryConstants;
import cn.dockerfoundry.ide.eclipse.server.core.internal.DockerFoundryServer;
import cn.dockerfoundry.ide.eclipse.server.core.internal.CloudServerEvent;
import cn.dockerfoundry.ide.eclipse.server.core.internal.CloudServerListener;
import cn.dockerfoundry.ide.eclipse.server.core.internal.ServerEventHandler;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.DockerFoundryURLNavigation;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.CloudUiUtil;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.Messages;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.UpdatePasswordDialog;

/**
 * @author Andy Clement
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @author Steffen Pingel
 * @author Terry Denney
 */
public class DockerFoundryAccountSection extends ServerEditorSection implements CloudServerListener {

	private DockerFoundryServer cfServer;

	private Text dockerfileEditor;

	private Text containerNameText;

	private String sectionTitle;

	private Text urlText;

	private Text containerVolumeText;

	private Text containerLinksText;

	private Label validateLabel;

	// private CloudUrlWidget urlWidget;

	// private Combo urlCombo;

	public DockerFoundryAccountSection() {
	}

	public void update() {
		if (cfServer.getUsername() != null && dockerfileEditor != null && !cfServer.getUsername().equals(dockerfileEditor.getText())) {
			dockerfileEditor.setText(cfServer.getUsername());
		}
		if (cfServer.getPassword() != null && containerNameText != null
				&& !cfServer.getPassword().equals(containerNameText.getText())) {
			containerNameText.setText(cfServer.getPassword());
		}
		if (cfServer.getUrl() != null
				&& urlText != null
				&& !CloudUiUtil.getDisplayTextFromUrl(cfServer.getUrl(), cfServer.getServer().getServerType().getId())
						.equals(urlText.getText())) {
			urlText.setText(CloudUiUtil.getDisplayTextFromUrl(cfServer.getUrl(), cfServer.getServer().getServerType()
					.getId()));
		}
//		if (cfServer.hasCloudSpace()) {
//			if (cfServer.getCloudFoundrySpace() != null && cfServer.getCloudFoundrySpace().getOrgName() != null
//					&& orgText != null && !cfServer.getCloudFoundrySpace().getOrgName().equals(orgText.getText())) {
//				orgText.setText(cfServer.getCloudFoundrySpace().getOrgName());
//			}
//			if (cfServer.getCloudFoundrySpace() != null && cfServer.getCloudFoundrySpace().getSpaceName() != null
//					&& spaceText != null && !cfServer.getCloudFoundrySpace().getSpaceName().equals(spaceText.getText())) {
//				spaceText.setText(cfServer.getCloudFoundrySpace().getSpaceName());
//			}
//		}

	}

	@Override
	public void createSection(Composite parent) {
		super.createSection(parent);

		FormToolkit toolkit = getFormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
		section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		section.setText(sectionTitle);

		Composite composite = toolkit.createComposite(section);
		section.setClient(composite);

		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite topComposite = new Composite(composite, SWT.NONE);
		topComposite.setLayout(new GridLayout(2, false));
		topComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label dockerfileLabel = toolkit.createLabel(topComposite, "Dockerfile: ", SWT.NONE);
		dockerfileLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		dockerfileLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		dockerfileEditor = toolkit.createText(topComposite, ""); //$NON-NLS-1$
		dockerfileEditor.setEditable(false);
		dockerfileEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		dockerfileEditor.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		if (cfServer.getDockerfile() != null) {
			dockerfileEditor.setText(cfServer.getDockerfile());
		}
		dockerfileEditor.addModifyListener(new DataChangeListener(DataType.EMAIL));

		Label containerNameLabel = toolkit.createLabel(topComposite, "Container Name:", SWT.NONE);
		containerNameLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		containerNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		containerNameText = toolkit.createText(topComposite, ""); //$NON-NLS-1$
		containerNameText.setEditable(false);
		containerNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		containerNameText.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		if (cfServer.getDockerContainerName() != null) {
			containerNameText.setText(cfServer.getDockerContainerName());
		}
		containerNameText.addModifyListener(new DataChangeListener(DataType.PASSWORD));

		Label label = toolkit.createLabel(topComposite, Messages.COMMONTXT_URL);
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		urlText = toolkit.createText(topComposite, "", SWT.NONE); //$NON-NLS-1$
		urlText.setEditable(false);
		urlText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		urlText.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		if (cfServer.getUrl() != null) {
			urlText.setText(CloudUiUtil.getDisplayTextFromUrl(cfServer.getUrl(), cfServer.getServer().getServerType()
					.getId()));
		}

		Label volumeLabel = toolkit.createLabel(topComposite, "ADD VOLUME:", SWT.NONE);
		volumeLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		volumeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		containerVolumeText = toolkit.createText(topComposite, "", SWT.NONE); //$NON-NLS-1$
		containerVolumeText.setEditable(false);
		containerVolumeText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		containerVolumeText.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		if (cfServer.getDockerVolume() != null) {
			containerVolumeText.setText(cfServer.getDockerVolume());
		}
//		if (cfServer.getCloudFoundrySpace() != null && cfServer.getCloudFoundrySpace().getOrgName() != null) {
//			orgText.setText(cfServer.getCloudFoundrySpace().getOrgName());
//		}

		Label containerLinksLabel = toolkit.createLabel(topComposite, "Container Links:", SWT.NONE);
		containerLinksLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		containerLinksLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		containerLinksText = toolkit.createText(topComposite, "", SWT.NONE); //$NON-NLS-1$
		containerLinksText.setEditable(false);
		containerLinksText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		containerLinksText.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		if (cfServer.getDockerContainerLinks() != null) {
			containerLinksText.setText(cfServer.getDockerContainerLinks());
		}
//		if (cfServer.getCloudFoundrySpace() != null && cfServer.getCloudFoundrySpace().getSpaceName() != null) {
//			spaceText.setText(cfServer.getCloudFoundrySpace().getSpaceName());
//		}

		// urlWidget = new CloudUrlWidget(cfServer);
		// urlWidget.createControls(topComposite);
		// urlWidget.getUrlCombo().addModifyListener(new
		// DataChangeListener(DataType.URL));
		//
		final Composite buttonComposite = toolkit.createComposite(composite);

		buttonComposite.setLayout(new GridLayout(4, false));
		GridDataFactory.fillDefaults().align(SWT.END, SWT.FILL).grab(true, false).applyTo(buttonComposite);

		final Composite validateComposite = toolkit.createComposite(composite);
		validateComposite.setLayout(new GridLayout(1, false));
		validateComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		validateLabel = toolkit.createLabel(validateComposite, "", SWT.NONE); //$NON-NLS-1$
		validateLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		toolkit.paintBordersFor(topComposite);
		section.setExpanded(true);

		ServerEventHandler.getDefault().addServerListener(this);
	}

	

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		// String serviceName = null;
		if (server != null) {
			cfServer = (DockerFoundryServer) server.loadAdapter(DockerFoundryServer.class, null);
			update();
			// serviceName =
			// CloudFoundryBrandingExtensionPoint.getServiceName(server.getServerType().getId());
		}
		// if (serviceName == null) {
		sectionTitle = Messages.COMMONTXT_ACCOUNT_INFO;

		// }
		// else {
		// sectionTitle = serviceName + " Account";
		// }
	}

	private class DataChangeListener implements ModifyListener {

		private String newValue;

		private String oldValue;

		private final DataType type;

		private DataChangeListener(DataType type) {
			this.type = type;
		}

		public void modifyText(ModifyEvent e) {
			switch (type) {
			case EMAIL:
				oldValue = cfServer.getUsername();
				newValue = dockerfileEditor.getText();
				break;
			case PASSWORD:
				oldValue = cfServer.getPassword();
				newValue = containerNameText.getText();
				break;
			// case URL:
			// Combo urlCombo = urlWidget.getUrlCombo();
			// int index = urlCombo.getSelectionIndex();
			// oldValue = cfServer.getUrl();
			// newValue = index < 0? null:
			// CloudUiUtil.getUrlFromDisplayText(urlCombo.getItem(index));
			// break;
			}

			execute(new AbstractOperation("CloudFoundryServerUpdate") { //$NON-NLS-1$

				@Override
				public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
					updateServer(newValue);
					return Status.OK_STATUS;
				}

				@Override
				public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
					updateServer(newValue);
					return Status.OK_STATUS;
				}

				@Override
				public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
					updateServer(oldValue);
					return Status.OK_STATUS;
				}

				private void updateServer(String value) {
					switch (type) {
					case EMAIL:
						if (!value.equals(cfServer.getUsername())) {
							cfServer.setUsername(value);
						}
						updateTextField(value, dockerfileEditor);
						break;
					case PASSWORD:
						if (!value.equals(cfServer.getPassword())) {
							cfServer.setPassword(value);
						}
						updateTextField(value, containerNameText);
						break;
					// case URL:
					// cfServer.setUrl(value);
					// updateComboBox(value, urlWidget.getUrlCombo());
					// break;
					}
				}

				private void updateTextField(String input, Text text) {
					if (!text.getText().equals(input)) {
						text.setText(input == null ? "" : input); //$NON-NLS-1$
					}
				}

				// private void updateComboBox(String input, Combo combo) {
				// int index = combo.getSelectionIndex();
				// if (index < 0) {
				// if (input == null) {
				// return;
				// }
				// } else if (combo.getItem(index).equals(input)) {
				// return;
				// }
				//
				// for(int i=0; i<combo.getItemCount(); i++) {
				// if (combo.getItem(i).equals(input)) {
				// combo.select(i);
				// return;
				// }
				// }
				// combo.deselectAll();
				// }
			});
		}
	}

	private enum DataType {
		EMAIL, PASSWORD, URL
	}

	public void serverChanged(CloudServerEvent event) {
		if (event.getType() == CloudServerEvent.EVENT_UPDATE_PASSWORD) {
			cfServer = event.getServer();

			Display.getDefault().syncExec(new Runnable() {

				public void run() {
					if (containerNameText != null && !containerNameText.isDisposed()
							&& !containerNameText.getText().equals(cfServer.getPassword())) {
						containerNameText.setText(cfServer.getPassword());
					}
				}
			});
		}
	}

	@Override
	public void dispose() {
		ServerEventHandler.getDefault().removeServerListener(this);
	}

}
