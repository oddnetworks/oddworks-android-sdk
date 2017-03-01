package io.oddworks.device.metric;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class OddViewLoadMetricTest {
    private String contentType = "aThing";
    private String contentId = "thingId";
    private String title = "view title";
    private OddViewLoadMetric oddViewLoadMetric;

    @Before
    public void beforeEach() {
        oddViewLoadMetric = new OddViewLoadMetric(contentType, contentId, title, null);
    }

    @Test
	public void testGetType() throws Exception {
        assertEquals("event", oddViewLoadMetric.getType());
    }

    @Test
	public void testGetAction() throws Exception {
        assertEquals(OddMetric.Companion.getACTION_VIEW_LOAD(), oddViewLoadMetric.getAction());
    }

    @Test
	public void testGetContentType() throws Exception {
        assertEquals(contentType, oddViewLoadMetric.getContentType());
    }

    @Test
	public void testGetContentId() throws Exception {
        assertEquals(contentId, oddViewLoadMetric.getContentId());
    }

    @Test
    public void testGetTitle() throws Exception {
        assertEquals(title, oddViewLoadMetric.getTitle());
    }

    @Test
	public void testToJSONObject() throws Exception {

        String expected = "{\"data\": {" +
                "type: \"" + oddViewLoadMetric.getType() + "\"," +
                "attributes: {" +
                "action: \"" + oddViewLoadMetric.getAction() + "\"," +
                "contentType: \"" + oddViewLoadMetric.getContentType() + "\"," +
                "contentId: \"" + oddViewLoadMetric.getContentId() + "\"," +
                "title: \"" + oddViewLoadMetric.getTitle() + "\"," +
                "viewer: \"" + oddViewLoadMetric.getViewerId() + "\"" +
                "}" +
                "}}";
        JSONAssert.assertEquals(expected, oddViewLoadMetric.toJSONObject(), true);
    }
}