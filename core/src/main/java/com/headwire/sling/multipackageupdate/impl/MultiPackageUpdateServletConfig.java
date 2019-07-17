package com.headwire.sling.multipackageupdate.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "%config.name", description = "%config.description")
public @interface MultiPackageUpdateServletConfig {

	@AttributeDefinition(name = "%sling.servlet.paths.name", description = "%sling.servlet.paths.description")
	String[] sling_servlet_paths() default "/bin/multipackageupdate-packages";

	@AttributeDefinition(name = "%server.name", description = "%server.description")
	String server_url() default "https://vagrant.headwire.com/peregrine";

	@AttributeDefinition(name = "%filename.name", description = "%filename.description")
	String filename() default "packages.txt";
}
