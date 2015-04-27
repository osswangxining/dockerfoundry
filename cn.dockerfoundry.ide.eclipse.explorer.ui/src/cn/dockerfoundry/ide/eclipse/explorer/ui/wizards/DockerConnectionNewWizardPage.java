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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerConnectionElement;

import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.Info;


public class DockerConnectionNewWizardPage extends WizardPage {
	
	protected StringFieldEditor editor;
	protected Button connSetting;
	protected Button[] optionsButton;
	protected Group group1;
	protected FileFieldEditor unixSocketFileFieldEditor;
	protected StringFieldEditor hostEditor;
	protected Button authenticationButton;
	protected FileFieldEditor authenticationFileFieldEditor;
	protected Button testConnection;

	public DockerConnectionNewWizardPage(String pageName) {
		super(pageName);
		setTitle(pageName); //NON-NLS-1
		setDescription("Select the connection mode to talk with Docker"); //NON-NLS-1
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
			return new FileInputStream(new File(editor.getStringValue()));
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

		
		editor = new StringFieldEditor("connName","Docker Connection Name: ",composite); //NON-NLS-1 //NON-NLS-2
		editor.getTextControl(composite).addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				String connName = DockerConnectionNewWizardPage.this.editor.getStringValue();
				System.out.println("connName:" + connName);
			}
		});
		
//		Group group0 = new Group(composite, SWT.NULL);
//		GridLayout layout = new GridLayout();
//	    layout.numColumns = 1;
//	    group0.setLayout(layout);
	    
		connSetting = new Button(composite, SWT.CHECK);
		connSetting.setSelection(true);
		connSetting.setText("Use default connection settings");
		GridData gridData =  new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		connSetting.setLayoutData(gridData);
	
		
		group1 = new Group(composite, SWT.NULL);
	    group1.setText("Connection Setting");
	    gridData =  new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		group1.setLayoutData(gridData);
		
	    optionsButton = new Button[2];
	    optionsButton[0] = new Button(group1, SWT.RADIO);
	    optionsButton[0] .setText("Unix Socket");
	    optionsButton[0].setSelection(true);
	    gridData =  new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		optionsButton[0].setLayoutData(gridData);
	    
	    unixSocketFileFieldEditor = new FileFieldEditor("Location","      Location: ",group1); //NON-NLS-1 //NON-NLS-2
	    unixSocketFileFieldEditor.getTextControl(group1).addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				String location = DockerConnectionNewWizardPage.this.unixSocketFileFieldEditor.getStringValue();
				System.out.println("Location:" + location);
			}
		});
	    optionsButton[1] = new Button(group1, SWT.RADIO);
	    optionsButton[1] .setText("HTTPS Connection");
	    gridData =  new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		optionsButton[1].setLayoutData(gridData);
	    hostEditor = new StringFieldEditor("host","      Host: ",group1); //NON-NLS-1 //NON-NLS-2
	    hostEditor.getTextControl(group1).addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				String host = DockerConnectionNewWizardPage.this.hostEditor.getStringValue();
				System.out.println("host:" + host);
			}
		});
	    authenticationButton = new Button(group1, SWT.CHECK);
	    authenticationButton.setSelection(true);
	    authenticationButton.setText("Enable authentication");
	    gridData =  new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		authenticationButton.setLayoutData(gridData);
	    authenticationFileFieldEditor = new FileFieldEditor("Path","     Path: ",group1); //NON-NLS-1 //NON-NLS-2
	    authenticationFileFieldEditor.getTextControl(group1).addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				String path = DockerConnectionNewWizardPage.this.authenticationFileFieldEditor.getStringValue();
				System.out.println("Path:" + path);
			}
		});
	    testConnection = new Button(composite, SWT.PUSH);
	    testConnection.setText("Test Connection");
	    testConnection.setAlignment(SWT.RIGHT);
	    testConnection.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent arg0) {
				try {
					String conn = testConnection();
					StringBuilder sb = new StringBuilder("Successfully connect to Docker with the following info:\n");
					sb.append(conn);
					MessageDialog.openInformation(getShell(), "Docker Connection", sb.toString());
				} catch (Exception e) {
					e.printStackTrace();
					StringBuilder sb = new StringBuilder("Failed to connect to Docker with the following reason:\n");
					sb.append(e.getLocalizedMessage());
					MessageDialog.openError(getShell(), "Docker Connection", sb.toString());
				} 
				
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				keyPressed(arg0);				
			}});
		composite.moveAbove(null);
		
		checkUseUnixSocket();
		checkUseDefaultConnSetting();
		
		addListener();		
	}
	
	private void addListener(){
		connSetting.addSelectionListener(new SelectionListener() {			
			@Override
			public void widgetSelected(SelectionEvent event) {
				if(connSetting.getSelection())
					checkUseDefaultConnSetting();
				else{
					checkUseDefaultConnSetting();
					checkUseUnixSocket();
					checkAuthentication();
				}				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				checkUseDefaultConnSetting();		
				checkUseUnixSocket();
				checkAuthentication();
			}
		});
		
		optionsButton[0].addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				checkUseUnixSocket();				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				checkUseUnixSocket();
			}
		});
		
		authenticationButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				checkAuthentication();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				checkAuthentication();
			}
		});
	}
	private void checkUseDefaultConnSetting(){
		for (int i = 0; i < optionsButton.length; i++) {
			optionsButton[i].setEnabled(!connSetting.getSelection());
		}
		unixSocketFileFieldEditor.setEnabled(!connSetting.getSelection(), group1);
		hostEditor.setEnabled(!connSetting.getSelection(), group1);
		authenticationButton.setEnabled(!connSetting.getSelection());
		authenticationFileFieldEditor.setEnabled(!connSetting.getSelection(), group1);
	}
	
	private void checkUseUnixSocket(){
		unixSocketFileFieldEditor.setEnabled(optionsButton[0].getSelection(), group1);
		hostEditor.setEnabled(!optionsButton[0].getSelection(), group1);
		authenticationButton.setEnabled(!optionsButton[0].getSelection());
		authenticationFileFieldEditor.setEnabled(!optionsButton[0].getSelection(), group1);
	}
	
	private void checkAuthentication() {
		authenticationFileFieldEditor.setEnabled(
				authenticationButton.getSelection()
						&& authenticationButton.getEnabled(), group1);
	}

	private String getConnName(){
		return this.editor.getStringValue();
	}
	public DockerConnectionElement getDockerConnection(){
		DockerConnectionElement elem = new DockerConnectionElement();
		elem.setName(getConnName());
		elem.setUseDefault(this.connSetting.getSelection());
		elem.setUseUnixSocket(this.optionsButton[0].getSelection());
		elem.setSocketPath(this.unixSocketFileFieldEditor.getStringValue());
		elem.setUseHTTPS(this.optionsButton[1].getSelection());
		elem.setHost(this.hostEditor.getStringValue());
		elem.setEnableAuth(this.authenticationButton.getSelection());
		elem.setAuthPath(this.authenticationFileFieldEditor.getStringValue());
		return elem;
	}
	
	private String testConnection() throws DockerException, InterruptedException, DockerCertificateException{
		DockerConnectionElement elem = getDockerConnection();
		DockerClient client = elem.getDockerClient();
		if(client == null)
			return null;
		else{
			Info info = client.info();
			return info.toString();
		}
	}
}
