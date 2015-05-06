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

package cn.dockerfoundry.ide.eclipse.dockerfile.validator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.wst.validation.AbstractValidator;
import org.eclipse.wst.validation.ValidationResult;
import org.eclipse.wst.validation.ValidationState;
import org.eclipse.wst.validation.ValidatorMessage;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;

/**
 * @author wangxn
 *
 */
public class DockerfileValidator extends AbstractValidator {

	/**
	 * 
	 */
	public DockerfileValidator() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ValidationResult validate(IResource resource, int kind,
			ValidationState state, IProgressMonitor monitor) {
		if (resource.getType() != IResource.FILE)
			return null;
		ValidationResult result = new ValidationResult();
		IReporter reporter = result.getReporter(monitor);
		try {
			validateDockerFile((IFile) resource, reporter, result);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	private void validateDockerFile(IFile resource, IReporter reporter,
			ValidationResult result) throws CoreException {
		if ((reporter != null) && (reporter.isCancelled() == true)) {
			throw new OperationCanceledException();
		}
		DockerfileDelegatingValidator validator = DockerfileValidationFactory.instance
				.getValidator(resource.getContents());

		Map<DockerfileValidationLevel, List<DockerfileValidationResult>> resultMap = validator
				.validate();
		List<DockerfileValidationResult> infos = resultMap
				.get(DockerfileValidationLevel.INFO);
		List<DockerfileValidationResult> errors = resultMap
				.get(DockerfileValidationLevel.ERROR);
		List<DockerfileValidationResult> warnings = resultMap
				.get(DockerfileValidationLevel.WARNING);
		if (infos != null) {
			for (Iterator<DockerfileValidationResult> iterator = infos
					.iterator(); iterator.hasNext();) {
				DockerfileValidationResult validationResult = (DockerfileValidationResult) iterator
						.next();
	
				ValidatorMessage vm = ValidatorMessage.create(validationResult.getMessage(),
						resource);
				vm.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
				vm.setAttribute(IMarker.SOURCE_ID, IMarker.PROBLEM);
				vm.setAttribute(IMarker.LINE_NUMBER, validationResult.getLine());
				vm.setAttribute(IMarker.CHAR_START, 0);
				vm.setAttribute(IMarker.CHAR_END, validationResult
						.getLineContent() == null ? 0 : validationResult
						.getLineContent().length());
				result.add(vm);
			}
		}
		if (errors != null) {
			for (Iterator<DockerfileValidationResult> iterator = errors
					.iterator(); iterator.hasNext();) {
				DockerfileValidationResult validationResult = (DockerfileValidationResult) iterator
						.next();
				ValidatorMessage vm = ValidatorMessage.create(validationResult.getMessage(),
						resource);
				vm.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				vm.setAttribute(IMarker.SOURCE_ID, IMarker.PROBLEM);
				vm.setAttribute(IMarker.LINE_NUMBER, validationResult.getLine());
//				vm.setAttribute(IMarker.CHAR_START, 0);
//				vm.setAttribute(IMarker.CHAR_END, validationResult
//						.getLineContent() == null ? 0 : validationResult
//						.getLineContent().length());
				result.add(vm);
			}
		}
		if (warnings != null) {
			for (Iterator<DockerfileValidationResult> iterator = warnings
					.iterator(); iterator.hasNext();) {
				DockerfileValidationResult validationResult = (DockerfileValidationResult) iterator
						.next();
				ValidatorMessage vm = ValidatorMessage.create(validationResult.getMessage(),
						resource);
				vm.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
				vm.setAttribute(IMarker.SOURCE_ID, IMarker.PROBLEM);
				vm.setAttribute(IMarker.LINE_NUMBER, validationResult.getLine());
				vm.setAttribute(IMarker.CHAR_START, 0);
				vm.setAttribute(IMarker.CHAR_END, validationResult
						.getLineContent() == null ? 0 : validationResult
						.getLineContent().length());
				result.add(vm);
			}
		}
	}

}
