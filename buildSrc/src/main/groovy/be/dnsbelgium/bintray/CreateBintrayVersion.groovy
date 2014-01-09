import org.gradle.api.*
import org.gradle.api.tasks.*
import groovyx.net.http.*

class CreateBintrayVersion extends DefaultTask {

  @Input String username
  @Input String password
  @Input String url
  @Input String subject
  @Input String repo
  @Input String pkg

  @TaskAction
  void start() {
    println("${url}/packages/${subject}/${repo}/${pkg}/versions")
    def http = new HTTPBuilder("${url}/packages/${subject}/${repo}/${pkg}/versions")
    http.auth.basic username, password
    http.request(Method.POST, ContentType.JSON) { req ->
      body = [ name: '0.3.1',desc: '0.3.1']
      response.success = { resp, json ->
        println resp
      }
      //response.failure { resp ->
        //println resp
      //}
    }
    println 'hi'
  }
}
