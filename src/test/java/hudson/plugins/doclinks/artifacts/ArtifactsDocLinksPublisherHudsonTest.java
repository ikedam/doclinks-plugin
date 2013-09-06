/*
 * The MIT License
 * 
 * Copyright (c) 2013 IKEDA Yasuyuki
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.plugins.doclinks.artifacts;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import hudson.Util;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.plugins.doclinks.artifacts.testtools.ArtifactDocLinksHudsonTestCase;
import hudson.plugins.doclinks.artifacts.testtools.TestZipBuilder;
import hudson.tasks.ArtifactArchiver;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 *
 */
public class ArtifactsDocLinksPublisherHudsonTest extends ArtifactDocLinksHudsonTestCase {
    public static final int BUILD_TIMEOUT = 10;
    
    public void testPublishSingleArtifact() throws Exception {
        FreeStyleProject p = createFreeStyleProject();
        p.getPublishersList().add(new ArtifactArchiver("artifact1.zip", "", false));
        p.getPublishersList().add(new ArtifactsDocLinksPublisher(Arrays.asList(
                new ArtifactsDocLinksConfig("Test", "artifact1.zip", null, null)
        )));
        p.save();
        updateTransientActions(p);
        // Opening configure with TestZipBuilder causes 500.
        p.getBuildersList().add(new TestZipBuilder("artifact1.zip"));
        
        assertNotNull(p.getAction(ArtifactsDocLinksProjectAction.class));
        {
            // There is no link in project.
            String url = p.getAction(ArtifactsDocLinksProjectAction.class).getUrlName();
            WebClient wc = new WebClient();
            HtmlPage page = wc.getPage(p);
            for(HtmlAnchor a: page.getAnchors()) {
                assertFalse(a.getHrefAttribute().contains(url));
            }
        }
        
        FreeStyleBuild build = p.scheduleBuild2(0).get(BUILD_TIMEOUT, TimeUnit.SECONDS);
        assertBuildStatusSuccess(build);
        
        assertNotNull(build.getAction(ArtifactsDocLinksAction.class));
        
        {
            // There is a link in project.
            String url = p.getAction(ArtifactsDocLinksProjectAction.class).getUrlName();
            boolean found = false;
            WebClient wc = new WebClient();
            HtmlPage page = wc.getPage(p);
            for(HtmlAnchor a: page.getAnchors()) {
                if (a.getHrefAttribute().contains(url)) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
        {
            WebClient wc = new WebClient();
            ArtifactsDocLinksAction action = build.getAction(ArtifactsDocLinksAction.class);
            HtmlPage page = wc.getPage(build, String.format(
                    "%s/%s",
                    action.getUrlName(),
                    Util.rawEncode(action.getArtifactsDocLinksDocumentList().get(0).getId())
            ));
            assertTrue(page.asText(), page.asText().contains("Default top page."));
        }
        
        {
            WebClient wc = new WebClient();
            ArtifactsDocLinksProjectAction action = p.getAction(ArtifactsDocLinksProjectAction.class);
            ArtifactsDocLinksAction buildAction = build.getAction(ArtifactsDocLinksAction.class);
            HtmlPage page = wc.getPage(p, String.format(
                    "%s/%s",
                    action.getUrlName(),
                    Util.rawEncode(buildAction.getArtifactsDocLinksDocumentList().get(0).getId())
            ));
            assertTrue(page.asText(), page.asText().contains("Default top page."));
        }
    }
}
