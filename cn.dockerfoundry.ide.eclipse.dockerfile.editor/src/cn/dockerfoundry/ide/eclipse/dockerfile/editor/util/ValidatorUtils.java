package cn.dockerfoundry.ide.eclipse.dockerfile.editor.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.Yaml;

import cn.dockerfoundry.ide.eclipse.dockerfile.validator.DockerfileValidationLevel;
import cn.dockerfoundry.ide.eclipse.dockerfile.validator.DockerfileValidationResult;

public class ValidatorUtils {
	public  final String _ERROR = "error";
	public  final String _WARN = "warn";
	public  final String _INFO = "info";

	public  Map<String, Object> getRules(InputStream input) {
		Yaml yaml = new Yaml();
		Object obj = yaml.load(input);
		if (obj instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, Object> obj2 = (Map<String, Object>) obj;
			return obj2;
		}
		return null;
	}

	public  String getRefUrl(String... strings) {
		String ref_url = "";
		if (strings != null && strings.length > 0) {
			if (strings.length == 1)
				ref_url = (strings[0] != null) ? strings[0] : "None";
			else {
				String base_url = (strings[0] != null) ? strings[0] : "";
				ref_url = (strings[1] != null) ? base_url + strings[1]
						: base_url;
			}
		}
		return ref_url;
	}

	public  Pattern createValidCommandRegex(List<String> commandList) {
		String regexStr = "^(";
		if (commandList != null) {
			StringBuilder sb = new StringBuilder();
			for (Iterator<String> iterator = commandList.iterator(); iterator
					.hasNext();) {
				String object = iterator.next();
				sb.append(object);
				if (iterator.hasNext())
					sb.append("|");
			}
			regexStr = regexStr + sb.toString();
		}
		regexStr = regexStr + ")(\\s)+";
		System.out.println(regexStr);
		return Pattern.compile(regexStr, Pattern.CASE_INSENSITIVE);
	}

