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

package hudson.plugins.doclinks;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.AbstractBuild;
import hudson.model.Action;

/**
 *
 */
public class ArtifactDocLinksBuildAction implements Action {
    public static final String URL_PATH = "artifactDocLink";
    private static final Logger LOGGER = Logger.getLogger(ArtifactDocLinksBuildAction.class.getName());
    
    public final AbstractBuild<?, ?> build;
    /**
     * @return the build
     */
    public AbstractBuild<?, ?> getBuild() {
        return build;
    }
    
    private String title;
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    
    private List<ArtifactDocument> documentList;
    /**
     * @return the documentList
     */
    public List<ArtifactDocument> getDocumentList() {
        return documentList;
    }
    
    
    public ArtifactDocLinksBuildAction(AbstractBuild<?, ?> build, String title) {
        this.build = build;
        this.title = title;
        documentList = new ArrayList<ArtifactDocument>();
    }
    
    public void add(ArtifactDocument artifactDocument) {
        getDocumentList().add(artifactDocument);
    }
    
    @Override
    public String getIconFileName() {
        return "document.gif";
    }

    @Override
    public String getDisplayName() {
        return getTitle();
    }

    @Override
    public String getUrlName() {
        return URL_PATH;
    }

    public void doDynamic(final StaplerRequest req, final StaplerResponse rsp)
            throws IOException, ServletException {
        
        ArtifactDocument doc = getDocumentList().get(0);
        
        File artifactPath = new File(build.getArtifactsDir(), doc.getArtifact());
        if (!artifactPath.exists()) {
            rsp.sendError(404);
            return;
        }
        if (!artifactPath.isFile()) {
            rsp.sendError(404);
            return;
        }
        
        URL url = artifactPath.toURI().toURL();
        if(artifactPath.isFile()) {
            String path = String.format("jar:%s!/", artifactPath.toURI().toString());
            url = new URL(path);
        }
        if (!StringUtils.isEmpty(doc.getBasePath())) {
            url = new URL(url, doc.getBasePath());
        }
        if (!StringUtils.isEmpty(req.getRestOfPath())) {
            url = new URL(url, req.getRestOfPath());
        }
        IOUtils.copy(url.openStream(), rsp.getOutputStream());
    }
    
}
