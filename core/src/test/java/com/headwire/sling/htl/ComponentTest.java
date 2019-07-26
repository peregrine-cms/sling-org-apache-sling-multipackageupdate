package com.headwire.sling.htl;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.NonExistingResource;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.headwire.sling.htl.Component.APPS_PREFIX;
import static com.headwire.sling.htl.Component.LIBS_PREFIX;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ComponentTest {

    public static final String RESOURCE_TYPE = "my-app/component";
    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private Resource resource;

    @Mock
    private ResourceResolver resourceResolver;

    @Mock
    private Resource type;

    @Before
    public void setUp() {
        when(request.getResource())
                .thenReturn(resource);
        when(resource.getResourceResolver())
                .thenReturn(resourceResolver);
    }

    private Component createModel(final String resourceType) {
        when(resource.getResourceType())
                .thenReturn(resourceType);
        return new Component(request);
    }

    @Test
    public void missingResourceType() {
        final Component model = createModel(null);
        Assert.assertEquals(NonExistingResource.class, model.getResource().getClass());
    }

    @Test
    public void missingResourceTypeResource() {
        final Component model = createModel(RESOURCE_TYPE);
        Assert.assertEquals(NonExistingResource.class, model.getResource().getClass());
    }

    private void mockGetResource(final String resourceType) {
        when(resourceResolver.getResource(resourceType))
                .thenReturn(type);
    }

    @Test
    public void absoluteAppsComponent() {
        final String resourceType = APPS_PREFIX + RESOURCE_TYPE;
        mockGetResource(resourceType);
        final Component model = createModel(resourceType);
        Assert.assertEquals(type, model.getResource());
    }

    @Test
    public void relativeAppsComponent() {
        final String resourceType = RESOURCE_TYPE;
        mockGetResource(APPS_PREFIX + resourceType);
        final Component model = createModel(resourceType);
        Assert.assertEquals(type, model.getResource());
    }

    @Test
    public void relativeLibsComponent() {
        final String resourceType = RESOURCE_TYPE;
        mockGetResource(LIBS_PREFIX + resourceType);
        final Component model = createModel(resourceType);
        Assert.assertEquals(type, model.getResource());
    }
}
