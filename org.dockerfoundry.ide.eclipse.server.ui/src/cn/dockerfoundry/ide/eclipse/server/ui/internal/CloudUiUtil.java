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
package cn.dockerfoundry.ide.eclipse.server.ui.internal;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;
import org.eclipse.wst.server.core.IServerWorkingCopy;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.springframework.web.client.ResourceAccessException;

import cn.dockerfoundry.ide.eclipse.explorer.ui.Activator;
import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerConnectionElement;
import cn.dockerfoundry.ide.eclipse.server.core.internal.CloudErrorUtil;
import cn.dockerfoundry.ide.eclipse.server.core.internal.CloudFoundryBrandingExtensionPoint;
import cn.dockerfoundry.ide.eclipse.server.core.internal.CloudFoundryServer;
import cn.dockerfoundry.ide.eclipse.server.core.internal.ServerEventHandler;
import cn.dockerfoundry.ide.eclipse.server.core.internal.CloudFoundryBrandingExtensionPoint.CloudServerURL;
import cn.dockerfoundry.ide.eclipse.server.core.internal.client.CloudFoundryServerBehaviour;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Terry Denney
 */
@SuppressWarnings("restriction")
public class CloudUiUtil {

	public static final String SERVERS_VIEW_ID = "org.eclipse.wst.server.ui.ServersView"; //$NON-NLS-1$

	public static String ATTR_USER_DEFINED_URLS = "org.dockerfoundry.ide.eclipse.server.user.defined.urls"; //$NON-NLS-1$

