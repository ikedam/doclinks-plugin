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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.DirectoryScanner;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

/**
 *
 */
public class ArtifactsDocLinksPublisher extends Recorder {
    private List<ArtifactsDocLinksConfig> artifactsDocLinksConfigList;
    
    /**
     * @return the artifactsDocLinksConfigList
     */
    public List<ArtifactsDocLinksConfig> getArtifactsDocLinksConfigList() {
        return artifactsDocLinksConfigList;
    }
    
    @DataBoundConstructor
    public ArtifactsDocLinksPublisher(List<ArtifactsDocLinksConfig> artifactsDocLinksConfigList) {
        this.artifactsDocLinksConfigList = artifactsDocLinksConfigList;
    }
    
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
            BuildListener listener) throws InterruptedException, IOException {
        
        List<ArtifactsDocLinksDocument> docList
            = new ArrayList<ArtifactsDocLinksDocument>();
        if (getArtifactsDocLinksConfigList() != null) {
            for (ArtifactsDocLinksConfig config: getArtifactsDocLinksConfigList()) {
                DirectoryScanner ds = new DirectoryScanner();
                ds.setBasedir(build.getArtifactsDir());
                ds.setIncludes(new String[]{config.getArtifactsPattern()});
                ds.scan();
                
                if (ds.getIncludedFilesCount() <= 0) {
                    listener.getLogger().println(String.format("ERROR: No artifacts found for %s", config.getArtifactsPattern()));
                    return false;
                }
                
                for (String file: ds.getIncludedFiles()) {
                    docList.add(new ArtifactsDocLinksDocument(
                            file, 
                            (ds.getIncludedFilesCount() <= 1)
                                ?config.getTitle()
                                :String.format("%s(%s)", config.getTitle(), file),
                            config.getIndexFile()
                    ));
                }
            }
        }
        
        if (docList.isEmpty()) {
            listener.getLogger().println("ERROR: No artifacts to publish as documents");
            return false;
        }
        
        ArtifactsDocLinksAction action = build.getAction(ArtifactsDocLinksAction.class);
        if (action == null) {
            action = new ArtifactsDocLinksAction();
            build.addAction(action);
        }
        action.addAll(docList);
        
        return true;
    }
    
    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        return new ArtifactDocLinksProjectAction();
    }
    
    /**
     * @return
     * @see hudson.tasks.BuildStep#getRequiredMonitorService()
     */
    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public BuildStepDescriptor<Publisher> getDescriptor() {
        return super.getDescriptor();
    }
    
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            return true;
        }
        
        @Override
        public String getDisplayName() {
            return Messages.ArtifactsDocLinksPublisher_DisplayName();
        }
    }
}
