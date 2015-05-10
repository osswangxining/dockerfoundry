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

package cn.dockerfoundry.ide.eclipse.explorer.ui.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author wangxn
 *
 */
public class DockerContainerInfo {
	private Map<String, String> envMap = new HashMap<String, String>();
	private Set<String> exposedPorts;
	private String containerName;
	private List<String> exposedURLs = new ArrayList<String>();
	private Date startedAt;
	private Date finishedAt;
	private String uptime;
	private String ipAddress;
	private Long memory;
	private Long cpuShares;
	private Boolean running;
	private List<String> links;

	public void addExposedURL(String url) {
		if (url != null) {
			this.exposedURLs.add(url);
		}
	}

	public void addEnv(String key, String value) {
		if (key != null)
			this.envMap.put(key, value);
	}

	public String getExposedPortsAsString() {
		if (exposedPorts != null) {
			StringBuilder sb = new StringBuilder();
			for (Iterator<String> iterator = exposedPorts.iterator(); iterator
					.hasNext();) {
				String p = (String) iterator.next();
				sb.append(p);
				if (iterator.hasNext()) {
					sb.append(",");
				}
			}
			return sb.toString();
		}
		return null;
	}

	/**
	 * @return the envMap
	 */
	public Map<String, String> getEnvMap() {
		return envMap;
	}

	/**
	 * @param envMap
	 *            the envMap to set
	 */
	public void setEnvMap(Map<String, String> envMap) {
		this.envMap = envMap;
	}

	/**
	 * @return the exposedPorts
	 */
	public Set<String> getExposedPorts() {
		return exposedPorts;
	}

	/**
	 * @param exposedPorts
	 *            the exposedPorts to set
	 */
	public void setExposedPorts(Set<String> exposedPorts) {
		this.exposedPorts = exposedPorts;
	}

	/**
	 * @return the containerName
	 */
	public String getContainerName() {
		return containerName;
	}

	/**
	 * @param containerName
	 *            the containerName to set
	 */
	public void setContainerName(String containerName) {
		this.containerName = containerName;
	}

	/**
	 * @return the exposedURLs
	 */
	public List<String> getExposedURLs() {
		return exposedURLs;
	}

	/**
	 * @param exposedURLs
	 *            the exposedURLs to set
	 */
	public void setExposedURLs(List<String> exposedURLs) {
		this.exposedURLs = exposedURLs;
	}

	/**
	 * @return the startedAt
	 */
	public Date getStartedAt() {
		return startedAt;
	}

	/**
	 * @param startedAt
	 *            the startedAt to set
	 */
	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}

	/**
	 * @return the finishedAt
	 */
	public Date getFinishedAt() {
		return finishedAt;
	}

	/**
	 * @param finishedAt
	 *            the finishedAt to set
	 */
	public void setFinishedAt(Date finishedAt) {
		this.finishedAt = finishedAt;
	}

	/**
	 * @return the uptime
	 */
	public String getUptime() {
		return uptime;
	}

	/**
	 * @param uptime
	 *            the uptime to set
	 */
	public void setUptime(String uptime) {
		this.uptime = uptime;
	}

	/**
	 * @return the ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * @param ipAddress
	 *            the ipAddress to set
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * @return the memory
	 */
	public Long getMemory() {
		return memory;
	}

	/**
	 * @param memory
	 *            the memory to set
	 */
	public void setMemory(Long memory) {
		this.memory = memory;
	}

	/**
	 * @return the cpuShares
	 */
	public Long getCpuShares() {
		return cpuShares;
	}

	/**
	 * @param cpuShares
	 *            the cpuShares to set
	 */
	public void setCpuShares(Long cpuShares) {
		this.cpuShares = cpuShares;
	}

	/**
	 * @return the running
	 */
	public Boolean getRunning() {
		return running;
	}

	/**
	 * @param running the running to set
	 */
	public void setRunning(Boolean running) {
		this.running = running;
	}

	/**
	 * @return the links
	 */
	public List<String> getLinks() {
		return links;
	}

	/**
	 * @param links the links to set
	 */
	public void setLinks(List<String> links) {
		this.links = links;
	}
}
