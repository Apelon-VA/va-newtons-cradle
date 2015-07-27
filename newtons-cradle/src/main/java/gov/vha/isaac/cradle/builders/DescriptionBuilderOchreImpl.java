/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
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

import gov.vha.isaac.metadata.coordinates.LanguageCoordinates;
import gov.vha.isaac.metadata.source.IsaacMetadataAuxiliaryBinding;
import gov.vha.isaac.ochre.api.ConceptProxy;
import gov.vha.isaac.ochre.api.Get;
import gov.vha.isaac.ochre.api.LookupService;
import gov.vha.isaac.ochre.api.commit.ChangeCheckerMode;
import gov.vha.isaac.ochre.api.component.concept.ConceptBuilder;
import gov.vha.isaac.ochre.api.component.concept.description.DescriptionBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilder;
import gov.vha.isaac.ochre.api.component.sememe.SememeBuilderService;
import gov.vha.isaac.ochre.api.component.sememe.SememeChronology;
import gov.vha.isaac.ochre.api.component.sememe.version.DescriptionSememe;
import gov.vha.isaac.ochre.api.coordinate.EditCoordinate;
import gov.vha.isaac.ochre.model.sememe.SememeChronologyImpl;
import gov.vha.isaac.ochre.model.sememe.version.DescriptionSememeImpl;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kec
 * @param <T>
 * @param <V>
 */
public class DescriptionBuilderOchreImpl<T extends SememeChronology<V>, V extends DescriptionSememeImpl> extends 
            ComponentBuilder<T> 
    implements DescriptionBuilder<T,V> {
   private static final int descriptionAssemblageSequence;

    static {
        descriptionAssemblageSequence = Get.identifierService().getConceptSequenceForUuids(
                IsaacMetadataAuxiliaryBinding.DESCRIPTION_ASSEMBLAGE.getUuids());
    }
    
    
    private final ArrayList<ConceptProxy> preferredInDialectAssemblages = new ArrayList<>();
    private final ArrayList<ConceptProxy> acceptableInDialectAssemblages = new ArrayList<>();
    
    private final String descriptionText;
    private final ConceptProxy descriptionType;
    private final ConceptProxy languageForDescription;
    private final ConceptBuilder conceptBuilder;
    private int conceptSequence = Integer.MAX_VALUE;

    public DescriptionBuilderOchreImpl(String descriptionText, 
            int conceptSequence,
            ConceptProxy descriptionType, 
            ConceptProxy languageForDescription) {
        this.descriptionText = descriptionText;
        this.conceptSequence = conceptSequence;
        this.descriptionType = descriptionType;
        this.languageForDescription = languageForDescription;
        this.conceptBuilder = null;
    }
    public DescriptionBuilderOchreImpl(String descriptionText, 
            ConceptBuilder conceptBuilder,
            ConceptProxy descriptionType, 
            ConceptProxy languageForDescription) {
        this.descriptionText = descriptionText;
        this.descriptionType = descriptionType;
        this.languageForDescription = languageForDescription;
        this.conceptBuilder = conceptBuilder;
    }

    @Override
    public DescriptionBuilder setPreferredInDialectAssemblage(ConceptProxy dialectAssemblage) {
        preferredInDialectAssemblages.add(dialectAssemblage);
        return this; 
   }

    @Override
    public DescriptionBuilder setAcceptableInDialectAssemblage(ConceptProxy dialectAssemblage) {
        acceptableInDialectAssemblages.add(dialectAssemblage);
        return this;
    }

    @Override
    public T build(EditCoordinate editCoordinate, ChangeCheckerMode changeCheckerMode,
            List builtObjects) throws IllegalStateException {
        if (conceptSequence == Integer.MAX_VALUE) {
            conceptSequence = Get.identifierService().getConceptSequenceForUuids(conceptBuilder.getUuids());
        }
        SememeBuilderService sememeBuilder = LookupService.getService(SememeBuilderService.class);
        SememeBuilder<? extends SememeChronology<? extends DescriptionSememe>> descBuilder
                = sememeBuilder.getDescriptionSememeBuilder(LanguageCoordinates.caseSignificanceToConceptSequence(false),
                        languageForDescription.getConceptSequence(),
                        descriptionType.getConceptSequence(),
                        descriptionText,
                        Get.identifierService().getConceptNid(conceptSequence),
                        descriptionAssemblageSequence);
        descBuilder.setPrimordialUuid(this.primordialUuid);
        SememeChronologyImpl<DescriptionSememeImpl> newDescription = (SememeChronologyImpl<DescriptionSememeImpl>)
                descBuilder.build(editCoordinate, changeCheckerMode, builtObjects);
        builtObjects.add(newDescription);
        SememeBuilderService sememeBuilderService = LookupService.getService(SememeBuilderService.class);
        preferredInDialectAssemblages.forEach(( assemblageProxy) -> {
            sememeBuilderService.getComponentSememeBuilder(
                    IsaacMetadataAuxiliaryBinding.PREFERRED.getNid(), newDescription.getNid(),
                    Get.identifierService().getConceptSequenceForProxy(assemblageProxy)).
                    build(editCoordinate, changeCheckerMode, builtObjects);
        });
        acceptableInDialectAssemblages.forEach(( assemblageProxy) -> {
            sememeBuilderService.getComponentSememeBuilder(
                    IsaacMetadataAuxiliaryBinding.ACCEPTABLE.getNid(), 
                    newDescription.getNid(),
                    Get.identifierService().getConceptSequenceForProxy(assemblageProxy)).
                    build(editCoordinate, changeCheckerMode, builtObjects);
        });
        return (T) newDescription;
    }

    @Override
    public T build(int stampSequence, List builtObjects) throws IllegalStateException {
        if (conceptSequence == Integer.MAX_VALUE) {
            conceptSequence = Get.identifierService().getConceptSequenceForUuids(conceptBuilder.getUuids());
        }
        SememeBuilderService sememeBuilder = LookupService.getService(SememeBuilderService.class);
        SememeBuilder<? extends SememeChronology<? extends DescriptionSememe>> descBuilder
                = sememeBuilder.getDescriptionSememeBuilder(LanguageCoordinates.caseSignificanceToConceptSequence(false),
                        languageForDescription.getConceptSequence(),
                        descriptionType.getConceptSequence(),
                        descriptionText,
                        Get.identifierService().getConceptNid(conceptSequence),
                        descriptionAssemblageSequence);
        SememeChronologyImpl<DescriptionSememeImpl> newDescription = (SememeChronologyImpl<DescriptionSememeImpl>)
                descBuilder.build(stampSequence, builtObjects);
        Get.identifierService().setConceptSequenceForComponentNid(conceptSequence, newDescription.getNid());
        builtObjects.add(newDescription);
        SememeBuilderService sememeBuilderService = LookupService.getService(SememeBuilderService.class);
        preferredInDialectAssemblages.forEach(( assemblageProxy) -> {
            sememeBuilderService.getComponentSememeBuilder(
                    IsaacMetadataAuxiliaryBinding.PREFERRED.getNid(), this,
                    Get.identifierService().getConceptSequenceForProxy(assemblageProxy)).
                    build(stampSequence, builtObjects);
        });
        acceptableInDialectAssemblages.forEach(( assemblageProxy) -> {
            sememeBuilderService.getComponentSememeBuilder(
                    IsaacMetadataAuxiliaryBinding.ACCEPTABLE.getNid(), this,
                    Get.identifierService().getConceptSequenceForProxy(assemblageProxy)).
                    build(stampSequence, builtObjects);
        });
        return (T) newDescription;
    }
    
}
