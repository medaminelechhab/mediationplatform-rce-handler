package com.MyProject.mediationplatformrcehandler.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class PersistenceServiceTest {

}