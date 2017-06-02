package io.oddworks.device.metric;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class OddVideoPlayingMetricTest {
    private String contentType = "aThing";
    private String contentId = "thingId";
    private String title = "vid title";
    private int elapsed = 456;
    private int duration = 1234;
    private int customInterval = 312;
    private OddVideoPlayingMetric oddVideoPlayingMetric;
    private Context ctx;
    private String sessionId;
    private String videoSessionId;

    @Before
    public void beforeEach() {
        sessionId = "foobarbaz";
        videoSessionId = "buzz";
        ctx = InstrumentationRegistry.getTargetContext();
        oddVideoPlayingMetric = new OddVideoPlayingMetric(ctx, contentType, contentId, sessionId, videoSessionId, title, null, elapsed, duration);
    }

    @Test
    public void testGetType() throws Exception {
        assertEquals("event", oddVideoPlayingMetric.getType());
    }

    @Test
    public void testGetAction() throws Exception {
        assertEquals(OddMetric.Type.VIDEO_PLAYING.getAction(), oddVideoPlayingMetric.getAction());
    }

    @Test
    public void testGetContentType() throws Exception {
        assertEquals(contentType, oddVideoPlayingMetric.getContentType());
    }

    @Test
    public void testGetContentId() throws Exception {
        assertEquals(contentId, oddVideoPlayingMetric.getContentId());
    }

    @Test
    public void testGetTitle() throws Exception {
        assertEquals(title, oddVideoPlayingMetric.getTitle());
    }

    @Test
    public void testToJSONObject() throws Exception {
        String expected = "{\"data\": {" +
                "type: \"" + oddVideoPlayingMetric.getType() + "\"," +
                "attributes: {" +
                "action: \"" + oddVideoPlayingMetric.getAction() + "\"," +
                "contentType: \"" + oddVideoPlayingMetric.getContentType() + "\"," +
                "contentId: \"" + oddVideoPlayingMetric.getContentId() + "\"," +
                "contentTitle: \"" + oddVideoPlayingMetric.getTitle() + "\"," +
                "elapsed: " + elapsed + "," +
                "duration: " + duration + "," +
                "sessionId: " + oddVideoPlayingMetric.getSessionId() + "," +
                "videoSessionId: " + oddVideoPlayingMetric.getVideoSessionId() + "," +
                "viewer: \"" + oddVideoPlayingMetric.getViewerId() + "\"" +
                "}" +
                "}}";
        JSONAssert.assertEquals(expected, oddVideoPlayingMetric.toJSONObject(), true);
    }
}