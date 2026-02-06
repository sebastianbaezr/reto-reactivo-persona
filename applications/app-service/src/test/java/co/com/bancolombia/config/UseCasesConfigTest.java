package co.com.bancolombia.config;

import co.com.bancolombia.model.person.gateways.PersonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No beans ending with 'UseCase' were found");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {

        @Bean
        public PersonRepository personRepository() {
            return mock(PersonRepository.class);
        }

        @Bean
        public co.com.bancolombia.model.bootcamp.gateways.BootcampClient bootcampClient() {
            return mock(co.com.bancolombia.model.bootcamp.gateways.BootcampClient.class);
        }

        @Bean
        public co.com.bancolombia.model.report.gateways.ReportClient reportClient() {
            return mock(co.com.bancolombia.model.report.gateways.ReportClient.class);
        }

        @Bean
        public MyUseCase myUseCase() {
            return new MyUseCase();
        }
    }

    static class MyUseCase {
        public String execute() {
            return "MyUseCase Test";
        }
    }
}