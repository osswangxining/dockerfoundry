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

import java.io.InputStream;

/**
 * @author wangxn
 *
 */
public class DockerfileValidationFactory {
	public final static DockerfileValidationFactory instance = new DockerfileValidationFactory();

	public DockerfileDelegatingValidator getValidator(
			InputStream dockerfileInputStream) {
		return new DockerfileDelegatingValidator(dockerfileInputStream);
	}
}
