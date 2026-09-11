package com.MyProject.mediationplatformrcehandler.repository;

import com.MyProject.mediationplatformrcehandler.model.database.GroupCCIALConstitData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupCCIALConstitRepository extends MongoRepository<GroupCCIALConstitData, String> {

    List<GroupCCIALConstitData> findByIdENT(String idEnt);

    List<GroupCCIALConstitData> findByIdENTIn(List<String> idEnts);

}

