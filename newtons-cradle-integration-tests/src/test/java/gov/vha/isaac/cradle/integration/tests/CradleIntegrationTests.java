/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.vha.isaac.cradle.integration.tests;

import static gov.vha.isaac.ochre.api.constants.Constants.CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY;
import gov.vha.isaac.cradle.taxonomy.walk.TaxonomyWalkAccumulator;
import gov.vha.isaac.cradle.taxonomy.walk.TaxonomyWalkCollector;
import gov.vha.isaac.metadata.coordinates.LanguageCoordinates;
import gov.vha.isaac.metadata.coordinates.LogicCoordinates;
import gov.vha.isaac.metadata.coordinates.StampCoordinates;
import gov.vha.isaac.metadata.coordinates.TaxonomyCoordinates;
import gov.vha.isaac.metadata.coordinates.ViewCoordinates;
import gov.vha.isaac.metadata.source.IsaacMetadataAuxiliaryBinding;
import gov.vha.isaac.ochre.api.ConceptModel;
import gov.vha.isaac.ochre.api.ConceptProxy;
import gov.vha.isaac.ochre.api.ConfigurationService;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.ObjectChronicleTaskService;
import gov.vha.isaac.ochre.api.TaxonomyService;
import gov.vha.isaac.ochre.api.component.concept.ConceptChronology;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.LogicGraphSememe;
import gov.vha.isaac.ochre.api.component.sememe.version.SememeVersion;
import gov.vha.isaac.ochre.api.coordinate.PremiseType;
import gov.vha.isaac.ochre.api.coordinate.TaxonomyCoordinate;
import gov.vha.isaac.ochre.api.logic.IsomorphicResults;
import gov.vha.isaac.ochre.api.memory.HeapUseTicker;
import gov.vha.isaac.ochre.api.progress.ActiveTasksTicker;
import gov.vha.isaac.ochre.api.tree.Tree;
import gov.vha.isaac.ochre.api.tree.TreeNodeVisitData;
import gov.vha.isaac.ochre.api.tree.hashtree.HashTreeWithBitSets;
import gov.vha.isaac.ochre.collections.ConceptSequenceSet;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import javafx.concurrent.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.api.MultiException;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.coordinate.ViewCoordinate;
import org.ihtsdo.otf.tcc.api.metadata.binding.Snomed;
import org.ihtsdo.otf.tcc.api.spec.ConceptSpec;
import org.ihtsdo.otf.tcc.api.spec.ValidationException;
import org.jvnet.testing.hk2testng.HK2;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author kec
 */
// https://www.jfokus.se/jfokus08/pres/jf08-HundredKilobytesKernelHK2.pdf
// https://github.com/saden1/hk2-testng
@HK2("cradle")
public class CradleIntegrationTests {

    public static void main(String[] args) {
        try {
            CradleIntegrationTests cit = new CradleIntegrationTests();
            cit.setUpSuite();
            cit.testLoad();
            cit.tearDownSuite();
        } catch (Exception ex) {
            log.fatal(ex.getLocalizedMessage(), ex);
        }
        System.exit(0);
    }

    private static final Logger log = LogManager.getLogger();
    private boolean dbExists = false;

    @BeforeGroups(groups = {"db"})
    public void setUpSuite() throws Exception {
        log.info("oneTimeSetUp");

        System.setProperty(CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY, "target/object-chronicles");

        java.nio.file.Path dbFolderPath = Paths.get(System.getProperty(CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY));
        dbExists = dbFolderPath.toFile().exists();
        System.out.println("termstore folder path: " + dbFolderPath.toFile().exists());

        LookupService.getService(ConfigurationService.class).setConceptModel(ConceptModel.OCHRE_CONCEPT_MODEL);
        LookupService.startupIsaac();
        ActiveTasksTicker.start(10);
        HeapUseTicker.start(10);
    }

