package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import com.MyProject.mediationplatformrcehandler.model.database.GroupCCIALOrgData;
import com.MyProject.mediationplatformrcehandler.model.rce.GroupCCIALOrg;
import com.MyProject.mediationplatformrcehandler.repository.GroupCCIALOrgRepository;
import com.MyProject.mediationplatformrcehandler.utils.Utils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
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

@Service
@RequiredArgsConstructor
@Log4j2
public class GroupCCIALOrgService {

    private final MongoTemplate mongoTemplate;

    private final GroupCCIALOrgRepository repository;

    private final DozerBeanMapper dozerMapper;

    public GroupCCIALOrg getByIdGroup(String idGroup) {
        Optional<GroupCCIALOrgData> data = repository.findByIdGroup(idGroup);

        if (data.isPresent()){
            log.debug("Got DB GroupCCIALOrgData {}", data);
            return dozerMapper.map(data.get(), GroupCCIALOrg.class);
        } else {
            log.warn("No GroupCCIALOrgData found in DB for idGroup={}", idGroup);
            return null;
        }
    }

    public List<GroupCCIALOrg> getAll() {

        log.info("Retrieving data ...");
        List<GroupCCIALOrgData> datas = repository.findAll();
        List<GroupCCIALOrg> dtos = new ArrayList<>();

        var chrono = System.currentTimeMillis();
        for (GroupCCIALOrgData data : datas) {
            var dto = dozerMapper.map(data, GroupCCIALOrg.class);
            dtos.add(dto);
        }

        log.info("{} mapped successfully in {}ms", dtos.size(), System.currentTimeMillis() - chrono);
        return dtos;
    }

    public void saveAll(List<GroupCCIALOrg> list, boolean purgeFirst) {

            log.info("====== {} import to DB started ======", list.size());
            if (purgeFirst) {
                DeleteResult deleted = mongoTemplate.remove(new Query(), GroupCCIALOrgData.class);
                log.info("{} deleted", deleted!=null?deleted.getDeletedCount():null);
            }

            long startTime = System.currentTimeMillis();

            log.info("Start mapping data");
            Collection<GroupCCIALOrgData> datas = map(list);
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
                    log.error("Error occurred while saving data into database: {}", e.getMessage());
                    throw new IllegalStateException("Failed to feed referential:" + e.getMessage());
                }
            }

            long executionTime = System.currentTimeMillis() - startTime;
            log.info("import to DB end successfully in {}ms", executionTime);
        }

        private Collection<GroupCCIALOrgData> map(List<GroupCCIALOrg> list) {
            return list
                .parallelStream()
                .map(this::mapGroupCCIALOrgToGroupCCIALOrgData)
                .collect(Collectors.toMap(GroupCCIALOrgData::getIdGroup, Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(GroupCCIALOrgData::getModificationDate)))).values();
        }
        


    private GroupCCIALOrgData mapGroupCCIALOrgToGroupCCIALOrgData(GroupCCIALOrg groupCCIALOrg) {
        return GroupCCIALOrgData.builder()
                .idGroup(groupCCIALOrg.getIdGroup())
                .nameGroup(groupCCIALOrg.getNameGroup())
                .codeEDSVente(groupCCIALOrg.getCodeEDSVente())
                .aliasEDSVente(groupCCIALOrg.getAliasEDSVente())
                .idEntMm(groupCCIALOrg.getIdEntMm())
                .creationDate(groupCCIALOrg.getCreationDate())
                .modificationDate(groupCCIALOrg.getModificationDate())
                .deletionDate(groupCCIALOrg.getDeletionDate())
                .build();
    }

    private void bulkUpdate(Collection<GroupCCIALOrgData> groupCCIALOrgDatas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, GroupCCIALOrgData.class);
        for (GroupCCIALOrgData data : groupCCIALOrgDatas) {
            Query query = new Query()
                .addCriteria(new Criteria("idGroup").is(data.getIdGroup()))
                .addCriteria(new Criteria("idEntMm").is(data.getIdEntMm()));
            Update update = new Update()
                    .set("idGroup", data.getIdGroup())
                    .set("nameGroup", data.getNameGroup())
                    .set("codeEDSVente", data.getCodeEDSVente())
                    .set("aliasEDSVente", data.getAliasEDSVente())
                    .set("idEntMm", data.getIdEntMm())
                    .set("creationDate", data.getCreatedDate())
                    .set("modificationDate", data.getModificationDate())
                    .set("deletionDate", data.getDeletionDate())
                    .set("createdDate", data.getCreatedDate()!=null?data.getCreatedDate():Instant.now())
                    .set("lastModifiedDate", Instant.now());
            bulkOps.upsert(query, update);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getUpserts().size());
        log.info("{} updated", result.getMatchedCount());
    }

    public void bulkInsert(Collection<GroupCCIALOrgData> datas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, GroupCCIALOrgData.class);
        for (GroupCCIALOrgData data : datas) {
            bulkOps.insert(data);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getInsertedCount());
    }
}

