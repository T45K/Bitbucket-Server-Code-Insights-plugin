package io.jenkins.plugins.codeInsights.domain

import io.jenkins.plugins.codeInsights.usecase.GitRepo
import spock.lang.Specification

class GitRepoTest extends Specification {
    def 'detectChangedFiles returns changed files based on git-diff'() {
        given:
        final def sut = new GitRepo(new File('.git'))

        expect:
        sut.detectChangedFiles('661878933afdc83670558234f9ed6a0ce6084794', 'a05b196dac972561f0d9456a9a5b95cd471d2337') ==~ [
            'src/test/resources/gitrepo-modified',
            'src/test/resources/gitrepo-added'
        ]
    }
}
