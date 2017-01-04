/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pentaho.di.trans.steps.annotation;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.models.annotations.CreateAttribute;
import org.pentaho.agilebi.modeler.models.annotations.CreateMeasure;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotation;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationGroup;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.job.entries.build.JobEntryBuildModel;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metadata.model.concept.types.AggregationType;

import java.util.Collections;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GuessAnnotationsStepTest {
  @BeforeClass
  public static void setUp() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    StepPluginType.getInstance().handlePluginAnnotation( GuessAnnotationsMeta.class, GuessAnnotationsMeta.class.getAnnotation(
      Step.class ), Collections.emptyList(), false, null );
    StepPluginType.getInstance().handlePluginAnnotation( ModelAnnotationMeta.class, ModelAnnotationMeta.class.getAnnotation(
      Step.class ), Collections.emptyList(), false, null );
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
  }

  @Test
  public void testIdentifiesOrderNumberAsAttributeKeepsUserAnnotations() throws Exception {
    String path = getClass().getResource( "/guessAnnotationsTest.ktr" ).getPath();
    TransMeta transMeta = new TransMeta( path );
    Trans trans = new Trans( transMeta );
    trans.execute( new String[]{} );
    trans.waitUntilFinished();
    ModelAnnotationGroup modelAnnotations =
      (ModelAnnotationGroup) trans.getExtensionDataMap().get( JobEntryBuildModel.KEY_MODEL_ANNOTATIONS );
    CreateAttribute productLine = new CreateAttribute();
    productLine.setField( "PRODUCT_LINE" );
    productLine.setName( "Product Line" );
    productLine.setDimension( "Product" );
    productLine.setHierarchy( "Product" );
    CreateAttribute productCode = new CreateAttribute();
    productCode.setField( "PRODUCT_CODE" );
    productCode.setName( "Product Code" );
    productCode.setDimension( "Product" );
    productCode.setHierarchy( "Product" );
    productCode.setParentAttribute( "Product Line" );
    CreateAttribute rowId = createAttribute( "Rowid" );
    CreateAttribute orderNumber = createAttribute( "Ordernumber" );
    CreateAttribute orderLineNumber = createAttribute( "Orderlinenumber" );
    CreateMeasure quantityordered = createMeasure( "Quantityordered" );
    CreateMeasure rowMeasure = createMeasure( "Rowid Count" );
    CreateMeasure sales = createMeasure( "Sales" );
    assertEquals( 8, modelAnnotations.size() );
    modelAnnotations.sort( Comparator.comparing( o -> o.getAnnotation().getName() ) );
    assertTrue( orderLineNumber.equalsLogically( modelAnnotations.get( 0 ).getAnnotation() ) );
    assertTrue( orderNumber.equalsLogically( modelAnnotations.get( 1 ).getAnnotation() ) );
    assertTrue( productCode.equalsLogically( modelAnnotations.get( 2 ).getAnnotation() ) );
    assertTrue( productLine.equalsLogically( modelAnnotations.get( 3 ).getAnnotation() ) );
    assertTrue( quantityordered.equalsLogically( modelAnnotations.get( 4 ).getAnnotation() ) );
    assertTrue( rowId.equalsLogically( modelAnnotations.get( 5 ).getAnnotation() ) );
    assertTrue( rowMeasure.equalsLogically( modelAnnotations.get( 6 ).getAnnotation() ) );
    assertTrue( sales.equalsLogically( modelAnnotations.get( 7 ).getAnnotation() ) );
  }

  @Test
  public void testLimitsToSampleSize() throws Exception {
    String path = getClass().getResource( "/guessAnnotationsSampleTest.ktr" ).getPath();
    TransMeta transMeta = new TransMeta( path );
    Trans trans = new Trans( transMeta );
    trans.execute( new String[]{} );
    trans.waitUntilFinished();
    ModelAnnotationGroup modelAnnotations =
      (ModelAnnotationGroup) trans.getExtensionDataMap().get( JobEntryBuildModel.KEY_MODEL_ANNOTATIONS );
    CreateAttribute rowId = createAttribute( "Rowid" );
    CreateAttribute orderNumber = createAttribute( "Ordernumber" );
    CreateMeasure orderLineNumber = createMeasure( "Orderlinenumber" );
    CreateMeasure quantityordered = createMeasure( "Quantityordered" );
    CreateMeasure rowMeasure = createMeasure( "Rowid Count" );
    CreateMeasure sales = createMeasure( "Sales" );
    assertEquals( 6, modelAnnotations.size() );
    modelAnnotations.sort( Comparator.comparing( o -> o.getAnnotation().getName() ) );
    assertTrue( orderLineNumber.equalsLogically( modelAnnotations.get( 0 ).getAnnotation() ) );
    assertTrue( orderNumber.equalsLogically( modelAnnotations.get( 1 ).getAnnotation() ) );
    assertTrue( quantityordered.equalsLogically( modelAnnotations.get( 2 ).getAnnotation() ) );
    assertTrue( rowId.equalsLogically( modelAnnotations.get( 3 ).getAnnotation() ) );
    assertTrue( rowMeasure.equalsLogically( modelAnnotations.get( 4 ).getAnnotation() ) );
    assertTrue( sales.equalsLogically( modelAnnotations.get( 5 ).getAnnotation() ) );
  }

  @Test
  public void testGuessesDateHierarchies() throws Exception {
    String path = getClass().getResource( "/guessAnnotationsDateTest.ktr" ).getPath();
    TransMeta transMeta = new TransMeta( path );
    Trans trans = new Trans( transMeta );
    trans.execute( new String[]{} );
    trans.waitUntilFinished();
    ModelAnnotationGroup modelAnnotations =
      (ModelAnnotationGroup) trans.getExtensionDataMap().get( JobEntryBuildModel.KEY_MODEL_ANNOTATIONS );
    CreateMeasure quantityordered = createMeasure( "Quantityordered" );
    CreateAttribute year = dateAttribute( "Year", "Year", ModelAnnotation.TimeType.TimeYears );
    CreateAttribute quarter = dateAttribute( "Quarter", "Quarter", ModelAnnotation.TimeType.TimeQuarters );
    quarter.setParentAttribute( "Year" );
    CreateAttribute month = dateAttribute( "Month", "Month", ModelAnnotation.TimeType.TimeMonths );
    month.setParentAttribute( "Quarter" );
    CreateMeasure sales = createMeasure( "Sales" );
    assertEquals( 5, modelAnnotations.size() );
    modelAnnotations.sort( Comparator.comparing( o -> o.getAnnotation().getName() ) );
    assertTrue( month.equalsLogically( modelAnnotations.get( 0 ).getAnnotation() ) );
    assertTrue( quantityordered.equalsLogically( modelAnnotations.get( 1 ).getAnnotation() ) );
    assertTrue( quarter.equalsLogically( modelAnnotations.get( 2 ).getAnnotation() ) );
    assertTrue( sales.equalsLogically( modelAnnotations.get( 3 ).getAnnotation() ) );
    assertTrue( year.equalsLogically( modelAnnotations.get( 4 ).getAnnotation() ) );
  }

  private CreateAttribute dateAttribute( final String field, final String name, final ModelAnnotation.TimeType timeType ) {
    CreateAttribute createAttribute = new CreateAttribute();
    createAttribute.setField( field );
    createAttribute.setName( name );
    createAttribute.setTimeType( timeType );
    createAttribute.setDimension( "Date" );
    createAttribute.setHierarchy( "Date" );
    return createAttribute;
  }

  private CreateAttribute createAttribute( final String name ) {
    CreateAttribute expected = new CreateAttribute();
    expected.setName( name );
    expected.setDimension( name );
    expected.setHierarchy( name );
    expected.setField( name );
    return expected;
  }

  private CreateMeasure createMeasure( final String name ) {
    CreateMeasure expected = new CreateMeasure();
    expected.setName( name );
    expected.setAggregateType( AggregationType.SUM );
    return expected;
  }
}