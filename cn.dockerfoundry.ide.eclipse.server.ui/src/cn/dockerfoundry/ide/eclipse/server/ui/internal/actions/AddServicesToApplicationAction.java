/*******************************************************************************
 * Copyright (c) 2012, 2015 Pivotal Software, Inc. 
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
package cn.dockerfoundry.ide.eclipse.server.ui.internal.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;

import cn.dockerfoundry.ide.eclipse.server.core.internal.client.DockerFoundryApplicationModule;
import cn.dockerfoundry.ide.eclipse.server.core.internal.client.DockerFoundryServerBehaviour;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.editor.DockerFoundryApplicationsEditorPage;

/**
 * @author Terry Denney
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class AddServicesToApplicationAction extends ModifyServicesForApplicationAction {

	private final List<String> services;

	public AddServicesToApplicationAction(IStructuredSelection selection, DockerFoundryApplicationModule appModule,
			DockerFoundryServerBehaviour serverBehaviour, DockerFoundryApplicationsEditorPage editorPage) {
		super(appModule, serverBehaviour, editorPage);

		this.services = getServiceNames(selection);
	}

	@Override
	public String getJobName() {
		return "Adding services"; //$NON-NLS-1$
	}

	@Override
	public List<String> getServicesToAdd() {
		return services;
	}

	@Override
	public List<String> getServicesToRemove() {
		return new ArrayList<String>();
	}

}
