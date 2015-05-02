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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;


public class DockerfileWizardPage extends WizardPage {
	
	protected FileFieldEditor dockerfileEditor;
	protected StringFieldEditor targetImageNameEditor;
	
	public DockerfileWizardPage(String pageName) {
		super(pageName);
		setTitle(pageName); //NON-NLS-1
		setDescription("Build an image from a Dockerfile"); //NON-NLS-1
	}
	
	 /* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createLinkTarget()
	 */
	protected void createLinkTarget() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#getInitialContents()
	 */
	protected InputStream getInitialContents() {
		try {
			return new FileInputStream(new File(dockerfileEditor.getStringValue()));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#getNewFileLabel()
	 */
	protected String getNewFileLabel() {
		return "New File Name:"; //NON-NLS-1
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#validateLinkedResource()
	 */
	protected IStatus validateLinkedResource() {
		return new Status(IStatus.OK, "cn.dockerfoundry.ide.eclipse.explorer.ui", IStatus.OK, "", null); //NON-NLS-1 //NON-NLS-2
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		composite.setLayoutData(fileSelectionData);

		GridLayout fileSelectionLayout = new GridLayout();
		fileSelectionLayout.numColumns = 3;
		fileSelectionLayout.makeColumnsEqualWidth = false;
		fileSelectionLayout.marginWidth = 0;
		fileSelectionLayout.marginHeight = 0;
		composite.setLayout(fileSelectionLayout);
        setControl(composite);

		
		dockerfileEditor = new FileFieldEditor("dockerfile","Dockerfile: ",composite); //NON-NLS-1 //NON-NLS-2
		dockerfileEditor.getTextControl(composite).addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				
			}
		});		
		GridData layoutData =  new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		dockerfileEditor.getLabelControl(composite).setLayoutData(layoutData);
		layoutData =  new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 1;
		dockerfileEditor.getTextControl(composite).setLayoutData(layoutData);
//		dockerfileEditor.fillIntoGrid(composite, 4);
		targetImageNameEditor = new StringFieldEditor("t","Repository name (and optionally a tag) to be applied to the resulting image in case of success: ",composite);
		layoutData =  new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		targetImageNameEditor.getLabelControl(composite).setLayoutData(layoutData);
		layoutData =  new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		targetImageNameEditor.getTextControl(composite).setLayoutData(layoutData);
//		targetImageNameEditor.fillIntoGrid(composite, 2);
		composite.moveAbove(null);
		addListener();		
	}
	
	private void addListener(){
	
	}
	
	public String getDockerfile(){
		return this.dockerfileEditor.getStringValue();
	}

	public String getTargetImageName() {
		return this.targetImageNameEditor.getStringValue();
	}
}
