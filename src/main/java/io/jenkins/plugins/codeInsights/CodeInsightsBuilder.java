package io.jenkins.plugins.codeInsights;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import lombok.Getter;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Optional;

@SuppressWarnings("unused")
@Getter
public class CodeInsightsBuilder extends Builder implements SimpleBuildStep {
    private final String repositoryName;
    private final String commitId;

    private String srcPath = "src/main/java";
    private String baseBranch = "origin/master";
    private String checkstyleFilePath = "";
    private String spotBugsFilePath = "";
    private String pmdFilePath = "";
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
    public void setSpotBugsFilePath(final String spotBugsFilePath) {
        this.spotBugsFilePath = spotBugsFilePath;
    }

    @DataBoundSetter
    public void setPmdFilePath(final String pmdFilePath) {
        this.pmdFilePath = pmdFilePath;
    }

    @DataBoundSetter
    public void setSonarQubeProjectKey(@NotNull final String sonarQubeProjectKey) {
        this.sonarQubeProjectKey = sonarQubeProjectKey;
    }

    @Override
    public void perform(@NotNull final Run<?, ?> run,
                        @NotNull final FilePath workspace,
                        @NotNull final EnvVars envVars,
                        @NotNull final Launcher launcher,
                        @NotNull final TaskListener listener) {
        final DescriptorImpl descriptor = (DescriptorImpl) super.getDescriptor();
        final Optional<UsernamePasswordCredentialsImpl> bitbucketCredential = Optional.ofNullable(
            CredentialsProvider.findCredentialById(descriptor.bitbucketCredentialId, UsernamePasswordCredentialsImpl.class, run));
        new KotlinEntryPoint(
            run, workspace, envVars, launcher, listener, // given by Jenkins
            descriptor.bitbucketUrl, descriptor.project, descriptor.reportKey,
            bitbucketCredential.map(UsernamePasswordCredentialsImpl::getUsername).orElse(""),
            bitbucketCredential.map(UsernamePasswordCredentialsImpl::getPassword).map(Secret::getPlainText).orElse(""), // mandatory global settings (Bitbucket)
            descriptor.sonarQubeUrl, descriptor.sonarQubeToken, descriptor.sonarQubeUsername, descriptor.sonarQubePassword, // optional global settings (SonarQube)
            repositoryName, commitId, // mandatory local settings
            srcPath, baseBranch, // optional local settings (with default values)
            checkstyleFilePath, spotBugsFilePath, pmdFilePath, sonarQubeProjectKey // optional local settings
        ).delegate();
    }

    @Symbol("codeInsights")
    @Extension
    @Getter
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        private String bitbucketUrl;
        private String project;
        private String reportKey;
        private String bitbucketCredentialId;
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
            this.bitbucketCredentialId = globalSettings.getString("bitbucketCredentialId");
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

        public ListBoxModel doFillBitbucketCredentialIdItems() {
            return new StandardListBoxModel()
                .includeEmptyValue()
                .includeAs(ACL.SYSTEM, Jenkins.get(), UsernamePasswordCredentialsImpl.class);
        }
    }
}