    @AfterGroups(groups = {"db"})
    public void tearDownSuite() throws Exception {
        log.info("oneTimeTearDown");
        LookupService.shutdownIsaac();
        ActiveTasksTicker.stop();
        HeapUseTicker.stop();
    }

    @BeforeMethod
    public void setUp() throws Exception {

    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test(groups = {"db"})
    public void testLoad() throws Exception {

        Instant instant = Instant.now();
        
         log.info(DateTimeFormatter.ISO_DATE.format(instant.atOffset(ZoneOffset.UTC)));           
         
         TaxonomyCoordinate tc = Get.coordinateFactory().createDefaultStatedTaxonomyCoordinate().makeAnalog(2002, 01, 31, 0, 0, 0).makeAnalog(PremiseType.STATED);
        log.info(DateTimeFormatter.ISO_DATE.format(tc.getStampCoordinate().getStampPosition().getTimeAsInstant().atOffset(ZoneOffset.UTC)));           
 

        log.info("  Testing load...");
        ObjectChronicleTaskService tts = LookupService.get().getService(ObjectChronicleTaskService.class);

        String mapDbFolder = System.getProperty(CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY);
        if (mapDbFolder == null || mapDbFolder.isEmpty()) {
            throw new IllegalStateException(CHRONICLE_COLLECTIONS_ROOT_LOCATION_PROPERTY + " has not been set.");
        }

        if (!dbExists) {
            loadDatabase(tts);
            boolean differences = testLoad(tts);

            /*
             There will be some differences as importing change sets or the 
             path eConcept file may have multiple incremental entries for 
             a single concept. 
            
             May want a white list or similar at some point.
             Assert.assertTrue(differences);
             */
        }

        testDescriptions();
        
        testDifferenceAlgorithm();
        testTaxonomy();

        walkTaxonomy();

        findRoots();

        HashTreeWithBitSets g = makeGraph();
        log.info("    taxonomy graph size:" + g.size());
        log.info("    taxonomy graph roots: " + Arrays.toString(g.getRootSequences()));
        for (int rootSequence : IntStream.of(g.getRootSequences()).limit(100).toArray()) {
            log.info("    rootSequence: " + rootSequence);
            ConceptChronology aRoot = Get.conceptService().getConcept(rootSequence);
            log.info("    root concept: " + aRoot.toUserString());
            log.info("    parents of root" + Arrays.toString(g.getParentSequences(rootSequence)));
            TreeNodeVisitData dfsData = g.depthFirstProcess(rootSequence,
                    (nodeVisitData, sequence) -> {
                    });

            int maxDepth = dfsData.getMaxDepth();
            log.info(" Max depth from root (dfs): " + maxDepth);
            TreeNodeVisitData bfsData = g.breadthFirstProcess(rootSequence,
                    (nodeVisitData, sequence) -> {
                    });

            int maxBfsDepth = bfsData.getMaxDepth();
            log.info(" Max depth from root (bfs): " + maxBfsDepth);
            log.info("\n\n");
        }

        log.info("  Taxonomy graph leaves: " + g.getLeafSequences().count());
        log.info("  Taxonomy graph size: " + g.size());
        log.info("  Taxonomy graph concepts with parents count: " + g.conceptSequencesWithParentsCount());
        log.info("  Taxonomy graph concepts with children count: " + g.conceptSequencesWithChildrenCount());

        StringBuilder sb = new StringBuilder();
        ConceptChronology root = Get.conceptService().getConcept(IsaacMetadataAuxiliaryBinding.ISAAC_ROOT.getNid());
        TaxonomyService taxonomyService = Get.taxonomyService();

        int[] originSequences = taxonomyService.getAllRelationshipOriginSequences(root.getConceptSequence()).toArray();
        AtomicInteger relCount = new AtomicInteger(1);
        if (originSequences.length == 0) {
            log.info(" No incoming rels for: " + root);
        } else {
            log.info(" Found " + originSequences.length + " incoming rels for: " + root);
            for (int originSequence : originSequences) {
                sb.append(relCount.getAndIncrement()).append(": ").append(Get.conceptService().getConcept(originSequence).toUserString()).append("\n");
            }
        }
        log.info(sb.toString());
        //

        cycleTest();
    }
   
    private void testDescriptions() throws ValidationException{
        log.info("Testing descriptions.");
        
        ConceptSpec substance = new ConceptSpec("Substance (substance)", 
                UUID.fromString("95f41098-8391-3f5e-9d61-4b019f1de99d"));
        String substanceFSN = "Substance (substance)";
        String substancePT = "Substance";
        
        ConceptSpec object = new ConceptSpec("Physical object (physical object)", 
                UUID.fromString("72765109-6b53-3814-9b05-34ebddd16592"));
        String objectFSN = "Physical object (physical object)";
        String objectPT = "Physical object";
        
        DescriptionSememe<?> fsn = Get.conceptService().getConcept(substance.getLenient().getPrimordialUuid()).getFullySpecifiedDescription(
                LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate(),
                StampCoordinates.getDevelopmentLatestActiveOnly()).get().value();
        assertEquals(fsn.getText(), substanceFSN);
        
         DescriptionSememe<?> pt = Get.conceptService().getConcept(substance.getLenient().getPrimordialUuid()).getPreferredDescription(
                LanguageCoordinates.getUsEnglishLanguagePreferredTermCoordinate(),
                StampCoordinates.getDevelopmentLatestActiveOnly()).get().value();
        assertEquals(pt.getText(), substancePT);
        
        fsn = Get.conceptService().getConcept(object.getLenient().getPrimordialUuid()).getFullySpecifiedDescription(
                LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate(),
                StampCoordinates.getDevelopmentLatestActiveOnly()).get().value();
        assertEquals(fsn.getText(), objectFSN);
        
        pt = Get.conceptService().getConcept(object.getLenient().getPrimordialUuid()).getPreferredDescription(
                LanguageCoordinates.getUsEnglishLanguagePreferredTermCoordinate(),
                StampCoordinates.getDevelopmentLatestActiveOnly()).get().value();
        assertEquals(pt.getText(), objectPT);
    }



    private void testDifferenceAlgorithm() {

        logDifferenceReport("8001dc9b-39c2-38fb-b4f5-df1dbdd5dbe5");
        logDifferenceReport("800bf3d7-498a-3b60-888f-41042a317b41");
    }

    private void logDifferenceReport(String uuidStr) {
        ConceptChronology conceptToTest = Get.conceptService().getConcept(UUID.fromString(uuidStr));
        String report = conceptToTest.getLogicalDefinitionChronologyReport(StampCoordinates.getDevelopmentLatest(), PremiseType.STATED, LogicCoordinates.getStandardElProfile());
        log.info(Get.conceptDescriptionText(conceptToTest.getConceptSequence()) + " UUID: " + conceptToTest.getUuidList());
        log.info("\n" + report);
    }

    private void findRoots() {
        try {
            ViewCoordinate vc = ViewCoordinates.getDevelopmentInferredLatestActiveOnly();
            log.info("Walking 10 concepts to root inferred.");
            IntStream conceptSequenceStream = Get.identifierService().getConceptSequenceStream().
                    filter((int conceptSequence) -> Get.conceptService().isConceptActive(conceptSequence,
                                    vc)).limit(10);
            TaxonomyService taxonomyService = Get.taxonomyService();
            conceptSequenceStream.forEach((int conceptSequence) -> {
                walkToRoot(conceptSequence, taxonomyService, vc, 0, new BitSet());
                System.out.println("\n\n");
            });
            log.info("Walking 10 concepts to root stated.");
            conceptSequenceStream = Get.identifierService().getConceptSequenceStream().
                    filter((int conceptSequence) -> Get.conceptService().isConceptActive(conceptSequence,
                                    vc)).limit(10);
            ViewCoordinate vc2 = ViewCoordinates.getDevelopmentStatedLatestActiveOnly();
            conceptSequenceStream.forEach((int conceptSequence) -> {
                walkToRoot(conceptSequence, taxonomyService, vc2, 0, new BitSet());
                System.out.println("\n\n");
            });
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    private void walkToRoot(int child, TaxonomyService taxonomyService, TaxonomyCoordinate tc,
            int depth, BitSet visited) {
        visited.set(child);
        if (depth > 50) {
            return;
        }

        if (Get.conceptService().isConceptActive(child, tc.getStampCoordinate())) {
            printTaxonomyLevel(child, depth, "");
            taxonomyService.getTaxonomyParentSequences(child, tc).forEach((parentSequence) -> {
                if (!visited.get(parentSequence)) {
                    walkToRoot(parentSequence, taxonomyService, tc, depth + 1, visited);
                } else {
                    printTaxonomyLevel(parentSequence, depth + 1, "...");
                }
            });
        }
    }

    private void printTaxonomyLevel(int child, int depth, String suffix) {

        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
        sb.append(Get.conceptDescriptionText(child));
        sb.append(suffix);
        System.out.println(sb.toString());
    }

    private void loadDatabase(ObjectChronicleTaskService tts) throws ExecutionException, IOException, MultiException, InterruptedException {
        Path snomedDataFile = Paths.get("target/data/sctSiEConcepts.jbin");
        Path isaacMetadataFile = Paths.get("target/data/isaac/metadata/econ/IsaacMetadataAuxiliary.econ");

        Instant start = Instant.now();

        Task<Integer> loadTask = tts.startLoadTask(ConceptModel.OCHRE_CONCEPT_MODEL, IsaacMetadataAuxiliaryBinding.DEVELOPMENT,
                isaacMetadataFile, snomedDataFile);
        int conceptCount = loadTask.get();
        Instant finish = Instant.now();
        Duration duration = Duration.between(start, finish);
        log.info("  Loaded " + conceptCount + " concepts in: " + duration);
        double nsPerConcept = 1.0d * duration.toNanos() / conceptCount;
        log.info("  nsPerConcept: {}", nsPerConcept);

        double msPerConcept = 1.0d * duration.toMillis() / conceptCount;
        log.info("  msPerConcept: {}", msPerConcept);

        log.info("  concepts in map: {}", Get.conceptService().getConceptCount());

        log.info("  sequences map: {}", Get.identifierService().getConceptSequenceStream().distinct().count());
    }

    private boolean testLoad(ObjectChronicleTaskService tts) throws ExecutionException, IOException, MultiException, InterruptedException {
        Path snomedDataFile = Paths.get("target/data/sctSiEConcepts.jbin");
        Path isaacMetadataFile = Paths.get("target/data/isaac/metadata/econ/IsaacMetadataAuxiliary.econ");
        Instant start = Instant.now();

        Task<Boolean> verifyTask = tts.startVerifyTask(IsaacMetadataAuxiliaryBinding.DEVELOPMENT,
                isaacMetadataFile, snomedDataFile);
        boolean verified = verifyTask.get();
        Instant finish = Instant.now();
        Duration duration = Duration.between(start, finish);
        log.info("  Verified concepts in: " + duration);
        log.info("  concepts in map: {}", Get.conceptService().getConceptCount());
        log.info("  sequences map: {}", Get.identifierService().getConceptSequenceStream().distinct().count());

        return verified;
    }

    private void testTaxonomy() {
        try {
            log.info("Stated circular rels, latest: \n");
            checkForCircularRels(Get.coordinateFactory().createDefaultStatedTaxonomyCoordinate());
            log.info("Inferred circular rels, latest: \n");
            checkForCircularRels(Get.coordinateFactory().createDefaultInferredTaxonomyCoordinate());
            
            //createCircularRelsMetrics();
            testTaxonomy(ViewCoordinates.getDevelopmentInferredLatest());
            testTaxonomy(ViewCoordinates.getDevelopmentStatedLatest());
        } catch (IOException | ContradictionException ex) {
            log.error(ex.getLocalizedMessage(), ex);
        }
    }

    private void createCircularRelsMetrics() {
        TaxonomyService taxonomy = Get.taxonomyService();
        Map<PremiseType, Map<String, Map<Instant, Integer>>> data = new HashMap<>();
        PremiseType[] premiseTypes = new PremiseType[]{PremiseType.STATED, PremiseType.INFERRED};
        Set<Instant> timeSet = new TreeSet<>();
       Set<String> roleSet = new TreeSet<>();
        int[] years = new int[]{2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015};
        //int[] years = new int[]{2003};
        int[] months = new int[]{01, 07};
        for (PremiseType premiseType : premiseTypes) {
            data.put(premiseType, new TreeMap<>());
            Map<String, Map<Instant, Integer>> roleTimeCountMap = data.get(premiseType);
            for (int year : years) {
                for (int month : months) {
                    TaxonomyCoordinate tc = Get.coordinateFactory().createDefaultStatedTaxonomyCoordinate().makeAnalog(year, month, 31, 0, 0, 0).makeAnalog(premiseType);
                    Instant time = tc.getStampCoordinate().getStampPosition().getTimeAsInstant();
                    timeSet.add(time);
                    Get.taxonomyService().getAllCircularRelationshipOriginSequences(tc).forEach((conceptSequence) -> {
                        taxonomy.getAllCircularRelationshipTypeSequences(conceptSequence, tc).forEach((typeSequence) -> {
                            String typeDescription = Get.conceptDescriptionText(typeSequence);
                            roleSet.add(typeDescription);
                            if (!roleTimeCountMap.containsKey(typeDescription)) {
                                roleTimeCountMap.put(typeDescription, new TreeMap<>());
                            }
                            Map<Instant, Integer> timeCountMap = roleTimeCountMap.get(typeDescription);
                            if (timeCountMap.containsKey(time)) {
                                timeCountMap.put(time, timeCountMap.get(time) + 1);
                            } else {
                                timeCountMap.put(time, 1);
                            }
                        });
                    });
                }
            }
        }
        
                
        StringBuilder statedResults = assembleResults(data, PremiseType.STATED, timeSet, roleSet);
        StringBuilder inferredResults = assembleResults(data, PremiseType.INFERRED, timeSet, roleSet);
        log.info("Stated results\n" + statedResults.toString());
        log.info("Inferred results\n" + inferredResults.toString());
    }

    private StringBuilder assembleResults(Map<PremiseType, Map<String, Map<Instant, Integer>>> data, PremiseType premiseType, Set<Instant> timeSet, Set<String> roleSet) {
        DateTimeFormatter formatter =DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.of("Z"));
        Map<String, Map<Instant, Integer>> roleTimeCountMap = data.get(premiseType);
        StringBuilder builder = new StringBuilder();
        builder.append("role\t");
        timeSet.forEach((time) -> {
            builder.append(formatter.format(time)).append('\t');
        });
        builder.deleteCharAt(builder.length()-1);
        builder.append('\n');
        
        roleSet.forEach((roleText) -> {
            builder.append(roleText).append('\t');
            Map<Instant, Integer> timeCountMap = roleTimeCountMap.get(roleText);
            timeSet.forEach((time) -> {
                if (timeCountMap == null) {
                    builder.append(0).append('\t');
                } else {
                    builder.append(timeCountMap.getOrDefault(time, 0)).append('\t');
                }
            });
            builder.deleteCharAt(builder.length()-1);
            builder.append('\n');
        });
        return builder;
    }

    private void checkForCircularRels(TaxonomyCoordinate tc) {
        TaxonomyService taxonomy = Get.taxonomyService();
        Map<Integer, Integer> typeCountMap = new HashMap<>();

        StringBuilder circularRelConcepts = new StringBuilder();

        Get.taxonomyService().getAllCircularRelationshipOriginSequences(tc).forEach((conceptSequence) -> {
            taxonomy.getAllCircularRelationshipTypeSequences(conceptSequence, tc).forEach((typeSequence) -> {
                if (typeCountMap.containsKey(typeSequence)) {
                    typeCountMap.put(typeSequence, typeCountMap.get(typeSequence) + 1);
                } else {
                    typeCountMap.put(typeSequence, 1);
                }
                circularRelConcepts.append(Get.conceptDescriptionText(typeSequence))
                        .append(": ").append(Get.conceptDescriptionText(conceptSequence))
                        .append("\n");
            });
        });
        log.info("Circular stated rel concepts: \n" + circularRelConcepts.toString());
        typeCountMap.forEach((typeSequence, count) -> {
            log.info(Get.conceptDescriptionText(typeSequence) + ": " + count);
        });
    }

    private void testTaxonomy(TaxonomyCoordinate<?> vc) throws IOException, ContradictionException {
        int disorderOfCorneaNid = Snomed.DISORDER_OF_CORNEA.getNid();
        int disorderOfEyeNid = Snomed.DISORDER_OF_EYE.getNid();
        TaxonomyService taxonomyService = Get.taxonomyService();

        boolean isChild = taxonomyService.isChildOf(disorderOfCorneaNid, disorderOfEyeNid, vc);
        boolean isKind = taxonomyService.isKindOf(disorderOfCorneaNid, disorderOfEyeNid, vc);

        System.out.println("Cornea is a " + vc.getTaxonomyType() + " child-of disorder of eye: " + isChild);
        System.out.println("Cornea is a " + vc.getTaxonomyType() + " kind-of of disorder of eye: " + isKind);
    }

    private void walkTaxonomy() throws IOException {
        log.info("  Start walking taxonomy.");
        Instant collectStart = Instant.now();
        IntStream conceptSequenceStream = Get.identifierService().getParallelConceptSequenceStream();
        TaxonomyWalkCollector collector = new TaxonomyWalkCollector(
                ViewCoordinates.getDevelopmentStatedLatestActiveOnly());
        TaxonomyWalkAccumulator taxonomyWalkAccumulator = conceptSequenceStream.collect(
                TaxonomyWalkAccumulator::new,
                collector,
                collector);
        Instant collectEnd = Instant.now();
        Duration collectDuration = Duration.between(collectStart, collectEnd);
        log.info("  Finished walking taxonomy: " + taxonomyWalkAccumulator);
        log.info("  Collection duration: " + collectDuration);
    }

    private HashTreeWithBitSets makeGraph() throws IOException {
        log.info("  Start to make taxonomy snapshot graph.");
        Instant collectStart = Instant.now();
        TaxonomyService taxonomyService = Get.taxonomyService();
        Tree taxonomyTree = taxonomyService.getTaxonomyTree(ViewCoordinates.getDevelopmentInferredLatestActiveOnly());
        Instant collectEnd = Instant.now();
        Duration collectDuration = Duration.between(collectStart, collectEnd);
        log.info("  Finished making graph: " + taxonomyTree);
        log.info("  Generation duration: " + collectDuration);
        return (HashTreeWithBitSets) taxonomyTree;
    }

    private void cycleTest() {
        int[] descriptionTypePreferenceList = new int[]{
            IsaacMetadataAuxiliaryBinding.SYNONYM.getConceptSequence(),
            IsaacMetadataAuxiliaryBinding.FULLY_SPECIFIED_NAME.getConceptSequence()
        };
        Get.configurationService().setDefaultDescriptionTypePreferenceList(descriptionTypePreferenceList);
        log.info("Testing with DevelopmentLatestActiveOnly StampCoordinate");
        cycleTestForTaxonomyCoordinate(TaxonomyCoordinates.getInferredTaxonomyCoordinate(StampCoordinates.getDevelopmentLatestActiveOnly(),
                LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate()));
        log.info("Testing with DevelopmentLatest StampCoordinate (includes active and inactive)");
        cycleTestForTaxonomyCoordinate(TaxonomyCoordinates.getInferredTaxonomyCoordinate(StampCoordinates.getDevelopmentLatest(),
                LanguageCoordinates.getUsEnglishLanguageFullySpecifiedNameCoordinate()));
    }

    private void cycleTestForTaxonomyCoordinate(TaxonomyCoordinate taxonomyCoordinate) {
        Tree tree = Get.taxonomyService().getTaxonomyTree(taxonomyCoordinate);

        ConceptProxy calcinosisProxy = new ConceptProxy("Calcinosis (disorder)", UUID.fromString("779ece66-7e95-323e-a261-214caf48c408"));
        ConceptSequenceSet calcinosisParents = getParentsSequences(calcinosisProxy.getConceptSequence(), tree, taxonomyCoordinate);
        log.info(calcinosisProxy.getConceptDescriptionText() + " parents: " + calcinosisParents);

        Optional<SememeChronology<? extends SememeVersion<?>>> statedDefinition = Get.statedDefinitionChronology(calcinosisProxy.getNid());
        if (statedDefinition.isPresent()) {
            List<? extends SememeVersion> versions = statedDefinition.get().getVisibleOrderedVersionList(taxonomyCoordinate.getStampCoordinate());
            for (int i = 1; i < versions.size(); i++) {
                LogicGraphSememe comparison = (LogicGraphSememe) versions.get(i - 1);
                LogicGraphSememe reference = (LogicGraphSememe) versions.get(i);
                IsomorphicResults isomorphicResults = reference.getLogicalExpression().findIsomorphisms(comparison.getLogicalExpression());
                log.info("isomorphic results: " + isomorphicResults);
            }
        }

//        ConceptProxy psychoactiveAbuseProxy = new ConceptProxy("Psychoactive substance abuse (disorder)", UUID.fromString("778a75c9-8264-36aa-9ad6-b9c6e5ee9187"));
//        
//        
//        
//        ConceptSequenceSet psychoactiveAbuseParents = getParentsSequences(psychoactiveAbuseProxy.getConceptSequence(), tree, taxonomyCoordinate);
//        log.info(psychoactiveAbuseProxy.getConceptDescriptionText() + " parents: " + psychoactiveAbuseParents);
    }

    public static ConceptSequenceSet getParentsSequences(int childSequence,
            Tree taxonomyTree, TaxonomyCoordinate tc) {
        int[] parentSequences = taxonomyTree.getParentSequences(childSequence);

        ConceptSequenceSet parentSequenceSet = new ConceptSequenceSet();

        for (int parentSequence : parentSequences) {
            if (Get.taxonomyService().isChildOf(childSequence, parentSequence, tc)) {
                if (!Get.taxonomyService().isChildOf(parentSequence, childSequence, tc)) {
                    parentSequenceSet.add(parentSequence);
                } else {
                    log.info("{} is BOTH child and parent of concept (retrieved by taxonomyTree.getParentSequences()) {}",
                            Get.conceptDescriptionText(childSequence),
                            Get.conceptDescriptionText(parentSequence));
                    log.info("\nStated definition: {}\nInferred definition: {}",
                            Get.statedDefinitionChronology(childSequence),
                            Get.statedDefinitionChronology(parentSequence));
                }
            } else {
                log.info("{} is NOT a child of concept (retrieved by taxonomyTree.getParentSequences()) {}",
                        Get.conceptDescriptionText(childSequence),
                        Get.conceptDescriptionText(parentSequence));
            }
        }

        return parentSequenceSet;
    }
}
