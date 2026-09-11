package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import com.MyProject.mediationplatformrcehandler.model.database.EntScoringNoteortData;
import com.MyProject.mediationplatformrcehandler.model.rce.EntScoringNoteort;
import com.MyProject.mediationplatformrcehandler.repository.EntScoringNoteortRepository;
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
public class EntScoringNoteortService {

    private final MongoTemplate mongoTemplate;

    private final EntScoringNoteortRepository repository;

    private final DozerBeanMapper dozerMapper;

    public List<EntScoringNoteort> getAll() {

        log.info("Retrieving data ...");
        List<EntScoringNoteortData> datas = repository.findAll();
        List<EntScoringNoteort> dtos = new ArrayList<>();

        var chrono = System.currentTimeMillis();
        for (EntScoringNoteortData data : datas) {
            var dto = dozerMapper.map(data, EntScoringNoteort.class);
            dtos.add(dto);
        }
        log.info("{} mapped successfully in {}ms", dtos.size(), System.currentTimeMillis() - chrono);
        return dtos;
    }

    public void saveAll(List<EntScoringNoteort> list, boolean purgeFirst) {

            log.info("====== {} import to DB started ======", list.size());
            if (purgeFirst) {
                DeleteResult deleted = mongoTemplate.remove(new Query(), EntScoringNoteortData.class);
                log.info("{} deleted", deleted!=null?deleted.getDeletedCount():null);
            }

            long startTime = System.currentTimeMillis();
            log.info("Start mapping data");
            Collection<EntScoringNoteortData> datas = map(list);
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

        private Collection<EntScoringNoteortData> map(List<EntScoringNoteort> list) {
            return list
                .parallelStream()
                .map(this::mapEntScoringNoteortToEntScoringNoteortData)
                .collect(Collectors.toMap(EntScoringNoteortData::getIdEnt, Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(EntScoringNoteortData::getModificationDate)))).values();
        }

    private EntScoringNoteortData mapEntScoringNoteortToEntScoringNoteortData(EntScoringNoteort entScoringNoteort) {
        return EntScoringNoteortData.builder()
                .idEnt(entScoringNoteort.getId())
                .noteScoring(entScoringNoteort.getNoteScoring())
                .deffJugement(entScoringNoteort.getDeffJugement())
                .noteORT(entScoringNoteort.getNoteORT())
                .modificationDate(entScoringNoteort.getModificationDate())
                .build();
    }

    private void bulkUpdate(Collection<EntScoringNoteortData> entScoringNoteortDatas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, EntScoringNoteortData.class);
        for (EntScoringNoteortData data : entScoringNoteortDatas) {
            Query query = new Query().addCriteria(new Criteria("idEnt").is(data.getIdEnt()));
            Update update = new Update()
                    .set("idEnt", data.getIdEnt())
                    .set("noteScoring", data.getNoteScoring())
                    .set("deffJugement", data.getDeffJugement())
                    .set("noteORT", data.getNoteORT())
                    .set("modificationDate", data.getModificationDate());
            bulkOps.upsert(query, update);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getUpserts().size());
        log.info("{} updated", result.getMatchedCount());
    }

    public void bulkInsert(Collection<EntScoringNoteortData> datas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, EntScoringNoteortData.class);
        for (EntScoringNoteortData data : datas) {
            bulkOps.insert(data);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getInsertedCount());
    }

    public List<EntScoringNoteort> getAllByIds(List<String> listIdEnt) {

        var chrono = System.currentTimeMillis();
        log.info("Retrieving data by idEnt...");
        List<EntScoringNoteortData> datas  = repository.findByIdEntIn(listIdEnt);

        log.info("{} data retrieved successfully in {}ms", datas.size(), System.currentTimeMillis() - chrono);
        List<EntScoringNoteort> dtos = new ArrayList<>();
        
        for (EntScoringNoteortData data : datas) {
            var dto = dozerMapper.map(data, EntScoringNoteort.class);
            dto.setId(data.getIdEnt());
            dtos.add(dto);
        }
        return dtos;
	}
}

