package com.circulosiete.curso.testing.clase07.app.user


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.spock.Testcontainers
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest
@Testcontainers
@Ignore
class UserRepoSpec extends Specification {
    @Shared
    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16")

    @Autowired
    UserRepository repo

    def "save and retrieve user with postgres testcontainer"() {
        when:
            def saved = repo.save(new User("alice"))

        then:
            saved.id != null

        when:
            def found = repo.findById(saved.id)

        then:
            found.present
            found.get().name == "alice"
    }
}
