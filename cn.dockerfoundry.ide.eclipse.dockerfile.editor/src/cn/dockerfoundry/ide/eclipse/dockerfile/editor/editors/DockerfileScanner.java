package cn.dockerfoundry.ide.eclipse.dockerfile.editor.editors;

import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.*;

public class DockerfileScanner extends RuleBasedScanner {

	public DockerfileScanner(ColorManager manager) {
		IToken procInstr =
			new Token(
				new TextAttribute(
					manager.getColor(IDockerfileColorConstants.PROC_INSTR)));

		IRule[] rules = new IRule[2];
		//Add rule for processing instructions
		rules[0] = new SingleLineRule("<?", "?>", procInstr);
		// Add generic whitespace rule.
		rules[1] = new WhitespaceRule(new DockerfileWhitespaceDetector());

		setRules(rules);
	}
}
