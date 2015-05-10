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

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerState;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.NetworkSettings;
import com.spotify.docker.client.messages.PortBinding;

/**
 * @author wangxn
 *
 */
public class DockerDomainHelper {
	public static DockerContainerInfo getDockerInfo(ContainerInfo containerInfo) {
		if (containerInfo == null)
			return null;
		DockerContainerInfo info = new DockerContainerInfo();
		ContainerConfig config = containerInfo.config();
		if (config != null) {
			
			info.setCpuShares(config.cpuShares());
			info.setMemory(config.memory());
			
			List<String> envList = config.env();
			if (envList != null) {
				for (String env : envList) {
					if (env != null && env.length() > 0) {
						int index = env.indexOf("=");
						String key = (index < 1) ? env : env.substring(0,
								env.indexOf("="));
						String value = (index < 1 || index >= env.length() - 1) ? env
								: env.substring(env.indexOf("=") + 1);
						info.addEnv(key, value);
					}
				}
			}// end for envList

			Set<String> exposedPorts = config.exposedPorts();
			info.setExposedPorts(exposedPorts);

			if (exposedPorts != null) {
				NetworkSettings networkSetting = containerInfo
						.networkSettings();
				if (networkSetting != null) {
					info.setIpAddress(networkSetting.ipAddress());
					
					for (String exposedPort : exposedPorts) {
						Map<String, List<PortBinding>> ports = networkSetting
								.ports();
						if (ports == null)
							continue;
						List<PortBinding> portBindings = ports.get(exposedPort);
						if (portBindings != null) {
							for (PortBinding portBinding : portBindings) {
								String hostPort = portBinding.hostPort();
								String hostIp = portBinding.hostIp();
								if (hostIp == null || hostIp.equals("0.0.0.0"))
									hostIp = "127.0.0.1";
								String url = /*"http://" + */hostIp + ":"
										+ hostPort;
								info.addExposedURL(url);
							}
						}

					}
				}//end for networkSetting
			}//end for exposedPorts
		}

		info.setContainerName(containerInfo.name());
		
		ContainerState state = containerInfo.state();
		info.setStartedAt(state.startedAt());
		info.setRunning(state.running());
		int _uptime = Calendar.getInstance().getTime().compareTo(state.startedAt());
		info.setUptime("Up "+_uptime);
		
		HostConfig hostConfig = containerInfo.hostConfig();
		if(hostConfig != null){
			List<String> links = hostConfig.links();
			
			info.setLinks(links);
		}
		return info;
	}
}
