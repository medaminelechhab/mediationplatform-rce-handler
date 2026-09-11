package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import com.MyProject.mediationplatformrcehandler.model.database.EntMeCompData;
import com.MyProject.mediationplatformrcehandler.model.rce.EntMeComp;
import com.MyProject.mediationplatformrcehandler.repository.EntMeCompRepository;
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
public class EntMeCompService {

    private final MongoTemplate mongoTemplate;

    private final EntMeCompRepository repository;

    private final DozerBeanMapper dozerMapper;

    public List<EntMeComp> getAll() {

        log.info("Retrieving data ...");
        List<EntMeCompData> datas = repository.findAll();
        List<EntMeComp> dtos = new ArrayList<>();
        var chrono = System.currentTimeMillis();

        for (EntMeCompData data : datas) {
            var dto = dozerMapper.map(data, EntMeComp.class);
            dto.setId(data.getIdEnt());
            dtos.add(dto);
        }
        log.info("{} mapped successfully in {}ms", dtos.size(), System.currentTimeMillis() - chrono);

        return dtos;
    }

    public void saveAll(List<EntMeComp> list, boolean purgeFirst) {

        log.info("====== {} import to DB started ======", list.size());
        if (purgeFirst) {
            DeleteResult deleted = mongoTemplate.remove(new Query(), EntMeCompData.class);
            log.info("{} deleted", deleted!=null?deleted.getDeletedCount():null);
        }

        long startTime = System.currentTimeMillis();
        log.info("Start mapping data");
        Collection<EntMeCompData> datas = map(list);
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

    private Collection<EntMeCompData> map(List<EntMeComp> list) {
        return  list
            .parallelStream()
            .map(this::mapEntMeCompToEntMeCompData)
            .collect(Collectors.toMap(EntMeCompData::getIdEnt, Function.identity(),
                    BinaryOperator.maxBy(Comparator.comparing(EntMeCompData::getModificationDate)))).values();
    }

    private EntMeCompData mapEntMeCompToEntMeCompData(EntMeComp entMeComp) {
        return EntMeCompData.builder()
                .idEnt(entMeComp.getId())
                .codeMacroSec(entMeComp.getCodeMacroSec())
                .codeMicroSec(entMeComp.getCodeMicroSec())
                .codeMicroSecLoc(entMeComp.getCodeMicroSecLoc())
                .codeSegemntationDCE(entMeComp.getCodeSegemntationDCE())
                .codeSegemntationDCELoc(entMeComp.getCodeSegemntationDCELoc())
                .modificationDate(entMeComp.getModificationDate())
                .build();
    }

    private void bulkUpdate(Collection<EntMeCompData> entMeCompDatas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, EntMeCompData.class);
        for(EntMeCompData data : entMeCompDatas) {
            Query query = new Query().addCriteria(new Criteria("idEnt").is(data.getIdEnt()));
            Update update = new Update()
                    .set("idEnt", data.getIdEnt())
                    .set("codeMacroSec", data.getCodeMacroSec())
                    .set("codeMicroSec", data.getCodeMicroSec())
                    .set("codeMicroSecLoc", data.getCodeMicroSecLoc())
                    .set("codeSegemntationDCE", data.getCodeSegemntationDCE())
                    .set("codeSegemntationDCELoc", data.getCodeSegemntationDCELoc())
                    .set("modificationDate", data.getModificationDate());
            bulkOps.upsert(query, update);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getUpserts().size());
        log.info("{} updated", result.getMatchedCount());
    }

    public void bulkInsert(Collection<EntMeCompData> datas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, EntMeCompData.class);
        for (EntMeCompData data : datas) {
            bulkOps.insert(data);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getInsertedCount());
    }

    public void refresh(List<EntMeComp> entMeComps) {
    }

    public List<EntMeComp> getAllByIds(List<String> entRefXlIds) {
        var chrono = System.currentTimeMillis();
        log.info("Retrieving data by idEnt...");
        List<EntMeCompData> datas = repository.findByIdEntIn(entRefXlIds);

        log.info("{} data retrieved successfully in {}ms", datas.size(), System.currentTimeMillis() - chrono);
        List<EntMeComp> dtos = new ArrayList<>();
        
        for (EntMeCompData data : datas) {
            var dto = dozerMapper.map(data, EntMeComp.class);
            dto.setId(data.getIdEnt());
            dtos.add(dto);
        }
        return dtos;
    }
}

