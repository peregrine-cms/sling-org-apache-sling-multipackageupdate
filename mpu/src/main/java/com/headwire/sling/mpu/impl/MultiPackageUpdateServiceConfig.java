package com.headwire.sling.mpu.impl;

/*-
 * #%L
 * Multi Package Update - Core
 * %%
 * Copyright (C) 2017 headwire inc.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */

import com.headwire.sling.mpu.MultiPackageUpdate;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * An <code>@interface</code> that contains configuration of the endpoint
 * that exposes the packages. It also allows to control
 * the download + upload + install process in regards to how many times
 * it will be retried.
 */

@Component
@Designate(ocd = MultiPackageUpdateServiceConfig.Config.class, factory = true)
public class MultiPackageUpdateServiceConfig {
	@ObjectClassDefinition(name = "%config.name", description = "%config.description", localization = "OSGI-INF/l10n/bundle")
	public @interface Config {

		/**
		 * @return Name of the Configuration which is treated as an unique identifier
		 */

		@AttributeDefinition(name = "%mpu.config.name", description = "%mpu.config.name.description")
		String name();

		/**
		 * @return the common path prefix to all packages and their listing file.
		 * Will be ignored for the paths that are absolute, i.e. that start with
		 * <code>http</code> or <code>https</code>.
		 */
		@AttributeDefinition(name = "%serverUrl.name", description = "%serverUrl.description")
		String serverUrl();

		/**
		 * @return the path to the packages listing file. Can be absolute (i.e. start with
		 * <code>http</code> or <code>https</code>) or relative (and then - will be prefixed
		 * with <code>serverUrl</code>).
		 */
		@AttributeDefinition(name = "%filename.name", description = "%filename.description")
		String filename() default "packages.txt";

		/**
		 * @return how many times the download + upload + install process will be replayed in case
		 * of failures.
		 */
		@AttributeDefinition(name = "%maxRetriesCount.name", description = "%maxRetriesCount.description")
		int maxRetriesCount() default 1;
	}

	@Reference
	private MultiPackageUpdate update;

	private Config config;

	@Activate
	public void activate(final Config config) {
		this.config = config;
		update.update(this, null);
	}

	@Modified
	public void modified(final Config config) {
		String oldName = getName();
		this.config = config;
		update.update(this, oldName);
	}

	@Deactivate
	public void deactivate(final Config config) {
		update.update(null, getName());
	}

	public String getName() { return config.name(); }

	public String getServerUrl() { return config.serverUrl(); }

	public String getFilename() { return config.filename(); }

	public int getMaxRetries() { return config.maxRetriesCount(); }
}

