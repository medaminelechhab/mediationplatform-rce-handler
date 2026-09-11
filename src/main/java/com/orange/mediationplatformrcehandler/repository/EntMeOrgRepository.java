package com.MyProject.mediationplatformrcehandler.repository;

import com.MyProject.mediationplatformrcehandler.model.database.EntMeOrgData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntMeOrgRepository extends MongoRepository<EntMeOrgData, String> {

    EntMeOrgData findByIdEnt(String idEnt);

    List<EntMeOrgData> findByIdEntIn(List<String> idEnts);

}

