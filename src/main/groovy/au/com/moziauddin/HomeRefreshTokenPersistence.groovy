package au.com.moziauddin

import groovy.util.logging.Slf4j
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.security.authentication.UserDetails
import io.micronaut.security.errors.IssuingAnAccessTokenErrorCode
import io.micronaut.security.errors.OauthErrorResponseException
import io.micronaut.security.token.event.RefreshTokenGeneratedEvent
import io.micronaut.security.token.refresh.RefreshTokenPersistence
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import org.reactivestreams.Publisher

import javax.inject.Singleton

@Slf4j
@Singleton
class HomeRefreshTokenPersistence implements RefreshTokenPersistence {
    /**
     * Author: Mo Ziauddin
     * Handle the refresh token workflow so the refresh token is generated
     */
    static String filename = 'refresh-tokens.txt'
    File f = new File(filename)

    HomeRefreshTokenPersistence() {
        // Create the file it not exists
        f.write('')
    }

    @Override
    @EventListener
    void persistToken(RefreshTokenGeneratedEvent event) {
        if(event && event.getRefreshToken() && event.getUserDetails().getUsername()) {
            List allTokens = readStoredRefreshTokens()
            String username = event.getUserDetails().getUsername()
            String newToken = username + '-->' + event.getRefreshToken()
            String myTokenString = allTokens.find {e -> e.contains(username)}
            if (myTokenString) {
                allTokens[allTokens.indexOf(myTokenString)] = newToken
            } else {
                allTokens << newToken
            }
            writeRefreshToken(allTokens)
            log.info "Added new token $newToken and saved"
        }
    }

    @Override
    Publisher<UserDetails> getUserDetails(String refreshToken) {
        return Flowable.create(emitter -> {
            List allTokens = readStoredRefreshTokens()
            String username = ''
            String refToken = ''
            for (item in allTokens) {
                if (item) {
                    def (un, rt) = item.split('-->')
                    if (rt == refreshToken) {
                        username = un
                        refToken = rt
                    }
                }
            }
            if (refToken) {
                emitter.onNext(new UserDetails(username, []))
                emitter.onComplete()
            } else {
                emitter.onError(new OauthErrorResponseException(
                        IssuingAnAccessTokenErrorCode.UNAUTHORIZED_CLIENT,
                        "Ref Token not in the list", null))
            }
        }, BackpressureStrategy.ERROR)
    }

    void writeRefreshToken(List list) {
        println "Writing List: ${list.toString()}"
        f.setText(list.toString())
    }

    List readStoredRefreshTokens() {
        String fileContent = f.getText('UTF-8')
        List tokens = fileContent.tokenize(',[]')
        return tokens
    }
}
