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
import lombok.Getter;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

@SuppressWarnings("unused")
@Getter
public class CodeInsightsBuilder extends Builder implements SimpleBuildStep {
    private final String repositoryName;
    private final String commitId;

    private String srcPath = "src/main/java";
    private String baseBranch = "origin/master";
    private String checkstyleFilePath = "";
    private String sonarQubeProjectKey = "";

    @DataBoundConstructor
    public CodeInsightsBuilder(@NotNull final String repositoryName, @NotNull final String commitId) {
        this.repositoryName = repositoryName;
        this.commitId = commitId;
    }

    @DataBoundSetter
    public void setSrcPath(@NotNull final String srcPath) {
        this.srcPath = srcPath;
    }

    @DataBoundSetter
    public void setBaseBranch(@NotNull final String baseBranch) {
        this.baseBranch = baseBranch;
    }

    @DataBoundSetter
    public void setCheckstyleFilePath(@NotNull final String checkstyleFilePath) {
        this.checkstyleFilePath = checkstyleFilePath;
    }

    @DataBoundSetter
    public void setSonarQubeProjectKey(@NotNull final String sonarQubeProjectKey) {
        this.sonarQubeProjectKey = sonarQubeProjectKey;
    }

    @Override
    public void perform(@NotNull final Run<?, ?> run,
                        @NotNull final FilePath workspace,
                        @NotNull final Launcher launcher,
                        @NotNull final TaskListener listener) {
        final DescriptorImpl descriptor = (DescriptorImpl) super.getDescriptor();
        new KotlinEntryPoint(
            run, workspace, launcher, listener, // given by Jenkins
            descriptor.bitbucketUrl, descriptor.project, descriptor.reportKey, descriptor.username, descriptor.password, // mandatory global settings (Bitbucket)
            descriptor.sonarQubeUrl, descriptor.sonarQubeToken, descriptor.sonarQubeUsername, descriptor.sonarQubePassword, // optional global settings (SonarQube)
            repositoryName, commitId, // mandatory local settings
            srcPath, baseBranch, checkstyleFilePath, sonarQubeProjectKey // optional local settings
        ).delegate();
    }

    @Symbol("codeInsights")
    @Extension
    @Getter
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        private String bitbucketUrl;
        private String project;
        private String reportKey;
        private String username;
        private String password;
        private String sonarQubeUrl;
        private String sonarQubeToken;
        private String sonarQubeUsername;
        private String sonarQubePassword;

        public DescriptorImpl() {
            super.load();
        }

        @Override
        public boolean configure(final StaplerRequest req, final JSONObject json) {
            final JSONObject globalSettings = json.getJSONObject("codeInsights");
            this.bitbucketUrl = globalSettings.getString("bitbucketUrl");
            this.project = globalSettings.getString("project");
            this.reportKey = globalSettings.getString("reportKey");
            this.username = globalSettings.getString("username");
            this.password = globalSettings.getString("password");
            this.sonarQubeUrl = globalSettings.getOrDefault("sonarQubeUrl", "").toString();
            this.sonarQubeToken = globalSettings.getOrDefault("sonarQubeToken", "").toString();
            this.sonarQubeUsername = globalSettings.getOrDefault("sonarQubeUsername", "").toString();
            this.sonarQubePassword = globalSettings.getOrDefault("sonarQubePassword", "").toString();
            save();
            return true;
        }

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        @NotNull
        public String getDisplayName() {
            return "Call Bitbucket Server Code Insights API";
        }
    }
}
