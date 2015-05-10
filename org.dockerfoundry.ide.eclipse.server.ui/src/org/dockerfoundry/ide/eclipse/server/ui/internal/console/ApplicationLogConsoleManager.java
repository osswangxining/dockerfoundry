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
package org.dockerfoundry.ide.eclipse.server.ui.internal.console;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudfoundry.client.lib.domain.ApplicationLog;
import org.cloudfoundry.client.lib.domain.ApplicationLog.MessageType;
import org.dockerfoundry.ide.eclipse.server.core.internal.CloudFoundryPlugin;
import org.dockerfoundry.ide.eclipse.server.core.internal.CloudFoundryServer;
import org.dockerfoundry.ide.eclipse.server.core.internal.Messages;
import org.dockerfoundry.ide.eclipse.server.core.internal.client.CloudFoundryApplicationModule;
import org.dockerfoundry.ide.eclipse.server.core.internal.client.CloudFoundryServerBehaviour;
import org.dockerfoundry.ide.eclipse.server.core.internal.log.LogContentType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.wst.server.core.IServer;

import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerConnectionElement;
import cn.dockerfoundry.ide.eclipse.explorer.ui.utils.ConsoleHelper;

import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.LogsParameter;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.LogMessage;
import com.spotify.docker.client.LogStream;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class ApplicationLogConsoleManager extends CloudConsoleManager {

	private IConsoleManager consoleManager;

	Map<String, ApplicationLogConsole> consoleByUri;

	private final IConsoleListener listener = new IConsoleListener() {

		public void consolesAdded(IConsole[] consoles) {
			// ignore
		}

		public void consolesRemoved(IConsole[] consoles) {
			for (IConsole console : consoles) {
				if (ApplicationLogConsole.CONSOLE_TYPE.equals(console.getType())) {
					Object server = ((MessageConsole) console).getAttribute(ApplicationLogConsole.ATTRIBUTE_SERVER);
					Object app = ((MessageConsole) console).getAttribute(ApplicationLogConsole.ATTRIBUTE_APP);
					Object index = ((MessageConsole) console).getAttribute(ApplicationLogConsole.ATTRIBUTE_INSTANCE);
					if (server instanceof IServer && app instanceof CloudFoundryApplicationModule
							&& index instanceof Integer) {
						stopConsole((IServer) server);
					}
				}
			}
		}
	};

	public ApplicationLogConsoleManager() {
		consoleByUri = new HashMap<String, ApplicationLogConsole>();
		consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		consoleManager.addConsoleListener(listener);
	}

	@Override
	public void startConsole(CloudFoundryServer server, LogContentType type, 
			int instanceIndex, boolean show, boolean clear, IProgressMonitor monitor) {
		try {
			doStartConsole(server, type,  show, clear);
		}
		catch (CoreException e) {
			CloudFoundryPlugin.logError(e);
		}
	}

	/**
	 * Starts the application console, and begins tailing for application logs.
	 * NOTE that this should only be invoked if the application has started and
	 * is published.
	 * @param server
	 * @param type
	 * @param appModule
	 * @param show
	 * @param clear
	 * @return
	 */
	protected CloudFoundryConsole doStartConsole(CloudFoundryServer server, LogContentType type,
			boolean show, boolean clear) throws CoreException {

//		if (!appModule.isDeployed()) {
//			throw CloudErrorUtil
//					.toCoreException(NLS
//							.bind(org.cloudfoundry.ide.eclipse.server.ui.internal.Messages.ApplicationLogConsoleManager_APPLICATION_NOT_PUBLISHED,
//									appModule.getDeployedApplicationName()));
//		}
		// As of 1.7.2, instances are not used for loggregator. Loggregator
		// shows content for all instances in the same console
		CloudFoundryConsole serverLogTail = getApplicationLogConsole(server);

		if (serverLogTail != null) {
			if (clear) {
				serverLogTail.getConsole().clearConsole();
			}
			serverLogTail.startTailing(type);
		}

		if (show && serverLogTail != null) {
			consoleManager.showConsoleView(serverLogTail.getConsole());
		}
		return serverLogTail;
	}

	protected synchronized ApplicationLogConsole getApplicationLogConsole(CloudFoundryServer server) {

		String appUrl = getConsoleId(server.getServer());
		ApplicationLogConsole serverLogTail = consoleByUri.get(appUrl);
		if (serverLogTail == null) {

			MessageConsole appConsole = getApplicationConsole(server);

			serverLogTail = new ApplicationLogConsole(new ConsoleConfig(appConsole, server));
			consoleByUri.put(getConsoleId(server.getServer()), serverLogTail);
		}
		return serverLogTail;
	}

	// public String getConsoleName() {
	// CloudApplication cloudApp = app != null ? app.getApplication() : null;
	// String name = (cloudApp != null && cloudApp.getUris() != null &&
	// cloudApp.getUris().size() > 0) ? cloudApp
	// .getUris().get(0) : app.getDeployedApplicationName();
	// return name;
	// }

	@Override
	public MessageConsole findCloudFoundryConsole(IServer server, CloudFoundryApplicationModule appModule) {
		String curConsoleId = getConsoleId(server);
		if (curConsoleId != null) {
			return consoleByUri.get(curConsoleId).getConsole();
		}
		return null;
	}

	@Override
	public void writeToStandardConsole(String message, CloudFoundryServer server,
			int instanceIndex, boolean clear, boolean isError) {
		// IMPORTANT: Writing to local standard console does NOT require the
		// application to be published, as it may
		// be used to display pre-publish messages for the application when
		// publishing the application for the first time.
		// Do not start tailing the application console when writing to local
		// standard out as tailing is meant for application
		// log streaming and it requires the application to be published.
		LogContentType type = isError ? StandardLogContentType.STD_ERROR : StandardLogContentType.STD_OUT;

		CloudFoundryConsole serverLogTail = getApplicationLogConsole(server);

		if (serverLogTail != null) {
			if (clear) {
				serverLogTail.getConsole().clearConsole();
			}
			doWriteToStdConsole(message, serverLogTail, type);
			consoleManager.showConsoleView(serverLogTail.getConsole());
		}
	}

	protected void doWriteToStdConsole(String message, CloudFoundryConsole console, LogContentType type) {
		if (StandardLogContentType.STD_ERROR.equals(type)) {
			console.writeToStdError(message);
		}
		else {
			console.writeToStdOut(message);
		}
	}

	@Override
	public void showCloudFoundryLogs(CloudFoundryServer server, 
			int instanceIndex, boolean clear, IProgressMonitor monitor) {
		CloudFoundryConsole console = null;
		try {
			console = doStartConsole(server, StandardLogContentType.APPLICATION_LOG, true, false);
		}
		catch (CoreException e) {
			CloudFoundryPlugin.logError(e);
			return;
		}

		if (console instanceof ApplicationLogConsole) {
			ApplicationLogConsole logConsole = (ApplicationLogConsole) console;
			CloudFoundryServerBehaviour behaviour = server.getBehaviour();
			List<ApplicationLog> logs = new ArrayList<ApplicationLog>();
			//				logs = /*behaviour.getRecentApplicationLogs(appModule.getDeployedApplicationName(), monitor);*/
			String containerId = server.getDockerContainerId();
			DockerConnectionElement dockerConnElem = server.getDockerConnElem();
			if(dockerConnElem != null){
				LogStream logStream = null;
				try {
					DockerClient dockerClient = dockerConnElem.getDockerClient();
					
					logStream = dockerClient.logs(containerId, LogsParameter.STDOUT, LogsParameter.STDERR, LogsParameter.TIMESTAMPS );
//					String content = logStream.readFully();
					while (logStream.hasNext()) {
						LogMessage logMessage = logStream.next();
						String message = ConsoleHelper.getString(logMessage.content());
						ApplicationLog log =  new ApplicationLog("", message, null, MessageType.STDOUT, "", "");
						logs.add(log);
					}
				}
				catch (DockerCertificateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (DockerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					if(logStream != null){
						logStream.close();
					}
				}
				
			}
//							logs = new ArrayList<ApplicationLog>();
							if (!logs.isEmpty()) {
								logConsole.writeApplicationLogs(logs);
							}
							else {
			
								doWriteToStdConsole(Messages.ApplicationLogConsoleManager_NO_RECENT_LOGS + '\n', logConsole,
										StandardLogContentType.STD_OUT);
							}
		}
	}

	@Override
	public void stopConsole(IServer server) {
		String appUrl = getConsoleId(server);
		CloudFoundryConsole serverLogTail = consoleByUri.get(appUrl);
		if (serverLogTail != null) {

			consoleByUri.remove(appUrl);

			serverLogTail.stop();

			MessageConsole messageConsole = serverLogTail.getConsole();
			consoleManager.removeConsoles(new IConsole[] { messageConsole });
			messageConsole.destroy();
		}
	}

	@Override
	public void stopConsoles() {
		for (Entry<String, ApplicationLogConsole> tailEntry : consoleByUri.entrySet()) {
			tailEntry.getValue().stop();
		}
		consoleByUri.clear();
	}

	public static MessageConsole getApplicationConsole(CloudFoundryServer server) {
		MessageConsole appConsole = null;
		String consoleName = getConsoleId(server.getServer());
		for (IConsole console : ConsolePlugin.getDefault().getConsoleManager().getConsoles()) {
			if (console instanceof MessageConsole && console.getName().equals(consoleName)) {
				appConsole = (MessageConsole) console;
			}
		}
		if (appConsole == null) {
			appConsole = new MessageConsole(getConsoleDisplayName(server),
					ApplicationLogConsole.CONSOLE_TYPE, null, true);
			appConsole.setAttribute(ApplicationLogConsole.ATTRIBUTE_SERVER, server);
//			appConsole.setAttribute(ApplicationLogConsole.ATTRIBUTE_APP, appModule);
			// appConsole.setAttribute(ApplicationLogConsole.ATTRIBUTE_INSTANCE,
			// instanceIndex);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { appConsole });
		}

		return appConsole;
	}

	public static String getConsoleId(IServer server) {
		return server.getId(); //$NON-NLS-1$
	}

	public static String getConsoleDisplayName(CloudFoundryServer server) {
		StringWriter writer = new StringWriter();
		writer.append(server.getServer().getName());
//		writer.append('-');
//
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
		return writer.toString();
	}
}
