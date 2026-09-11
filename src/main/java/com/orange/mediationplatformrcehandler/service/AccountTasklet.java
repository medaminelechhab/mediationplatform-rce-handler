package com.MyProject.mediationplatformrcehandler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.MyProject.mediationplatform.common.error.ApiCallException;
import com.MyProject.mediationplatform.common.model.customerlinks.rce.Body;
import com.MyProject.mediationplatform.common.model.customerlinks.rce.ResponseBody;
import com.MyProject.mediationplatform.common.service.remote.customerlinks.RCEService;
import com.MyProject.mediationplatformrcehandler.enums.FilePrefix;
import com.MyProject.mediationplatformrcehandler.mapper.EntrepriseMapper;
import com.MyProject.mediationplatformrcehandler.mapper.EstablishMapper;
import com.MyProject.mediationplatformrcehandler.mapper.GroupeMapper;
import com.MyProject.mediationplatformrcehandler.model.ErrorDTO;
import com.MyProject.mediationplatformrcehandler.model.customerlinks.Account;
import com.MyProject.mediationplatformrcehandler.model.customerlinks.Group;
import com.MyProject.mediationplatformrcehandler.model.efiles.EFile;
import com.MyProject.mediationplatformrcehandler.model.rce.*;
import com.MyProject.mediationplatformrcehandler.service.efiles.EFilesService;
import com.MyProject.mediationplatformrcehandler.service.efiles.FileContentService;
import com.MyProject.mediationplatformrcehandler.service.referential.*;
import com.MyProject.mediationplatformrcehandler.service.utils.EmailUtils;
import com.MyProject.mediationplatformrcehandler.service.utils.ErrorHandler;
import com.MyProject.mediationplatformrcehandler.service.utils.FileReader;
import com.MyProject.mediationplatformrcehandler.service.utils.FilesManagement.TypeManagement;
import com.MyProject.mediationplatformrcehandler.service.utils.StatsService;
import com.MyProject.mediationplatformrcehandler.utils.AccountMapper;
import com.MyProject.mediationplatformrcehandler.utils.Constants;
import com.MyProject.mediationplatformrcehandler.utils.TarUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.springframework.batch.core.*;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class AccountTasklet implements Tasklet, StepExecutionListener {

    private static final String THAT_TOOK_MILLISECONDS = "That took {} milliseconds";
    private static final String EXTRACTED_FILE_EXT = ".txt";

    private final StatsService statsService;
    private final RCEService rceService;
    private final FileContentService fileContentService;
    private final EFilesService eFilesService;
    private final ErrorHandler errorHandler;
    private final EmailUtils emailUtils;
    private final PersistenceService persistenceService;
    private final GroupCCIALOrgService groupCCIALOrgService;
    private final GroupCCIALCompService groupCCIALCompService;
    private final GroupCCIALConstitService groupCCIALConstitService;
    private final EntMeOrgService entMeOrgService;
    private final EntMeCompService entMeCompService;
    private final EntRefXL2Service entRefXl2Service;
    private final RefRCEService refRCEService;
    private final EntScoringNoteortService entScoringNoteortService;
    private final EntEcoNewService entEcoNewService;
    private final EtaMeOrgNewService etaMeOrgNewService;
    private final EtaRefXL2Service etaRefXl2Service;
    private final CacheService cacheService;

    private GroupeMapper groupeMapper;
    private EntrepriseMapper entrepriseMapper;
    private EstablishMapper establishMapper;

    private String typeManagement;
    private Map<String, String> customParams = new HashMap<>();

    @Value("${clinksplatform.tmp-storage-dir}")
    private String temporaryDir;

    @Value("${mail.enable-sending:true}")
    private boolean isEnableSending;

    @Value("#{${clinksplatform.efiles.connection.query-params}}")
    private HashMap<String, Object> eFilesParams;

    @Value("#{${clinksplatform.customerlinks.rce.rce-segmentation-mapping}}")
    private HashMap<String, String> segmentationList;
    @Value("#{${clinksplatform.customerlinks.rce.rce-agence-list}}")
    private List<HashMap<String, String>> agenceList;
    @Value("#{${clinksplatform.customerlinks.rce.rce-edg-codes}}")
    private HashMap<String, String> edgCodes;

    
    private int groupCount;
    private int entrepriseCount;
    private int etablissementCount;
    private int errorCreate;
    private int errorUpdate;
    private int errorGroupDelete;
    private int errorMapping;
    private int created;
    private int updated;
    private int deletedGroups;
    private long chrono;
    
    @Value("${debug.pick-today-files:true}")
    private boolean pickTodayFiles;
    @Value("${debug.keep-data:false}")
	private boolean keepData;
    @Value("${debug.keep-tmp-files:false}")
    private boolean keepTmpFiles;
    @Value("${debug.skip-efiles:false}")
    private boolean skipEfiles;
    @Value("${debug.ignore-db:false}")
    private boolean ignoreDb;
    @Value("${rce-days-before-full:7}")
    private int daysBeforeFull;

    @Value("${rce-days-before-full-in-etaMeOrg:7}")
    private int daysBeforeFullInEtaMeOrg;
    @Value("${rce-days-before-full-in-entMeOrg:7}")
    private int daysBeforeFullInEntMeOrg;
    @Value("${rce-days-before-full-in-scoring:7}")
    private int daysBeforeFullInScoring;

    private Map<Object, Object> userCache = new HashMap<>();

    private Boolean updateCustomerLinks = true;
    private List<String> filteredGroups = new ArrayList<>();
    private List<String> filteredEntreprises = new ArrayList<>();
    private List<String> filteredEtablissements = new ArrayList<>();

    public AccountTasklet(StatsService statsService, GroupeMapper groupeMapper, EntrepriseMapper entrepriseMapper, EstablishMapper establishMapper, RCEService rceService, FileContentService fileContentService,
                          EFilesService eFilesService, ErrorHandler errorHandler, EmailUtils emailUtils, PersistenceService persistenceService,
                          RefRCEService refRCEService, EntRefXL2Service entRefXl2Service, EtaMeOrgNewService etaMeOrgNewService,
                          EntEcoNewService entEcoNewService, EntScoringNoteortService entScoringNoteortService, EntMeCompService entMeCompService, EntMeOrgService entMeOrgService,
                          GroupCCIALConstitService groupCCIALConstitService, GroupCCIALCompService groupCCIALCompService, GroupCCIALOrgService groupCCIALOrgService, EtaRefXL2Service etaRefXl2Service,
                          CacheService cacheService
                          ) {
        this.statsService = statsService;
        this.groupeMapper = groupeMapper;
        this.entrepriseMapper = entrepriseMapper;
        this.establishMapper = establishMapper;
        this.rceService = rceService;
        this.fileContentService = fileContentService;
        this.eFilesService = eFilesService;
        this.errorHandler = errorHandler;
        this.emailUtils = emailUtils;
        this.persistenceService = persistenceService;
        this.refRCEService = refRCEService;
        this.entRefXl2Service = entRefXl2Service;
        this.etaMeOrgNewService = etaMeOrgNewService;
        this.entEcoNewService = entEcoNewService;
        this.entScoringNoteortService = entScoringNoteortService;
        this.entMeCompService = entMeCompService;
        this.entMeOrgService = entMeOrgService;
        this.groupCCIALConstitService = groupCCIALConstitService;
        this.groupCCIALCompService = groupCCIALCompService;
        this.groupCCIALOrgService = groupCCIALOrgService;
        this.etaRefXl2Service = etaRefXl2Service;
        this.cacheService = cacheService;
    }

    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        chrono = System.currentTimeMillis();

        init(stepExecution);

        if(!skipEfiles){
            var tarFile = eFilesService.getFiles(eFilesParams, customParams)
                    .flatMap(this::retrieveFileContent)
                    .doOnError(e ->
                            log.error("RCE accounts import failed - [{}]", e.getMessage()))
                    .onErrorResume(Mono::error);

            tarFile.doOnNext(file -> {
                try {
                    TarUtils.unTar(file, file.getParentFile());
                } catch (IOException | ArchiveException e) {
                    log.error("Error while untar file:  {}", e.getMessage());
                    throw new IllegalArgumentException(e);
                }
                parseAndSaveFiles();
            }).block();
        } else {
            parseAndSaveFiles();
        }

        log.info("END OF BEFORE STEP");
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        if(updateCustomerLinks) {
            startProcessGroupes();
            startProcessEntreprises();
            startProcessEtablissements();
        } else {
            log.warn("<<<<<<<<< /!\\ Will not update CL /!\\ >>>>>>>>>");
            statsService.addStat("Warning : CL Updates disabled for this run");
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        
        //Resetting all garabage
        groupeMapper = null;
        entrepriseMapper = null;
        establishMapper = null;
        cacheService.reset();
        customParams.clear();        
        System.gc();
        
        if (isEnableSending) {
            communicateTechnicalErrorsIfExist(stepExecution);
            communicateMappingErrorsIfExist(stepExecution);
        } else {
            serializeErrors(stepExecution);
        }   
        cleanupTemporaryDirectory();
        
        return ExitStatus.COMPLETED;
    }

    private void init(StepExecution stepExecution) {
        statsService.reset();

        if (temporaryDir == null || temporaryDir.isBlank()) {
            throw new IllegalArgumentException("Temporary dir is not set");
        }

        typeManagement = String.valueOf(
                stepExecution.getJobParameters().getParameters().get("typeManagement").getValue());

        //Get param to switch on CL CRUD
        var param = stepExecution.getJobParameters().getParameters()
                .get("updateCustomerLinks");
        if(param!=null) {
            updateCustomerLinks = Boolean.parseBoolean(String.valueOf(param.getValue()));
        } else {
            updateCustomerLinks = true;
        }

        //Get filtered groups
        var param2 = stepExecution.getJobParameters().getParameters()
                .get("filteredGroups");
        if(param2!=null && !((String)param2.getValue()).isEmpty()) {
            filteredGroups = List.of(((String)param2.getValue()).split(";"));
        }
        //Get filtered Etablissements
        var param3 = stepExecution.getJobParameters().getParameters()
                .get("filteredEtablissements");
        if(param3!=null && !((String)param3.getValue()).isEmpty()) {
            filteredEtablissements = List.of(((String)param3.getValue()).split(";"));
        }
        //Get filtered Entreprises
        var param4 = stepExecution.getJobParameters().getParameters()
                .get("filteredEntreprises");
        if(param4!=null && !((String)param4.getValue()).isEmpty()) {
            filteredEntreprises = List.of(((String)param4.getValue()).split(";"));
        }

        stepExecution.getJobParameters().getParameters().forEach((p,v) -> {
            if(p.startsWith("custom_"))
                customParams.put(p.replace("custom_", ""), (String) v.getValue());
        });

        eFilesParams.put("creationDate.gt", LocalDateTime.now().format(Constants.DF_MIDNIGHT));
    }

    private Mono<File> retrieveFileContent(List<EFile> files) {
        
        log.info("({}) files found for today ", files.size());
        files.forEach(e -> log.info(e.getFileName()));

        List<EFile> retainedFiles = files.stream()
            .filter(file -> file.getFileName().endsWith(Constants.RCE_FILE_EXTENTION))
            .toList();

        var params = customParams.isEmpty()?eFilesParams:customParams;
        if (retainedFiles.isEmpty()) {
            throw new IllegalArgumentException("List of e-files must not be empty, there was no files for '" + LocalDate.now() 
            + "' with params:'" + params + "' and file extension '" + Constants.RCE_FILE_EXTENTION + "'");        
        }

        log.info("Retained ({}) files found for today ", retainedFiles.size());
        retainedFiles.forEach(e -> {

            log.info(e.getFileName());
        });

        var fileToProcess = retainedFiles.get(0);
        statsService.addStat("=====    Processed file : " + fileToProcess.getFileName());
        statsService.addStat("=====     id : " + fileToProcess.getOperationId());
        statsService.addStat("=====     size : " + fileToProcess.getFileSize() + " bytes");
        statsService.addStat("=====     created on : " + fileToProcess.getCreationDate().toLocalDateTime());
        statsService.addStat("=========================================================");
        return fileContentService.getContentFile(fileToProcess.getOperationId());
    }

    public void parseAndSaveFiles() {
        try {
            log.info("Current working dir is: {}", temporaryDir);
            //Loading all groups + entreprises + etablissement for DB persistence
            var allGroupCCIALOrgs = FileReader.parseFile(buildFilePath(FilePrefix.GPE_CCIAL_ORG), GroupCCIALOrg.class);
            var allEntRefXl2s = (List<EntRefXL>) FileReader.parseBigFile(buildFilePath(FilePrefix.ENT_REFERENCE_NEW), EntRefXL.class);
            var allEtaRefXl2s = (List<EtaRefXL2>) FileReader.parseBigFile(buildFilePath(FilePrefix.ETA_REFERENCE_NEW), EtaRefXL2.class);
            var etaMeOrgNews = (List<EtaMeOrgNew>) FileReader.parseBigFile(buildFilePath(FilePrefix.ETA_ME_ORG_NEW), EtaMeOrgNew.class);

            var groupCCIALComps = FileReader.parseFile(buildFilePath(FilePrefix.GPE_CCIAL_COMP), GroupCCIALComp.class);
            var groupCCIALConstits = FileReader.parseFile(buildFilePath(FilePrefix.GPE_CCIAL_CONSTIT), GroupCCIALConstit.class);
            var entMeOrgs = FileReader.parseFile(buildFilePath(FilePrefix.ENT_ME_ORG), EntMeOrg.class);
            var entMeComps = FileReader.parseFile(buildFilePath(FilePrefix.ENT_ME_COMP), EntMeComp.class);
            var refRCES = FileReader.parseFile(buildFilePath(FilePrefix.REF_RCE), RefRCE.class);
            var entScoringNoteorts = FileReader.parseFile(buildFilePath(FilePrefix.ENT_SCORING_NOTEORT), EntScoringNoteort.class);
            var entEcoNews = FileReader.parseFile(buildFilePath(FilePrefix.ENT_ECO_NEW), EntEcoNew.class);

            statsService.addStat("Using following files: ");
            statsService.addStat(" [File] EntRefXl2 :   " + allEntRefXl2s.size());
            statsService.addStat(" [File] GroupCCIALOrg :  " + allGroupCCIALOrgs.size());
            statsService.addStat(" [File] EtaRefXl2 :    " + allEtaRefXl2s.size());
            statsService.addStat(" [File] refRCE : " + refRCES.size());
            statsService.addStat(" [File] entEcoNew :  " + entEcoNews.size());
            statsService.addStat(" [File] entMeOrg :   " + entMeOrgs.size());
            statsService.addStat(" [File] entMeComp :  " + entMeComps.size());
            statsService.addStat(" [File] entScoringNoteort :  " + entScoringNoteorts.size());
            statsService.addStat(" [File] etaMeOrgNew :    " + etaMeOrgNews.size());
            statsService.addStat(" [File] groupCCIALConstit :  " + groupCCIALConstits.size());
            statsService.addStat(" [File] groupCCIALComp : " + groupCCIALComps.size());

            AccountMapper accountMapper = null;
            if (typeManagement.toUpperCase().equals(TypeManagement.SUNDAY.toString())){
                statsService.addStat("-> Database data is not loaded for FULL");
                if(!ignoreDb){
                    log.info("Begin of save ...");
                    persistenceService.saveAll(allGroupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps,
                            allEntRefXl2s, refRCES, entScoringNoteorts, allEtaRefXl2s, entEcoNews, etaMeOrgNews, !keepData);
                    log.info("End of save ...");
                    statsService.putMetric("DONE", " -> End of DB Save ");
                }

                //Filter ME-ORG to include more accounts
                var filtredEtaMeOrgNew = etaMeOrgNews.stream().filter(e -> (e.getModificationDate() != null && LocalDate.parse(e.getModificationDate(), Constants.DF_YYYYMMDD)
                        .isAfter(LocalDate.now().minusDays(daysBeforeFullInEtaMeOrg)))).toList();
                statsService.putMetric("DONE", " -> collected filtredEtaMeOrgNew with size : " + filtredEtaMeOrgNew.size());

                var filtredEntMeOrgNew = entMeOrgs.stream().filter(e -> (e.getModificationDate() != null && LocalDate.parse(e.getModificationDate(), Constants.DF_YYYYMMDD)
                        .isAfter(LocalDate.now().minusDays(daysBeforeFullInEntMeOrg)))).toList();
                statsService.putMetric("DONE", " -> collected filtredEntMeOrgNew with size : " + filtredEtaMeOrgNew.size());

                //Filter SCORING to include more accounts
                var filtredScoring = entScoringNoteorts.stream().filter(e -> (e.getModificationDate() != null && LocalDate.parse(e.getModificationDate(), Constants.DF_YYYYMMDD)
                        .isAfter(LocalDate.now().minusDays(daysBeforeFullInScoring)))).toList();
                statsService.putMetric("DONE", " -> collected filtredScoring with size : " + filtredEtaMeOrgNew.size());

                //Predicates on group account to preocess
                Predicate<GroupCCIALOrg> groupToPrecess = e -> (e.getModificationDate() != null && LocalDate.parse(e.getModificationDate(), Constants.DF_YYYYMMDD)
                        .isAfter(LocalDate.now().minusDays(daysBeforeFull))); // A une date inferieur ou égal à N jour
                //Predicates on etablissement account to preocess
                Predicate<EtaRefXL2> etablissementToPrecess = e -> (e.getModificationDate() != null && LocalDate.parse(e.getModificationDate(), Constants.DF_YYYYMMDD)
                        .isAfter(LocalDate.now().minusDays(daysBeforeFull))) // A une date inferieur ou égal à N jour
                        ||  filtredEtaMeOrgNew.stream().anyMatch(p -> e.getIdETA().equals(p.getId()));// Ou sa date dans etaMeOrgNew est inferieur à N jour
                //Predicates on entreprises account to preocess
                Predicate<EntRefXL> entrepriseToPrecess = e -> (e.getModificationDate() != null && LocalDate.parse(e.getModificationDate(), Constants.DF_YYYYMMDD)
                        .isAfter(LocalDate.now().minusDays(daysBeforeFull))) // Et date inferieur à N jour
                        ||  filtredEntMeOrgNew.stream().anyMatch(p -> e.getId().equals(p.getIdEnt()))// Ou sa date dans entMeOrgNew est inferieur à N jour
                        ||  filtredScoring.stream().anyMatch(p -> e.getId().equals(p.getId()));// Ou sa date dans Scoring est inferieur à N jour


                //Element to be processed today
                var filtredGroupCCIALOrgs = allGroupCCIALOrgs.stream().filter(groupToPrecess).toList();
                statsService.putMetric("DONE", " -> collected filtredGroupCCIALOrgs with size : " + filtredGroupCCIALOrgs.size());

                var filtredEntRefXl2s = allEntRefXl2s.stream().filter(entrepriseToPrecess).toList();
                statsService.putMetric("DONE", " -> collected filtredEntRefXl2s with size : " + filtredEntRefXl2s.size());

                var filtredEtaRefXl2s = allEtaRefXl2s.stream().filter(etablissementToPrecess).toList();
                statsService.putMetric("DONE", " -> collected filtredEtaRefXl2s with size : " + filtredEtaRefXl2s.size());


                accountMapper = new AccountMapper(filtredGroupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps,
                        filtredEntRefXl2s, refRCES, entScoringNoteorts, filtredEtaRefXl2s, entEcoNews, etaMeOrgNews, rceService, segmentationList, agenceList, edgCodes, cacheService, groupCCIALOrgService);

            } else {
                log.info("Begin of refresh ...");
                persistenceService.refresh(allGroupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps,
                    allEntRefXl2s, refRCES, entScoringNoteorts, allEtaRefXl2s, entEcoNews, etaMeOrgNews);
                log.info("End of refresh ...");
                statsService.putMetric("DONE", " -> End of DB Refresh ");

                //Liste des id accounts
                List<String> etaMeOrgIds = etaMeOrgNews.stream().map(EtaMeOrgNew::getId).collect(Collectors.toList());
                statsService.addStat(" collected etaMeOrgIds from : " + etaMeOrgNews.size());
                List<String> entMeOrgIds = new ArrayList<>(entMeOrgs.stream().map(EntMeOrg::getIdEnt).toList());
                statsService.addStat(" collected entMeOrgIds from : " + entMeOrgs.size());
                List<String> constitIds = new ArrayList<>(groupCCIALConstits.stream().map(GroupCCIALConstit::getIdENT).toList());
                statsService.addStat(" collected constitIds from : " + groupCCIALConstits.size());
                List<String> scoringIds = new ArrayList<>(entScoringNoteorts.stream().map(EntScoringNoteort::getId).toList());
                statsService.addStat(" collected scoringIds from : " + entScoringNoteorts.size());
                List<String> entRefXlIds = allEntRefXl2s.stream().map(EntRefXL::getId).toList();
                statsService.addStat(" collected entRefXlIds from : " + allEntRefXl2s.size());

                statsService.addStat(" + Etablissements from etaMeOrgIds :  " + etaMeOrgIds.size());
                //Elimination des entreprises doublons
                statsService.addStat(" + Entreprises from entMeOrgs :  " + entMeOrgIds.size());
                entMeOrgIds.removeIf(scoringIds::contains);
                statsService.addStat(" + Entreprises from entScoringNoteorts :  " + scoringIds.size());
                scoringIds.removeIf(entMeOrgIds::contains);

                //Remove if already exists in REF-XL2
                scoringIds.removeIf(entRefXlIds::contains);
                entMeOrgIds.removeIf(entRefXlIds::contains);

                var entIds = new ArrayList<String>();
                entIds.addAll(entMeOrgIds);
                entIds.addAll(scoringIds);
                constitIds.removeIf(entIds::contains);
                statsService.addStat(" + Entreprises from groupCCIALConstits :  " + constitIds.size());
                entIds.addAll(constitIds);
                statsService.addStat(" [File] entScoringNoteorts / entMeOrgs / groupCCIALConstits after duplicates removed :  " + entIds.size());

                //Recherche des comptes trouvé via MeOrg, Scoring (S'il existe dans ces fichiers)
                //Et Ajout de ces comptes à la liste des comptes à traiter
                List<String> etaRefXlIds = allEtaRefXl2s.stream().map(EtaRefXL2::getIdETA).toList();
                if(!etaMeOrgIds.isEmpty()) {
                    etaMeOrgIds.removeIf(etaRefXlIds::contains); //Elimination de doublons
                    allEtaRefXl2s.addAll(etaRefXl2Service.getAllByIds(etaMeOrgIds));
                }
                if(!entIds.isEmpty()) {
                    entIds.removeIf(entRefXlIds::contains); //Elimination de doublons
                    allEntRefXl2s.addAll(entRefXl2Service.getAllByIds(entIds));
                }

                //Liste des id accounts
                entRefXlIds = allEntRefXl2s.stream().map(EntRefXL::getId).collect(Collectors.toList());
                etaRefXlIds = allEtaRefXl2s.stream().map(EtaRefXL2::getIdETA).collect(Collectors.toList());
                List<String> groupIds = allGroupCCIALOrgs.stream().map(GroupCCIALOrg::getIdGroup).collect(Collectors.toList());

                //Recherche des dépendances dans la BD
                etaMeOrgNews = etaMeOrgNewService.getAllByIds(etaRefXlIds);
                statsService.putMetric("DONE", " -> collected etaMeOrgNews with size : " + etaMeOrgNews.size());
                refRCES = refRCEService.retrieveAllReferencesRCE();
                statsService.putMetric("DONE", " -> collected refRCES with size : " + refRCES.size());
                entEcoNews = entEcoNewService.getAllByIds(entRefXlIds);
                statsService.putMetric("DONE", " -> collected entEcoNews with size : " + entEcoNews.size());
                entScoringNoteorts = entScoringNoteortService.getAllByIds(entRefXlIds);
                statsService.putMetric("DONE", " -> collected entScoringNoteorts with size : " + entScoringNoteorts.size());
                entMeComps = entMeCompService.getAllByIds(entRefXlIds);
                statsService.putMetric("DONE", " -> collected entMeComps with size : " + entMeComps.size());
                entMeOrgs = entMeOrgService.getAllByIds(entRefXlIds);
                statsService.putMetric("DONE", " -> collected entMeOrgs with size : " + entMeOrgs.size());
                groupCCIALConstits = groupCCIALConstitService.getAllByEntIds(entRefXlIds);
                statsService.putMetric("DONE", " -> collected groupCCIALConstits with size : " + groupCCIALConstits.size());
                groupCCIALComps = groupCCIALCompService.getAllByIds(groupIds);
                statsService.putMetric("DONE", " -> collected groupCCIALComps with size : " + groupCCIALComps.size());

                //Instanciation des mappings
                accountMapper = new AccountMapper(allGroupCCIALOrgs, groupCCIALComps, groupCCIALConstits, entMeOrgs, entMeComps,
                        allEntRefXl2s, refRCES, entScoringNoteorts, allEtaRefXl2s, entEcoNews, etaMeOrgNews, rceService, segmentationList, agenceList, edgCodes, cacheService, groupCCIALOrgService);

                statsService.addStat("Used following data: ");
                statsService.addStat(" [DB] refRCES :   " + refRCES.size());
                statsService.addStat(" [DB] entEcoNews :    " + entEcoNews.size());
                statsService.addStat(" [DB] entMeOrgs : " + entMeOrgs.size());
                statsService.addStat(" [DB] entMeComps :    " + entMeComps.size());
                statsService.addStat(" [DB] entScoringNoteorts :    " + entScoringNoteorts.size());
                statsService.addStat(" [DB] etaMeOrgNews :  " + etaMeOrgNews.size());
                statsService.addStat(" [DB] groupCCIALConstits :    " + groupCCIALConstits.size());
                statsService.addStat(" [DB] groupCCIALComps :   " + groupCCIALComps.size());
            }

            groupCount = accountMapper.groupCCIALOrgs().size();
            etablissementCount = accountMapper.etaRefXL2s().size();
            entrepriseCount = accountMapper.entRefXLS().size();

            groupeMapper = new GroupeMapper(accountMapper, rceService, errorHandler, entRefXl2Service, etaRefXl2Service);
            entrepriseMapper = new EntrepriseMapper(accountMapper, rceService, errorHandler, etaRefXl2Service);
            establishMapper = new EstablishMapper(accountMapper, rceService, errorHandler, etaMeOrgNewService);

        } catch (FileNotFoundException e) {
            log.error("Cannot find a file: {}", e.getMessage());
            throw new IllegalArgumentException("A File is not found while processing archive");
        } catch (IOException e) {
            log.error("Parsing error occured : {}", e.getMessage());
            throw new IllegalArgumentException("Cannot process a txt file extracted from archive");
        } catch (IllegalStateException e) {
            log.error("Error encountered while updating referential in DB : {}", e.getMessage());
            throw new IllegalStateException("Cannot refresh referential in DB");
        }
    }

    private String buildFilePath(FilePrefix filePrefix) {
        if (!pickTodayFiles) { // For dev and debug purposes
            for (File f : new File(temporaryDir).listFiles()) {
                if (f.getName().startsWith(filePrefix.getFilePrefix()))
                    return f.getPath();
            }
            throw new IllegalArgumentException("Can't get files in current directory "+temporaryDir);
        } else {
            return temporaryDir + filePrefix.getFilePrefix() +
                    Constants.DF_YYYYMMDD.format(LocalDate.now()) +
                    EXTRACTED_FILE_EXT;
        }
    }

    private void startProcessGroupes() {
        log.info("Start process for Groupes at {}", LocalDateTime.now());
        long startTime = System.currentTimeMillis();

        var result = groupeMapper.mapGroupToCL(filteredGroups);
        errorMapping = errorHandler.getErrors().size();

        int totalItems = result.size();
        AtomicInteger processedItems = new AtomicInteger(0);

        log.info("{} successfully mapped group to process", totalItems);
        result.parallelStream().forEach(
            group -> {
                if (group.getId() == null) {
                    if (group.getSuppressionDate() == null) {
                        createAccount(group).block();
                    }
                } else {
                    if (group.getSuppressionDate() != null) {
                        deleteGroup(group).block();
                    } else {
                        updateAccount(group).block();
                    }
                }

                int currentProcessed = processedItems.incrementAndGet();
                double percentage = (currentProcessed * 100.0) / totalItems;
                log.info("<<<<<<<{}% Processed groups  ({} out of {})>>>>>>>", String.format("%.2f", percentage), currentProcessed, totalItems);
                statsService.putMetric("Groups", "Processed " + currentProcessed+"/"+totalItems);
            });
        statsService.putMetric("DONE", "Processed All Groups ");

        long endTime = System.currentTimeMillis();
        log.info(THAT_TOOK_MILLISECONDS, (endTime - startTime));
        log.info("End process for Groupes at {}", LocalDateTime.now());
        groupeMapper = null;
    }

    private void startProcessEntreprises() {
        log.info("start process for Entreprises at {}", LocalDateTime.now());
        long startTime = System.currentTimeMillis();

        var result = entrepriseMapper.mapEnterpriseToCL(filteredEntreprises);
        errorMapping = errorHandler.getErrors().size();

        int totalItems = result.size();
        log.info("{} successfully mapped entreprise to process", result.size());
        AtomicInteger processedItems = new AtomicInteger(0);

        result.parallelStream().forEach(ent -> {
            if (ent.getId() == null) {
                createAccount(ent).block();
            } else {
                updateAccount(ent).block();
            }
            int currentProcessed = processedItems.incrementAndGet();
            double percentage = (currentProcessed * 100.0) / totalItems;
            log.info("<<<<<<<{}% Processed entreprises  ({} out of {})>>>>>>> ", String.format("%.2f", percentage), currentProcessed, totalItems);
            statsService.putMetric("Entreprises", "Processed " + currentProcessed+"/"+totalItems);
        });

        statsService.putMetric("DONE", "Processed All Entreprises ");

        long endTime = System.currentTimeMillis();
        log.info(THAT_TOOK_MILLISECONDS, (endTime - startTime));
        log.info("End process for Entreprises at {}", LocalDateTime.now());
        entrepriseMapper = null;
    }

    private void startProcessEtablissements() {
        log.info("start process for Etablissements at {}", LocalDateTime.now());
        long startTime = System.currentTimeMillis();

        var result = establishMapper.mapEstablishmentToCL(filteredEtablissements);
        errorMapping = errorHandler.getErrors().size();

        int totalItems = result.size();
        log.info("{} successfully mapped etablissement to process", result.size());
        AtomicInteger processedItems = new AtomicInteger(0);

        result.parallelStream().forEach(eta -> {
            if (eta.getId() == null) {
                createAccount(eta).block();
            } else {
                updateAccount(eta).block();
            }

            int currentProcessed = processedItems.incrementAndGet();
            double percentage = (currentProcessed * 100.0) / totalItems;
            log.info("<<<<<<{}% Processed Etablissements  ({} out of {})>>>>>>", String.format("%.2f", percentage), currentProcessed, totalItems);
            statsService.putMetric("Etablissements", "Processed " + currentProcessed+"/"+totalItems);
        });

        statsService.putMetric("DONE", "Processed All Etablissements ");

        long endTime = System.currentTimeMillis();
        log.info(THAT_TOOK_MILLISECONDS, (endTime - startTime));
        log.info("End process for Etablissements at {}", LocalDateTime.now());
        establishMapper = null;
    }

    private Mono<List<ResponseBody>> deleteGroup(Group group) {
        try {
            return rceService.delete(List.of(group.getId()), false)
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(10))
                .filter(throwable -> throwable instanceof HttpServerErrorException || throwable instanceof WebClientRequestException))
                .doOnNext((List<ResponseBody>  response) -> {
                    log.debug("<<<<<<<<< CL" + response);
                    //in case of a 200 with errors we through an exception
                    if(!response.isEmpty() && !response.get(0).isSuccess()){
                        log.error("Error on update account : {} ", group.getRceID(), response.get(0).getErrors());
                        reportError(group, "CL_DELETE_ERROR",response.get(0).getErrors().toString());
                        errorGroupDelete++;
                    } else {
                        deletedGroups++;
                    }
                });
        } catch (Exception e){
            log.error("Error on group deletion : {}", e.getMessage());
            var httpError = ((ApiCallException)e);
            String httpStatus = String.valueOf(httpError!=null?httpError.getStatusCode().value():"");
            reportError(group, "CL_DELETE_ERROR" + httpStatus, e.getMessage());
            errorGroupDelete++;
            return Mono.empty();
        }
    }

    private <T extends Account> Mono<List<ResponseBody>> createAccount(T account) {
        try {
            return rceService.post(getBody(account))
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(10))
                .filter(throwable -> throwable instanceof HttpServerErrorException || throwable instanceof WebClientRequestException))
                .doOnNext((List<ResponseBody>  response) -> {
                    log.debug("<<<<<<<<< CL" + response);
                    //in case of a 200 with errors we through an exception
                    if(!response.isEmpty() && !response.get(0).isSuccess()){
                        log.error("Error on create account : {} ", account.getRceID(), response.get(0).getErrors());
                        reportError(account, "CL_CREATE_ERROR",response.get(0).getErrors().toString());
                        errorCreate++;
                    } else {
                        created++;
                    }
                });
        } catch (Exception e) {
            log.error("Error on group creation : {}", e.getMessage());
            var httpError = ((ApiCallException)e);
            String httpStatus = String.valueOf(httpError!=null?httpError.getStatusCode().value():"");
            reportError(account, "CL_CREATE_ERROR" + httpStatus, e.getMessage());
            errorCreate++;
            return Mono.empty();        
        }                
    }

    private <T extends Account> Mono<List<ResponseBody>> updateAccount(T account) {
        try {
            return rceService
                .patch(getBody(account))
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(10))
                .filter(throwable -> throwable instanceof HttpServerErrorException || throwable instanceof WebClientRequestException))
                .doOnNext((List<ResponseBody>  response) -> {
                    log.debug("<<<<<<<<< CL" + response);
                    //in case of a 200 with errors we through an exception
                    if(!response.isEmpty() && !response.get(0).isSuccess()){
                        log.error("Error on update account : {} ", account.getRceID(), response.get(0).getErrors());
                        reportError(account, "CL_UPDATE_ERROR",response.get(0).getErrors().toString());
                        errorUpdate++;
                    } else {
                        updated++;
                    }
                });
        } catch (Exception e) {
            log.error("Error on group update : {}", e.getMessage());
            var httpError = ((ApiCallException)e);
            String httpStatus = String.valueOf(httpError!=null?httpError.getStatusCode().value():"");
            reportError(account, "CL_UPDATE_ERROR" + httpStatus, e.getMessage());
            errorUpdate++;
            return Mono.empty();        
        }  
    }

    private void reportError(Account account, String errorCode, String errors) {

        if(errors==null){
            errors = "no details";
        } else {
            errors = errors.replaceAll("[\\r\\n]", " ");
        }

        errorHandler.add(ErrorDTO.builder() 
            .errorMessage(errors)
            .errorCode(errorCode)
            .clinksId(account.getId())
            .rceId(account.getRceID())
            .type(ErrorDTO.AccountType.valueOf(account.getRceAccountType().toUpperCase()))
            .build());
    }

    private <T extends Account> Body<T> getBody(T account){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Body<T> body;
        try {
            String json = "{\n" +
                "   \"allOrNone\" : false,\n" +
                "   \"records\" : [" +objectMapper.writeValueAsString(account)+
                "]\n" +
                "}";
            body = objectMapper.readValue(json, Body.class);

        } catch (Exception e) {
            throw new IllegalStateException(String.format("Error processing JSON : %s.", e.getMessage()));
        }
      return body;
    }

    /**
     * Send mail in case of error when connecting to Efile or inexisting file
     */
    private void communicateTechnicalErrorsIfExist(StepExecution stepExecution) {
        List<Throwable> failures = stepExecution.getFailureExceptions();
        if (!failures.isEmpty())  {
            try {
                emailUtils.sendSimpleMessage(failures);
            } catch (Exception e) {
                log.warn("Exception sending email, {}", e.getMessage());
            }
        }
    }

    /**
     * serilise errors
     */
    private void serializeErrors(StepExecution stepExecution) {
        List<Throwable> failures = stepExecution.getFailureExceptions();
        if (!failures.isEmpty())  {
            var errorLogs = new File("technical_errors.txt");
            try (FileWriter fileWriter = new FileWriter(errorLogs)) {
                fileWriter.write(failures.toString());
            } catch (Exception e) {
                log.warn("Exception saving technical errors in {}, {}", errorLogs, e.getMessage());
            }
        }

        errorHandler.createCSV("", "errors_" + Constants.DF_YYYYMMDD.format(LocalDate.now()) + ".txt");
        writeStats(new File(temporaryDir + "synthese_" + Constants.DF_YYYYMMDD.format(LocalDate.now()) + ".txt"));
    }

    private void writeStats(File file) {
        log.info("Writing stats to synthese file in {}", file.getPath());
        try (FileWriter fileWriter = new FileWriter(file.getPath())) {
            statsService.addStat("\n========================== SYNTHESE " + LocalDateTime.now() + " ===========================");
            statsService.addStat("\n====== Received Groups : " + groupCount);
            statsService.addStat("\n====== Received Entreprises : " + entrepriseCount);
            statsService.addStat("\n====== Received Etablissements : " + etablissementCount);
            statsService.addStat("\n====================== Filters : " + (filteredGroups.size()+filteredEntreprises.size()+filteredEtablissements.size()));
            statsService.addStat("\n====== Filtered Groups " + filteredGroups);
            statsService.addStat("\n====== Filtered Entreprises " + filteredEntreprises);
            statsService.addStat("\n====== Filtered Etablissements " + filteredEtablissements);
            statsService.addStat("\n====================== CL Operations : " + (created+updated+deletedGroups));
            statsService.addStat("\n====== Created : " + created);
            statsService.addStat("\n====== Updated : " + updated);
            statsService.addStat("\n====== Deleted (Groups) : " + deletedGroups);
            statsService.addStat("\n====================== Errors : " + errorHandler.getErrors().size());
            statsService.addStat("\n======      Error Mappings : " + errorMapping);
            statsService.addStat("\n======      Error Create : " + errorCreate);
            statsService.addStat("\n======      Error Update : " + errorUpdate);
            statsService.addStat("\n======      Error Delete (Groups) : " + errorGroupDelete);
            statsService.addStat("\n====== Execution Time : " + new DecimalFormat("#.##").format((System.currentTimeMillis() - chrono)/(float)60000) + " minutes");
            statsService.addStat("\n=========================================================");
            statsService.getStats().forEach(e -> {log.info("\n" + e);try {fileWriter.write("\n" + e);} catch (IOException ex) {log.warn("unable to write element {}", e);}});
            fileWriter.write("\n=========================================================");

            //Resetting errors and stats
            errorCreate = 0;
            errorUpdate = 0;
            errorGroupDelete = 0;
            created = 0;
            updated = 0;
            deletedGroups = 0;
        } catch (Exception e) {
            log.warn("Exception saving synthese {}", e.getMessage());
        }
    }

    /**
     * Send mail to CustomerLinks when error
     * @param stepExecution 
     */
    private void communicateMappingErrorsIfExist(StepExecution stepExecution) {
        var synthese = new File(temporaryDir + "synthese_" + Constants.DF_YYYYMMDD.format(LocalDate.now()) + ".txt");
        writeStats(synthese);
        try {            
            if (!errorHandler.getErrors().isEmpty()) {
                log.info("Some errors occured, sending mail with synthese + error report");
                var errorFileName = buildErrorsFileName();
                errorHandler.createCSV(temporaryDir, errorFileName);
                emailUtils.sendMailWithAttachment(true, errorFileName, synthese.getName());
                errorHandler.reset();
            } else if(stepExecution.getFailureExceptions().isEmpty()) {
                log.info("Everything OK, sending mail with synthese file only");
                emailUtils.sendMailWithAttachment(false, synthese.getName());
            }
        } catch (Exception e) {
            log.warn("Exception sending email with CSV error file, {}", e.getMessage());
        }
        userCache.clear();
    }

    private String buildErrorsFileName() {
        return "errors_" +
                Constants.DF_YYYYMMDD.format(LocalDate.now()) +
               ".csv";
    }

    private void cleanupTemporaryDirectory() {
        if(!keepTmpFiles){
            log.info("Cleaning tmp dir {} ...", temporaryDir);
            File[] tmpFiles = new File(temporaryDir).listFiles();
            for (int i = 0; tmpFiles!=null && i < tmpFiles.length; i++) {
                try {
                    tmpFiles[i].delete();
                } catch (Exception e) {
                    log.warn("Can't delete {} : {}", tmpFiles[i], e.getMessage());
                }
            }
        }
    }
}
