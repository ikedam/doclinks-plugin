package hudson.plugins.doclinks.artifacts;

import org.kohsuke.stapler.StaplerRequest;

import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

public abstract class ArtifactDocsLinksActionBase implements Action {
    public static final String URLNAME = "ArtifactsDocLinks";
    
    public static AbstractBuild<?,?> getLastDocumentedBuild(
            AbstractProject<?, ?> project) {
        for (AbstractBuild<?,?> build = project.getLastBuild();
                build != null;
                build = build.getPreviousBuild()
        ){
            if (build.getAction(ArtifactDocsLinksActionBase.class) != null) {
                return build;
            }
        }
        return null;
    }
    
    public Object getOwner(StaplerRequest req) {
        AbstractBuild<?,?> build = req.findAncestorObject(AbstractBuild.class);
        if (build != null) {
            return build;
        }
        
        AbstractProject<?,?> project = req.findAncestorObject(AbstractProject.class);
        if (project != null) {
            return project;
        }
        
        return null;
    }
    
    public AbstractProject<?,?> getProject(StaplerRequest req) {
        AbstractProject<?,?> project = req.findAncestorObject(AbstractProject.class);
        if (project != null) {
            return project;
        }
        
        return null;
    }
    
    public AbstractBuild<?,?> getBuild(StaplerRequest req) {
        AbstractBuild<?,?> build = req.findAncestorObject(AbstractBuild.class);
        if (build != null) {
            return build;
        }
        
        AbstractProject<?,?> project = req.findAncestorObject(AbstractProject.class);
        if (project != null) {
            return getLastDocumentedBuild(project);
        }
        
        return null;
    }
    
    /**
     * @return
     * @see hudson.model.Action#getIconFileName()
     */
    @Override
    public String getIconFileName() {
        return "document.gif";
    }
    
    /**
     * @return
     * @see hudson.model.Action#getUrlName()
     */
    @Override
    public String getUrlName() {
        return URLNAME;
    }
}
