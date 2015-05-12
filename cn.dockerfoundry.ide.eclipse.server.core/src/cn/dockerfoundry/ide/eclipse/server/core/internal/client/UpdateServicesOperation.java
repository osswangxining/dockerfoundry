/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc. 
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
package cn.dockerfoundry.ide.eclipse.server.core.internal.client;

import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import cn.dockerfoundry.ide.eclipse.server.core.internal.DockerApplicationService;
import cn.dockerfoundry.ide.eclipse.server.core.internal.ServerEventHandler;

public class UpdateServicesOperation extends BehaviourOperation {

	private final BaseClientRequest<List<DockerApplicationService>> request;

	public UpdateServicesOperation(BaseClientRequest<List<DockerApplicationService>> request, DockerFoundryServerBehaviour behaviour) {
		super(behaviour, null);
		this.request = request;
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		List<DockerApplicationService> existingServices = request.run(monitor);
		ServerEventHandler.getDefault().fireServicesUpdated(getBehaviour().getCloudFoundryServer(),
				existingServices);
	}

}
