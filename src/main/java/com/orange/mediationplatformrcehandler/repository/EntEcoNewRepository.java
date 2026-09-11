package com.MyProject.mediationplatformrcehandler.repository;

import com.MyProject.mediationplatformrcehandler.model.database.EntEcoNewData;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntEcoNewRepository extends MongoRepository<EntEcoNewData, String> {

    EntEcoNewData findByIdEnt(String idEnt);

    List<EntEcoNewData> findByIdEntIn(List<String> idEnts);


}

