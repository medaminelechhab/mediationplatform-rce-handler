package com.MyProject.mediationplatformrcehandler.repository;

import com.MyProject.mediationplatformrcehandler.model.database.EntScoringNoteortData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntScoringNoteortRepository extends MongoRepository<EntScoringNoteortData, String> {

    EntScoringNoteortData findByIdEnt(String id);

    List<EntScoringNoteortData> findByIdEntIn(List<String> idEnts);

}
