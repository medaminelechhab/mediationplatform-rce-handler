package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import com.MyProject.mediationplatformrcehandler.model.database.GroupCCIALConstitData;
import com.MyProject.mediationplatformrcehandler.model.rce.GroupCCIALConstit;
import com.MyProject.mediationplatformrcehandler.repository.GroupCCIALConstitRepository;
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
public class GroupCCIALConstitService {

    private final MongoTemplate mongoTemplate;

    private final GroupCCIALConstitRepository repository;

    private final DozerBeanMapper dozerMapper;

    public List<GroupCCIALConstit> getAll() {

        log.info("Retrieving data ...");
        List<GroupCCIALConstitData> datas = repository.findAll();
        List<GroupCCIALConstit> dtos = new ArrayList<>();

        for (GroupCCIALConstitData data : datas) {
            var dto = dozerMapper.map(data, GroupCCIALConstit.class);
            dtos.add(dto);
        }

        return dtos;
    }

    public void saveAll(List<GroupCCIALConstit> list, boolean purgeFirst) {

        log.info("====== {} import to DB started ======", list.size());
        if (purgeFirst) {
            DeleteResult deleted = mongoTemplate.remove(new Query(), GroupCCIALConstitData.class);
            log.info("{} deleted", deleted!=null?deleted.getDeletedCount():null);
        }

        long startTime = System.currentTimeMillis();

        log.info("Start mapping data");
        Collection<GroupCCIALConstitData> datas = list
                .parallelStream()
                .map(this::mapGroupCCIALConstitToGroupCCIALConstitData)
                .collect(Collectors.toMap(GroupCCIALConstitData::getIdENT, Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(GroupCCIALConstitData::getModificationDate)))).values();
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

    private GroupCCIALConstitData mapGroupCCIALConstitToGroupCCIALConstitData(GroupCCIALConstit group) {
        return GroupCCIALConstitData.builder()
                .idGroup(group.getIdGroup())
                .idENT(group.getIdENT())
                .modificationDate(group.getModificationDate())
                .build();
    }

    public void bulkUpdate(Collection<GroupCCIALConstitData> datas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, GroupCCIALConstitData.class);
        for (GroupCCIALConstitData data : datas) {
            Query query = new Query()
                .addCriteria(new Criteria("idENT").is(data.getIdENT()))
                .addCriteria(new Criteria("idGroup").is(data.getIdGroup()));
            Update update = new Update()
                    .set("idENT", data.getIdENT())
                    .set("modificationDate", data.getModificationDate())
                    .set("idGroup", data.getIdGroup());
            bulkOps.upsert(query, update);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getUpserts().size());
        log.info("{} updated", result.getMatchedCount());
    }

    public void bulkInsert(Collection<GroupCCIALConstitData> datas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, GroupCCIALConstitData.class);
        for (GroupCCIALConstitData data : datas) {
            bulkOps.insert(data);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getInsertedCount());
    }

    public List<GroupCCIALConstit> getAllByEntIds(List<String> enterpriseIDs) {
        var chrono = System.currentTimeMillis();
        log.info("Retrieving data by enterpriseIDs...");
        List<GroupCCIALConstitData> datas = repository.findByIdENTIn(enterpriseIDs);

        log.info("{} data retrieved successfully in {}ms", datas.size(), System.currentTimeMillis() - chrono);
        List<GroupCCIALConstit> dtos = new ArrayList<>();

        for (GroupCCIALConstitData data : datas) {
            var dto = dozerMapper.map(data, GroupCCIALConstit.class);
            dtos.add(dto);
        }
        return dtos;
    }
}

