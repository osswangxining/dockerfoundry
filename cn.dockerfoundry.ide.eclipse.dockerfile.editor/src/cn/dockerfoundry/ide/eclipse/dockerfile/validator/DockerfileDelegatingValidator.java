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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import cn.dockerfoundry.ide.eclipse.dockerfile.editor.util.ValidatorUtils;

/**
 * @author wangxn
 *
 */
public class DockerfileDelegatingValidator {
	private InputStream dockerfileInputStream;
	
	public DockerfileDelegatingValidator(InputStream dockerfileInputStream){
		this.dockerfileInputStream = dockerfileInputStream;
	}
	
	@SuppressWarnings("unchecked")
	public Map<DockerfileValidationLevel, List<DockerfileValidationResult>> validate(){
		Map<DockerfileValidationLevel, List<DockerfileValidationResult>> result = new HashMap<DockerfileValidationLevel, List<DockerfileValidationResult>>();
		if(this.dockerfileInputStream == null)
			return result;
		
		ValidatorUtils validatorUtils = new ValidatorUtils();
		boolean fromCheck = false;
		int currentLine = 0;
		List<DockerfileValidationResult> errors = new ArrayList<DockerfileValidationResult>();
		List<DockerfileValidationResult> warnings = new ArrayList<DockerfileValidationResult>();
		List<DockerfileValidationResult> infos = new ArrayList<DockerfileValidationResult>();

		Map<String, Object> ruleObject = validatorUtils.getRules(DockerfileDelegatingValidator.class
				.getResourceAsStream("default.yaml"));
		List<Map<String, Object>> requiredInstructions = validatorUtils.createReqInstructionHash(ruleObject);
		Map<String, Object> general = (Map<String, Object>) ruleObject
				.get("general");
		List<String> valid_instructions = (List<String>) general
				.get("valid_instructions");
		Pattern validInstructionsRegex = validatorUtils.createValidCommandRegex(valid_instructions);
		Pattern continuationRegex = null;
//		Pattern ignoreRegex = null;
		Object multiline_regex = general.get("multiline_regex");
		if (multiline_regex != null && multiline_regex.toString().length() > 2) {
			String _multiline_regex = multiline_regex.toString().substring(1,
					multiline_regex.toString().length() - 1);
			continuationRegex = Pattern.compile(_multiline_regex,
					Pattern.CASE_INSENSITIVE);
		}
		Object ignore_regex = general.get("ignore_regex");
		if (ignore_regex != null && ignore_regex.toString().length() > 2) {
			String _ignore_regex = ignore_regex.toString().substring(1,
					ignore_regex.toString().length() - 1);
			Pattern ignoreRegex = Pattern.compile(_ignore_regex,
					Pattern.CASE_INSENSITIVE);
			System.out.println("ignore_regex is not used for now: " + ignoreRegex.pattern());
		}

		try {
			String dockerfile = IOUtils.toString(dockerfileInputStream);
			String[] linesArr = dockerfile.split("(\\r|\\n)");
			if (linesArr != null && linesArr.length > 0) {
				for (int i = 0; i < linesArr.length; i++) {
					currentLine++;
					String line = linesArr[i];
					int lineOffSet = 0;
					if (line == null || line.length() == 0
							|| line.charAt(0) == '#') {
						continue;
					}

					while (validatorUtils.isPartialLine(line, continuationRegex)) {
						line = continuationRegex.matcher(line).replaceAll(" ");
						if (linesArr[currentLine + lineOffSet].charAt(0) == '#') {
							linesArr[currentLine + lineOffSet] = null;
							line = line + "\\";
						} else {
							line = line + linesArr[currentLine + lineOffSet];
							linesArr[currentLine + lineOffSet] = null;
						}
						lineOffSet++;
					}

					// First instruction must be FROM
					if (!fromCheck) {
						fromCheck = true;
						if (line.toUpperCase().indexOf("FROM") != 0) {
							DockerfileValidationResult error = new DockerfileValidationResult();
							error.setLine(currentLine);
							error.setLevel(DockerfileValidationLevel.ERROR);
							error.setMessage("Missing or misplaced FROM");
							error.setLineContent(line);
							errors.add(error);
						}
					}// end for FROM

					Matcher matcher = validInstructionsRegex.matcher(line);
					if (!matcher.find()) {
						DockerfileValidationResult error = new DockerfileValidationResult();
						error.setLine(currentLine);
						error.setLevel(DockerfileValidationLevel.ERROR);
						error.setMessage("Invalid instruction");
						error.setLineContent(line);
						errors.add(error);
					} else {
						String instruction = line.substring(matcher.start(),
								matcher.end()).trim();
						String params = matcher.replaceAll("");
						validatorUtils.checkLineRules(ruleObject, instruction, params, line,
								currentLine, errors, warnings, infos);
						requiredInstructions.remove(instruction);
					}// end for valid instructions checking
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		validatorUtils.checkRequiredInstructions(requiredInstructions, errors, warnings, infos);
		
		result.put(DockerfileValidationLevel.ERROR, errors);
		result.put(DockerfileValidationLevel.WARNING, warnings);
		result.put(DockerfileValidationLevel.INFO, infos);
		return result;		
	}
	
	public static void main(String[] args) {

		InputStream dockerfileInputStream = DockerfileDelegatingValidator.class
				.getResourceAsStream("dockerfile");		
		DockerfileDelegatingValidator validator = new DockerfileDelegatingValidator(dockerfileInputStream);

		Map<DockerfileValidationLevel, List<DockerfileValidationResult>> result = validator.validate();
		List<DockerfileValidationResult> infos = result.get(DockerfileValidationLevel.INFO);
		List<DockerfileValidationResult> errors = result.get(DockerfileValidationLevel.ERROR);
		List<DockerfileValidationResult> warnings = result.get(DockerfileValidationLevel.WARNING);
		
		System.out.println("infos:");
		for (Iterator<DockerfileValidationResult> iterator = infos.iterator(); iterator.hasNext();) {
			DockerfileValidationResult validationResult = (DockerfileValidationResult) iterator
					.next();
			System.out.println(validationResult);
		}
		System.out.println("errors:");
		for (Iterator<DockerfileValidationResult> iterator = errors.iterator(); iterator.hasNext();) {
			DockerfileValidationResult validationResult = (DockerfileValidationResult) iterator
					.next();
			System.out.println(validationResult);
		}
		System.out.println("warnings:");
		for (Iterator<DockerfileValidationResult>iterator = warnings.iterator(); iterator.hasNext();) {
			DockerfileValidationResult validationResult = (DockerfileValidationResult) iterator
					.next();
			System.out.println(validationResult);
		}
	}

}
