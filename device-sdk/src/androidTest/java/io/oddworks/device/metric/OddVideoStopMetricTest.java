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
public class OddVideoStopMetricTest {
    private String contentType = "aThing";
    private String contentId = "thingId";
    private String title = "vid title";
    private int elapsed = 456;
    private int duration = 1234;
    private OddVideoStopMetric oddVideoStopMetric;
    private Context ctx;
    private String sessionId;
    private String videoSessionId;

    @Before
    public void beforeEach() {
        sessionId = "foobarbaz";
        videoSessionId = "buzz";
        ctx = InstrumentationRegistry.getTargetContext();
        oddVideoStopMetric = new OddVideoStopMetric(ctx, contentType, contentId, sessionId, videoSessionId, title, null, elapsed, duration);
    }

    @Test
    public void testGetType() throws Exception {
        assertEquals("event", oddVideoStopMetric.getType());
    }

    @Test
    public void testGetAction() throws Exception {
        assertEquals(OddMetric.Type.VIDEO_STOP.getAction(), oddVideoStopMetric.getAction());
    }

    @Test
    public void testGetContentType() throws Exception {
        assertEquals(contentType, oddVideoStopMetric.getContentType());
    }

    @Test
    public void testGetContentId() throws Exception {
        assertEquals(contentId, oddVideoStopMetric.getContentId());
    }

    @Test
    public void testGetTitle() throws Exception {
        assertEquals(title, oddVideoStopMetric.getTitle());
    }

    @Test
    public void testToJSONObject() throws Exception {

        String expected = "{\"data\": {" +
                "type: \"" + oddVideoStopMetric.getType() + "\"," +
                "attributes: {" +
                "action: \"" + oddVideoStopMetric.getAction() + "\"," +
                "contentType: \"" + oddVideoStopMetric.getContentType() + "\"," +
                "contentId: \"" + oddVideoStopMetric.getContentId() + "\"," +
                "contentTitle: \"" + oddVideoStopMetric.getTitle() + "\"," +
                "sessionId: \"" + oddVideoStopMetric.getSessionId() + "\"," +
                "videoSessionId: \"" + oddVideoStopMetric.getVideoSessionId() + "\"," +
                "elapsed: " + elapsed + "," +
                "duration: " + duration + "," +
                "viewer: \"" + oddVideoStopMetric.getViewerId() + "\"" +
                "}" +
                "}}";
        JSONAssert.assertEquals(expected, oddVideoStopMetric.toJSONObject(), true);
    }
}
