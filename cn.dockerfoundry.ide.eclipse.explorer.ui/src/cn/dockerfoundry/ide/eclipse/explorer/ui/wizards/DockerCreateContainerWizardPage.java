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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.spotify.docker.client.DockerClient;

public class DockerCreateContainerWizardPage extends WizardPage {

	private Text txtName;
	private DockerClient client;

	public DockerCreateContainerWizardPage(String pageName, DockerClient client) {
		super(pageName);
		setTitle(pageName); // NON-NLS-1
		setDescription("Search the Docker Hub for images"); // NON-NLS-1
		this.client = client;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createLinkTarget()
	 */
	protected void createLinkTarget() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#getNewFileLabel()
	 */
	protected String getNewFileLabel() {
		return "New File Name:"; // NON-NLS-1
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.dialogs.WizardNewFileCreationPage#validateLinkedResource()
	 */
	protected IStatus validateLinkedResource() {
		return new Status(IStatus.OK,
				"cn.dockerfoundry.ide.eclipse.explorer.ui", IStatus.OK, "",
				null); // NON-NLS-1 //NON-NLS-2
	}

	@Override
	public void createControl(Composite parent) {
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
		setControl(composite);

		final Label label = new Label(composite, SWT.NONE);
		label.setText("Please input the name if you do not want to use the random name:");

		txtName = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData layoutData = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 1;
		txtName.setLayoutData(layoutData);
		composite.moveAbove(null);

		addListener();
	}

	private void addListener() {

	}

	public DockerClient getClient() {
		return client;
	}

	public void setClient(DockerClient client) {
		this.client = client;
	}

	public String getName() {
		return this.txtName.getText();
	}
}
