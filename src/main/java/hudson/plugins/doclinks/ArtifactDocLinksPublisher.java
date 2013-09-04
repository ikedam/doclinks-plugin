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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.tools.ant.DirectoryScanner;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

/**
 *
 */
public class ArtifactDocLinksPublisher extends Recorder {
    private String title;
    
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    
    private String artifactPattern;
    
    /**
     * @return the artifactPattern
     */
    public String getArtifactPattern() {
        return artifactPattern;
    }
    
    private String basePath;
    
    /**
     * @return the basePath
     */
    public String getBasePath() {
        return basePath;
    }
    
    private String indexFile;
    /**
     * @return the indexFile
     */
    public String getIndexFile() {
        return indexFile;
    }
    
    @DataBoundConstructor
    public ArtifactDocLinksPublisher(String title, String artifactPattern, String basePath, String indexFile) {
        this.title = title;
        this.artifactPattern = artifactPattern;
        this.basePath = basePath;
        this.indexFile = indexFile;
    }
    
    
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
    
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
            BuildListener listener) throws InterruptedException, IOException {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(build.getArtifactsDir());
        ds.setIncludes(getArtifactPattern().split("\\s*,(?:\\s*,)*\\s*"));
        ds.scan();
        
        List<String> artifactList = new ArrayList<String>(
                ds.getIncludedDirsCount()
                + ds.getIncludedFilesCount()
        );
        for (String file: ds.getIncludedFiles()) {
            artifactList.add(file);
        }
        for (String dir: ds.getIncludedDirectories()) {
            artifactList.add(dir);
        }
        
        if(artifactList.isEmpty()) {
            listener.getLogger().println(String.format("ERROR: There is no atifact matching %s", getArtifactPattern()));
            return false;
        }
        
        Collections.sort(artifactList);
        
        ArtifactDocLinksBuildAction action = new ArtifactDocLinksBuildAction(build, getTitle());
        
        for(String artifact: artifactList) {
            action.add(new ArtifactDocument(
                    artifact,
                    getBasePath(),
                    getIndexFile()
            ));
        }
        
        build.addAction(action);
        
        return true;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return (BuildStepDescriptor<Publisher>)super.getDescriptor();
    }
    
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            return true;
        }
        
        @Override
        public String getDisplayName() {
            return Messages.ArtifactDocLinksPublisher_DisplayName();
        }
    }
}
