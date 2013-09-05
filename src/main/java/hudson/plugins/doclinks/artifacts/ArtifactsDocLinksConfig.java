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

import org.codehaus.plexus.util.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Hudson;

/**
 *
 */
public class ArtifactsDocLinksConfig implements Describable<ArtifactsDocLinksConfig> {
    private String title;
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    
    private String artifactsPattern;
    /**
     * @return the artifactsPattern
     */
    public String getArtifactsPattern() {
        return artifactsPattern;
    }
    
    private String initialPath;
    /**
     * @return the initialPath
     */
    public String getInitialPath() {
        return initialPath;
    }
    
    private String indexFile;
    /**
     * @return the indexFile
     */
    public String getIndexFile() {
        return indexFile;
    }
    
    @DataBoundConstructor
    public ArtifactsDocLinksConfig(String title, String artifactsPattern, String initialPath, String indexFile) {
        this.title = StringUtils.trim(title);
        this.artifactsPattern = StringUtils.trim(artifactsPattern);
        this.initialPath = initialPath;
        this.indexFile = StringUtils.trim(indexFile);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Descriptor<ArtifactsDocLinksConfig> getDescriptor() {
        return Hudson.getInstance().getDescriptor(getClass());
    }
    
    @Extension
    public static class DescriptorImpl extends Descriptor<ArtifactsDocLinksConfig> {
        @Override
        public String getDisplayName() {
            return "Configuration Entry for ArtifactsDocLinksPublisher";
        }
    }
}
