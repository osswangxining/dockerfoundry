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
 *     IBM - add external finder method for console
 ********************************************************************************/
package cn.dockerfoundry.ide.eclipse.server.ui.internal.console.file;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.wst.server.core.IServer;

import cn.dockerfoundry.ide.eclipse.server.core.internal.DockerFoundryPlugin;
import cn.dockerfoundry.ide.eclipse.server.core.internal.DockerFoundryServer;
import cn.dockerfoundry.ide.eclipse.server.core.internal.client.DockerFoundryApplicationModule;
import cn.dockerfoundry.ide.eclipse.server.core.internal.log.LogContentType;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.console.CloudConsoleManager;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.console.StandardLogContentType;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class FileConsoleManager extends CloudConsoleManager {
	private IConsoleManager consoleManager;

	Map<String, DockerFoundryFileConsole> consoleByUri;

	private final IConsoleListener listener = new IConsoleListener() {

		public void consolesAdded(IConsole[] consoles) {
			// ignore

		}

		public void consolesRemoved(IConsole[] consoles) {
			for (IConsole console : consoles) {
				if (DockerFoundryFileConsole.CONSOLE_TYPE.equals(console.getType())) {
					Object server = ((MessageConsole) console).getAttribute(DockerFoundryFileConsole.ATTRIBUTE_SERVER);
					Object app = ((MessageConsole) console).getAttribute(DockerFoundryFileConsole.ATTRIBUTE_APP);
					Object index = ((MessageConsole) console).getAttribute(DockerFoundryFileConsole.ATTRIBUTE_INSTANCE);
					if (server instanceof IServer && app instanceof DockerFoundryApplicationModule
							&& index instanceof Integer) {
						stopConsole((IServer) server);
					}
				}

			}
		}
	};

	public FileConsoleManager() {
		consoleByUri = new HashMap<String, DockerFoundryFileConsole>();
		consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		consoleManager.addConsoleListener(listener);
	}

	@Override
	public void startConsole(DockerFoundryServer server, LogContentType type, 
			int instanceIndex, boolean show, boolean clear, IProgressMonitor monitor) {

		IConsoleContents contents = null;
		if (StandardLogContentType.APPLICATION_LOG.equals(type)) {
			contents = new StagingConsoleContents();
		}
		else if (StandardLogContentType.SHOW_EXISTING_LOGS.equals(type)) {
			contents = new StdConsoleContents();
		}

		if (contents == null) {
			DockerFoundryPlugin
					.logError("Internal Error: No log content type specified for log file streaming console manager"); //$NON-NLS-1$
			return;
		}

		DockerFoundryFileConsole serverLogTail = getCloudFoundryConsole(server);

		if (serverLogTail != null) {
			if (clear) {
				serverLogTail.getConsole().clearConsole();
			}
			serverLogTail.startTailing(contents.getContents(server, "",
					instanceIndex));
		}

		if (show && serverLogTail != null) {
			consoleManager.showConsoleView(serverLogTail.getConsole());
		}
	}

	protected DockerFoundryFileConsole getCloudFoundryConsole(DockerFoundryServer server) {
		String appUrl = getConsoleId(server.getServer());
		DockerFoundryFileConsole serverLogTail = consoleByUri.get(appUrl);
		if (serverLogTail == null) {

			MessageConsole appConsole = getApplicationConsole(server);

			serverLogTail = new DockerFoundryFileConsole(appConsole);
			consoleByUri.put(getConsoleId(server.getServer()), serverLogTail);
		}
		return serverLogTail;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.dockerfoundry.ide.eclipse.server.ui.internal.console.file.
	 * TEMPFILEConsoleManager
	 * #findCloudFoundryConsole(org.eclipse.wst.server.core.IServer,
	 * org.cloudfoundry
	 * .ide.eclipse.server.core.internal.client.CloudFoundryApplicationModule)
	 */
	@Override
	public MessageConsole findCloudFoundryConsole(IServer server, DockerFoundryApplicationModule appModule) {
		String curConsoleId = getConsoleId(server);
		if (curConsoleId != null) {
			return consoleByUri.get(curConsoleId).getConsole();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.dockerfoundry.ide.eclipse.server.ui.internal.console.file.
	 * TEMPFILEConsoleManager#synchWriteToStd(java.lang.String,
	 * cn.dockerfoundry.ide.eclipse.server.core.internal.CloudFoundryServer,
	 * cn.dockerfoundry.ide.eclipse.server.core.internal.client.
	 * CloudFoundryApplicationModule, int, boolean, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void writeToStandardConsole(String message, DockerFoundryServer server,
			int instanceIndex, boolean clear, boolean isError) {
		DockerFoundryFileConsole serverLogTail = getCloudFoundryConsole(server);

		if (serverLogTail != null) {
			if (clear) {
				serverLogTail.getConsole().clearConsole();
			}

			if (isError) {
				serverLogTail.synchWriteToStdError(message);
			}
			else {
				serverLogTail.synchWriteToStdOut(message);
			}
			consoleManager.showConsoleView(serverLogTail.getConsole());
		}
	}

	public void stopConsole(IServer server) {
		String appUrl = getConsoleId(server);
		DockerFoundryFileConsole serverLogTail = consoleByUri.get(appUrl);
		if (serverLogTail != null) {
			
			consoleByUri.remove(appUrl);
			
			serverLogTail.stop();
			MessageConsole messageConsole = serverLogTail.getConsole();
			consoleManager.removeConsoles(new IConsole[] { messageConsole });
			messageConsole.destroy();
		}
	}

	public void stopConsoles() {
		for (Entry<String, DockerFoundryFileConsole> tailEntry : consoleByUri.entrySet()) {
			tailEntry.getValue().stop();
		}
		consoleByUri.clear();

	}

	public static MessageConsole getApplicationConsole(DockerFoundryServer server) {
		MessageConsole appConsole = null;
		String consoleName = getConsoleId(server.getServer());
		for (IConsole console : ConsolePlugin.getDefault().getConsoleManager().getConsoles()) {
			if (console instanceof MessageConsole && console.getName().equals(consoleName)) {
				appConsole = (MessageConsole) console;
			}
		}
		if (appConsole == null) {
			appConsole = new MessageConsole(getConsoleDisplayName(server),
					DockerFoundryFileConsole.CONSOLE_TYPE, null, true);
			appConsole.setAttribute(DockerFoundryFileConsole.ATTRIBUTE_SERVER, server);
//			appConsole.setAttribute(CloudFoundryFileConsole.ATTRIBUTE_APP, appModule);
//			appConsole.setAttribute(CloudFoundryFileConsole.ATTRIBUTE_INSTANCE, instanceIndex);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { appConsole });
		}

		return appConsole;
	}

	public static String getConsoleId(IServer server) {
		return server.getId(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String getConsoleDisplayName(DockerFoundryServer server) {
		StringWriter writer = new StringWriter();
		writer.append(server.getServer().getName());
		writer.append('-');

//		CloudFoundrySpace space = server.getCloudFoundrySpace();
//
//		if (space != null) {
//			writer.append('-');
//			writer.append(space.getOrgName());
//			writer.append('-');
//			writer.append('-');
//			writer.append(space.getSpaceName());
//			writer.append('-');
//			writer.append('-');
//		}
//
//		writer.append(appModule.getDeployedApplicationName());
//		writer.append('#');
//		writer.append(instanceIndex + ""); //$NON-NLS-1$
		return writer.toString();
	}

	@Override
	public void showCloudFoundryLogs(DockerFoundryServer server, 
			int instanceIndex, boolean clear, IProgressMonitor monitor) {
		startConsole(server, StandardLogContentType.SHOW_EXISTING_LOGS,  instanceIndex, true, clear, monitor);
	}

}
