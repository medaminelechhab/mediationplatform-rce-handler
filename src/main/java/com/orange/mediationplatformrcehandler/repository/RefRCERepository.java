package com.MyProject.mediationplatformrcehandler.repository;

import com.MyProject.mediationplatformrcehandler.model.database.RefRCEData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RefRCERepository extends MongoRepository<RefRCEData, String> {
    RefRCEData findByReferenceNameAndCode(String refName, String code);
}
