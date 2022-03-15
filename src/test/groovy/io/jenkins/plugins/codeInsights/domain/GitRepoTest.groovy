package io.jenkins.plugins.codeInsights.domain

import spock.lang.IgnoreIf
import spock.lang.Specification

@IgnoreIf({ env['ci'] })
class GitRepoTest extends Specification {
    def 'detectChangedFiles: check'() {
        given:
        def sut = new GitRepo(".git")

        expect:
        sut.detectChangedFiles("661878933afdc83670558234f9ed6a0ce6084794", "master") ==~ [
            "src/test/resources/gitrepo-modified",
            "src/test/resources/gitrepo-added"
        ]
    }
}
