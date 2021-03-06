/**
 * Copyright Notice
 *
 * This is a work of the U.S. Government and is not subject to copyright
 * protection in the United States. Foreign copyrights may apply.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.cradle.integration.tests;

import gov.vha.isaac.metadata.coordinates.ViewCoordinates;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeNid;
import gov.vha.isaac.ochre.model.sememe.dataTypes.DynamicSememeUUID;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.UUID;
import org.ihtsdo.otf.tcc.api.concept.ConceptVersionBI;
import org.ihtsdo.otf.tcc.api.contradiction.ContradictionException;
import org.ihtsdo.otf.tcc.api.store.Ts;
import org.jvnet.testing.hk2testng.HK2;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * {@link RefexDynamicValidatorLiveDataTests}
 *
 * @author <a href="mailto:daniel.armbrust.list@gmail.com">Dan Armbrust</a>
 */
//@HK2("cradle")
////TODO dan hacking fix these tests
//public class RefexDynamicValidatorLiveDataTests
//{
//
//	public RefexDynamicValidatorLiveDataTests()
//	{
//	}
//
//	@BeforeClass
//	public static void setUpClass()
//	{
//	}
//
//	@AfterClass
//	public static void tearDownClass()
//	{
//	}
//
//	@Test (groups = {"db"})
//	public void isChildOf() throws IOException, ContradictionException, PropertyVetoException
//	{
//		ConceptVersionBI centrifugalForceVersion = Ts.get().getConceptVersion(ViewCoordinates.getDevelopmentInferredLatest(),
//				UUID.fromString("2b684fe1-8baf-34ef-9d2a-df03142c915a"));
//
//		ConceptVersionBI motionVersion = Ts.get().getConceptVersion(ViewCoordinates.getDevelopmentInferredLatest(),
//				UUID.fromString("45a8fde8-535d-3d2a-b76b-95ab67718b41"));
//		
//		ConceptVersionBI accelerationVersion = Ts.get().getConceptVersion(ViewCoordinates.getDevelopmentInferredLatest(),
//				UUID.fromString("6ef49616-e2c7-3557-b7f1-456a2c5a5e54"));
//
//		assertFalse(RefexDynamicValidatorType.IS_CHILD_OF.passesValidator(new RefexDynamicNid(motionVersion.getNid()), new RefexDynamicNid(
//				accelerationVersion.getNid()), ViewCoordinates.getDevelopmentInferredLatest()));
//		
//		assertFalse(RefexDynamicValidatorType.IS_CHILD_OF.passesValidator(new RefexDynamicNid(centrifugalForceVersion.getNid()), new RefexDynamicNid(
//				motionVersion.getNid()), ViewCoordinates.getDevelopmentInferredLatest()));
//		
//		assertTrue(RefexDynamicValidatorType.IS_CHILD_OF.passesValidator(new RefexDynamicNid(accelerationVersion.getNid()), new RefexDynamicNid(
//				motionVersion.getNid()), ViewCoordinates.getDevelopmentInferredLatest()));
//		
//		assertTrue(RefexDynamicValidatorType.IS_CHILD_OF.passesValidator(new RefexDynamicUUID(accelerationVersion.getUUIDs().get(0)),
//				new RefexDynamicUUID(motionVersion.getUUIDs().get(0)), ViewCoordinates.getDevelopmentInferredLatest()));
//	}
//	
//	@Test (groups = {"db"})
//	public void isKindOf() throws IOException, ContradictionException, PropertyVetoException
//	{
//		ConceptVersionBI centrifugalForceVersion = Ts.get().getConceptVersion(ViewCoordinates.getDevelopmentInferredLatest(),
//				UUID.fromString("2b684fe1-8baf-34ef-9d2a-df03142c915a"));
//
//		ConceptVersionBI motionVersion = Ts.get().getConceptVersion(ViewCoordinates.getDevelopmentInferredLatest(),
//				UUID.fromString("45a8fde8-535d-3d2a-b76b-95ab67718b41"));
//		
//		ConceptVersionBI accelerationVersion = Ts.get().getConceptVersion(ViewCoordinates.getDevelopmentInferredLatest(),
//				UUID.fromString("6ef49616-e2c7-3557-b7f1-456a2c5a5e54"));
//
//		assertFalse(RefexDynamicValidatorType.IS_KIND_OF.passesValidator(new RefexDynamicNid(motionVersion.getNid()), new RefexDynamicNid(
//				accelerationVersion.getNid()), ViewCoordinates.getDevelopmentInferredLatest()));
//		
//		assertTrue(RefexDynamicValidatorType.IS_KIND_OF.passesValidator(new RefexDynamicNid(centrifugalForceVersion.getNid()), new RefexDynamicNid(
//				motionVersion.getNid()), ViewCoordinates.getDevelopmentInferredLatest()));
//		
//		assertTrue(RefexDynamicValidatorType.IS_KIND_OF.passesValidator(new RefexDynamicNid(accelerationVersion.getNid()), new RefexDynamicNid(
//				motionVersion.getNid()), ViewCoordinates.getDevelopmentInferredLatest()));
//		
//		assertTrue(RefexDynamicValidatorType.IS_KIND_OF.passesValidator(new RefexDynamicUUID(accelerationVersion.getUUIDs().get(0)),
//				new RefexDynamicUUID(motionVersion.getUUIDs().get(0)), ViewCoordinates.getDevelopmentInferredLatest()));
//	}
//}