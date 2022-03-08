package io.jenkins.plugins.codeInsights;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class CodeInsightsBuilder extends Builder implements SimpleBuildStep {

    private final String projectPath;
    private final String srcPath;
    private String checkstyleFilePath;

    @DataBoundConstructor
    public CodeInsightsBuilder(String projectPath, final String srcPath) {
        this.projectPath = projectPath;
        this.srcPath = srcPath;
    }

    @DataBoundSetter
    public void setCheckstyleFilePath(final String checkstyleFilePath) {
        this.checkstyleFilePath = checkstyleFilePath;
    }

    @Override
    public void perform(final Run<?, ?> run,
                        final FilePath workspace,
                        final Launcher launcher,
                        final TaskListener listener) {
        if (this.checkstyleFilePath == null) {
            listener.getLogger().println("checkstyleFilePath must not be null");
        }

        listener.getLogger().println(projectPath + "/" + srcPath + "/" + checkstyleFilePath);
    }

    @Symbol("codeInsights")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Call Bitbucket Server Code Insights API";
        }
    }
}
