/*******************************************************************************
 * Copyright (c) 2015 www.DockerFoundry.cn
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
 *     Xi Ning Wang
 ********************************************************************************/

package cn.dockerfoundry.ide.eclipse.explorer.ui.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import cn.dockerfoundry.ide.eclipse.explorer.ui.domain.DockerImageElement;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.AuthConfig;
import com.spotify.docker.client.messages.ImageSearchResult;

public class DockerSearchWizardPage extends WizardPage {

	private Text searchField;
	private TableViewer viewer;
	private Button goButton;
	private Group group1;

	private DockerClient client;
	private Table table;

	public DockerSearchWizardPage(String pageName, DockerClient client) {
		super(pageName);
		setTitle(pageName); // NON-NLS-1
		setDescription("Search the Docker Hub for images"); // NON-NLS-1
		this.client = client;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createLinkTarget()
	 */
	protected void createLinkTarget() {
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#getNewFileLabel()
	 */
	protected String getNewFileLabel() {
		return "New File Name:"; // NON-NLS-1
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.dialogs.WizardNewFileCreationPage#validateLinkedResource()
	 */
	protected IStatus validateLinkedResource() {
		return new Status(IStatus.OK,
				"cn.dockerfoundry.ide.eclipse.explorer.ui", IStatus.OK, "",
				null); // NON-NLS-1 //NON-NLS-2
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		composite.setLayoutData(fileSelectionData);

		GridLayout fileSelectionLayout = new GridLayout();
		fileSelectionLayout.numColumns = 3;
		fileSelectionLayout.makeColumnsEqualWidth = false;
		fileSelectionLayout.marginWidth = 0;
		fileSelectionLayout.marginHeight = 0;
		composite.setLayout(fileSelectionLayout);
		setControl(composite);

		searchField = new Text(composite, SWT.SINGLE | SWT.BORDER); 
		GridData layoutData = new GridData(GridData.GRAB_HORIZONTAL
				| GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		searchField.setLayoutData(layoutData);
		goButton = new Button(composite, SWT.PUSH);
		goButton.setText("Search");
//		layoutData = new GridData(GridData.GRAB_HORIZONTAL
//				| GridData.FILL_HORIZONTAL);
//		layoutData.horizontalSpan = 1;
//		goButton.setLayoutData(layoutData);
		goButton.setAlignment(SWT.RIGHT);
		goButton.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				if (getClient() != null) {
					String term = searchField.getText();
					List<ImageSearchResult> result = new ArrayList<ImageSearchResult>();
					try {
						AuthConfig authConfig = AuthConfig.builder().email("osswangxining@163.com").username("osswangxining")
						        .password("").build();
						
						result = getClient().searchImages(term);
						
					} catch (DockerException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println(result.size());
					viewer.setInput(result);
					viewer.refresh(true);
				}

			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				keyPressed(arg0);
			}
		});

		viewer = new TableViewer(composite, SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());

		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);

		table = viewer.getTable();
		table.setLinesVisible(false);
		table.setHeaderVisible(true);
	    createColumns(parent, viewer);
	    
		composite.moveAbove(null);

		addListener();
	}

	private void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { "NAME", "STAR COUNT", "OFFICIAL", "AUTOMATED",
				"DESCRIPTION" };
		int[] bounds = { 200, 120, 120, 120, 200 };
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ImageSearchResult elem = (ImageSearchResult) element;
				return elem.getName();
			}
		});
		col = createTableViewerColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ImageSearchResult elem = (ImageSearchResult) element;
				return Integer.toString(elem.getStarCount());
			}
		});
		col = createTableViewerColumn(titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ImageSearchResult elem = (ImageSearchResult) element;
				return  Boolean.toString(elem.isOfficial());
			}
		});
		col = createTableViewerColumn(titles[3], bounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ImageSearchResult elem = (ImageSearchResult) element;
				return  Boolean.toString(elem.isAutomated());
			}
		});
		col = createTableViewerColumn(titles[4], bounds[4], 4);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ImageSearchResult elem = (ImageSearchResult) element;
				return elem.getDescription();
			}
		});
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound,
			final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
//		column.addSelectionListener(getSelectionAdapter(column, colNumber));
		return viewerColumn;
	}
	
	private void addListener() {

	}

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			if (parent != null && parent instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<ImageSearchResult> images = (List<ImageSearchResult>) parent;
				ImageSearchResult[] elements = new ImageSearchResult[images
						.size()];
				return images.toArray(elements);
			}
			return new ImageSearchResult[] {};
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			if (obj instanceof ImageSearchResult) {
				ImageSearchResult elem = (ImageSearchResult) obj;
				switch (index) {
				case 0:
					return elem.getName();
				case 1:
					return Integer.toString(elem.getStarCount());
				case 2:
					return Boolean.toString(elem.isOfficial());
				case 3:
					return Boolean.toString(elem.isAutomated());
				case 4:
					return elem.getDescription();
				default:
					break;
				}
			}
			return null;
		}

		public Image getColumnImage(Object obj, int index) {
			return null;
			// return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	class NameSorter extends ViewerSorter {
	}

	public DockerClient getClient() {
		return client;
	}

	public void setClient(DockerClient client) {
		this.client = client;
	}
	
	public ImageSearchResult getSelectedImage(){
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection) selection)
				.getFirstElement();
		if(obj != null && obj instanceof ImageSearchResult)
			return (ImageSearchResult)obj;
		return null;
	}
}
