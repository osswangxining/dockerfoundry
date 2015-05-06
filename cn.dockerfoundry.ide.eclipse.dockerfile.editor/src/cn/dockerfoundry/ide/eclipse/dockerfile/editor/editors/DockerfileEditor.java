package cn.dockerfoundry.ide.eclipse.dockerfile.editor.editors;

import org.eclipse.ui.editors.text.TextEditor;

public class DockerfileEditor extends TextEditor {

	private ColorManager colorManager;

	public DockerfileEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new DockerfileConfiguration(colorManager));
		setDocumentProvider(new DockerfileDocumentProvider());
	}
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

}
