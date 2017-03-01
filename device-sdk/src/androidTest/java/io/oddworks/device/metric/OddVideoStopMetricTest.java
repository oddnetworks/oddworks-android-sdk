package io.oddworks.device.metric;

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

    @Before
    public void beforeEach() {
        oddVideoStopMetric = new OddVideoStopMetric(contentType, contentId, title, null, elapsed, duration);
    }

    @Test
    public void testGetType() throws Exception {
        assertEquals("event", oddVideoStopMetric.getType());
    }

    @Test
    public void testGetAction() throws Exception {
        assertEquals(OddMetric.Companion.getACTION_VIDEO_STOP(), oddVideoStopMetric.getAction());
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
                "title: \"" + oddVideoStopMetric.getTitle() + "\"," +
                "elapsed: " + elapsed + "," +
                "duration: " + duration + "," +
                "viewer: \"" + oddVideoStopMetric.getViewerId() + "\"" +
                "}" +
                "}}";
        JSONAssert.assertEquals(expected, oddVideoStopMetric.toJSONObject(), true);
    }
}
