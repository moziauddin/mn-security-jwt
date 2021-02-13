package au.com.moziauddin

import groovy.util.logging.Slf4j
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule

import javax.annotation.security.PermitAll
import javax.inject.Inject

@Slf4j
@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/home")
class HomeController {
    HomeService homeService =  new HomeService()

    HomeController() {
    }

    @PermitAll
    @Get('/')
    String index() {
        log.info "Saying hello..."
        return homeService.sayHello()
    }

    @Get('/secret')
    String secretResponse() {
        log.info "Secret was accessed"
        return 'MySecretPassword'
    }
}
