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

/**
 * Created by kwalker on 1/3/17.
 */
public class AutoAnnotateStepTest {
  @BeforeClass
  public static void setUp() throws Exception {
    KettleClientEnvironment.init();
    PluginRegistry.addPluginType( StepPluginType.getInstance() );
    PluginRegistry.init();
    StepPluginType.getInstance().handlePluginAnnotation( AutoAnnotateMeta.class, AutoAnnotateMeta.class.getAnnotation(
      Step.class ), Collections.emptyList(), false, null );
    if ( !Props.isInitialized() ) {
      Props.init( 0 );
    }
  }

  @Test
  public void testIdentifiesOrderNumberAsAttribute() throws Exception {
    String path = getClass().getResource( "/autoAnnotateTest.ktr" ).getPath();
    TransMeta transMeta = new TransMeta( path );
    Trans trans = new Trans( transMeta );
    trans.execute( new String[]{} );
    trans.waitUntilFinished();
    ModelAnnotationGroup modelAnnotations =
      (ModelAnnotationGroup) trans.getExtensionDataMap().get( JobEntryBuildModel.KEY_MODEL_ANNOTATIONS );
    CreateAttribute rowId = createAttribute( "Rowid" );
    CreateAttribute orderNumber = createAttribute( "Ordernumber" );
    CreateAttribute orderLineNumber = createAttribute( "Orderlinenumber" );
    CreateMeasure quantityordered = createMeasure( "Quantityordered" );
    CreateMeasure rowMeasure = createMeasure( "Rowid Count" );
    assertEquals( 5, modelAnnotations.size() );
    modelAnnotations.sort( Comparator.comparing( o -> o.getAnnotation().getName() ) );
    assertTrue( orderLineNumber.equalsLogically( modelAnnotations.get( 0 ).getAnnotation() ) );
    assertTrue( orderNumber.equalsLogically( modelAnnotations.get( 1 ).getAnnotation() ) );
    assertTrue( quantityordered.equalsLogically( modelAnnotations.get( 2 ).getAnnotation() ) );
    assertTrue( rowId.equalsLogically( modelAnnotations.get( 3 ).getAnnotation() ) );
    assertTrue( rowMeasure.equalsLogically( modelAnnotations.get( 4 ).getAnnotation() ) );
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