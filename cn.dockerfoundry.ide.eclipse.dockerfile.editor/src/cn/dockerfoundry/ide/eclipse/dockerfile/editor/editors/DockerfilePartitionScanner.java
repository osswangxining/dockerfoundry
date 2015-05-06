package cn.dockerfoundry.ide.eclipse.dockerfile.editor.editors;

import org.eclipse.jface.text.rules.*;

public class DockerfilePartitionScanner extends RuleBasedPartitionScanner {
	public final static String DOCKERFILE_COMMENT = "__dockerfile_comment";
	public final static String DOCKERFILE_FROM = "__dockerfile_from";

	public DockerfilePartitionScanner() {
		IPredicateRule[] rules = new IPredicateRule[2];

		rules[0] = new SingleLineRule("#", "",  new Token(DOCKERFILE_COMMENT));
		rules[1] = new SingleLineRule("FROM", " ", new Token(DOCKERFILE_FROM));

		setPredicateRules(rules);
	}
}
