package com.headwire.sling.htl;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.models.annotations.Model;

@Model(adaptables = { Resource.class, SlingHttpServletRequest.class })
public final class Component extends ResourceWrapper {

    protected static final String APPS_PREFIX = "/apps/";
    protected static final String LIBS_PREFIX = "/libs/";

    public Component(final Resource resource) {
        super(resolveType(resource));
    }

    public Component(final SlingHttpServletRequest request) {
        this(request.getResource());
    }

    private static Resource resolveType(final Resource resource) {
        final String resourceType = resource.getResourceType();
        final ResourceResolver resourceResolver = resource.getResourceResolver();
        if (StringUtils.isBlank(resourceType)) {
            return new NonExistingResource(resourceResolver, resourceType);
        }

        Resource type = null;
        if (StringUtils.startsWithAny(resourceType, APPS_PREFIX, LIBS_PREFIX)) {
            type = resourceResolver.getResource(resourceType);
        }

        final String appsResourceType = APPS_PREFIX + resourceType;
        if (type == null) {
            type = resourceResolver.getResource(appsResourceType);
        }

        if (type == null) {
            type = resourceResolver.getResource(LIBS_PREFIX + resourceType);
        }

        if (type == null) {
            return new NonExistingResource(resourceResolver, appsResourceType);
        }

        return type;
    }

}
