package com.MyProject.mediationplatformrcehandler.service.referential;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import com.MyProject.mediationplatformrcehandler.model.database.EtaRefXL2Data;
import com.MyProject.mediationplatformrcehandler.model.rce.EtaRefXL2;
import com.MyProject.mediationplatformrcehandler.repository.EtaRefXl2Repository;
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
@Log4j2
public class EtaRefXL2Service {

    private final EtaRefXl2Repository repository;

    private final MongoTemplate mongoTemplate;

    private final DozerBeanMapper dozerMapper;

    public EtaRefXL2 getHqByIdENT(String entId){
        try {
            Optional<EtaRefXL2Data> data = repository.findByIdENTAndSiege(entId, "Y");
            if (data.isPresent()) {
                log.debug("Got DB etaRefXl2Data {}", data);
                return dozerMapper.map(data.get(), EtaRefXL2.class);
            } else {
                log.warn("No etaRefXl2Data found in DB for entId={}", entId);
                return null;
            }
        } catch (Exception ex){
            log.error("Unable to get HeadQuarter for entreprise {}", entId, ex);
            throw new IllegalStateException(ex);
        }
    }


       public void saveAll(List<EtaRefXL2> list, boolean purgeFirst) {

        log.info("====== {} import to DB started ======", list.size());
        if (purgeFirst) {
            DeleteResult deleted = mongoTemplate.remove(new Query(), EtaRefXL2Data.class);
            log.info("{} deleted", deleted!=null?deleted.getDeletedCount():null);
        }

        long startTime = System.currentTimeMillis();

       log.info("Start mapping data");
       Collection<EtaRefXL2Data> datas = list.parallelStream().map(e -> dozerMapper.map(e, EtaRefXL2Data.class)).toList();
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
                executorService.awaitTermination(1, TimeUnit.HOURS);
            } catch (Exception e) {
                log.error("Error occurred while saving data into database: {}", e.getMessage());
                throw new IllegalStateException("Failed to feed referential:" + e.getMessage());
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;
        log.info("EtaRefXL2 import to DB end successfully in {}ms", executionTime);
    }

    public void bulkUpdate(Collection<EtaRefXL2Data> entScoringNoteortDatas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, EtaRefXL2Data.class);
        for (EtaRefXL2Data data : entScoringNoteortDatas) {
            Query query = new Query().addCriteria(new Criteria("idETA").is(data.getIdETA()));
            Update update = new Update()
                    .set("idENT", data.getIdENT())
                    .set("dReferencement", data.getDReferencement())
                    .set("motifReferencement", data.getMotifReferencement())
                    .set("dDReferencement", data.getDDReferencement())
                    .set("motifDeReferencement", data.getMotifDeReferencement())
                    .set("modificationDate", data.getModificationDate())
                    .set("statutINSEE", data.getStatutINSEE())
                    .set("siren", data.getSiren())
                    .set("nic", data.getNic())
                    .set("siege", data.getSiege())
                    .set("enseigne", data.getEnseigne())
                    .set("numStreet", data.getNumStreet())
                    .set("indrep", data.getIndrep())
                    .set("typeStreet", data.getTypeStreet())
                    .set("labelStreet", data.getLabelStreet())
                    .set("postalCode", data.getPostalCode())
                    .set("cedex", data.getCedex())
                    .set("streetCode", data.getStreetCode())
                    .set("libcom", data.getLibcom())
                    .set("countryCode", data.getCountryCode())
                    .set("rpet", data.getRpet())
                    .set("depet", data.getDepet())
                    .set("comet", data.getComet())
                    .set("arronet", data.getArronet())
                    .set("ctonet", data.getCtonet())
                    .set("du", data.getDu())
                    .set("tu", data.getTu())
                    .set("uu", data.getUu())
                    .set("epci", data.getEpci())
                    .set("tdc", data.getTdc())
                    .set("zemet_xl2", data.getZemet_xl2())
                    .set("l1Normalisee", data.getL1Normalisee())
                    .set("l2Normalisee", data.getL2Normalisee())
                    .set("l3Normalisee", data.getL3Normalisee())
                    .set("l4Normalisee", data.getL4Normalisee())
                    .set("l5Normalisee", data.getL5Normalisee())
                    .set("l6Normalisee", data.getL6Normalisee())
                    .set("l7Normalisee", data.getL7Normalisee())
                    .set("codeNAF31", data.getCodeNAF31())
                    .set("codeNaf", data.getCodeNaf())
                    .set("tefet", data.getTefet())
                    .set("efetcent", data.getEfetcent())
                    .set("defet", data.getDefet())
                    .set("originCode", data.getOriginCode())
                    .set("dcret", data.getDcret())
                    .set("activNat", data.getACTISURF())
                    .set("dreatet", data.getDreatet())
                    .set("lieuAct", data.getLieuAct())
                    .set("saisonNat", data.getSaisonNat())
                    .set("AUXILT", data.getAUXILT())
                    .set("IND_PUBLIPO", data.getIND_PUBLIPO())
                    .set("DIFFCOM", data.getDIFFCOM())
                    .set("AMINTRET", data.getAMINTRET())
                    .set("ACTISURF", data.getACTISURF())
                    .set("MODET", data.getMODET())
                    .set("PRODET", data.getPRODET())
                    .set("PRODPART", data.getPRODPART())
                    .set("SIRETPS", data.getSIRETPS())
                    .set("TEL", data.getTEL())
                    .set("EVE", data.getEVE())
                    .set("createdDate", data.getCreatedDate()!=null?data.getCreatedDate():Instant.now())
                    .set("lastModifiedDate", Instant.now());



            bulkOps.upsert(query, update);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getUpserts().size());
        log.info("{} updated", result.getMatchedCount());
    }

    public void bulkInsert(Collection<EtaRefXL2Data> datas) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, EtaRefXL2Data.class);
        for (EtaRefXL2Data data : datas) {
            bulkOps.insert(data);
        }
        BulkWriteResult result = bulkOps.execute();

        log.info("{} inserted", result.getInsertedCount());
    }

    public List<EtaRefXL2> getAllByIds(List<String> etaRefXlIds) {
        var chrono = System.currentTimeMillis();
        log.info("Retrieving data by idEta...");
        List<EtaRefXL2Data> datas = repository.findByIdETAIn(etaRefXlIds);

        log.info("{} data retrieved successfully in {}ms", datas.size(), System.currentTimeMillis() - chrono);
        List<EtaRefXL2> dtos = new ArrayList<>();

        for (EtaRefXL2Data data : datas) {
            var dto = dozerMapper.map(data, EtaRefXL2.class);
            dtos.add(dto);
        }
        return dtos;
    }
}
