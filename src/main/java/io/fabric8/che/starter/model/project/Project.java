/*-
 * #%L
 * che-starter
 * %%
 * Copyright (C) 2017 Red Hat, Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */
package io.fabric8.che.starter.model.project;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * @author mlabuda@redhat.com
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {

	private String name;
	private String projectType;
	// DEFAULT VALUE SET IN CHE STARTER
	private String description;
	private String path;
	private Source source;
	private Attribute attributes;
	private List<ProjectLink> links;
	private List<String> mixins;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getProjectType() {
		return projectType;
	}
	public void setProjectType(String projectType) {
		this.projectType = projectType;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Source getSource() {
		return source;
	}
	public void setSource(Source source) {
		this.source = source;
	}
	public Attribute getAttributes() {
		return attributes;
	}
	public void setAttributes(Attribute attributes) {
		this.attributes = attributes;
	}
	public List<ProjectLink> getLinks() {
		return links;
	}
	public void setLinks(List<ProjectLink> links) {
		this.links = links;
	}
	public List<String> getMixins() {
		return mixins;
	}
	public void setMixins(List<String> mixins) {
		this.mixins = mixins;
	}
}
