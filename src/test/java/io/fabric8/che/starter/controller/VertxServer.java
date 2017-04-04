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
package io.fabric8.che.starter.controller;

import java.io.IOException;

public class VertxServer {

	public static final int NUMBER_OF_TEST_CLASSES_USING_VERTX_SERVER = 2;
	
	private int vertxServerUsedTimes = 0;
	
	private static VertxServer instance;
	
	private Process process;

	private VertxServer() {
	}

	public static VertxServer getInstance() {
		if (instance == null) {
			instance = new VertxServer();
		}
		return instance;
	}

	public void startVertxServer() throws IOException {
		vertxServerUsedTimes++;
		if (process == null) {
			process = Runtime.getRuntime().exec("java -jar ./target/vertx-server.jar");
		}
	}

	public void stopVertxServer() throws InterruptedException {
		if (vertxServerUsedTimes == NUMBER_OF_TEST_CLASSES_USING_VERTX_SERVER &&
				process != null) {
			process.destroyForcibly();
			process.waitFor();
		}
	}
}
