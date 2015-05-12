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
package cn.dockerfoundry.ide.eclipse.server.ui.internal.editor;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableColumn;

import cn.dockerfoundry.ide.eclipse.server.core.internal.DockerApplicationService;
import cn.dockerfoundry.ide.eclipse.server.ui.internal.DockerFoundryImages;

/**
 * @author Terry Denney
 * @author Christian Dupuis
 */
public class ServicesTreeLabelProvider extends LabelProvider implements ITableLabelProvider {

	private final TableViewer viewer;

	public ServicesTreeLabelProvider(TableViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof DockerApplicationService) {
			return DockerFoundryImages.getImage(DockerFoundryImages.OBJ_SERVICE);
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof DockerApplicationService) {
			DockerApplicationService service = (DockerApplicationService) element;
			return service.getName();
		}
		return super.getText(element);
	}

	public Image getColumnImage(Object element, int columnIndex) {

		TableColumn column = viewer.getTable().getColumn(columnIndex);
		if (column != null && column.getData() instanceof ServiceViewColumn) {

			switch ((ServiceViewColumn) column.getData()) {
			case Alias:
				return getImage(element);
			default:
				return getColumnImage((DockerApplicationService) element, (ServiceViewColumn) column.getData());
			}
		}

		return null;
	}

	protected Image getColumnImage(DockerApplicationService service, ServiceViewColumn column) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		String result = null;
		TableColumn column = viewer.getTable().getColumn(columnIndex);
		if (column != null && element instanceof DockerApplicationService) {
			DockerApplicationService cloudService = (DockerApplicationService) element;
			ServiceViewColumn serviceColumn = (ServiceViewColumn) column.getData();

			if (serviceColumn != null) {
				switch (serviceColumn) {
				case Container:
					result = getText(element);
					break;
				case Alias:
					result = cloudService.getLinkName();
					break;

				}
			}
		}
		return result;
	}

}
