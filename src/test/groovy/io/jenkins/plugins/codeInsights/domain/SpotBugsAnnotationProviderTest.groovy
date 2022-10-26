package io.jenkins.plugins.codeInsights.domain

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.jenkins.plugins.codeInsights.testUtil.FileUtil
import spock.lang.Specification

import java.nio.file.Paths

class SpotBugsAnnotationProviderTest extends Specification {

    def 'check'() {
        given:
        def sut = new SpotBugsAnnotationProvider('src/main/java', new XmlMapper(), FileUtil.readString(Paths.get('src/test/resources/spotbugs-test.xml')))

        expect:
        sut.convert() == [
            new Annotation(6, '非 null フィールド logger は io.jenkins.plugins.codeInsights.JenkinsLogger.<static initializer for JenkinsLogger>() によって初期化されていません。', 'src/main/java/io/jenkins/plugins/codeInsights/JenkinsLogger.kt', Severity.LOW, null),
            new Annotation(9, 'インスタンスメソッド io.jenkins.plugins.codeInsights.JenkinsLogger.setLogger(PrintStream) から static フィールド io.jenkins.plugins.codeInsights.JenkinsLogger.logger に書き込みをしています。', 'src/main/java/io/jenkins/plugins/codeInsights/JenkinsLogger.kt', Severity.LOW, null),
            new Annotation(66, 'Collection から抽象クラス java.util.List への疑わしいキャストです。io.jenkins.plugins.codeInsights.KotlinEntryPoint.delegate()', 'src/main/java/io/jenkins/plugins/codeInsights/KotlinEntryPoint.kt', Severity.LOW, null),
            new Annotation(13, 'new io.jenkins.plugins.codeInsights.KotlinEntryPoint(Run, FilePath, Launcher, TaskListener, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String) は，KotlinEntryPoint.run の中に外部の可変オブジェクトを格納することによって内部表現を暴露するかもしれません。', 'src/main/java/io/jenkins/plugins/codeInsights/KotlinEntryPoint.kt', Severity.LOW, null),
            new Annotation(14, 'new io.jenkins.plugins.codeInsights.KotlinEntryPoint(Run, FilePath, Launcher, TaskListener, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String) は，KotlinEntryPoint.workspace の中に外部の可変オブジェクトを格納することによって内部表現を暴露するかもしれません。', 'src/main/java/io/jenkins/plugins/codeInsights/KotlinEntryPoint.kt', Severity.LOW, null),
            new Annotation(3, 'クラス名 io.jenkins.plugins.codeInsights.domain.Annotation$$serializer は，大文字から始まっていません。', 'src/main/java/io/jenkins/plugins/codeInsights/domain/Annotation.kt', Severity.LOW, null),
            new Annotation(38, 'Collection から抽象クラス java.util.List への疑わしいキャストです。io.jenkins.plugins.codeInsights.domain.CheckstyleAnnotationProvider.convert(String)', 'src/main/java/io/jenkins/plugins/codeInsights/domain/CheckstyleAnnotationProvider.kt', Severity.LOW, null),
            new Annotation(35, 'Collection から抽象クラス java.util.List への疑わしいキャストです。io.jenkins.plugins.codeInsights.domain.CheckstyleAnnotationProvider.convert(String)', 'src/main/java/io/jenkins/plugins/codeInsights/domain/CheckstyleAnnotationProvider.kt', Severity.LOW, null),
            new Annotation(7, 'new io.jenkins.plugins.codeInsights.domain.CheckstyleAnnotationProvider(XmlMapper, String) は，CheckstyleAnnotationProvider.xmlMapper の中に外部の可変オブジェクトを格納することによって内部表現を暴露するかもしれません。', 'src/main/java/io/jenkins/plugins/codeInsights/domain/CheckstyleAnnotationProvider.kt', Severity.LOW, null),
            new Annotation(12, 'クラス名 io.jenkins.plugins.codeInsights.domain.Severity$$serializer は，大文字から始まっていません。', 'src/main/java/io/jenkins/plugins/codeInsights/domain/Annotation.kt', Severity.LOW, null),
            new Annotation(113, 'Collection から抽象クラス java.util.List への疑わしいキャストです。io.jenkins.plugins.codeInsights.domain.SonarQubeAnnotationProvider.convert(String)', 'src/main/java/io/jenkins/plugins/codeInsights/domain/SonarQubeAnnotationProvider.kt', Severity.LOW, null),
            new Annotation(117, 'Collection から抽象クラス java.util.List への疑わしいキャストです。io.jenkins.plugins.codeInsights.domain.SonarQubeAnnotationProvider.convert(String)', 'src/main/java/io/jenkins/plugins/codeInsights/domain/SonarQubeAnnotationProvider.kt', Severity.LOW, null),
            new Annotation(121, 'Collection から抽象クラス java.util.List への疑わしいキャストです。io.jenkins.plugins.codeInsights.domain.SonarQubeAnnotationProvider.convert(String)', 'src/main/java/io/jenkins/plugins/codeInsights/domain/SonarQubeAnnotationProvider.kt', Severity.LOW, null),
            new Annotation(39, 'null ではないことがわかっている値 Object.toString() の冗長な null チェックがあります。io.jenkins.plugins.codeInsights.domain.SonarQubeAnnotationProvider.convert(String)', 'src/main/java/io/jenkins/plugins/codeInsights/domain/SonarQubeAnnotationProvider.kt', Severity.LOW, null),
            new Annotation(36, 'null ではないことがわかっている値 kotlinx.serialization.json.JsonElementKt.getJsonPrimitive(JsonElement) の冗長な null チェックがあります。io.jenkins.plugins.codeInsights.domain.SonarQubeAnnotationProvider.convert(String)', 'src/main/java/io/jenkins/plugins/codeInsights/domain/SonarQubeAnnotationProvider.kt', Severity.LOW, null),
            new Annotation(40, 'null ではないことがわかっている値 kotlinx.serialization.json.JsonElementKt.getJsonPrimitive(JsonElement) の冗長な null チェックがあります。io.jenkins.plugins.codeInsights.domain.SonarQubeAnnotationProvider.convert(String)', 'src/main/java/io/jenkins/plugins/codeInsights/domain/SonarQubeAnnotationProvider.kt', Severity.LOW, null),
            new Annotation(40, 'null ではないことがわかっている値 kotlinx.serialization.json.JsonPrimitive.getContent() の冗長な null チェックがあります。io.jenkins.plugins.codeInsights.domain.SonarQubeAnnotationProvider.convert(String)', 'src/main/java/io/jenkins/plugins/codeInsights/domain/SonarQubeAnnotationProvider.kt', Severity.LOW, null),
            new Annotation(7, 'new io.jenkins.plugins.codeInsights.framework.FileTransferServiceImpl(FilePath, Run) は，FileTransferServiceImpl.workspace の中に外部の可変オブジェクトを格納することによって内部表現を暴露するかもしれません。', 'src/main/java/io/jenkins/plugins/codeInsights/framework/FileTransferServiceImpl.kt', Severity.LOW, null),
            new Annotation(30, 'io.jenkins.plugins.codeInsights.domain.ExecutableAnnotationProvidersBuilder.setSonarQube(String, String, String, String, String) は，java.lang.Exception を無視しているかもしれません。', 'src/main/java/io/jenkins/plugins/codeInsights/usecase/ExecutableAnnotationProvidersBuilder.kt', Severity.LOW, null),
            new Annotation(30, 'io.jenkins.plugins.codeInsights.domain.ExecutableAnnotationProvidersBuilder.setSonarQube(String, String, String, String, String) は，java.lang.Exception を無視しているかもしれません。', 'src/main/java/io/jenkins/plugins/codeInsights/usecase/ExecutableAnnotationProvidersBuilder.kt', Severity.LOW, null),
            new Annotation(36, 'io.jenkins.plugins.codeInsights.domain.ExecutableAnnotationProvidersBuilder.build() は，ExecutableAnnotationProvidersBuilder.executables を返すことによって内部表現を暴露するかもしれません。', 'src/main/java/io/jenkins/plugins/codeInsights/usecase/ExecutableAnnotationProvidersBuilder.kt', Severity.LOW, null),
            new Annotation(65, 'Collection から抽象クラス java.util.List への疑わしいキャストです。io.jenkins.plugins.codeInsights.infrastructure.GitRepo.detectChangedFiles(String, String)', 'src/main/java/io/jenkins/plugins/codeInsights/usecase/GitRepo.kt', Severity.LOW, null),
            new Annotation(69, 'Collection から抽象クラス java.util.List への疑わしいキャストです。io.jenkins.plugins.codeInsights.infrastructure.GitRepo.detectChangedFiles(String, String)', 'src/main/java/io/jenkins/plugins/codeInsights/usecase/GitRepo.kt', Severity.LOW, null),
        ]
    }
}
