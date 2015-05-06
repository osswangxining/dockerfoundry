package cn.dockerfoundry.ide.eclipse.dockerfile.editor.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;

public class DockerfileConfiguration extends SourceViewerConfiguration {
	private DockerfileDoubleClickStrategy doubleClickStrategy;
	private DockerfileTagScanner tagScanner;
	private DockerfileScanner scanner;
	private ColorManager colorManager;

	public DockerfileConfiguration(ColorManager colorManager) {
		this.colorManager = colorManager;
	}
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			IDocument.DEFAULT_CONTENT_TYPE,
			DockerfilePartitionScanner.DOCKERFILE_COMMENT,
			DockerfilePartitionScanner.DOCKERFILE_FROM };
	}
	public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer,
		String contentType) {
		if (doubleClickStrategy == null)
			doubleClickStrategy = new DockerfileDoubleClickStrategy();
		return doubleClickStrategy;
	}

	protected DockerfileScanner getXMLScanner() {
		if (scanner == null) {
			scanner = new DockerfileScanner(colorManager);
			scanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(IDockerfileColorConstants.DEFAULT))));
		}
		return scanner;
	}
	protected DockerfileTagScanner getXMLTagScanner() {
		if (tagScanner == null) {
			tagScanner = new DockerfileTagScanner(colorManager);
			tagScanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						colorManager.getColor(IDockerfileColorConstants.KEYWORD), null ,SWT.BOLD)));
		}
		return tagScanner;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr =
			new DefaultDamagerRepairer(getXMLTagScanner());
		reconciler.setDamager(dr, DockerfilePartitionScanner.DOCKERFILE_FROM);
		reconciler.setRepairer(dr, DockerfilePartitionScanner.DOCKERFILE_FROM);

		dr = new DefaultDamagerRepairer(getXMLScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		NonRuleBasedDamagerRepairer ndr =
			new NonRuleBasedDamagerRepairer(
				new TextAttribute(
					colorManager.getColor(IDockerfileColorConstants.COMMENT)));
		reconciler.setDamager(ndr, DockerfilePartitionScanner.DOCKERFILE_COMMENT);
		reconciler.setRepairer(ndr, DockerfilePartitionScanner.DOCKERFILE_COMMENT);

		return reconciler;
	}

}