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
package gov.vha.isaac.cradle.builders;

import gov.vha.isaac.ochre.api.ConceptProxy;
import gov.vha.isaac.ochre.api.IdentifiedComponentBuilder;
import gov.vha.isaac.ochre.api.logic.LogicalExpression;
import gov.vha.isaac.ochre.api.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.sememe.SememeBuilderService;
import gov.vha.isaac.ochre.api.sememe.SememeType;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author kec
 */
@Service
public class SememeBuilderProvider implements SememeBuilderService {

    @Override
    public SememeBuilder getComponentSememeBuilder(int memeComponentNid, IdentifiedComponentBuilder referencedComponent, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponent, assemblageConceptSequence, SememeType.COMPONENT_NID, new Object[] {memeComponentNid});
    }

    @Override
    public SememeBuilder getComponentSememeBuilder(int memeComponentNid, int referencedComponentNid, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponentNid, assemblageConceptSequence, SememeType.COMPONENT_NID, new Object[] {memeComponentNid});
    }

    @Override
    public SememeBuilder getConceptSememeBuilder(ConceptProxy memeConceptProxy, IdentifiedComponentBuilder referencedComponent, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponent, assemblageConceptSequence, SememeType.CONCEPT_SEQUENCE, new Object[] {memeConceptProxy});
    }

    @Override
    public SememeBuilder getConceptSememeBuilder(ConceptProxy memeConceptProxy, int referencedComponentNid, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponentNid, assemblageConceptSequence, SememeType.CONCEPT_SEQUENCE, new Object[] {memeConceptProxy});
    }

    @Override
    public SememeBuilder getConceptTimeSememeBuilder(ConceptProxy memeConceptProxy,
            long memeTime, IdentifiedComponentBuilder referencedComponent, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponent, assemblageConceptSequence, SememeType.CONCEPT_SEQUENCE_TIME, new Object[] {memeConceptProxy, memeTime});
    }

    @Override
    public SememeBuilder getConceptTimeSememeBuilder(ConceptProxy memeConceptProxy,
            long memeTime, int referencedComponentNid, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponentNid, assemblageConceptSequence, SememeType.CONCEPT_SEQUENCE_TIME, new Object[] {memeConceptProxy, memeTime});
    }

    @Override
    public SememeBuilder getLogicalExpressionSememeBuilder(LogicalExpression expression, IdentifiedComponentBuilder referencedComponent, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponent, assemblageConceptSequence, SememeType.LOGIC_GRAPH, new Object[] {expression});
    }

    @Override
    public SememeBuilder getLogicalExpressionSememeBuilder(LogicalExpression expression, int referencedComponentNid, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponentNid, assemblageConceptSequence, SememeType.LOGIC_GRAPH, new Object[] {expression});
    }

    @Override
    public SememeBuilder getMembershipSememeBuilder(IdentifiedComponentBuilder referencedComponent, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponent, assemblageConceptSequence, SememeType.MEMBER, new Object[] {});
    }

    @Override
    public SememeBuilder getMembershipSememeBuilder(int referencedComponentNid, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponentNid, assemblageConceptSequence, SememeType.STRING, new Object[] {});
    }

    @Override
    public SememeBuilder getStringSememeBuilder(String memeString, IdentifiedComponentBuilder referencedComponent, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponent, assemblageConceptSequence, SememeType.STRING, new Object[] {memeString});
    }

    @Override
    public SememeBuilder getStringSememeBuilder(String memeString, int referencedComponentNid, int assemblageConceptSequence) {
        return new SememeBuilderImpl(referencedComponentNid, assemblageConceptSequence, SememeType.STRING, new Object[] {memeString});
    }
    
}