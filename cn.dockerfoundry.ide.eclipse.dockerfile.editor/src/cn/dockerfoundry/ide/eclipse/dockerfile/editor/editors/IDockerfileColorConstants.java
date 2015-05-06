package cn.dockerfoundry.ide.eclipse.dockerfile.editor.editors;

import org.eclipse.swt.graphics.RGB;

public interface IDockerfileColorConstants {
	RGB COMMENT = new RGB(128, 0, 0);
	RGB PROC_INSTR = new RGB(128, 128, 128);
	RGB STRING = new RGB(0, 128, 0);
	RGB DEFAULT = new RGB(0, 0, 0);
	RGB KEYWORD = new RGB(0, 0, 128);
}
