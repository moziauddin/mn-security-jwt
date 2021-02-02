package au.com.moziauddin


import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import io.micronaut.http.*
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
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
        HttpRequest loginRequest = HttpRequest.POST('/login', adminUserCreds)
        HttpResponse<BearerAccessRefreshToken> response = client.toBlocking().exchange(loginRequest, BearerAccessRefreshToken)
        BearerAccessRefreshToken token = response?.body()

        then :
        response.status == HttpStatus.OK
        token.accessToken
        token.username == 'admin'
    }

    void "Make a successful request to secured endpoint"() {
        when:
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials('admin', 'admin')
        HttpRequest request = HttpRequest.POST('/login', creds)
        HttpResponse response = client.toBlocking().exchange(request, BearerAccessRefreshToken)
        String accessToken = response.body().accessToken
        String refreshToken = response.body().refreshToken
        println "Ref Tok: $refreshToken"
        HttpRequest reqAuthorized = HttpRequest.GET('/home/secret').header(HttpHeaders.AUTHORIZATION, "Bearer ${accessToken.toString()}")
        HttpResponse<String> res = client.toBlocking().exchange(reqAuthorized, String)
        String text = res.body()

        then:
        res.status == HttpStatus.OK
        text == 'MySecretPassword'
        JWTParser.parse(accessToken) instanceof SignedJWT

        when: "Gen a new access token using refresh token"
        String data = '{"grant_type":"refresh_token", "refresh_token": "' + refreshToken + '"}'
        HttpRequest reqToOauthAccessToken = HttpRequest
                .POST('/oauth/access_token', data)
                .contentType(MediaType.APPLICATION_JSON)
        println "Data: $data"
        HttpResponse<BearerAccessRefreshToken> refreshRes =
                client
                .toBlocking()
                .retrieve(reqToOauthAccessToken, BearerAccessRefreshToken) as HttpResponse<BearerAccessRefreshToken>

        then:
        println "Refresh Response: $refreshRes.properties"
        refreshRes
    }
}
