package io.oddworks.device.metric;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class OddVideoErrorMetricTest {
    private String contentType = "aThing";
    private String contentId = "thingId";
    private String title = "vid title";
    private OddVideoErrorMetric oddVideoErrorMetric;
    private Context ctx;
    private String sessionId;
    private String videoSessionId;

    @Before
    public void beforeEach() {
        sessionId = "foobarbaz";
        videoSessionId = "buzz";
        ctx = InstrumentationRegistry.getTargetContext();
        oddVideoErrorMetric = new OddVideoErrorMetric(ctx, contentType, contentId, sessionId, videoSessionId, title, null);
    }

    @Test
	public void testGetType() throws Exception {
        assertEquals("event", oddVideoErrorMetric.getType());
    }
    @Test
	public void testGetAction() throws Exception {
        assertEquals(OddMetric.Type.VIDEO_ERROR.getAction(), oddVideoErrorMetric.getAction());
    }

    @Test
	public void testGetContentType() throws Exception {
        assertEquals(contentType, oddVideoErrorMetric.getContentType());
    }

    @Test
	public void testGetContentId() throws Exception {
        assertEquals(contentId, oddVideoErrorMetric.getContentId());
    }

    @Test
    public void testGetTitle() throws Exception {
        assertEquals(title, oddVideoErrorMetric.getTitle());
    }

    @Test
	public void testToJSONObject() throws Exception {
        OddVideoErrorMetric metric = new OddVideoErrorMetric(ctx, contentType, contentId, sessionId, videoSessionId, title, null);

        String expected = "{\"data\": {" +
                "type: \"" + metric.getType() + "\"," +
                "attributes: {" +
                "action: \"" + metric.getAction() + "\"," +
                "contentType: \"" + metric.getContentType() + "\"," +
                "contentTitle: \"" + metric.getTitle() + "\"," +
                "contentId: \"" + metric.getContentId() + "\"," +
                "sessionId: \"" + metric.getSessionId() + "\"," +
                "videoSessionId: \"" + metric.getVideoSessionId() + "\"," +
                "viewer: \"" + metric.getViewerId() + "\"" +
                "}" +
                "}}";

        JSONAssert.assertEquals(expected, metric.toJSONObject(), true);
    }
}