package com.MyProject.mediationplatformrcehandler.repository;

import com.MyProject.mediationplatformrcehandler.model.database.EtaRefXL2Data;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EtaRefXl2Repository extends MongoRepository<EtaRefXL2Data, String> {

    Optional<EtaRefXL2Data> findByIdENTAndSiege(String entId, String siege);
    Optional<EtaRefXL2Data> findByIdETA(String idEta);
    List<EtaRefXL2Data> findByIdETAIn(List<String> idEnts);

}
