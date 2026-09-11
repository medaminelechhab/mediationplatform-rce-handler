package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import com.MyProject.mediationplatformrcehandler.model.database.GroupCCIALCompData;
import com.MyProject.mediationplatformrcehandler.model.rce.GroupCCIALComp;
import com.MyProject.mediationplatformrcehandler.repository.GroupCCIALCompRepository;
import com.MyProject.mediationplatformrcehandler.utils.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class GroupCCIALCompService {

    private final MongoTemplate mongoTemplate;

    private final GroupCCIALCompRepository repository;

    private final DozerBeanMapper dozerMapper;

    public List<GroupCCIALComp> getAll() {

        log.info("Retrieving data ...");
        List<GroupCCIALCompData> datas = repository.findAll();
        List<GroupCCIALComp> dtos = new ArrayList<>();

        var chrono = System.currentTimeMillis();
        for (GroupCCIALCompData data : datas) {
            var dto = dozerMapper.map(data, GroupCCIALComp.class);
            dtos.add(dto);
        }

        log.info("{} mapped successfully in {}ms", dtos.size(), System.currentTimeMillis() - chrono);
        return dtos;
    }

    public void saveAll(List<GroupCCIALComp> list, boolean purgeFirst) {

        log.info("====== {} import to DB started ======", list.size());
        if (purgeFirst) {
            DeleteResult deleted = mongoTemplate.remove(new Query(), GroupCCIALCompData.class);
            log.info("{} deleted", deleted!=null?deleted.getDeletedCount():null);
        }

        long startTime = System.currentTimeMillis();

        log.info("Start mapping data");
        Collection<GroupCCIALCompData> datas = map(list);
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

    private Collection<GroupCCIALCompData> map(List<GroupCCIALComp> list) {
        return list
            .parallelStream()
            .map(this::mapGroupCCIALCompToGroupCCIALCompData)
            .collect(Collectors.toMap(GroupCCIALCompData::getIdGroup, Function.identity(),
                    BinaryOperator.maxBy(Comparator.comparing(GroupCCIALCompData::getModificationDate)))).values();
    }

    public void refresh(List<GroupCCIALComp> list) {
        
        log.info("====== {} import to DB started ======", list.size());
        
        long startTime = System.currentTimeMillis();

        Collection<GroupCCIALCompData> receivedGroupCCIALComps = list
                .parallelStream()
                .map(this::mapGroupCCIALCompToGroupCCIALCompData)
                .collect(Collectors.toMap(GroupCCIALCompData::getIdGroup, Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(GroupCCIALCompData::getModificationDate)))).values();

        if (!receivedGroupCCIALComps.isEmpty()) {
            try {
                                    //Divide list by small lists
                // Convert the collection to a list
                List<GroupCCIALCompData> originalList = new ArrayList<>(receivedGroupCCIALComps);

                // Specify the size of each sublist
                int sublistSize = 10000;

                // Call the splitList method
                List<List<GroupCCIALCompData>> sublists = Utils.splitList(originalList, sublistSize);

                for (List<GroupCCIALCompData> sublist : sublists) {
                    bulkInsert(sublist);
                }
            } catch (RuntimeException e) {
                log.error("Error occurred while saving gpe-ccial-comp data into database: {}", e.getMessage());
                throw new IllegalStateException("Failed to feed gpe_ccial_comp referential");
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;

        log.info("GroupCCIALComp import to DB end successfully in {}ms", executionTime);
    }

    private GroupCCIALCompData mapGroupCCIALCompToGroupCCIALCompData(GroupCCIALComp groupCCIALComp) {
        return GroupCCIALCompData.builder()
                .idGroup(groupCCIALComp.getIdGroup())
                .sectorMacroCode(groupCCIALComp.getSectorMacroCode())
                .sectorMicroCode(groupCCIALComp.getSectorMicroCode())
                .sectorMicroCodeLoc(groupCCIALComp.getSectorMicroCodeLoc())
                .segmentationDCECode(groupCCIALComp.getSegmentationDCECode())
                .segmentationDCECodeLoc(groupCCIALComp.getSegmentationDCECodeLoc())
                .reserve(groupCCIALComp.getReserve())
                .modificationDate(groupCCIALComp.getModificationDate())
                .build();
    }

    private void bulkUpdate(Collection<GroupCCIALCompData> groupCCIALCompDatas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, GroupCCIALCompData.class);
        for (GroupCCIALCompData data : groupCCIALCompDatas) {
            Query query = new Query()
                .addCriteria(new Criteria("idGroup").is(data.getIdGroup()));
            Update update = new Update()
                    .set("idGroup", data.getIdGroup())
                    .set("sectorMacroCode", data.getSectorMacroCode())
                    .set("sectorMicroCode", data.getSectorMicroCode())
                    .set("sectorMicroCodeLoc", data.getSectorMicroCodeLoc())
                    .set("segmentationDCECode", data.getSegmentationDCECode())
                    .set("segmentationDCECodeLoc", data.getSegmentationDCECodeLoc())
                    .set("reserve", data.getReserve())
                    .set("modificationDate", data.getModificationDate());
            bulkOps.upsert(query, update);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getUpserts().size());
        log.info("{} updated", result.getMatchedCount());
    }

    public void bulkInsert(Collection<GroupCCIALCompData> datas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, GroupCCIALCompData.class);
        for (GroupCCIALCompData data : datas) {
            bulkOps.insert(data);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getInsertedCount());
    }

    public List<GroupCCIALComp> getAllByIds(List<String> groupIds) {
        var chrono = System.currentTimeMillis();
        log.info("Retrieving data by groupIds...");
        List<GroupCCIALCompData> datas = repository.findByIdGroupIn(groupIds);

        log.info("{} data retrieved successfully in {}ms", datas.size(), System.currentTimeMillis() - chrono);
        List<GroupCCIALComp> dtos = new ArrayList<>();
        
        for (GroupCCIALCompData data : datas) {
            var dto = dozerMapper.map(data, GroupCCIALComp.class);
            dto.setIdGroup(data.getIdGroup());
            dtos.add(dto);
        }
        return dtos;
    }
}
