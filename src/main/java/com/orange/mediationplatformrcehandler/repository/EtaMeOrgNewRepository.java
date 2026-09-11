package com.MyProject.mediationplatformrcehandler.repository;

import com.MyProject.mediationplatformrcehandler.model.database.EtaMeOrgNewData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtaMeOrgNewRepository extends MongoRepository<EtaMeOrgNewData, String> {

    EtaMeOrgNewData findByIdEts(String idEta);

    List<EtaMeOrgNewData> findByIdEtsIn(List<String> idEtas);
}

