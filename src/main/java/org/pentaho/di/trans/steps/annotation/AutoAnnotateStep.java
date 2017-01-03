package org.pentaho.di.trans.steps.annotation;

import org.pentaho.agilebi.modeler.models.annotations.CreateAttribute;
import org.pentaho.agilebi.modeler.models.annotations.CreateMeasure;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotation;
import org.pentaho.agilebi.modeler.models.annotations.ModelAnnotationGroup;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.entries.build.JobEntryBuildModel;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metadata.model.concept.types.AggregationType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static org.pentaho.metadata.automodel.PhysicalTableImporter.beautifyName;

/**
 * Created by kwalker on 12/28/16.
 */
public class AutoAnnotateStep extends BaseStep implements StepInterface {

  public AutoAnnotateStep( final StepMeta stepMeta,
                           final StepDataInterface stepDataInterface, final int copyNr,
                           final TransMeta transMeta, final Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override public boolean processRow( final StepMetaInterface smi, final StepDataInterface sdi )
    throws KettleException {
    AutoAnnotateData autoAnnotateData = (AutoAnnotateData) sdi;
    Object[] row = getRow();
    if ( row == null ) {
      setOutputDone();
      guessAnnotations( autoAnnotateData );
      return false;
    }
    RowMetaInterface inputRowMeta = getInputRowMeta();
    List<ValueMetaInterface> valueMetaList = inputRowMeta.getValueMetaList();
    autoAnnotateData.rowCount++;
    Map<ValueMetaInterface, Set<Long>> valueMap = autoAnnotateData.valueMap;
    for ( int i = 0; i < valueMetaList.size(); i++ ) {
      if ( valueMetaList.get( i ).isInteger() ) {
        if ( !valueMap.containsKey( valueMetaList.get( i ) ) ) {
          valueMap.put( valueMetaList.get( i ), new HashSet<>() );
        }
        valueMap.get( valueMetaList.get( i ) ).add( inputRowMeta.getInteger( row, i ) );
      }
    }
    putRow( inputRowMeta, row );
    return true;
  }

  private void guessAnnotations( final AutoAnnotateData autoAnnotateData ) {
    ModelAnnotationGroup modelAnnotations = new ModelAnnotationGroup();
    Set<ValueMetaInterface> valueMetas = autoAnnotateData.valueMap.keySet();
    for ( ValueMetaInterface valueMeta : valueMetas ) {
      if ( valueMeta.isInteger() ) {
        if ( shouldBeAttribute( autoAnnotateData, valueMeta ) ) {
          CreateAttribute createAttribute = new CreateAttribute();
          createAttribute.setField( valueMeta.getName() );
          createAttribute.setName( beautifyName( valueMeta.getName() ) );
          createAttribute.setDimension( valueMeta.getName() );
          createAttribute.setHierarchy( valueMeta.getName() );
          createAttribute.setLevel( valueMeta.getName() );
          modelAnnotations.add( new ModelAnnotation<>( createAttribute ) );
        } else {
          CreateMeasure createMeasure = new CreateMeasure();
          createMeasure.setAggregateType( AggregationType.SUM );
          createMeasure.setName( beautifyName( valueMeta.getName() ) );
          createMeasure.setField( valueMeta.getName() );
          modelAnnotations.add( new ModelAnnotation<>( createMeasure ) );
        }
        if ( isUniqueId( autoAnnotateData, valueMeta ) ) {
          CreateMeasure createMeasure = new CreateMeasure();
          createMeasure.setAggregateType( AggregationType.COUNT );
          createMeasure.setName( beautifyName( valueMeta.getName() ) + " Count" );
          createMeasure.setField( valueMeta.getName() );
          modelAnnotations.add( new ModelAnnotation<>( createMeasure ) );
        }
      }
    }
    if ( getTrans().getParentJob() != null ) {
      getTrans().getParentJob().getExtensionDataMap().put( JobEntryBuildModel.KEY_MODEL_ANNOTATIONS, modelAnnotations );
    } else {
      getTrans().getExtensionDataMap().put( JobEntryBuildModel.KEY_MODEL_ANNOTATIONS, modelAnnotations );
    }
  }

  private boolean shouldBeAttribute( final AutoAnnotateData autoAnnotateData, final ValueMetaInterface valueMeta ) {
    return isUniqueId( autoAnnotateData, valueMeta ) || isNumericCode( autoAnnotateData, valueMeta );
  }

  private boolean isNumericCode( final AutoAnnotateData autoAnnotateData, final ValueMetaInterface valueMeta ) {
    return
      allValuesSameLength( autoAnnotateData, valueMeta )
      || ( smallNumberUniqueValues( autoAnnotateData, valueMeta ) && valuesContiguous( autoAnnotateData, valueMeta ) );
  }

  private boolean valuesContiguous( final AutoAnnotateData autoAnnotateData, final ValueMetaInterface valueMeta ) {
    return valueMeta.isInteger() && autoAnnotateData.valueMap.get( valueMeta ).stream().distinct().allMatch(
      new Predicate<Long>() {
        Long last = 0L;
        @Override public boolean test( final Long aLong ) {
          last++;
          return aLong.equals( last );
        }
      } );
  }

  private boolean smallNumberUniqueValues( final AutoAnnotateData autoAnnotateData,
                                           final ValueMetaInterface valueMeta ) {
    return
      autoAnnotateData.valueMap.get( valueMeta ).stream().distinct().count() < autoAnnotateData.rowCount - ( .99 * autoAnnotateData.rowCount );
  }

  private boolean allValuesSameLength( final AutoAnnotateData autoAnnotateData, final ValueMetaInterface valueMeta ) {
    return
      autoAnnotateData.valueMap.get( valueMeta ).stream().map( n -> n.toString().length() ).distinct().count() == 1;
  }

  private boolean isUniqueId( final AutoAnnotateData autoAnnotateData, final ValueMetaInterface valueMeta ) {
    return autoAnnotateData.rowCount == autoAnnotateData.valueMap.get( valueMeta ).size();
  }
}
