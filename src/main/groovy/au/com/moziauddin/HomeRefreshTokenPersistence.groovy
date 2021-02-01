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
    static String filename = 'etc/refresh-tokens.txt'
    File f = new File(filename)

    HomeRefreshTokenPersistence() {
        // Create the file it not exists
        f.write('')
    }

    @Override
    @EventListener
    void persistToken(RefreshTokenGeneratedEvent event) {
        if(event && event.getRefreshToken() && event.getUserDetails().getUsername()) {
            println "Event: ${event.getUserDetails().getUsername()}"
            if (!getContent().contains(event.getUserDetails().getUsername())) {
                String pl = event.getUserDetails().getUsername() + '-->' +
                        event.getRefreshToken()
                log.info "Payload - $pl"
                writeRefreshToken(pl)
            }
        }
    }

    @Override
    Publisher<UserDetails> getUserDetails(String refreshToken) {
        return Flowable.create(emitter -> {
            List allTokens = readRefreshTokens()
            for (item in allTokens) {
                def (username,refToken) = item.split('-->')
                if (refreshToken == refToken) {
                    emitter.onNext(new UserDetails(username, []))
                    emitter.onComplete()
                } else {
                    emitter.onError(new OauthErrorResponseException(
                            IssuingAnAccessTokenErrorCode.UNAUTHORIZED_CLIENT,
                            "Ref Token not in the list", null))
                }
            }
        }, BackpressureStrategy.ERROR)
    }

    void writeRefreshToken(String text) {
        f.append(text + '\n')
    }

    List readRefreshTokens() {
        def lines = f.readLines('UTF-8')
        return lines
    }

    String getContent() {
        return f.getText('UTF-8')
    }
}
