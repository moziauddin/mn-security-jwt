package au.com.moziauddin

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule

import javax.annotation.security.PermitAll
import javax.inject.Inject

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/home")
class HomeController {
    HomeService homeService =  new HomeService()

    HomeController() {
    }

    @PermitAll
    @Get('/')
    String index() {
        return homeService.sayHello()
    }

    @Get('/secret')
    String secretResponse() {
        return 'MySecretPassword'
    }
}
