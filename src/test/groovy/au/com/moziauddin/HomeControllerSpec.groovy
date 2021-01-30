package au.com.moziauddin

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class HomeControllerSpec extends Specification {

    @Inject
    @Client('/')
    RxHttpClient client
    @Inject HomeController controller

    void "Calling index action returns hello there"() {
        when: "Calling index action"
        String hello = controller.index()

        then:
        hello == 'Hello There'
    }

    void "Calling API returns unauthorized"() {
        when:
        String result = client.toBlocking().retrieve('/home', String)

        then:
        result == 'Hello There'
    }

    void "Check if accessToken exists in the response from /login endpoint"() {
        when:
        UsernamePasswordCredentials adminUserCreds = new UsernamePasswordCredentials("admin", "admin")
        HttpRequest login = HttpRequest.POST('/login', adminUserCreds)
        HttpResponse<BearerAccessRefreshToken> response =  client.toBlocking().exchange(login, BearerAccessRefreshToken)
        BearerAccessRefreshToken token = response.body()

        then:
        response.status == HttpStatus.OK
        token.accessToken
        token.username == 'admin'
        println("AccessTokem: ${token.accessToken} \n Refresh Token: ${token.refreshToken}")
    }

//    void "Make a successful request to secured endpoint"() {
//        when:
//        UsernamePasswordCredentials creds = new UsernamePasswordCredentials('admin', 'secret')
//        HttpRequest request = HttpRequest.POST('/login', creds)
//        HttpResponse response = client.toBlocking().exchange(request, BearerAccessRefreshToken)
//        BearerAccessRefreshToken token = response.body().accessToken
//        HttpResponse res = client.toBlocking().retrieve('/home/secret')
//        String text = res.body()
//
//        then:
//        text == 'MySecretPassword'
//
//    }

    void "Make a request with invalid password"() {
        when:
        try {
            UsernamePasswordCredentials creds = new UsernamePasswordCredentials('admin', 'secret')
            HttpRequest request = HttpRequest.POST('/login', creds)
            client.toBlocking().exchange(request, BearerAccessRefreshToken)
        } catch (Exception e) {
            println "Exception: ${e.getClass()}"
        }

        then:
        thrown(AuthenticationFailed)
    }
}
