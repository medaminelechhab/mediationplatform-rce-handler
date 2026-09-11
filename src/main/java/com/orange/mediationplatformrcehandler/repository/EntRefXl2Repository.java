package com.MyProject.mediationplatformrcehandler.repository;

import com.MyProject.mediationplatformrcehandler.model.database.EntRefXl2Data;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface EntRefXl2Repository extends MongoRepository<EntRefXl2Data, String> {
    Optional<EntRefXl2Data> findById(String idEnt);

    List<EntRefXl2Data> findByIdIn(List<String> idEnts);
}
