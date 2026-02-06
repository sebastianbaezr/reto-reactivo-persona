package co.com.bancolombia.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Optional;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(Handler handler, Optional<PersonHandler> personHandler) {
        var router = route(GET("/api/usecase/path"), handler::listenGETUseCase)
            .andRoute(GET("/api/otherusercase/path"), handler::listenGETOtherUseCase)
            .andRoute(POST("/api/usecase/otherpath"), handler::listenPOSTUseCase);

        if (personHandler.isPresent()) {
            router = router
                .andRoute(POST("/api/persons"), personHandler.get()::registerPerson)
                .andRoute(GET("/api/bootcamps/{bootcampId}/persons"), personHandler.get()::listPersonsByBootcamp);
        }

        return router;
    }
}
