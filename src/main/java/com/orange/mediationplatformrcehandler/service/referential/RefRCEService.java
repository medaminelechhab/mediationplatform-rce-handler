package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import com.MyProject.mediationplatformrcehandler.model.database.RefRCEData;
import com.MyProject.mediationplatformrcehandler.model.rce.RefRCE;
import com.MyProject.mediationplatformrcehandler.repository.RefRCERepository;
import com.MyProject.mediationplatformrcehandler.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefRCEService {

    private final RefRCERepository refRCERepository;

    private final MongoTemplate mongoTemplate;

    public void saveAll(List<RefRCE> list, boolean purgeFirst) {

        log.info("====== {} import to DB started ======", list.size());
        if (purgeFirst) {
            DeleteResult deleted = mongoTemplate.remove(new Query(), RefRCEData.class);
            log.info("{} deleted", deleted!=null?deleted.getDeletedCount():null);
        }

        long startTime = System.currentTimeMillis();
        
        Collection<RefRCEData> datas = map(list);

        if (!datas.isEmpty()) {
            try {
                for (var sublist : Utils.splitList(new ArrayList<>(datas), 10000)) {
                    if(purgeFirst){
                        bulkInsert(sublist);
                    }else{
                        bulkUpdate(sublist);
                    }
                }
            } catch (RuntimeException e) {
                log.error("Error occurred while saving data into database: {}", e.getMessage());
                throw new IllegalStateException("Failed to feed referential:" + e.getMessage());
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;
        log.info("import to DB end successfully in {}ms", executionTime);
    }

    private Collection<RefRCEData> map(List<RefRCE> list) {
        return removeDuplicatesRefRCES(list)
                .parallelStream()
                .map(e -> RefRCEData.builder()
                    .referenceName(e.getReferenceName())
                    .code(e.getCode())
                    .label(e.getLabel())
                    .modificationDate(e.getModificationDate())
                    .deletionDate(e.getDeletionDate())
                    .build())
                .toList();
    }

    public void bulkInsert(Collection<RefRCEData> datas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, RefRCEData.class);
        for (RefRCEData data : datas) {
            bulkOps.insert(data);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getInsertedCount());
    }

    private List<RefRCE> removeDuplicatesRefRCES(List<RefRCE> refRCES) {
        Collections.reverse(refRCES);
        Set<RefRCE> uniqueRefRCES = new HashSet<>(refRCES);
        return new ArrayList<>(uniqueRefRCES);
    }

    public List<RefRCE> retrieveAllReferencesRCE() {

        List<RefRCEData> referencesRCEData = refRCERepository.findAll();
        List<RefRCE> referencesRCE = new ArrayList<>();

        for (RefRCEData refRCEData : referencesRCEData) {
            RefRCE refRCE = new RefRCE();
            refRCE.setReferenceName(refRCEData.getReferenceName());
            refRCE.setCode(refRCEData.getCode());
            refRCE.setLabel(refRCEData.getLabel());
            refRCE.setModificationDate(refRCEData.getModificationDate());
            refRCE.setDeletionDate(refRCEData.getDeletionDate());

            referencesRCE.add(refRCE);
        }

        return referencesRCE;
    }

    private void bulkUpdate(Collection<RefRCEData> datas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, RefRCEData.class);
        for (RefRCEData data : datas) {
            Query query = new Query()
                .addCriteria(new Criteria("referenceName").is(data.getReferenceName()))
                .addCriteria(new Criteria("code").is(data.getCode()));
            Update update = new Update()
                    .set("label", data.getLabel())
                    .set("modificationDate", data.getModificationDate())
                    .set("deletionDate", data.getDeletionDate());

            bulkOps.upsert(query, update);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getUpserts().size());
        log.info("{} updated", result.getMatchedCount());
    }
}
