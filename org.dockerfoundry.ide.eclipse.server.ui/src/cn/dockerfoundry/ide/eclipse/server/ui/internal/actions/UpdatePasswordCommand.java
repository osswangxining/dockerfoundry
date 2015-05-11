/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc. 
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
 *     Keith Chong, IBM - Support more general branded server type IDs via org.eclipse.ui.menus
 ********************************************************************************/
package cn.dockerfoundry.ide.eclipse.server.ui.internal.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.server.core.IServerWorkingCopy;

import cn.dockerfoundry.ide.eclipse.server.core.internal.CloudFoundryPlugin;
import cn.dockerfoundry.ide.eclipse.server.core.internal.CloudFoundryServer;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.CloudUiUtil;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.Messages;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.UpdatePasswordDialog;

public class UpdatePasswordCommand extends BaseCommandHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		initializeSelection(event);
		IServerWorkingCopy wc = selectedServer.createWorkingCopy();
		final CloudFoundryServer cloudServer = (CloudFoundryServer) wc.loadAdapter(CloudFoundryServer.class, null);
//		final UpdatePasswordDialog dialog = new UpdatePasswordDialog(Display.getDefault().getActiveShell(), cfServer.getUsername());
//		
//		if (dialog.open() == IDialogConstants.OK_ID) {
//			String errorMsg = CloudUiUtil.updatePassword(dialog.getPassword(), cfServer, wc);
//			if (errorMsg != null) {
//				MessageDialog.openError(Display.getDefault().getActiveShell(), Messages.UpdatePasswordCommand_TEXT_PW_UPDATE, NLS.bind(Messages.UpdatePasswordCommand_ERROR_PW_UPDATE_BODY, errorMsg));
//			} else {
//				MessageDialog.openInformation(Display.getDefault().getActiveShell(), Messages.UpdatePasswordCommand_TEXT_PW_UPDATE, Messages.UpdatePasswordCommand_TEXT_PW_UPDATE_SUCC);
//			}
//		}
		
//		new ShowConsoleEditorAction(cfServer, appModule, 0).run();
		new ShowConsoleEditorAction(cloudServer, 0).run();
		
		return null;
	}
}