	public  List<Map<String, Object>> createReqInstructionHash(
			Map<String, Object> ruleObject) {

		Object required_instructions = ruleObject.get("required_instructions");
		if (required_instructions instanceof List) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> reqInstructions = (List<Map<String, Object>>) required_instructions;
			for (Iterator<Map<String, Object>> iterator = reqInstructions
					.iterator(); iterator.hasNext();) {
				Map<String, Object> map = (Map<String, Object>) iterator.next();
				map.put("exists", false);
			}

			return reqInstructions;
			// for (var i = 0, len = arr.length; i < len; i++) {
			// hash[arr[i].instruction] = arr[i];
			// arr[i].exists = false;
			// }
		}
		return new ArrayList<Map<String, Object>>();
	}
	
	public  boolean isPartialLine(String line, Pattern continuationRegex) {
		if (line == null || continuationRegex == null)
			return false;

		Matcher matcher = continuationRegex.matcher(line);
		return matcher.find();
	}

	@SuppressWarnings("unchecked")
	public  void checkLineRules(Map<String, Object> ruleObject,
			String instruction, String params, String line, int currentLine,
			List<DockerfileValidationResult> errors, List<DockerfileValidationResult> warnings,
			List<DockerfileValidationResult> infos) {
		Map<String, Object> line_rules = (Map<String, Object>) ruleObject
				.get("line_rules");
		// for (String s : line_rules.keySet()) {
		// System.out.println(instruction+"-" + s);
		// System.out.println(s.equals(instruction));
		// }
		if (!line_rules.containsKey(instruction)) {
			System.out.println("No Line Rules for instruction :" + instruction);
			return;
		}
		Object instruction_rules_obj = line_rules.get(instruction);
		if (instruction_rules_obj == null
				|| !(instruction_rules_obj instanceof Map))
			return;
		Map<String, Object> instruction_rules_map = (Map<String, Object>) instruction_rules_obj;
		Object paramSyntaxRegex = instruction_rules_map.get("paramSyntaxRegex");
		if (paramSyntaxRegex != null
				&& paramSyntaxRegex.toString().length() > 2) {
			String _paramSyntaxRegex = paramSyntaxRegex.toString()
					.substring(1, paramSyntaxRegex.toString().length() - 1)
					.trim();
			Matcher paramSyntaxRegexMatcher = Pattern
					.compile(_paramSyntaxRegex).matcher(params);
			if (!paramSyntaxRegexMatcher.find()) {
				DockerfileValidationResult error = new DockerfileValidationResult();
				error.setLine(currentLine);
				error.setLevel(DockerfileValidationLevel.ERROR);
				error.setMessage("Bad Parameters");
				error.setLineContent(line);
				errors.add(error);
			}
		}
		Object rules_obj = instruction_rules_map.get("rules");
		if (rules_obj == null || !(rules_obj instanceof List))
			return;
		List<Map<String, Object>> rules_list = (List<Map<String, Object>>) rules_obj;
		for (Map<String, Object> map : rules_list) {
			Object regex = map.get("regex");
			Object inverse_rule_obj = map.get("inverse_rule");
			boolean inverse_rule = inverse_rule_obj != null
					&& Boolean.parseBoolean(inverse_rule_obj.toString());
			if (regex != null && regex.toString().length() > 2) {
				String _regex = regex.toString().substring(1,
						regex.toString().length() - 1);
				Matcher m = Pattern.compile(_regex).matcher(line);

				Object level_obj = map.get("level");
				Object message_obj = map.get("message");
				Object description_obj = map.get("description");
				Object referenceUrl_obj = map.get("reference_url");
				String level = (level_obj == null) ? _INFO : level_obj
						.toString().toLowerCase();
				boolean isValid = (m.find() && !inverse_rule)
						|| (!m.find() && inverse_rule);
				if (isValid) {
					DockerfileValidationResult r = new DockerfileValidationResult();
					r.setLine(currentLine);
					r.setMessage(message_obj == null ? "" : message_obj
							.toString());
					r.setDescription(description_obj == null ? ""
							: description_obj.toString());
					r.setReferenceUrl(referenceUrl_obj == null ? ""
							: referenceUrl_obj.toString());
					r.setLineContent(line);
					if (level.equals(_ERROR)) {
						r.setLevel(DockerfileValidationLevel.ERROR);
						errors.add(r);
					} else if (level.equals(_WARN)) {
						r.setLevel(DockerfileValidationLevel.WARNING);
						warnings.add(r);
					} else {
						r.setLevel(DockerfileValidationLevel.INFO);
						infos.add(r);
					}
				}
			}
		}// rules
	}

	public  void checkRequiredInstructions(
			List<Map<String, Object>> requiredInstructions,
			List<DockerfileValidationResult> errors, List<DockerfileValidationResult> warnings,
			List<DockerfileValidationResult> infos) {
		for (Map<String, Object> requiredInstruction : requiredInstructions) {
			Object instruction_obj = requiredInstruction.get("instruction");
			Object level_obj = requiredInstruction.get("level");
			Object message_obj = requiredInstruction.get("message");
			Object description_obj = requiredInstruction.get("description");
			Object referenceUrl_obj = requiredInstruction.get("reference_url");
			String level = (level_obj == null) ? _INFO : level_obj.toString()
					.toLowerCase();

			DockerfileValidationResult r = new DockerfileValidationResult();
			// r.setLine(currentLine);
			r.setMessage(message_obj == null ? "" : message_obj.toString());
			r.setDescription(description_obj == null ? "" : description_obj
					.toString());
			r.setReferenceUrl(referenceUrl_obj == null ? "" : referenceUrl_obj
					.toString());
			r.setInstruction(instruction_obj == null ? "" : instruction_obj
					.toString());
			if (level.equals(_ERROR)) {
				r.setLevel(DockerfileValidationLevel.ERROR);
				errors.add(r);
			} else if (level.equals(_WARN)) {
				r.setLevel(DockerfileValidationLevel.WARNING);
				warnings.add(r);
			} else {
				r.setLevel(DockerfileValidationLevel.INFO);
				infos.add(r);
			}

		}
	}
}
