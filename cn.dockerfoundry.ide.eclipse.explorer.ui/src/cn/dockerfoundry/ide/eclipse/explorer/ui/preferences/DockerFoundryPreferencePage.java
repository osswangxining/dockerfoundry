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

package cn.dockerfoundry.ide.eclipse.explorer.ui.preferences;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import cn.dockerfoundry.ide.eclipse.explorer.ui.Activator;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.AuthConfig;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class DockerFoundryPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public DockerFoundryPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Specify the connection information to Docker Registry");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		final StringFieldEditor serverAddressEditor = new StringFieldEditor(
				PreferenceConstants.P_SERVERADDRESS, "&Server Address:",
				getFieldEditorParent());
		final StringFieldEditor userNameEditor = new StringFieldEditor(
				PreferenceConstants.P_USERNAME, "&User Name:",
				getFieldEditorParent());
		final StringFieldEditor passwordEditor = new StringFieldEditor(
				PreferenceConstants.P_PASSWORD, "&Password:",
				getFieldEditorParent());
		final StringFieldEditor emailEditor = new StringFieldEditor(
				PreferenceConstants.P_EMAIL, "&Email:", getFieldEditorParent());
		addField(serverAddressEditor);
		addField(userNameEditor);
		addField(passwordEditor);
		addField(emailEditor);

		/*Button testConnection = new Button(getFieldEditorParent(), SWT.PUSH);
		testConnection.setText("Test Connection");
		testConnection.setAlignment(SWT.RIGHT);
		testConnection.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				try {
					AuthConfig authConfig = AuthConfig.builder()
							.email(serverAddressEditor.getStringValue())
							.username(userNameEditor.getStringValue())
							.password(passwordEditor.getStringValue()).build();
					final DefaultDockerClient.Builder builder = DefaultDockerClient.fromEnv();
				    builder.readTimeoutMillis(120000);
					DockerClient client = builder.build();
					int status = client.auth(authConfig);
					StringBuilder sb = new StringBuilder();
					if(status == 200)
						sb = new StringBuilder(
							"Successfully connect to Docker Registry [" + serverAddressEditor.getStringValue() +"]");
					else
						sb = new StringBuilder(
								"Failed to connect to Docker Registry [" + serverAddressEditor.getStringValue() +"]");
					MessageDialog.openInformation(getShell(),
							"Docker Connection", sb.toString());
				} catch (Exception e) {
					e.printStackTrace();
					StringBuilder sb = new StringBuilder(
							"Failed to connect to Docker Registry [" + serverAddressEditor.getStringValue() +"]");
					sb.append(e.getLocalizedMessage());
					MessageDialog.openError(getShell(), "Docker Connection",
							sb.toString());
				}

			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				keyPressed(arg0);
			}
		});*/
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}