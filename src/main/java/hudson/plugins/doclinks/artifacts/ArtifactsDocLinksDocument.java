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

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.Util;
import hudson.model.ModelObject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

/**
 *
 */
public class ArtifactsDocLinksDocument implements ModelObject {
    private static Logger LOGGER = Logger.getLogger(ArtifactsDocLinksDocument.class.getName());
    private String artifactName;
    /**
     * @return the artifactName
     */
    public String getArtifactName() {
        return artifactName;
    }
    
    private String title;
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    
    private String indexFile;
    /**
     * @return the indexFile
     */
    public String getIndexFile() {
        return indexFile;
    }
    
    public String getId() {
        return getArtifactName();
    }
    
    public String getUrl() {
        return Util.rawEncode(getId());
    }
    
    public ArtifactsDocLinksDocument(String artifactName, String title, String indexFile) {
        this.artifactName = artifactName;
        this.title = title;
        this.indexFile = indexFile;
    }
    
    
    /**
     * @return
     * @see hudson.model.ModelObject#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return getTitle();
    }
    
    
    protected AbstractBuild<?, ?> getBuild(StaplerRequest req) {
        AbstractBuild<?,?> build = req.findAncestorObject(AbstractBuild.class);
        if (build != null) {
            return build;
        }
        
        AbstractProject<?,?> project = req.findAncestorObject(AbstractProject.class);
        if (project != null) {
            return project.getLastSuccessfulBuild();
        }
        
        return null;
    }
    
    public void doDynamic(StaplerRequest req, StaplerResponse resp) throws IOException {
        AbstractBuild<?,?> build = getBuild(req);
        if (build == null) {
            LOGGER.warning(String.format("No build found for url %s", req.getRequestURI()));
            resp.sendError(404);
            return;
        }
        
        File artifact = new File(build.getArtifactsDir(), getArtifactName());
        if (!artifact.exists()) {
            LOGGER.warning(String.format("Artifact does not exists: %s for %s", getArtifactName(), build.getFullDisplayName()));
            resp.sendError(404);
            return;
        }
        if (!artifact.isFile()) {
            LOGGER.warning(String.format("Artifact is not a file: %s for %s", getArtifactName(), build.getFullDisplayName()));
            resp.sendError(403);
            return;
        }
        
        String path = req.getRestOfPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        ZipFile zip = new ZipFile(artifact);
        ZipEntry entry = getFileEntry(zip, path);
        if (entry == null) {
            resp.sendError(404);
            return;
        }
        
        resp.setContentType(URLConnection.guessContentTypeFromName(entry.getName()));
        IOUtils.copy(zip.getInputStream(entry), resp.getOutputStream());
        return;
    }

    private ZipEntry getFileEntry(ZipFile zip, String path) {
        if (!StringUtils.isEmpty(path)) {
            ZipEntry entry = zip.getEntry(path);
            if (entry == null) {
                return null;
            }
            
            if (!entry.isDirectory()) {
                return entry;
            }
        }
        
        String indexFile = getIndexFile();
        if (StringUtils.isEmpty(indexFile)) {
            indexFile = "index.html,index.htm";
        }
        
        for (String file: StringUtils.split(indexFile, ",")) {
            file = StringUtils.trim(file);
            String filePath = StringUtils.isEmpty(path)?file:String.format("%s/%s", path, file);
            ZipEntry entry = zip.getEntry(filePath);
            if (entry != null && !entry.isDirectory()) {
                return entry;
            }
        }
        
        return null;
    }
}
