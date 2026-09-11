package com.MyProject.mediationplatformrcehandler.service;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CacheService {
  private static final Map<String, String> usernamesUserIdsMapping = new HashMap<>();
  private static final Map<String, String> aliasUserIdsMapping = new HashMap<>();

  public void reset() {
    log.info("Resetting user caches ...");
    usernamesUserIdsMapping.clear();
    aliasUserIdsMapping.clear();
  }

  public void addUsernameAndUserId(@NonNull String username, @NonNull String userId) {
    log.info("Saving username : {} and userId : {} in the cache", username, userId);
    usernamesUserIdsMapping.put(username, userId);
  }

  public void addAliasAndUserId(@NonNull String alias, @NonNull String userId) {
    log.info("Saving alias : {} and userId : {} in the cache", alias, userId);
    aliasUserIdsMapping.put(alias, userId);
  }

  public String getUserIdByUsername(@NonNull String username) {
    log.info("Attempting to get idUser by username : {} from the cache ", username);
    return usernamesUserIdsMapping.get(username);
  }

  public String getUserIdByAlias(@NonNull String alias) {
    log.info("Attempting to get idUser by alias : {} from the cache ", alias);
    return aliasUserIdsMapping.get(alias);
  }

  public boolean containsUsername(@NonNull String username) {
    return usernamesUserIdsMapping.containsKey(username) && usernamesUserIdsMapping.get(username) != null;
  }

  public boolean containsAlias(@NonNull String alias) {
    return aliasUserIdsMapping.containsKey(alias) && aliasUserIdsMapping.get(alias) != null;
  }
}
