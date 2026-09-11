package com.MyProject.mediationplatformrcehandler.repository;

import com.MyProject.mediationplatformrcehandler.model.database.GroupCCIALOrgData;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupCCIALOrgRepository extends MongoRepository<GroupCCIALOrgData, String> {

  Optional<GroupCCIALOrgData> findByIdGroup(String idGroup);
}

