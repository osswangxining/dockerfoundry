package cn.dockerfoundry.ide.eclipse.dockerfile.editor.editors;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

public class DockerfileWhitespaceDetector implements IWhitespaceDetector {

	public boolean isWhitespace(char c) {
		return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
	}
}
