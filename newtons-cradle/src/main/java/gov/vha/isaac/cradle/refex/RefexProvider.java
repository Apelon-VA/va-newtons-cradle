/*
 * Copyright 2015 kec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.cradle.refex;

import org.ihtsdo.otf.tcc.model.cc.refex.RefexService;
import gov.vha.isaac.cradle.Cradle;
import gov.vha.isaac.cradle.collections.ConcurrentSequenceSerializedObjectMap;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.IdentifierService;
import gov.vha.isaac.ochre.api.SystemStatusService;
import gov.vha.isaac.ochre.collections.RefexSequenceSet;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.hk2.runlevel.RunLevel;
import org.ihtsdo.otf.tcc.model.cc.NidPairForRefex;
import org.ihtsdo.otf.tcc.model.cc.refex.RefexMember;
import org.jvnet.hk2.annotations.Service;

/**
 * TODO convert to CasSequenceObjectMap
 * @author kec
 */
@Service
@RunLevel(value = 0)
public class RefexProvider implements RefexService {

    private static final Logger log = LogManager.getLogger();

    final ConcurrentSequenceSerializedObjectMap<RefexMember<?, ?>> refexMap;
    final ConcurrentSkipListSet<RefexKey> assemblageSequenceReferencedNidRefexSequenceMap = new ConcurrentSkipListSet<>();
    final ConcurrentSkipListSet<RefexKey> referencedNidAssemblageSequenceRefexSequenceMap = new ConcurrentSkipListSet<>();
    final IdentifierService sequenceProvider;

    //For HK2 only
    private RefexProvider() throws IOException {
        try {
            sequenceProvider = LookupService.getService(IdentifierService.class);
            
            Path refexMapPath = Cradle.getCradlePath().resolve("refex-map");
            log.info("Starting RefexProvider - using from " + refexMapPath.toAbsolutePath().toString());
            
            refexMap = new ConcurrentSequenceSerializedObjectMap(new RefexSerializer(), refexMapPath, "seg.", ".refex.map");
        }
        catch (Exception e) {
            LookupService.getService(SystemStatusService.class).notifyServiceConfigurationFailure("Refex Provider", e);
            throw e;
        }
    }