	public static IStatus runForked(final ICoreRunnable coreRunner, IWizard wizard) {
		try {
			IRunnableWithProgress runner = new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						coreRunner.run(monitor);
					}
					catch (Exception e) {
						throw new InvocationTargetException(e);
					}
					finally {
						monitor.done();
					}
				}
			};
			wizard.getContainer().run(true, false, runner);
		}
		catch (InvocationTargetException e) {
			IStatus status;
			if (e.getCause() instanceof CoreException) {
				status = new Status(IStatus.ERROR, CloudFoundryServerUiPlugin.PLUGIN_ID, NLS.bind(
						Messages.CloudUiUtil_ERROR_FORK_OP_FAILED, e.getCause().getMessage()), e);
			}
			else {
				status = new Status(IStatus.ERROR, CloudFoundryServerUiPlugin.PLUGIN_ID, NLS.bind(
						Messages.CloudUiUtil_ERROR_FORK_UNEXPECTED, e.getMessage()), e);
			}
			CloudFoundryServerUiPlugin.getDefault().getLog().log(status);
			IWizardPage page = wizard.getContainer().getCurrentPage();
			if (page instanceof DialogPage) {
				((DialogPage) page).setErrorMessage(status.getMessage());
			}
			return status;
		}
		catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

	public static List<CloudServerURL> getAllUrls(String serverTypeId) {
		List<CloudServerURL> urls = new ArrayList<CloudFoundryBrandingExtensionPoint.CloudServerURL>();
//		urls.add(getDefaultUrl(serverTypeId));
		urls.addAll(getUrls(serverTypeId));
		return urls;
	}

	public static CloudServerURL getDefaultUrl(String serverTypeId) {
		return CloudFoundryBrandingExtensionPoint.getDefaultUrl(serverTypeId);
	}

	public static List<CloudServerURL> getUrls(String serverTypeId) {
		List<CloudServerURL> cloudUrls = new ArrayList<CloudServerURL>();

		Set<String> urlNames = new HashSet<String>();

		List<CloudServerURL> userDefinedUrls = CloudUiUtil.getUserDefinedUrls(serverTypeId);
		for (CloudServerURL userDefinedUrl : userDefinedUrls) {
			cloudUrls.add(userDefinedUrl);
			urlNames.add(userDefinedUrl.getName());
		}

		List<CloudServerURL> defaultUrls = CloudFoundryBrandingExtensionPoint.getCloudUrls(serverTypeId);
		if (defaultUrls != null) {
			for (CloudServerURL defaultUrl : defaultUrls) {
				if (!urlNames.contains(defaultUrl.getName())) {
					cloudUrls.add(defaultUrl);
				}
			}

			Collections.sort(cloudUrls, new Comparator<CloudServerURL>() {

				public int compare(CloudServerURL o1, CloudServerURL o2) {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}

			});
		}

		return cloudUrls;
	}

	public static CloudServerURL getCloudServerURL(String serverName){
		if(serverName == null)
			return null;
		
		Preferences preferences = InstanceScope.INSTANCE
				.getNode(Activator.PLUGIN_ID);
		try {
			String[] childrenNames = preferences.childrenNames();
			if(childrenNames != null){
				for (int i = 0; i < childrenNames.length; i++) {
					Preferences sub1 = preferences.node(childrenNames[i]);
					String name = sub1.get("name", "");
					if(!serverName.equals(name))
						continue;
					boolean isUseDefault = sub1.getBoolean("isUseDefault", true);
					boolean isUseUnixSocket = sub1.getBoolean("isUseUnixSocket", false);
					String socketPath = sub1.get("socketPath", "");
					boolean isUseHTTPS = sub1.getBoolean("isUseHTTPS", false);
					String host = sub1.get("host", "");
					boolean isEnableAuth = sub1.getBoolean("isEnableAuth", false);
					String authPath = sub1.get("authPath", "");		
					
					DockerConnectionElement connElem = new DockerConnectionElement();
					connElem.setAuthPath(authPath);
					connElem.setEnableAuth(isEnableAuth);
					connElem.setHost(host);
					connElem.setName(name);
					connElem.setSocketPath(socketPath);
					connElem.setUseDefault(isUseDefault);
					connElem.setUseHTTPS(isUseHTTPS);
					connElem.setUseUnixSocket(isUseUnixSocket);
					
					String connName = name;
					if (connElem.isUseDefault()) {
						connName += "[Default]";
					} else if (connElem.isUseHTTPS()) {
						connName += "[" + connElem.getHost() + "]";
					}
					
					return (new CloudServerURL(name, connName, true, null, connElem));
				}
			}
		} catch (BackingStoreException e) {
			 e.printStackTrace();
		}
		return null;
	}
	public static List<CloudServerURL> getUserDefinedUrls(String serverTypeId) {
		List<CloudServerURL> urls = new ArrayList<CloudServerURL>();
		
		
		Preferences preferences = InstanceScope.INSTANCE
				.getNode(Activator.PLUGIN_ID);
		try {
			String[] childrenNames = preferences.childrenNames();
			if(childrenNames != null){
				for (int i = 0; i < childrenNames.length; i++) {
					Preferences sub1 = preferences.node(childrenNames[i]);
					String name = sub1.get("name", "");
					boolean isUseDefault = sub1.getBoolean("isUseDefault", true);
					boolean isUseUnixSocket = sub1.getBoolean("isUseUnixSocket", false);
					String socketPath = sub1.get("socketPath", "");
					boolean isUseHTTPS = sub1.getBoolean("isUseHTTPS", false);
					String host = sub1.get("host", "");
					boolean isEnableAuth = sub1.getBoolean("isEnableAuth", false);
					String authPath = sub1.get("authPath", "");		
					
					DockerConnectionElement connElem = new DockerConnectionElement();
					connElem.setAuthPath(authPath);
					connElem.setEnableAuth(isEnableAuth);
					connElem.setHost(host);
					connElem.setName(name);
					connElem.setSocketPath(socketPath);
					connElem.setUseDefault(isUseDefault);
					connElem.setUseHTTPS(isUseHTTPS);
					connElem.setUseUnixSocket(isUseUnixSocket);
					
					String connName = "";
					if (connElem.isUseDefault()) {
						connName += "Default";
					} else if (connElem.isUseHTTPS()) {
						connName +=  connElem.getHost() ;
					}
					
					urls.add(new CloudServerURL(name, connName, true, null, connElem));
				}
			}
		} catch (BackingStoreException e) {
			 e.printStackTrace();
		}
		
		IPreferenceStore prefStore = CloudFoundryServerUiPlugin.getDefault().getPreferenceStore();
		String urlString = prefStore.getString(ATTR_USER_DEFINED_URLS + "." + serverTypeId); //$NON-NLS-1$

		if (urlString != null && urlString.length() > 0) {
			// Split on "||"
			String[] urlEntries = urlString.split("\\|\\|"); //$NON-NLS-1$
			if (urlEntries != null) {
				for (String entry : urlEntries) {
					if (entry.length() > 0) {
						String[] values = entry.split(","); //$NON-NLS-1$
						if (values != null) {
							String name = null;
							String url = null;

							if (values.length >= 2) {
								name = values[0];
								url = values[1];
							}

							urls.add(new CloudServerURL(name, url, true));
						}
					}

				}
			}
		}

		return urls;
	}

	public static void storeUserDefinedUrls(String serverTypeId, List<CloudServerURL> urls) {
		IPreferenceStore prefStore = CloudFoundryServerUiPlugin.getDefault().getPreferenceStore();
		StringBuilder builder = new StringBuilder();

		for (CloudServerURL url : urls) {
			if (url.getUserDefined()) {
				builder.append(url.getName());

				builder.append(","); //$NON-NLS-1$
				builder.append(url.getUrl());

				builder.append("||"); //$NON-NLS-1$
			}
		}

		prefStore.setValue(ATTR_USER_DEFINED_URLS + "." + serverTypeId, builder.toString()); //$NON-NLS-1$
	}

	public static String updatePassword(final String newPassword, final CloudFoundryServer cfServer,
			final IServerWorkingCopy serverWc) {
		ICoreRunnable coreRunner = new ICoreRunnable() {
			public void run(final IProgressMonitor monitor) throws CoreException {
				cfServer.getBehaviour().updatePassword(newPassword, monitor);
				cfServer.setPassword(newPassword);
				cfServer.saveConfiguration(monitor);
				ServerEventHandler.getDefault().firePasswordUpdated(cfServer);
				serverWc.save(true, monitor);
			}
		};

		try {
			CloudUiUtil.runForked(coreRunner);
		}
		catch (OperationCanceledException ex) {
		}
		catch (CoreException ex) {
			return ex.getMessage();
		}

		return null;
	}

	/**
	 * Validates the given credentials. Throws {@link CoreException} if error
	 * occurred during validation.
	 * @param userName
	 * @param password
	 * @param urlText
	 * @param displayURL
	 * @param selfSigned true if its a server using self-signed certificate. If
	 * this information is not known, set this to false
	 * @param context
	 * 
	 * @throws CoreException if validation failed and error type cannot be
	 * determined
	 * @throws OperationCanceledException if validation is cancelled.
	 */
	public static void validateCredentials(final CloudFoundryServer cfServer, final String userName, final String password, final String urlText,
			final boolean displayURL, final boolean selfSigned, IRunnableContext context) throws CoreException,
			OperationCanceledException {
		try {
			ICoreRunnable coreRunner = new ICoreRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					String url = urlText;
					if (displayURL) {
						url = getUrlFromDisplayText(urlText);
					}
					CloudFoundryServerBehaviour.validate(cfServer, url, userName, password, selfSigned, monitor);
				}
			};
			if (context != null) {
				runForked(coreRunner, context);
			}
			else {
				runForked(coreRunner);
			}
		}
		catch (CoreException ce) {
			ce.printStackTrace();
		}

	}

	
	public static String getUrlFromDisplayText(String displayText) {
		String url = displayText;
		if (url != null) {
			int pos = url.lastIndexOf(" - "); //$NON-NLS-1$
			if (pos >= 0) {
				return url.substring(pos + 3);
			}
		}

		return url;
	}

	public static String getNameFromDisplayText(String displayText) {
		String url = displayText;
		if (url != null) {
			int pos = url.lastIndexOf(" - "); //$NON-NLS-1$
			if (pos >= 0) {
				return url.substring(0, pos);
			}
		}

		return url;
	}

	
	public static String getDisplayTextFromUrl(String url, String serverTypeId) {
		List<CloudServerURL> cloudUrls = getAllUrls(serverTypeId);
		for (CloudServerURL cloudUrl : cloudUrls) {
			if (cloudUrl.getUrl().equals(url)) {
				return cloudUrl.getName() + " - " + url; //$NON-NLS-1$
			}
		}
		return url;
	}

	public static void runForked(final ICoreRunnable coreRunner) throws OperationCanceledException, CoreException {
		runForked(coreRunner, PlatformUI.getWorkbench().getProgressService());
	}

	public static void runForked(final ICoreRunnable coreRunner, IRunnableContext progressService)
			throws OperationCanceledException, CoreException {
		try {
			IRunnableWithProgress runner = new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					monitor.beginTask("", IProgressMonitor.UNKNOWN); //$NON-NLS-1$
					try {
						coreRunner.run(monitor);
					}
					catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
					finally {
						monitor.done();
					}
				}

			};
			progressService.run(true, true, runner);
		}
		catch (InvocationTargetException e) {
			if (e.getCause() instanceof CoreException) {
				throw (CoreException) e.getCause();
			}
			else {
				CloudFoundryServerUiPlugin
						.getDefault()
						.getLog()
						.log(new Status(IStatus.ERROR, CloudFoundryServerUiPlugin.PLUGIN_ID, "Unexpected exception", e)); //$NON-NLS-1$
			}
		}
		catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
	}

	public static void openUrl(String location) {
		openUrl(location, WebBrowserPreference.getBrowserChoice());
	}

	public static void openUrl(String location, int browserChoice) {
		try {
			URL url = null;
			if (location != null) {
				url = new URL(location);
			}
			if (browserChoice == WebBrowserPreference.EXTERNAL) {
				try {
					IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
					support.getExternalBrowser().openURL(url);
				}
				catch (Exception e) {
				}
			}
			else {
				IWebBrowser browser;
				int flags;
				if (WorkbenchBrowserSupport.getInstance().isInternalWebBrowserAvailable()) {
					flags = IWorkbenchBrowserSupport.AS_EDITOR | IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				}
				else {
					flags = IWorkbenchBrowserSupport.AS_EXTERNAL | IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				}

				String generatedId = "org.eclipse.mylyn.web.browser-" + Calendar.getInstance().getTimeInMillis(); //$NON-NLS-1$
				browser = WorkbenchBrowserSupport.getInstance().createBrowser(flags, generatedId, null, null);
				browser.openURL(url);
			}
		}
		catch (PartInitException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), Messages.CloudUiUtil_ERROR_OPEN_BROWSER_FAIL_TITLE,
					Messages.CloudUiUtil_ERROR_OPEN_BROWSER_BODY);
		}
		catch (MalformedURLException e) {
			if (location == null || location.trim().equals("")) { //$NON-NLS-1$
				MessageDialog.openInformation(Display.getDefault().getActiveShell(), Messages.CloudUiUtil_ERROR_OPEN_BROWSER_FAIL_TITLE,
						NLS.bind(Messages.CloudUiUtil_ERROR_EMPTY_URL_BODY, location));
			}
			else {
				MessageDialog.openInformation(Display.getDefault().getActiveShell(), Messages.CloudUiUtil_ERROR_OPEN_BROWSER_FAIL_TITLE,
						NLS.bind(Messages.CloudUiUtil_ERROR_MALFORM_URL_BODY, location));
			}
		}

	}

	/**
	 * Prompts user to define a value for the wildcard in the cloud URL, then
	 * return the new URL
	 * 
	 * @param cloudUrl
	 * @param allCloudUrls
	 * @param shell
	 * @return new URL, null if no wildcard appears in cloudUrl or if user
	 * cancels out of defining a new value
	 */
	public static CloudServerURL getWildcardUrl(CloudServerURL cloudUrl, List<CloudServerURL> allCloudUrls, Shell shell) {
		String url = cloudUrl.getUrl();
		if (url.contains("{")) { //$NON-NLS-1$
			int startIndex = url.indexOf("{"); //$NON-NLS-1$
			int endIndex = url.indexOf("}"); //$NON-NLS-1$
			String wildcard = url.substring(startIndex + 1, endIndex);

			TargetURLDialog dialog = new TargetURLDialog(shell, cloudUrl, wildcard, allCloudUrls);
			if (dialog.open() == IDialogConstants.OK_ID) {
				url = dialog.getUrl();
				String name = dialog.getName();
				// CloudUiUtil.addUserDefinedUrl(serverTypeId, name, url);
				return new CloudServerURL(name, url, true);
			}
			else {
				return null;
			}
		}

		return null;
	}

	/**
	 * If the Servers view is available and it contains a selection, the
	 * corresponding structured selection is returned. In any other case,
	 * including the Servers view being unavailable, either because it is not
	 * installed or it is closed, null is returned.
	 * @return structured selection in the Servers view, if the Servers view is
	 * open and available, or null otherwise
	 */
	public static IStructuredSelection getServersViewSelection() {

		IViewRegistry registry = PlatformUI.getWorkbench().getViewRegistry();
		String serversViewID = SERVERS_VIEW_ID;

		// fast check to verify that the servers View is available.
		IViewDescriptor serversViewDescriptor = registry.find(serversViewID);
		if (serversViewDescriptor != null) {

			// Granular null checks required as any of the workbench components
			// may not be available at some given point in time (e.g., during
			// start/shutdown)
			IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

			if (activeWorkbenchWindow != null) {

				IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();

				if (activePage != null) {
					IViewReference[] references = activePage.getViewReferences();

					if (references != null) {
						IViewPart serversViewPart = null;
						for (IViewReference reference : references) {
							if (serversViewID.equals(reference.getId())) {
								serversViewPart = reference.getView(true);
								break;
							}
						}

						if (serversViewPart != null) {

							IViewSite viewSite = serversViewPart.getViewSite();
							if (viewSite != null) {
								ISelectionProvider selectionProvider = viewSite.getSelectionProvider();
								if (selectionProvider != null) {
									ISelection selection = selectionProvider.getSelection();
									if (selection instanceof IStructuredSelection) {
										return (IStructuredSelection) selection;
									}
								}
							}
						}
					}
				}
			}

		}
		return null;
	}

	/**
	 * Returns the current shell or null.
	 * @return
	 */
	public static Shell getShell() {
		return PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
	}

}
