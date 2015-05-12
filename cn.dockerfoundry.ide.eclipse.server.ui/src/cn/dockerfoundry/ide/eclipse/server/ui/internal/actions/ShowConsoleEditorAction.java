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
package cn.dockerfoundry.ide.eclipse.server.ui.internal.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;

import cn.dockerfoundry.ide.eclipse.server.core.internal.DockerFoundryPlugin;
import cn.dockerfoundry.ide.eclipse.server.core.internal.DockerFoundryServer;
import cn.dockerfoundry.ide.eclipse.server.core.internal.client.DockerFoundryApplicationModule;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.Messages;

/**
 * @author Steffen Pingel
 */
public class ShowConsoleEditorAction extends Action {

	private final DockerFoundryServer server;

//	private final CloudFoundryApplicationModule appModule;

	private final int instanceIndex;

	public ShowConsoleEditorAction(DockerFoundryServer server,  int instanceIndex) {
		this.server = server;
//		this.appModule = appModule;
		this.instanceIndex = instanceIndex;
		setText(Messages.ShowConsoleEditorAction_TEXT_SHOW_CONSOLE);
	}

	@Override
	public void run() {
		Job job = new Job(Messages.SHOWING_CONSOLE) {

			@Override
			public IStatus run(IProgressMonitor monitor) {
				if (DockerFoundryPlugin.getCallback() != null) {
					DockerFoundryPlugin.getCallback().stopApplicationConsole(server);

					DockerFoundryPlugin.getCallback().printToConsole(server, Messages.SHOWING_CONSOLE, true,
							false);

					DockerFoundryPlugin.getCallback().showCloudFoundryLogs(server, instanceIndex, monitor);
					return Status.OK_STATUS;
				}
				else {
					return DockerFoundryPlugin
							.getErrorStatus("Internal Error: No Cloud Foundry console callback available. Unable to refresh console contents."); //$NON-NLS-1$
				}
			}

		};
		job.setSystem(true);
		job.setPriority(Job.INTERACTIVE);
		job.schedule();

	}

}
