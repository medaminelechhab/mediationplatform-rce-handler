package com.MyProject.mediationplatformrcehandler.repository;

import com.MyProject.mediationplatformrcehandler.model.database.EntMeCompData;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntMeCompRepository extends MongoRepository<EntMeCompData, String> {

    EntMeCompData findByIdEnt(String idEnt);

    List<EntMeCompData> findByIdEntIn(List<String> idEnts);

}

