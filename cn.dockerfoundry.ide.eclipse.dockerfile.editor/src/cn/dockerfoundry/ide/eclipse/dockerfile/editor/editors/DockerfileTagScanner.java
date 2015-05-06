package cn.dockerfoundry.ide.eclipse.dockerfile.editor.editors;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.rules.*;

public class DockerfileTagScanner extends RuleBasedScanner {

	public DockerfileTagScanner(ColorManager manager) {
		IToken string =
			new Token(
				new TextAttribute(manager.getColor(IDockerfileColorConstants.STRING)));

		IRule[] rules = new IRule[3];

		// Add rule for double quotes
		rules[0] = new SingleLineRule("\"", "\"", string, '\\');
		// Add a rule for single quotes
		rules[1] = new SingleLineRule("'", "'", string, '\\');
		// Add generic whitespace rule.
		rules[2] = new WhitespaceRule(new DockerfileWhitespaceDetector());

		setRules(rules);
	}
}
