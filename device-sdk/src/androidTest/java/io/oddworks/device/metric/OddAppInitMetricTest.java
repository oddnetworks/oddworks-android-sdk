package io.oddworks.device.metric;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class OddAppInitMetricTest {
    private OddAppInitMetric oddAppInitMetric;
    private JSONObject customJSON;
    private Context ctx;
    private String sessionId;

    @Before
    public void beforeEach() {
        sessionId = "foobarbaz";
        ctx = InstrumentationRegistry.getTargetContext();
        customJSON = new JSONObject();
        oddAppInitMetric = new OddAppInitMetric(ctx, null, null, sessionId, customJSON);
    }

    @Test
	public void testGetType() throws Exception {
        assertEquals("event", oddAppInitMetric.getType());
    }


    @Test
	public void testGetAction() throws Exception {
        assertEquals(OddMetric.Type.APP_INIT.getAction(), oddAppInitMetric.getAction());
    }

    @Test
	public void testGetContentType() throws Exception {
        assertEquals(null, oddAppInitMetric.getContentType());
    }

    @Test
	public void testGetContentId() throws Exception {
        assertEquals(null, oddAppInitMetric.getContentId());
    }

    @Test
	public void testToJSONObject() throws Exception {
        customJSON.put("foo", "bar");
        OddAppInitMetric metric = new OddAppInitMetric(ctx, null, null, sessionId, customJSON);

        String expected = "{\"data\": {" +
                "type: \"" + metric.getType() + "\"," +
                "attributes: {" +
                "action: \"" + metric.getAction() + "\"," +
                "sessionId: \"" + metric.getSessionId() + "\"," +
                "viewer: \"" + metric.getViewerId() + "\"" +
                "}," +
                "meta: {" +
                "foo: \"bar\"" +
                "}}}";

        JSONAssert.assertEquals(expected, metric.toJSONObject(), true);
    }
}