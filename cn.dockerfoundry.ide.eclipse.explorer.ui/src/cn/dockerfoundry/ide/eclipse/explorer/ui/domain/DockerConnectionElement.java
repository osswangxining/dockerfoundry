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

package cn.dockerfoundry.ide.eclipse.explorer.ui.domain;

import java.nio.file.Paths;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificateException;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;

/**
 * @author wangxn
 *
 */
public class DockerConnectionElement implements IAdaptable {
	private String name;
	private boolean useDefault;
	private boolean useUnixSocket;
	private boolean useHTTPS;
	private boolean enableAuth;
	private String socketPath;
	private String host;
	private String authPath;
	private List<DockerContainerElement> containers;
	private List<DockerImageElement> images;
	
	public DockerClient getDockerClient() throws DockerCertificateException {
		DockerClient client = null;
		if (this.isUseDefault()) {
			client = DefaultDockerClient.builder()
					.uri(DefaultDockerClient.DEFAULT_UNIX_ENDPOINT).build();
		} else {
			if (this.isUseUnixSocket()) {
				client = DefaultDockerClient.builder().uri(getSocketPath())
						.build();
			} else if (isUseHTTPS()) {
				if (isEnableAuth()) {
					client = DefaultDockerClient
							.builder()
							.dockerCertificates(
									new DockerCertificates(Paths
											.get(getAuthPath())))
							.uri(getHost()).build();
				} else {
					client = DefaultDockerClient.builder().uri(getHost())
							.build();
				}
			}
		}
		return client;
	}
	
	public Object getAdapter(Class key) {
		return null;
	}


	public String getName() {
		return name;
	}


	public void setName(String repository) {
		this.name = repository;
	}


	public boolean isUseDefault() {
		return useDefault;
	}


	public void setUseDefault(boolean useDefault) {
		this.useDefault = useDefault;
	}


	public boolean isUseUnixSocket() {
		return useUnixSocket;
	}


	public void setUseUnixSocket(boolean useUnixSocket) {
		this.useUnixSocket = useUnixSocket;
	}


	public boolean isUseHTTPS() {
		return useHTTPS;
	}


	public void setUseHTTPS(boolean useHTTPS) {
		this.useHTTPS = useHTTPS;
	}


	public boolean isEnableAuth() {
		return enableAuth;
	}


	public void setEnableAuth(boolean enableAuth) {
		this.enableAuth = enableAuth;
	}


	public String getSocketPath() {
		return socketPath;
	}


	public void setSocketPath(String socketPath) {
		this.socketPath = socketPath;
	}


	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public String getAuthPath() {
		return authPath;
	}


	public void setAuthPath(String authPath) {
		this.authPath = authPath;
	}


	public List<DockerContainerElement> getContainers() {
		return containers;
	}


	public void setContainers(List<DockerContainerElement> containers) {
		this.containers = containers;
	}


	public List<DockerImageElement> getImages() {
		return images;
	}


	public void setImages(List<DockerImageElement> images) {
		this.images = images;
	}
}

