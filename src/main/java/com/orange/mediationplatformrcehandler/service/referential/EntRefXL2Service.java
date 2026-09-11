package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import com.MyProject.mediationplatformrcehandler.model.database.EntRefXl2Data;
import com.MyProject.mediationplatformrcehandler.model.rce.EntRefXL;
import com.MyProject.mediationplatformrcehandler.repository.EntRefXl2Repository;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.MyProject.mediationplatformrcehandler.service.utils.Utils.calculateOptimalThreads;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntRefXL2Service {

    private final MongoTemplate mongoTemplate;

    private final EntRefXl2Repository entRefXl2Repository;

    private final DozerBeanMapper dozerMapper;

    public EntRefXL getByIdEnt(String idEnt) {
        Optional<EntRefXl2Data> data = entRefXl2Repository.findById(idEnt);

        if (data.isPresent()){
            log.debug("Got DB entRefXl2Data {}", data);
            return dozerMapper.map(data.get(), EntRefXL.class);
        } else {
            log.warn("No entRefXl2Data found in DB for idEnt={}", idEnt);
            return null;
        }
    }

    public void saveAll(List<EntRefXL> list, boolean purgeFirst) {

        log.info("====== {} import to DB started ======", list.size());
        if (purgeFirst) {
            DeleteResult deleted = mongoTemplate.remove(new Query(), EntRefXl2Data.class);
            log.info("{} deleted", deleted!=null?deleted.getDeletedCount():null);
        }

        long startTime = System.currentTimeMillis();
        log.info("Start mapping data");
        Collection<EntRefXl2Data> datas = list.parallelStream().map(e -> dozerMapper.map(e, EntRefXl2Data.class)).toList();
        log.info("End mapping data");

        if (!datas.isEmpty()) {
            try {
                var sublistSize = 10000;
                int nThreads = calculateOptimalThreads(datas.size(), sublistSize);
                ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
                for (var sublist : Utils.splitList(new ArrayList<>(datas), sublistSize)) {
                    executorService.submit(() -> {
                        if (purgeFirst) {
                            bulkInsert(sublist);
                        } else {
                            bulkUpdate(sublist);
                        }
                    });
                }
                // Shut down the executor and wait for all tasks to complete
                executorService.shutdown();
                executorService.awaitTermination(1, TimeUnit.HOURS);  // Adjust the timeout as needed
            } catch (Exception e) {
                log.error("Error occurred while saving data into database: {}", e.getMessage());
                throw new IllegalStateException("Failed to feed referential");
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;

        log.info("EntRefXL import to DB end successfully in {}ms", executionTime);
    }

    private void bulkUpdate(Collection<EntRefXl2Data> entRefXl2Datas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, EntRefXl2Data.class);
        for(EntRefXl2Data data : entRefXl2Datas) {
            Query query = new Query().addCriteria(new Criteria("idEnt").is(data.getId()));
            Update update = new Update()
                    .set("dReferencement", data.getDReferencement())
                    .set("motifReferencement", data.getMotifReferencement())
                    .set("dDReferencement", data.getDDReferencement())
                    .set("motifDeReferencement", data.getMotifDeReferencement())
                    .set("codPaysSiege", data.getCodPaysSiege())
                    .set("enCoursImmat", data.getEnCoursImmat())
                    .set("modificationDate", data.getModificationDate())
                    .set("statusInsee", data.getStatusInsee())
                    .set("siren", data.getSiren())
                    .set("nomrs", data.getNomrs())
                    .set("sigle", data.getSigle())
                    .set("civility", data.getCivility())
                    .set("familyName", data.getFamilyName())
                    .set("firstName", data.getFirstName())
                    .set("indPersphys", data.getIndPersphys())
                    .set("codeCatj", data.getCodeCatj())
                    .set("codNaf31", data.getCodNaf31())
                    .set("codNaf", data.getCodNaf())
                    .set("dcren", data.getDcren())
                    .set("dreacten", data.getDreacten())
                    .set("monthCreationEnt", data.getMonthCreationEnt())
                    .set("yearCreationEnt", data.getYearCreationEnt())
                    .set("tefen", data.getTefen())
                    .set("efencent", data.getEfencent())
                    .set("defen", data.getDefen())
                    .set("recme", data.getRecme())
                    .set("monoAct", data.getMonoAct())
                    .set("regimp", data.getRegimp())
                    .set("monoreg", data.getMonoreg())
                    .set("category", data.getCategory())
                    .set("rna", data.getRna())
                    .set("tca", data.getTca())
                    .set("mailAddress", data.getMailAddress())
                    .set("proden", data.getProden())
                    .set("ess", data.getEss())
                    .set("dateEss", data.getDateEss())
                    .set("aprm", data.getAprm())
                    .set("createdDate", data.getCreatedDate()!=null?data.getCreatedDate():Instant.now())
                    .set("lastModifiedDate", Instant.now());
            bulkOps.upsert(query, update);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getUpserts().size());
        log.info("{} updated", result.getMatchedCount());
    }

    public void bulkInsert(Collection<EntRefXl2Data> datas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, EntRefXl2Data.class);
        for (EntRefXl2Data data : datas) {
            bulkOps.insert(data);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getInsertedCount());
    }

    public List<EntRefXL> getAllByIds(List<String> entRefXlIds) {
        var chrono = System.currentTimeMillis();
        log.info("Retrieving data by idEnt...");
        List<EntRefXl2Data> datas = entRefXl2Repository.findByIdIn(entRefXlIds);

        log.info("{} data retrieved successfully in {}ms", datas.size(), System.currentTimeMillis() - chrono);
        List<EntRefXL> dtos = new ArrayList<>();

        for (EntRefXl2Data data : datas) {
            var dto = dozerMapper.map(data, EntRefXL.class);
            dtos.add(dto);
        }
        return dtos;
    }

}