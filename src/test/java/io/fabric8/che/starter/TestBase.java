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
package io.fabric8.che.starter;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class TestBase {

private static Process proc;
	
	public static final String VERTX_SERVER = "http://localhost:33333";
	public static final String NAMESPACE = "eclipse-che";
	public static final String OPENSHIFT_TOKEN = "openshifttoken";
	public static final String KEYCLOAK_TOKEN = "keycloaktoken";
	public static final String GITHUB_TOKEN = "githubtoken";
	
	@BeforeClass
	public static void setUp() throws IOException {
		System.out.println(new File(".").getAbsolutePath());
		proc = Runtime.getRuntime().exec("java -jar ./target/vertx-server.jar");
	}
	
	@AfterClass
	public static void tearDown() {
		proc.destroyForcibly();
	}
}
