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
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.wst.server.core.IServer;

import cn.dockerfoundry.ide.eclipse.server.core.internal.DockerFoundryServer;

public class ServerMenuActionHandler extends MenuActionHandler<IServer> {

	protected ServerMenuActionHandler() {
		super(IServer.class);
	}

	@Override
	protected List<IAction> getActionsFromSelection(IServer server) {
		DockerFoundryServer cloudFoundryServer = (DockerFoundryServer) server.loadAdapter(DockerFoundryServer.class, null);
		if (cloudFoundryServer == null || server.getServerState() != IServer.STATE_STARTED) {
			return Collections.emptyList();
		}
		List<IAction> actions = new ArrayList<IAction>();

		
		return actions;
	}

}
