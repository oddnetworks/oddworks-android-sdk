package io.oddworks.device.request;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;

import io.oddworks.device.model.OddCollection;
import io.oddworks.device.model.OddConfig;
import io.oddworks.device.model.OddPromotion;
import io.oddworks.device.model.OddView;
import io.oddworks.device.model.OddViewer;
import io.oddworks.device.model.common.OddIdentifier;
import io.oddworks.device.model.common.OddImage;
import io.oddworks.device.model.common.OddRelationship;
import io.oddworks.device.model.common.OddResource;
import io.oddworks.device.model.common.OddResourceType;
import io.oddworks.device.model.config.Display;
import io.oddworks.device.model.config.Features;
import io.oddworks.device.model.config.features.Authentication;
import io.oddworks.device.testutils.AssetUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class OddParserTest {

    private Context ctx;

    @Before
    public void beforeEach() throws IOException {
        ctx = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testParseConfigResponse() throws Exception {
        String json = AssetUtils.readFileToString(ctx, "ConfigResponse.json");
        OddConfig oddConfig = (OddConfig) OddParser.INSTANCE.parseSingleResponse(json);

        assertThat(oddConfig.getId(), is("channel-id-channel-id-android"));
        assertThat(oddConfig.getType(), is(OddResourceType.CONFIG));

        // Views
        assertThat(oddConfig.getViews().size(), is(3));
        String homepageId = "channel-id-home-view";
        assertThat(oddConfig.getViews().get("homepage"), is(homepageId));
        String splashId = "channel-id-splash-view";
        assertThat(oddConfig.getViews().get("splash"), is(splashId));

        // Features
        Features features = oddConfig.getFeatures();
        // - Sharing
        assertTrue(features.getSharing().getEnabled());
        assertThat(features.getSharing().getText(), is("Watch the Channel Name shows Live on mobile and TV connected platforms!"));
        // - Authentication
        assertTrue(features.getAuthentication().getEnabled());
        assertThat(features.getAuthentication().getType(), is(Authentication.AuthenticationType.LINK));
        assertThat(features.getAuthentication().getProperties().get("url"), is("http://oddchannels.com/login"));
        // - Metrics
        assertTrue(features.getMetricsEnabled());
        assertThat(features.getMetrics().size(), is(8));

        // Display
        Display display = oddConfig.getDisplay();
        // - Images
        assertThat(display.getImages().size(), is(1));
        OddImage image = display.getImages().iterator().next();
        assertThat(image.getUrl(), is("https://assets.oddnetworks.com/paul.svg"));
        assertThat(image.getLabel(), is("logo"));
        assertThat(image.getMimeType(), is("image/svg+xml"));
        assertThat(image.getWidth(), is(0));
        assertThat(image.getHeight(), is(0));
        // - Colors
        assertThat(display.getColors().size(), is(0));
        // - Fonts
        assertThat(display.getFonts().size(), is(0));
    }

    @Test
    public void testParseViewResponse() throws Exception {
        String json = AssetUtils.readFileToString(ctx, "ViewResponse.json");
        OddView oddView = (OddView) OddParser.INSTANCE.parseSingleResponse(json);

        assertThat(oddView.getId(), is("channel-id-home-view"));
        assertThat(oddView.getType(), is(OddResourceType.VIEW));

        assertThat(oddView.getTitle(), is("Channel Sample Homepage"));

        // Relationships
        assertThat(oddView.getRelationships().size(), is(2));
        OddRelationship callouts = oddView.getRelationship("callouts");
        assertThat(callouts.getName(), is("callouts"));
        Iterator<OddIdentifier> identifiers = callouts.getIdentifiers().iterator();
        OddIdentifier id1 = identifiers.next();
        OddIdentifier id2 = identifiers.next();
        OddIdentifier id3 = identifiers.next();
        assertFalse(identifiers.hasNext());
        assertThat(id1.getId(), is("channel-id-callout-1"));
        assertThat(id1.getType(), is(OddResourceType.COLLECTION));
        assertThat(id2.getId(), is("channel-id-callout-2"));
        assertThat(id2.getType(), is(OddResourceType.COLLECTION));
        assertThat(id3.getId(), is("channel-id-callout-3"));
        assertThat(id3.getType(), is(OddResourceType.COLLECTION));

        OddRelationship promotion = oddView.getRelationship("promotion");
        assertThat(promotion.getName(), is("promotion"));
        Iterator<OddIdentifier> promotionIdentifiers = promotion.getIdentifiers().iterator();
        OddIdentifier pid1 = promotionIdentifiers.next();
        assertFalse(promotionIdentifiers.hasNext());
        assertThat(pid1.getId(), is("channel-id-promotion"));
        assertThat(pid1.getType(), is(OddResourceType.PROMOTION));
    }

    @Test
    public void testParseViewResponseWithIncluded() throws Exception {
        String json = AssetUtils.readFileToString(ctx, "ViewResponseWithIncluded.json");
        OddView oddView = (OddView) OddParser.INSTANCE.parseSingleResponse(json);

        assertThat(oddView.getId(), is("channel-id-home-view"));
        assertThat(oddView.getType(), is(OddResourceType.VIEW));

        assertThat(oddView.getTitle(), is("Channel Sample Homepage"));

        // Relationships
        assertThat(oddView.getRelationships().size(), is(2));
        OddRelationship calloutRelationship = oddView.getRelationship("callouts");
        assertThat(calloutRelationship.getName(), is("callouts"));
        Iterator<OddIdentifier> calloutIdentifiers = calloutRelationship.getIdentifiers().iterator();
        OddIdentifier id1 = calloutIdentifiers.next();
        OddIdentifier id2 = calloutIdentifiers.next();
        OddIdentifier id3 = calloutIdentifiers.next();
        assertFalse(calloutIdentifiers.hasNext());
        assertThat(id1.getId(), is("channel-id-callout-1"));
        assertThat(id1.getType(), is(OddResourceType.COLLECTION));
        assertThat(id2.getId(), is("channel-id-callout-2"));
        assertThat(id2.getType(), is(OddResourceType.COLLECTION));
        assertThat(id3.getId(), is("channel-id-callout-3"));
        assertThat(id3.getType(), is(OddResourceType.COLLECTION));

        OddRelationship promotionRelationship = oddView.getRelationship("promotion");
        assertThat(promotionRelationship.getName(), is("promotion"));
        Iterator<OddIdentifier> promotionIdentifiers = promotionRelationship.getIdentifiers().iterator();
        OddIdentifier pid1 = promotionIdentifiers.next();
        assertFalse(promotionIdentifiers.hasNext());
        assertThat(pid1.getId(), is("channel-id-promotion"));
        assertThat(pid1.getType(), is(OddResourceType.PROMOTION));

        // Included
        assertThat(oddView.getIncluded().size(), is(4));
        // - callouts
        LinkedHashSet<OddResource> callouts = oddView.getIncludedByRelationship("callouts");
        Iterator<OddResource> calloutIterator = callouts.iterator();
        OddCollection callout1 = (OddCollection) calloutIterator.next();
        OddCollection callout2 = (OddCollection) calloutIterator.next();
        OddCollection callout3 = (OddCollection) calloutIterator.next();
        assertFalse(calloutIterator.hasNext());
        // - promotion
        LinkedHashSet<OddResource> promotion = oddView.getIncludedByRelationship("promotion");
        Iterator<OddResource> promotionIterator = promotion.iterator();
        OddPromotion promotion1 = (OddPromotion) promotionIterator.next();
        assertFalse(promotionIterator.hasNext());
    }

    @Test
    public void testParseViewer() throws Exception {
        String json = AssetUtils.readFileToString(ctx, "ViewerResponse.json");
        OddViewer oddViewer = (OddViewer) OddParser.INSTANCE.parseSingleResponse(json);

        assertThat(oddViewer.getId(), is("success@channel-name.com"));
    }
}
