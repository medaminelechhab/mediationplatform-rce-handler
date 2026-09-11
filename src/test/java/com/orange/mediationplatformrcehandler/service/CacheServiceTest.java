package com.MyProject.mediationplatformrcehandler.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ActiveProfiles;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
@ActiveProfiles({"test", "db", "account", "rce", "clinks", "tx"})
class CacheServiceTest {

  private CacheService cacheService;

  @BeforeAll
   void initialize() {
    cacheService = new CacheService();
    cacheService.addUsernameAndUserId("Virtual_user DEF_DC_AURA_HDM", "0051v000009mGAUAA2");
    cacheService.addAliasAndUserId("RQD_DSTM", "005580000016fb6AAA");
  }

  @Test
  @Order(1)
  @DisplayName("Get userId by username OK")
  void getUserIdByUsername() {
    var userId = cacheService.getUserIdByUsername("Virtual_user DEF_DC_AURA_HDM");
    assertNotNull(userId);
    assertEquals("0051v000009mGAUAA2", userId);
  }

  @Test
  @Order(2)
  @DisplayName("Get userId by alias OK")
  void getUserIdByAlias() {
    var userId = cacheService.getUserIdByAlias("RQD_DSTM");
    assertNotNull(userId);
    assertEquals("005580000016fb6AAA", userId);
  }

  @Test
  @Order(3)
  @DisplayName("Verify existence of username OK")
  void containsUsername() {
    var result = cacheService.containsUsername("Virtual_user DEF_DC_AURA_HDM");
    assertEquals(true, result);
  }

  @Test
  @Order(4)
  @DisplayName("Verify existence of alias OK")
  void containsAlias() {
    var result = cacheService.containsAlias("RQD_DSTM");
    assertEquals(true, result);
  }

  @Test
  @Order(5)
  @DisplayName("When calling reset method, we must clear the cache OK")
  void reset() {
    cacheService.reset();
    assertFalse(cacheService.containsUsername("Virtual_user DEF_DC_AURA_HDM"));
    assertFalse(cacheService.containsAlias("RQD_DSTM"));
  }
}