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
public class OddVideoPlayMetricTest {
    private String contentType = "aThing";
    private String contentId = "thingId";
    private String title = "vid title";
    private OddVideoPlayMetric oddVideoPlayMetric;
    private Context ctx;
    private String sessionId;
    private String videoSessionId;

    @Before
    public void beforeEach() {
        sessionId = "foobarbaz";
        videoSessionId = "buzz";
        ctx = InstrumentationRegistry.getTargetContext();
        oddVideoPlayMetric = new OddVideoPlayMetric(ctx, contentType, contentId, sessionId, videoSessionId, title, null);
    }

    @Test
	public void testGetType() throws Exception {
        assertEquals("event", oddVideoPlayMetric.getType());
    }

    @Test
	public void testGetAction() throws Exception {
        assertEquals(OddMetric.Type.VIDEO_PLAY.getAction(), oddVideoPlayMetric.getAction());
    }

    @Test
	public void testGetContentType() throws Exception {
        assertEquals(contentType, oddVideoPlayMetric.getContentType());
    }

    @Test
	public void testGetContentId() throws Exception {
        assertEquals(contentId, oddVideoPlayMetric.getContentId());
    }

    @Test
    public void testGetTitle() throws Exception {
        assertEquals(title, oddVideoPlayMetric.getTitle());
    }

    @Test
	public void testToJSONObject() throws Exception {
        String expected = "{\"data\": {" +
                "type: \"" + oddVideoPlayMetric.getType() + "\"," +
                "attributes: {" +
                "action: \"" + oddVideoPlayMetric.getAction() + "\"," +
                "contentType: \"" + oddVideoPlayMetric.getContentType() + "\"," +
                "contentTitle: \"" + oddVideoPlayMetric.getTitle() + "\"," +
                "contentId: \"" + oddVideoPlayMetric.getContentId() + "\"," +
                "sessionId: \"" + oddVideoPlayMetric.getSessionId() + "\"," +
                "videoSessionId: \"" + oddVideoPlayMetric.getVideoSessionId() + "\"," +
                "viewer: \"" + oddVideoPlayMetric.getViewerId() + "\"" +
                "}" +
                "}}";
        JSONAssert.assertEquals(expected, oddVideoPlayMetric.toJSONObject(), true);
    }
}