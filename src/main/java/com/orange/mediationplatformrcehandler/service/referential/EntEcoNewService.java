package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import com.MyProject.mediationplatformrcehandler.model.database.EntEcoNewData;
import com.MyProject.mediationplatformrcehandler.model.rce.EntEcoNew;
import com.MyProject.mediationplatformrcehandler.repository.EntEcoNewRepository;
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
public class EntEcoNewService {

    private final MongoTemplate mongoTemplate;

    private final EntEcoNewRepository repository;

    private final DozerBeanMapper dozerMapper;

    public List<EntEcoNew> getAll() {

        var chrono = System.currentTimeMillis();
        log.info("Retrieving data ...");
        List<EntEcoNewData> datas = repository.findAll();
        log.info("{} data retrieved successfully in {}ms", datas.size(), System.currentTimeMillis() - chrono);
        List<EntEcoNew> dtos = new ArrayList<>();
        
        for (EntEcoNewData data : datas) {
            var dto = dozerMapper.map(data, EntEcoNew.class);
            dto.setId(data.getIdEnt());
            dtos.add(dto);
        }
        return dtos;
    }

    public List<EntEcoNew> getAllByIds(List<String> listIdEnt) {

        var chrono = System.currentTimeMillis();
        log.info("Retrieving data by idEnt...");
        List<EntEcoNewData> datas = repository.findByIdEntIn(listIdEnt);

        log.info("{} data retrieved successfully in {}ms", datas.size(), System.currentTimeMillis() - chrono);
        List<EntEcoNew> dtos = new ArrayList<>();
        
        for (EntEcoNewData data : datas) {
            var dto = dozerMapper.map(data, EntEcoNew.class);
            dto.setId(data.getIdEnt());
            dtos.add(dto);
        }
        return dtos;
    }

    public void saveAll(List<EntEcoNew> list, boolean purgeFirst) {

        log.info("====== {} import to DB started ======", list.size());
        if (purgeFirst) {
            DeleteResult deleted = mongoTemplate.remove(new Query(), EntEcoNewData.class);
            log.info("{} deleted", deleted!=null?deleted.getDeletedCount():null);
        }

        long startTime = System.currentTimeMillis();
        log.info("Start mapping data");
        Collection<EntEcoNewData> datas = map(list);
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

    private Collection<EntEcoNewData> map(List<EntEcoNew> list) {
        return list
                .parallelStream()
                .map(this::mapEntEcoNewToEntEcoNewData)
                .collect(Collectors.toMap(EntEcoNewData::getIdEnt, Function.identity(),
                        BinaryOperator.maxBy(Comparator.comparing(EntEcoNewData::getModificationDate)))).values();

    }

    private EntEcoNewData mapEntEcoNewToEntEcoNewData(EntEcoNew entEcoNew) {
        return EntEcoNewData.builder()
                .idEnt(entEcoNew.getId())
                .ca(entEcoNew.getCa())
                .caExp(entEcoNew.getCaExp())
                .effectif(entEcoNew.getEffectif())
                .dateBilan(entEcoNew.getDateBilan())
                .durationExercice(entEcoNew.getDurationExercice())
                .modificationDate(entEcoNew.getModificationDate())
                .build();
    }

    private void bulkUpdate(Collection<EntEcoNewData> entEcoNewDatas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, EntEcoNewData.class);
        for (EntEcoNewData data : entEcoNewDatas) {
            Query query = new Query().addCriteria(new Criteria("idEnt").is(data.getIdEnt()));
            Update update = new Update()
                    .set("idEnt", data.getIdEnt())
                    .set("ca", data.getCa())
                    .set("caExp", data.getCaExp())
                    .set("effectif", data.getEffectif())
                    .set("dateBilan", data.getDateBilan())
                    .set("durationExercice", data.getDurationExercice())
                    .set("modificationDate", data.getModificationDate());
            bulkOps.upsert(query, update);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getUpserts().size());
        log.info("{} updated", result.getMatchedCount());

    }

    public void bulkInsert(Collection<EntEcoNewData> datas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, EntEcoNewData.class);
        for (EntEcoNewData data : datas) {
            bulkOps.insert(data);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getInsertedCount());
    }

	public void refresh(List<EntEcoNew> entEcoNews) {
	}
}
