package com.headwire.sling.htl;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.models.annotations.Model;

@Model(adaptables = { Resource.class, SlingHttpServletRequest.class })
public final class Component extends ResourceWrapper {

    private static final String APPS_PREFIX = "/apps/";
    private static final String LIBS_PREFIX = "/libs/";

    public Component(final Resource resource) {
        super(resolveType(resource));
    }

    public Component(final SlingHttpServletRequest request) {
        this(request.getResource());
    }

    public static Resource resolveType(final Resource resource) {
        final ResourceResolver resourceResolver = resource.getResourceResolver();
        final String resourceType = resource.getResourceType();
        if (StringUtils.startsWithAny(resourceType, APPS_PREFIX, LIBS_PREFIX)) {
            return resourceResolver.getResource(resourceType);
        }

        final Resource type = resourceResolver.getResource(APPS_PREFIX + resourceType);
        if (type == null) {
            return resourceResolver.getResource(LIBS_PREFIX + resourceType);
        }

        return type;
    }

}
