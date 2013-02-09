/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jarachne.sentry.util.logging;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;
import org.jarachne.sentry.util.logging.jdk.JdkESLoggerFactory;
import org.jarachne.sentry.util.logging.log4j.Log4jESLoggerFactory;


/**
 * @author kimchy (shay.banon)
 */
public abstract class ESLoggerFactory {

	private static volatile ESLoggerFactory defaultFactory = new JdkESLoggerFactory();

	private static String logpath = "./conf/log4j.properties";
	
	static {
		// try {
		// Class.forName("org.slf4j.Logger");
		// defaultFactory = new Slf4jESLoggerFactory();
		// } catch (Throwable e) {
		// throw new RuntimeException(e);
		// no slf4j
		try {
			Class.forName("org.apache.log4j.Logger");
			File f = new File( logpath );
			if (!f.exists()) {
				System.out.println("Cannot find log config file:"
						+ f.getAbsolutePath());
			}
			PropertyConfigurator.configureAndWatch(f.getAbsolutePath());
			defaultFactory = new Log4jESLoggerFactory();

		} catch (Throwable e1) {
			// no log4j
		}
		// }
	}

	/**
	 * Changes the default factory.
	 */
	public static void setDefaultFactory(ESLoggerFactory defaultFactory) {
		if (defaultFactory == null) {
			throw new NullPointerException("defaultFactory");
		}
		ESLoggerFactory.defaultFactory = defaultFactory;
	}

	public static ESLogger getLogger(String prefix, String name) {
		return defaultFactory.newInstance(prefix, name);
	}

	public static ESLogger getLogger(String name) {
		return defaultFactory.newInstance(name);
	}

	public ESLogger newInstance(String name) {
		return newInstance(null, name);
	}

	public abstract ESLogger newInstance(String prefix, String name);
}
