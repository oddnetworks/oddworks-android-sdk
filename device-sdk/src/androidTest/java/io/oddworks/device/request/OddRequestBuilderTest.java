package io.oddworks.device.request;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import io.oddworks.device.Oddworks;
import io.oddworks.device.exception.OddRequestException;
import io.oddworks.device.metric.OddAppInitMetric;
import io.oddworks.device.metric.OddMetric;
import io.oddworks.device.model.OddAuthentication;
import io.oddworks.device.model.common.OddIdentifier;
import io.oddworks.device.model.common.OddRelationship;
import io.oddworks.device.model.common.OddResource;
import io.oddworks.device.model.common.OddResourceType;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class OddRequestBuilderTest {
    private Context ctx;
    private OddRequest.Builder builder;

    @Before
    public void beforeEach() {
        ctx = InstrumentationRegistry.getTargetContext();
        builder = new OddRequest.Builder(ctx, OddResourceType.CONFIG);
    }

    @Test
    public void testInclude() throws Exception {
        assertNull(builder.getInclude());
        OddRequest.Builder test = builder.include("foo,bar");
        assertThat(test.getInclude(), is("foo,bar"));
    }

    @Test
    public void testAuthorizationJWT() throws Exception {
        assertNull(builder.getAuthorizationJWT());
        OddRequest.Builder test = builder.authorizationJWT("a1c81ed2-76d8-4394-94c0-8e0cf299ce3e");
        assertThat(test.getAuthorizationJWT(), is("a1c81ed2-76d8-4394-94c0-8e0cf299ce3e"));
    }

    @Test
    public void testVersionName() throws Exception {
        assertNull(builder.getVersionName());
        OddRequest.Builder test = builder.versionName("1.0");
        assertThat(test.getVersionName(), is("1.0"));
    }

    @Test
    public void testAcceptLanguageHeader() throws Exception {
        assertNull(builder.getAcceptLanguageHeader());
        OddRequest.Builder test = builder.acceptLanguage("pt-br");
        assertThat(test.getAcceptLanguageHeader(), is("pt-br"));
    }

    @Test
    public void testApiBaseURL() throws Exception {
        assertNull(builder.getApiBaseURL());
        OddRequest.Builder test = builder.apiBaseURL("http://foo.bar");
        assertThat(test.getApiBaseURL(), is("http://foo.bar"));
    }

    @Test
    public void testResourceId() throws Exception {
        assertNull(builder.getResourceId());
        OddRequest.Builder test = builder.resourceId("123");
        assertThat(test.getResourceId(), is("123"));
    }

    @Test
    public void testRelationshipName() throws Exception {
        assertNull(builder.getRelationshipName());
        OddRequest.Builder test = builder.relationshipName("entities");
        assertThat(test.getRelationshipName(), is("entities"));
    }

    @Test
    public void testLimit() throws Exception {
        assertNull(builder.getLimit());
        OddRequest.Builder test = builder.limit(3);
        assertThat(test.getLimit(), is(3));
    }

    @Test
    public void testOffset() throws Exception {
        assertNull(builder.getOffset());
        OddRequest.Builder test = builder.offset(3);
        assertThat(test.getOffset(), is(3));
    }

    @Test
    public void testSort() throws Exception {
        assertNull(builder.getSort());
        OddRequest.Builder test = builder.sort("meta.source");
        assertThat(test.getSort(), is("meta.source"));
    }

    @Test
    public void testQuery() throws Exception {
        assertNull(builder.getQuery());
        OddRequest.Builder test = builder.query("wakeboarding video");
        assertThat(test.getQuery(), is("wakeboarding video"));
    }

    @Test
    public void testEvent() throws Exception {
        OddAppInitMetric event = new OddAppInitMetric();
        assertNull(builder.getEvent());
        OddRequest.Builder test = builder.event(event);
        assertThat(test.getEvent(), is((OddMetric) event));
    }

    @Test
    public void testAuthentication() throws Exception {
        String email = "email@example.com";
        String password = "secret";
        OddRequest.Builder test = builder.login("email@example.com", "secret");
        assertThat(test.getAuthentication().getEmail(), is(email));
        assertThat(test.getAuthentication().getPassword(), is(password));
    }

    @Test
    public void testSkipCache() throws Exception {
        assertFalse(builder.getSkipCache());
        OddRequest.Builder test = builder.skipCache(true);
        assertTrue(test.getSkipCache());
    }

    // TODO - figure out how to set PackageInfo meta-data
    @Test
    public void testBuildFailsMissingApiBaseURL() throws Exception {
        try {
            builder
                    .versionName("blowfish")
                    .build();
        } catch (OddRequestException e) {
            assertThat(e.getMessage(), is("Missing " + Oddworks.API_BASE_URL_KEY + " in Application meta-data"));
        }
    }

    @Test
    public void testBuildWithUserDefinedJWTAndVersionNameAndURL() throws Exception {
        OddRequest request = builder
                                .versionName("blowfish")
                                .authorizationJWT("c58654a3-0be7-4ea7-a6f9-ed1cc6a403ea")
                                .apiBaseURL("http://example.com")
                                .build();
        assertThat(request, isA(OddRequest.class));
    }
}
