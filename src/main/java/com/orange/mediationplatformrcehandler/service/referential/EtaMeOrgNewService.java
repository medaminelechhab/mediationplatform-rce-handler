package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import com.MyProject.mediationplatformrcehandler.model.database.EtaMeOrgNewData;
import com.MyProject.mediationplatformrcehandler.model.rce.EtaMeOrgNew;
import com.MyProject.mediationplatformrcehandler.repository.EtaMeOrgNewRepository;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.MyProject.mediationplatformrcehandler.service.utils.Utils.calculateOptimalThreads;

@Service
@RequiredArgsConstructor
@Log4j2
public class EtaMeOrgNewService {

    private final MongoTemplate mongoTemplate;

    private final EtaMeOrgNewRepository repository;

    private final DozerBeanMapper dozerMapper;

    public List<EtaMeOrgNew> getAll() {

        log.info("Retrieving data ...");
        List<EtaMeOrgNewData> datas = repository.findAll();
        List<EtaMeOrgNew> dtos = new ArrayList<>();

        var chrono = System.currentTimeMillis();
        for (EtaMeOrgNewData data : datas) {
            var dto = dozerMapper.map(data, EtaMeOrgNew.class);
            dto.setIdETA(data.getIdEts());
            dtos.add(dto);
        }

        log.info("{} mapped successfully in {}ms", dtos.size(), System.currentTimeMillis() - chrono);

        return dtos;
    }

    public List<EtaMeOrgNew> getAllByIds(List<String> listIdEta) {

        var chrono = System.currentTimeMillis();
        log.info("Retrieving data by idEta...");
        List<EtaMeOrgNewData> datas = repository.findByIdEtsIn(listIdEta);

        log.info("{} data retrieved successfully in {}ms", datas.size(), System.currentTimeMillis() - chrono);
        List<EtaMeOrgNew> dtos = new ArrayList<>();
        
        for (EtaMeOrgNewData data : datas) {
            var dto = dozerMapper.map(data, EtaMeOrgNew.class);
            dto.setId(data.getIdEts());
            dtos.add(dto);
        }
        return dtos;
	}

    public void saveAll(List<EtaMeOrgNew> list, boolean purgeFirst) {

        log.info("====== {} import to DB started ======", list.size());
        if (purgeFirst) {
            DeleteResult deleted = mongoTemplate.remove(new Query(), EtaMeOrgNewData.class);
            log.info("{} deleted", deleted!=null?deleted.getDeletedCount():null);
        }

        long startTime = System.currentTimeMillis();
        log.info("Start mapping data");
        Collection<EtaMeOrgNewData> datas = map(list);
        log.info("End mapping data");

        if (!datas.isEmpty()) {
            try {
                var sublistSize = 10000;
                int nThreads = calculateOptimalThreads(datas.size(), sublistSize);
                ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
                for (var sublist : Utils.splitList(new ArrayList<>(datas), 10000)) {
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
                executorService.awaitTermination(1, TimeUnit.HOURS);
            } catch (Exception e) {
                log.error("Error occurred while saving data into database: {}", e.getMessage());
                throw new IllegalStateException("Failed to feed referential:" + e.getMessage());
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;
        log.info("import to DB end successfully in {}ms", executionTime);
    }

    private Collection<EtaMeOrgNewData> map(List<EtaMeOrgNew> list) {
        return list
            .parallelStream()
            .map(this::mapEtaMeOrgNewToEtaMeOrgNewData)
            .collect(Collectors.toMap(EtaMeOrgNewData::getIdEts, Function.identity(),
                    BinaryOperator.maxBy(Comparator.comparing(EtaMeOrgNewData::getModificationDate)))).values();
    }

    private EtaMeOrgNewData mapEtaMeOrgNewToEtaMeOrgNewData(EtaMeOrgNew etaMeOrgNew) {
        return EtaMeOrgNewData.builder()
                .idEts(etaMeOrgNew.getId())
                .codeDGDEC(etaMeOrgNew.getCodeDGDEC())
                .codeAGGEO(etaMeOrgNew.getCodeAGGEO())
                .codeEDSVente(etaMeOrgNew.getCodeEDSVente())
                .aliasEDSVente(etaMeOrgNew.getAliasEDSVente())
                .dattachment(etaMeOrgNew.getDattachment())
                .motifAttachement(etaMeOrgNew.getMotifAttachement())
                .dDattachment(etaMeOrgNew.getDDattachment())
                .motifDetachment(etaMeOrgNew.getMotifDetachment())
                .modificationDate(etaMeOrgNew.getModificationDate())
                .build();
    }

    private void bulkUpdate(Collection<EtaMeOrgNewData> datas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, EtaMeOrgNewData.class);
        for (EtaMeOrgNewData data : datas) {
            Query query = new Query().addCriteria(new Criteria("idEts").is(data.getIdEts()));
            Update update = new Update()
                    .set("codeDGDEC", data.getCodeDGDEC())
                    .set("codeAGGEO", data.getCodeAGGEO())
                    .set("codeEDSVente", data.getCodeEDSVente())
                    .set("aliasEDSVente", data.getAliasEDSVente())
                    .set("dattachment", data.getDattachment())
                    .set("motifAttachement", data.getMotifAttachement())
                    .set("dDattachment", data.getDDattachment())
                    .set("motifDetachment", data.getMotifDetachment())
                    .set("modificationDate", data.getModificationDate())
                    .set("idEts", data.getIdEts());
            bulkOps.upsert(query, update);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getUpserts().size());
        log.info("{} updated", result.getMatchedCount());
    }

    public void bulkInsert(Collection<EtaMeOrgNewData> datas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, EtaMeOrgNewData.class);
        for (EtaMeOrgNewData data : datas) {
            bulkOps.insert(data);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getInsertedCount());
    }

}
