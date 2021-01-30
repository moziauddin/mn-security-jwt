package au.com.moziauddin

import edu.umd.cs.findbugs.annotations.Nullable
import groovy.util.logging.Slf4j
import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.AuthenticationFailed
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.UserDetails
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Singleton
import javax.naming.AuthenticationException

@Slf4j
@Singleton
class AuthenticationProviderUserPassword implements AuthenticationProvider {

    @Override
    Publisher<AuthenticationResponse> authenticate(@Nullable HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequest) {
        return Flowable.create(emitter -> {
            def userIdentity = authenticationRequest.getIdentity()
            def userSecret = authenticationRequest.getSecret()
            log.info("User Identity: $userIdentity - User Secret: $userSecret")
            if (userIdentity.equals('admin') && userSecret.equals('admin')) {
                emitter.onNext(new UserDetails(userIdentity as String, new ArrayList<>()))
                emitter.onComplete()
                return
            }
            emitter.onError(new AuthenticationException(new AuthenticationFailed("Incorrect Login") as String))
        }, BackpressureStrategy.ERROR)
    }
}
