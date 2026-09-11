package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import com.MyProject.mediationplatformrcehandler.model.database.EntMeOrgData;
import com.MyProject.mediationplatformrcehandler.model.rce.EntMeOrg;
import com.MyProject.mediationplatformrcehandler.repository.EntMeOrgRepository;
import com.MyProject.mediationplatformrcehandler.utils.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.dozer.DozerBeanMapper;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class EntMeOrgService {

    private final MongoTemplate mongoTemplate;

    private final EntMeOrgRepository repository;

    private final DozerBeanMapper dozerMapper;

    public List<EntMeOrg> getAll() {

        log.info("Retrieving data ...");
        List<EntMeOrgData> datas = repository.findAll();
        List<EntMeOrg> dtos = new ArrayList<>();

        var chrono = System.currentTimeMillis();
        for (EntMeOrgData data : datas) {
            var dto = dozerMapper.map(data, EntMeOrg.class);
            dtos.add(dto);
        }
        log.info("{} mapped successfully in {}ms", dtos.size(), System.currentTimeMillis() - chrono);
        return dtos;
    }

    public void saveAll(List<EntMeOrg> entMeOrgs, boolean purgeFirst) {

        log.info("====== {} EntMeOrg received for import ======", entMeOrgs.size());
        if (purgeFirst) {
            DeleteResult deleted = mongoTemplate.remove(new Query(), EntMeOrgData.class);
            log.info("{} deleted", deleted!=null?deleted.getDeletedCount():null);
        }

        long startTime = System.currentTimeMillis();
        log.info("Start mapping data");
        Collection<EntMeOrgData> datas = entMeOrgs
                .parallelStream()
                .map(this::mapEntMeOrgToEntMeOrgData)
                .collect(Collectors.toMap(EntMeOrgData::getIdEnt, Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(EntMeOrgData::getModificationDate)))).values();
        log.info("End mapping data");

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
                log.error("Error occurred while saving ent-me-org data into database: {}", e.getMessage());
                throw new IllegalStateException("Failed to feed ent_me_org referential");
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;
        log.info("EntMeOrg import to DB end successfully in {}ms", executionTime);
    }

    public void refresh(List<EntMeOrg> entMeOrgs) {

        log.info("====== {} EntMeOrg received for import ======", entMeOrgs.size());
        long startTime = System.currentTimeMillis();

        Collection<EntMeOrgData> receivedEntEcos = entMeOrgs
                .parallelStream()
                .map(this::mapEntMeOrgToEntMeOrgData)
                .collect(Collectors.toMap(EntMeOrgData::getIdEnt, Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(EntMeOrgData::getModificationDate)))).values();

        if (!receivedEntEcos.isEmpty()) {
            try {
                // Convert the collection to a list
                List<EntMeOrgData> originalList = new ArrayList<>(receivedEntEcos);

                // Specify the size of each sublist
                int sublistSize = 1000;

                // Call the splitList method
                List<List<EntMeOrgData>> sublists = Utils.splitList(originalList, sublistSize);

                for (List<EntMeOrgData> sublist : sublists) {
                    bulkUpdate(sublist);
                }
            } catch (RuntimeException e) {
                log.error("Error occurred while saving ent-me-org data into database: {}", e.getMessage());
                throw new IllegalStateException("Failed to feed ent_me_org referential");
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;

        log.info("EntEcoNew import to DB end successfully in {}ms", executionTime);
    }

    private EntMeOrgData mapEntMeOrgToEntMeOrgData(EntMeOrg entMeOrg) {
        return EntMeOrgData.builder()
                .idEnt(entMeOrg.getIdEnt())
                .codeSegmentCCial(entMeOrg.getCodeSegmentCCial())
                .dattachement(entMeOrg.getDattachement())
                .motifattachement(entMeOrg.getMotifattachement())
                .dDetachement(entMeOrg.getDDetachement())
                .motifDetachement(entMeOrg.getMotifDetachement())
                .codeEDSVente(entMeOrg.getCodeEDSVente())
                .aliasEDSVente(entMeOrg.getAliasEDSVente())
                .modificationDate(entMeOrg.getModificationDate())
                .build();
    }

    private void bulkUpdate(Collection<EntMeOrgData> entMeOrgDatas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, EntMeOrgData.class);
        for (EntMeOrgData data : entMeOrgDatas) {
            Query query = new Query().addCriteria(new Criteria("idEnt").is(data.getIdEnt()));
            Update update = new Update()
                    .set("idEnt", data.getIdEnt())
                    .set("codeSegmentCCial", data.getCodeSegmentCCial())
                    .set("dattachement", data.getDattachement())
                    .set("motifattachement", data.getMotifattachement())
                    .set("dDetachement", data.getDDetachement())
                    .set("motifDetachement", data.getMotifDetachement())
                    .set("codeEDSVente", data.getCodeEDSVente())
                    .set("aliasEDSVente", data.getAliasEDSVente())
                    .set("modificationDate", data.getModificationDate());
            bulkOps.upsert(query, update);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getUpserts().size());
        log.info("{} updated", result.getMatchedCount());
    }

        
    public void bulkInsert(Collection<EntMeOrgData> datas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, EntMeOrgData.class);
        for (EntMeOrgData data : datas) {
            bulkOps.insert(data);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getInsertedCount());
    }

	public void refresh(List<EntMeOrg> entMeOrgs, boolean purgeFirst) {
	}

    public List<EntMeOrg> getAllByIds(List<String> entRefXlIds) {
        var chrono = System.currentTimeMillis();
        log.info("Retrieving data by idEnt...");
        List<EntMeOrgData> datas = repository.findByIdEntIn(entRefXlIds);

        log.info("{} data retrieved successfully in {}ms", datas.size(), System.currentTimeMillis() - chrono);
        List<EntMeOrg> dtos = new ArrayList<>();
        
        for (EntMeOrgData data : datas) {
            var dto = dozerMapper.map(data, EntMeOrg.class);
            dtos.add(dto);
        }
        return dtos;
    }
}

