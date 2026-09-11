package com.MyProject.mediationplatformrcehandler.repository;

import com.MyProject.mediationplatformrcehandler.model.database.GroupCCIALCompData;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface GroupCCIALCompRepository extends MongoRepository<GroupCCIALCompData, String> {

    List<GroupCCIALCompData> findByIdGroup(String idGroup);

    List<GroupCCIALCompData> findByIdGroupIn(List<String> idGroups);

}
