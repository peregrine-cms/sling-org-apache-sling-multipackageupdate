package com.headwire.sling.update.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "%config.name", description = "%config.description")
public @interface UpdatePackagesServletConfig {

	@AttributeDefinition(name = "%sling.servlet.paths.name", description = "%sling.servlet.paths.description")
	String[] sling_servlet_paths() default "/bin/update-packages";

	@AttributeDefinition(name = "%subservice.name", description = "%subservice.description")
	String subservice() default "updater";

	@AttributeDefinition(name = "%server.name", description = "%server.description")
	String server_url() default "https://vagrant.headwire.com/peregrine";

	@AttributeDefinition(name = "%filename.name", description = "%filename.description")
	String filename() default "packages.txt";
}
