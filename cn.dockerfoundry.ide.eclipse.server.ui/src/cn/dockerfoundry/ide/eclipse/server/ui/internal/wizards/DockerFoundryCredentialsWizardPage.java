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
package cn.dockerfoundry.ide.eclipse.server.ui.internal.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import cn.dockerfoundry.ide.eclipse.server.core.internal.DockerFoundryServer;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.DockerFoundryCredentialsPart;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.Messages;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.ValidationEventHandler;

/**
 * Credentials wizard page used to prompt users for credentials in case an
 * EXISTING server instance can no longer connect with the existing credentials.
 * This wizard page is not used in the new server wizard when creating a new
 * server instance.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @author Steffen Pingel
 * @author Terry Denney
 */
public class DockerFoundryCredentialsWizardPage extends WizardPage {

	private final DockerFoundryCredentialsPart credentialsPart;

//	private CloudServerSpacesDelegate cloudServerSpaceDelegate;

	private ValidationEventHandler validationNotifier;

	protected DockerFoundryCredentialsWizardPage(DockerFoundryServer server) {
		super(server.getServer().getName() + Messages.DockerFoundryCredentialsWizardPage_TEXT_CRENDENTIAL);
//		cloudServerSpaceDelegate = new CloudServerSpacesDelegate(server);
		WizardStatusHandler wizardUpdateHandler = new WizardPageStatusHandler(this);

		 validationNotifier = new ValidationEventHandler(new CredentialsWizardValidator(server));
		 validationNotifier.addStatusHandler(wizardUpdateHandler);

		credentialsPart = new DockerFoundryCredentialsPart(server, this);


		validationNotifier.addValidationListener(credentialsPart);

		// The credentials part notifies the wizard as well as the validator
		// when new input is set in the UI
		// (e.g., credentials changed..)
		credentialsPart.addPartChangeListener(validationNotifier);

	}

	public void createControl(Composite parent) {
		Control control = credentialsPart.createPart(parent);
		setControl(control);
	}

	@Override
	public boolean isPageComplete() {
		return validationNotifier.isOK();
	}

//	public CloudServerSpacesDelegate getServerSpaceDelegate() {
//		return cloudServerSpaceDelegate;
//	}

	public boolean canFlipToNextPage() {
		// There should only be a next page for the spaces page if there is a
		// cloud space descriptor set
		return isPageComplete() && getNextPage() != null;
	}

}
