package au.com.moziauddin

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
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

    void "Check authentication with username and password"() {
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
}
