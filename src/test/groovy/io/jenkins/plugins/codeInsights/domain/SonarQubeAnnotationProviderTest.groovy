package io.jenkins.plugins.codeInsights.domain


import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import spock.lang.Specification

class SonarQubeAnnotationProviderTest extends Specification {
    private static final def RESPONSE_BODY = '''\
{
   "total":1,
   "p":1,
   "ps":500,
   "paging":{
      "pageIndex":1,
      "pageSize":500,
      "total":1
   },
   "effortTotal":10,
   "issues":[
      {
         "key":"AX-YFnV_aoaIWGn8Z8OF",
         "rule":"java:S120",
         "severity":"MINOR",
         "component":"trial:src/main/java/io/jenkins/plugins/codeInsights/CodeInsightsBuilder.java",
         "project":"trial",
         "line":1,
         "hash":"131671b8c6c5ea47583e2e766c7018b9",
         "textRange":{
            "startLine":1,
            "endLine":1,
            "startOffset":8,
            "endOffset":39
         },
         "flows":[
            
         ],
         "status":"OPEN",
         "message":"Rename this package name to match the regular expression \\u0027^[a-z_]+(\\\\.[a-z_][a-z0-9_]*)*$\\u0027.",
         "effort":"10min",
         "debt":"10min",
         "author":"tasktas9@gmail.com",
         "tags":[
            "convention"
         ],
         "creationDate":"2022-03-07T14:02:33+0000",
         "updateDate":"2022-03-17T13:34:34+0000",
         "type":"CODE_SMELL",
         "scope":"MAIN",
         "quickFixAvailable":false
      }
   ],
   "components":[
      {
         "key":"trial",
         "enabled":true,
         "qualifier":"TRK",
         "name":"Bitbucket Server Code Insights plugin",
         "longName":"Bitbucket Server Code Insights plugin"
      },
      {
         "key":"trial:src/main/java/io/jenkins/plugins/codeInsights/CodeInsightsBuilder.java",
         "enabled":true,
         "qualifier":"FIL",
         "name":"CodeInsightsBuilder.java",
         "longName":"src/main/java/io/jenkins/plugins/codeInsights/CodeInsightsBuilder.java",
         "path":"src/main/java/io/jenkins/plugins/codeInsights/CodeInsightsBuilder.java"
      }
   ],
   "facets":[
      
   ]
}'''

    private final def server = new MockWebServer()

    def 'convert returns annotations'() {
        given:
        server.setDispatcher { new MockResponse().setResponseCode(200).setBody(RESPONSE_BODY) }

        def sut = new SonarQubeAnnotationProvider(server.url('').toString(), 'trial', new SonarQubeCredential('', 'admin', 'admin'))

        expect:
        sut.convert('') == [new Annotation(
            1,
            'SonarQube says: Rename this package name to match the regular expression \'^[a-z_]+(\\.[a-z_][a-z0-9_]*)*$\'.',
            'src/main/java/io/jenkins/plugins/codeInsights/CodeInsightsBuilder.java',
            Severity.LOW
        )]
    }

    def 'SonarQubeCredential generates string for Basic authorization'() {
        expect:
        new SonarQubeCredential(token, username, password).value == 'Basic ' + expect

        where:
        token | username | password || expect
        'a'   | ''       | ''       || Base64.encoder.encodeToString('a:'.bytes)
        ''    | 'b'      | 'c'      || Base64.encoder.encodeToString('b:c'.bytes)
        'a'   | 'b'      | 'c'      || Base64.encoder.encodeToString('a:'.bytes)
    }

    def 'SonarQubeCredential throws error when valid params are not given'() {
        when:
        new SonarQubeCredential(token, username, password)

        then:
        def e = thrown(RuntimeException)
        e.message == 'Please use SonarQubeCredential after setting valid items'

        where:
        token | username | password
        ''    | ''       | ''
        ''    | 'b'      | ''
        ''    | ''       | 'c'
    }

    def cleanup() {
        server.shutdown()
    }
}
