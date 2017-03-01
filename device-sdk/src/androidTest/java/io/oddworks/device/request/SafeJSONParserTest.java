package io.oddworks.device.request;

import android.support.test.runner.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class SafeJSONParserTest {

    @Test
    public void testGetDate() throws Exception {
        JSONObject json = new JSONObject();

        // Short format
        String string1 = "2014-06-12T13:53:54Z";
        String key1 = "short";
        json.put(key1, string1);
        Date date1 = SafeJSONParser.INSTANCE.getDate(json, key1);
        assertThat(date1, isA(Date.class));

        // Long format with Z
        String string2 = "2014-06-12T13:53:54.124-0400";
        String key2 = "long";
        json.put(key2, string1);
        Date date2 = SafeJSONParser.INSTANCE.getDate(json, key2);
        assertThat(date2, isA(Date.class));

        // Long format with X
        String string3 = "2014-06-12T13:53:54.124-04:00";
        String key3 = "long";
        json.put(key3, string1);
        Date date3 = SafeJSONParser.INSTANCE.getDate(json, key3);
        assertThat(date3, isA(Date.class));

        // Long format without zone
        String string4 = "2014-06-12T13:53:54.124Z";
        String key4 = "long";
        json.put(key4, string1);
        Date date4 = SafeJSONParser.INSTANCE.getDate(json, key4);
        assertThat(date4, isA(Date.class));
    }
}
