package io.jenkins.plugins.codeInsights;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
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
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Collections;
import java.util.Optional;

@SuppressWarnings("unused")
@Getter
@Setter(onMethod = @__(@DataBoundSetter))
@RequiredArgsConstructor(onConstructor = @__(@DataBoundConstructor))
public class CodeInsightsBuilder extends Builder implements SimpleBuildStep {
    private final String repositoryName;
    private final String commitId;

    private String srcPath = "src/main/java";
    private String baseBranch = "origin/master";
    private String checkstyleFilePath = "";
    private String spotBugsFilePath = "";
    private String pmdFilePath = "";
    private String sonarQubeProjectKey = "";
    private String qodanaFilePath = "";
    private String jacocoFilePath = "";

    @Override
    public void perform(@NotNull final Run<?, ?> run,
                        @NotNull final FilePath workspace,
                        @NotNull final EnvVars envVars,
                        @NotNull final Launcher launcher,
                        @NotNull final TaskListener listener) {
        final DescriptorImpl descriptor = (DescriptorImpl) super.getDescriptor();
        final Optional<UsernamePasswordCredentialsImpl> bitbucketUsernamePassword = Optional.ofNullable(
            CredentialsProvider.findCredentialById(descriptor.bitbucketCredentialId, UsernamePasswordCredentialsImpl.class, run));
        final Optional<StringCredentialsImpl> bitbucketHttpAccessToken = Optional.ofNullable(
            CredentialsProvider.findCredentialById(descriptor.bitbucketCredentialId, StringCredentialsImpl.class, run));
        final Optional<StringCredentialsImpl> sonarQubeToken = Optional.ofNullable(
            CredentialsProvider.findCredentialById(descriptor.sonarQubeCredentialId, StringCredentialsImpl.class, run));
        final Optional<UsernamePasswordCredentialsImpl> sonarQubeUsernamePassword = Optional.ofNullable(
            CredentialsProvider.findCredentialById(descriptor.sonarQubeCredentialId, UsernamePasswordCredentialsImpl.class, run));
        new KotlinEntryPoint(
            run, workspace, envVars, launcher, listener, // given by Jenkins
            descriptor.bitbucketUrl, descriptor.project, descriptor.reportKey,
            bitbucketUsernamePassword.map(UsernamePasswordCredentialsImpl::getUsername).orElse(""),
            bitbucketUsernamePassword.map(UsernamePasswordCredentialsImpl::getPassword).map(Secret::getPlainText).orElse(""),
            bitbucketHttpAccessToken.map(StringCredentialsImpl::getSecret).map(Secret::getPlainText).orElse(""), // mandatory global settings (Bitbucket)
            descriptor.sonarQubeUrl, sonarQubeToken.map(StringCredentialsImpl::getSecret).map(Secret::getPlainText).orElse(""),
            sonarQubeUsernamePassword.map(UsernamePasswordCredentialsImpl::getUsername).orElse(""),
            sonarQubeUsernamePassword.map(UsernamePasswordCredentialsImpl::getPassword).map(Secret::getPlainText).orElse(""), // optional global settings (SonarQube)
            repositoryName, commitId, // mandatory local settings
            srcPath, baseBranch, // optional local settings (with default values)
            checkstyleFilePath, spotBugsFilePath, pmdFilePath, sonarQubeProjectKey, qodanaFilePath, jacocoFilePath // optional local settings
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
        private String sonarQubeCredentialId;

        public DescriptorImpl() {
            super.load();
        }

        @Override
        public boolean configure(final StaplerRequest req, final JSONObject json) {
            final JSONObject globalSettings = json.getJSONObject("codeInsights");
            this.bitbucketUrl = globalSettings.getString("bitbucketUrl");
            this.project = globalSettings.getString("project");
            this.reportKey = globalSettings.getOrDefault("reportKey", "").toString();
            this.bitbucketCredentialId = globalSettings.getString("bitbucketCredentialId");
            this.sonarQubeUrl = globalSettings.getOrDefault("sonarQubeUrl", "").toString();
            this.sonarQubeCredentialId = globalSettings.getOrDefault("sonarQubeCredentialId", "").toString();
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

        @SuppressWarnings("deprecation") // FIXME: Investigate alternative ways to use system auth instead of ACL.SYSTEM
        public ListBoxModel doFillBitbucketCredentialIdItems() {
            return new StandardListBoxModel()
                .includeEmptyValue()
                .includeAs(ACL.SYSTEM, Jenkins.get(), UsernamePasswordCredentialsImpl.class)
                .includeAs(ACL.SYSTEM, Jenkins.get(), StringCredentialsImpl.class);
        }

        @SuppressWarnings("deprecation")
        public ListBoxModel doFillSonarQubeCredentialIdItems() {
            return new StandardListBoxModel()
                .includeEmptyValue()
                .includeMatchingAs(ACL.SYSTEM, Jenkins.get(), StandardCredentials.class, Collections.emptyList(),
                    type -> type instanceof UsernamePasswordCredentialsImpl || type instanceof StringCredentialsImpl);
        }
    }
}