    @PostConstruct
    private void startMe() throws IOException {
        try {
            if (!Cradle.cradleStartedEmpty()) {
                log.info("Loading refexMap.");
                refexMap.read();
    
                log.info("Loading RefexKeys.");
    
                try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(Cradle.getCradlePath().toFile(), "refex.keys"))))) {
                    int size = in.readInt();
                    for (int i = 0; i < size; i++) {
                        int key1 = in.readInt();
                        int key2 = in.readInt();
                        int sequence = in.readInt();
                        assemblageSequenceReferencedNidRefexSequenceMap.add(new RefexKey(key1, key2, sequence));
                        referencedNidAssemblageSequenceRefexSequenceMap.add(new RefexKey(key2, key1, sequence));
                    }
                }
                log.info("Finished RefexProvider load.");
            }
        }
        catch (Exception e) {
            LookupService.getService(SystemStatusService.class).notifyServiceConfigurationFailure("Refex Provider", e);
            throw e;
        }

    }

    @PreDestroy
    private void stopMe() throws IOException {
        log.info("Stopping RefexProvider pre-destroy. ");

        log.info("refexMap size: {}", refexMap.getSize());
        log.info("writing refex-map.");
        refexMap.write();

        log.info("writing RefexKeys.");
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(Cradle.getCradlePath().toFile(), "refex.keys"))))) {
            out.writeInt(assemblageSequenceReferencedNidRefexSequenceMap.size());
            for (RefexKey key : assemblageSequenceReferencedNidRefexSequenceMap) {
                out.writeInt(key.key1);
                out.writeInt(key.key2);
                out.writeInt(key.refexSequence);
            }
        }
        log.info("Finished RefexProvider stop.");
    }

    @Override
    public RefexMember<?, ?> getRefex(int refexSequence) {
        refexSequence = sequenceProvider.getRefexSequence(refexSequence);
        return refexMap.getQuick(refexSequence);
    }

    @Override
    public Stream<RefexMember<?, ?>> getRefexesFromAssemblage(int assemblageSequence) {
        RefexSequenceSet refexSequences = getRefexSequencesFromAssemblage(assemblageSequence);
        return refexSequences.stream().mapToObj((int value) -> refexMap.getQuick(value));
    }

    @Override
    public RefexSequenceSet getRefexSequencesFromAssemblage(int assemblageSequence) {
        assemblageSequence = sequenceProvider.getRefexSequence(assemblageSequence);
        RefexKey rangeStart = new RefexKey(assemblageSequence, Integer.MIN_VALUE, Integer.MIN_VALUE); // yes
        RefexKey rangeEnd = new RefexKey(assemblageSequence, Integer.MAX_VALUE, Integer.MAX_VALUE); // no
        NavigableSet<RefexKey> assemblageRefexKeys
                = assemblageSequenceReferencedNidRefexSequenceMap.subSet(rangeStart, true,
                        rangeEnd, true
                );
        return RefexSequenceSet.of(assemblageRefexKeys.stream().mapToInt((RefexKey key) -> key.refexSequence));
    }

    @Override
    public Stream<RefexMember<?, ?>> getRefexesForComponent(int componentNid) {
        RefexSequenceSet refexSequences = getRefexSequencesForComponent(componentNid);
        return refexSequences.stream().mapToObj((refexSequence)-> getRefex(refexSequence));
        
    }

    @Override
    public RefexSequenceSet getRefexSequencesForComponent(int componentNid) {
        if (componentNid >= 0) {
            throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
        }
        NavigableSet<RefexKey> assemblageRefexKeys
                = referencedNidAssemblageSequenceRefexSequenceMap.subSet(
                        new RefexKey(componentNid, Integer.MIN_VALUE, Integer.MIN_VALUE), true,
                        new RefexKey(componentNid, Integer.MAX_VALUE, Integer.MAX_VALUE), true
                );
        return RefexSequenceSet.of(assemblageRefexKeys.stream().mapToInt((RefexKey key) -> key.refexSequence));
    }

    @Override
    public Stream<RefexMember<?, ?>> getRefexsForComponentFromAssemblage(int componentNid, int assemblageSequence) {
        RefexSequenceSet refexSequences = getRefexSequencesForComponentFromAssemblage(componentNid, assemblageSequence);
        return refexSequences.stream().mapToObj((refexSequence)-> getRefex(refexSequence));
    }

    @Override
    public RefexSequenceSet getRefexSequencesForComponentFromAssemblage(int componentNid, int assemblageSequence) {
        if (componentNid >= 0) {
            throw new IndexOutOfBoundsException("Component identifiers must be negative. Found: " + componentNid);
        }
        assemblageSequence = sequenceProvider.getRefexSequence(assemblageSequence);
        RefexKey rangeStart = new RefexKey(assemblageSequence, componentNid, Integer.MIN_VALUE); // yes
        RefexKey rangeEnd = new RefexKey(assemblageSequence, componentNid, Integer.MAX_VALUE); // no
        NavigableSet<RefexKey> assemblageRefexKeys
                = assemblageSequenceReferencedNidRefexSequenceMap.subSet(rangeStart, true,
                        rangeEnd, true
                );
        return RefexSequenceSet.of(assemblageRefexKeys.stream().mapToInt((RefexKey key) -> key.refexSequence));
    }

    @Override
    public void writeRefex(RefexMember<?, ?> refex) {
        int sequence = sequenceProvider.getRefexSequence(refex.getNid());
        int assemblageSequence = sequenceProvider.getRefexSequence(refex.assemblageNid);
        if (!refexMap.containsKey(sequence)) {
            assemblageSequenceReferencedNidRefexSequenceMap.add(new RefexKey(assemblageSequence,
                    refex.referencedComponentNid, sequence));
            referencedNidAssemblageSequenceRefexSequenceMap.add(new RefexKey(refex.referencedComponentNid,
                    assemblageSequence,
                    sequence));
        }
        refexMap.put(sequence, refex);
    }

    @Override
    public Stream<RefexMember<?, ?>> getRefexStream() {
        return refexMap.getStream();
    }

    @Override
    public Stream<RefexMember<?, ?>> getParallelRefexStream() {
        return refexMap.getParallelStream();
    }

    @Override
    public void forgetXrefPair(int referencedComponentNid, NidPairForRefex nidPairForRefex) {
        int sequence = sequenceProvider.getSememeSequence(nidPairForRefex.getMemberNid());
        int assemblageSequence = sequenceProvider.getRefexSequence(nidPairForRefex.getRefexNid());
        assemblageSequenceReferencedNidRefexSequenceMap.remove(new RefexKey(assemblageSequence, referencedComponentNid, sequence));
        referencedNidAssemblageSequenceRefexSequenceMap.remove(new RefexKey(referencedComponentNid, assemblageSequence, sequence));
    }

}
