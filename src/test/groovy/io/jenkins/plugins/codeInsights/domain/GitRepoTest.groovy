package io.jenkins.plugins.codeInsights.domain

import spock.lang.Specification

class GitRepoTest extends Specification {
    def 'detectChangedFiles: check'() {
        given:
        def sut = new GitRepo(".git")

        expect:
        sut.detectChangedFiles("a8a1cc084938aa5fe9722d16d7cbd1b887ece511", "master") == []
    }
}
