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
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.pentaho.metadata.automodel.PhysicalTableImporter.beautifyName;

public class GuessAnnotationsStep extends BaseStep implements StepInterface {

  public GuessAnnotationsStep( final StepMeta stepMeta,
                               final StepDataInterface stepDataInterface, final int copyNr,
                               final TransMeta transMeta, final Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override public boolean processRow( final StepMetaInterface smi, final StepDataInterface sdi )
    throws KettleException {
    GuessAnnotationsData guessAnnotationsData = (GuessAnnotationsData) sdi;
    Object[] row = getRow();
    if ( row == null ) {
      setOutputDone();
      guessAnnotations( guessAnnotationsData );
      return false;
    }
    RowMetaInterface inputRowMeta = recordRow( (GuessAnnotationsMeta) smi, guessAnnotationsData, row );
    putRow( inputRowMeta, row );
    return true;
  }

  private RowMetaInterface recordRow( final GuessAnnotationsMeta smi, final GuessAnnotationsData guessAnnotationsData,
                                      final Object[] row ) {
    RowMetaInterface inputRowMeta = getInputRowMeta();
    int sampleSize = smi.getSampleSize();
    if ( sampleSize == 0 || guessAnnotationsData.rowCount <= sampleSize ) {
      List<ValueMetaInterface> valueMetaList = inputRowMeta.getValueMetaList();
      guessAnnotationsData.rowCount++;
      Map<ValueMetaInterface, Set<Object>> valueMap = guessAnnotationsData.valueMap;
      for ( int i = 0; i < valueMetaList.size(); i++ ) {
        if ( row[i] != null ) {
          if ( !valueMap.containsKey( valueMetaList.get( i ) ) ) {
            valueMap.put( valueMetaList.get( i ), new HashSet<>() );
          }
          valueMap.get( valueMetaList.get( i ) ).add( row[i] );
        }
      }
    }
    return inputRowMeta;
  }

  private void addAnnotations( final ModelAnnotationGroup modelAnnotations ) {
    if ( getTrans().getParentJob() != null ) {
      addAnnotations( getTrans().getParentJob().getExtensionDataMap(), modelAnnotations );
    } else {
      addAnnotations( getTrans().getExtensionDataMap(), modelAnnotations );
    }
  }

  private void guessAnnotations( final GuessAnnotationsData guessAnnotationsData ) {
    ModelAnnotationGroup modelAnnotations = new ModelAnnotationGroup();
    guessDateHierarchy( guessAnnotationsData, modelAnnotations );
    guessIntegers( guessAnnotationsData, modelAnnotations );
    addAnnotations( modelAnnotations );
  }

  private void guessDateHierarchy(
    final GuessAnnotationsData guessAnnotationsData, final ModelAnnotationGroup modelAnnotations ) {
    Optional<ValueMetaInterface> possibleYear = guessAnnotationsData.valueMap.keySet().stream()
      .filter( valueMeta -> guessAnnotationsData.valueMap.get( valueMeta ).stream()
        .allMatch( o -> o.toString().length() == 4
          && ( o.toString().startsWith( "19" ) || o.toString().startsWith( "20" ) ) ) )
      .findFirst();
    if ( possibleYear.isPresent() ) {
      Optional<ValueMetaInterface> possibleQuarter = guessAnnotationsData.valueMap.keySet().stream()
        .filter( valueMeta -> ( guessAnnotationsData.valueMap.get( valueMeta ).stream().anyMatch(
          o -> o.toString().contains( "1" ) || o.toString().toLowerCase().contains( "one" ) ) )
          && guessAnnotationsData.valueMap.get( valueMeta ).stream().distinct().count() == 4 )
        .findFirst();
      if ( possibleQuarter.isPresent() ) {
        Optional<ValueMetaInterface> possibleMonth = guessAnnotationsData.valueMap.keySet().stream()
          .filter( valueMeta -> guessAnnotationsData.valueMap.get( valueMeta ).stream().distinct().count() == 12 )
          .findFirst();
        if ( possibleMonth.isPresent() ) {
          CreateAttribute year = new CreateAttribute();
          year.setField( possibleYear.get().getName() );
          year.setName( beautifyName( possibleYear.get().getName() ) );
          year.setDimension( "Date" );
          year.setHierarchy( "Date" );
          year.setTimeType( ModelAnnotation.TimeType.TimeYears );

          CreateAttribute quarter = new CreateAttribute();
          quarter.setField( possibleQuarter.get().getName() );
          quarter.setName( beautifyName( possibleQuarter.get().getName() ) );
          quarter.setDimension( "Date" );
          quarter.setHierarchy( "Date" );
          quarter.setTimeType( ModelAnnotation.TimeType.TimeQuarters );
          quarter.setParentAttribute( year.getName() );

          CreateAttribute month = new CreateAttribute();
          month.setField( possibleMonth.get().getName() );
          month.setName( beautifyName( possibleMonth.get().getName() ) );
          month.setDimension( "Date" );
          month.setHierarchy( "Date" );
          month.setTimeType( ModelAnnotation.TimeType.TimeMonths );
          month.setParentAttribute( quarter.getName() );

          modelAnnotations.add( new ModelAnnotation<>( year ) );
          modelAnnotations.add( new ModelAnnotation<>( quarter ) );
          modelAnnotations.add( new ModelAnnotation<>( month ) );
          guessAnnotationsData.valueMap.remove( possibleYear.get() );
          guessAnnotationsData.valueMap.remove( possibleQuarter.get() );
          guessAnnotationsData.valueMap.remove( possibleMonth.get() );
        }
      }
    }

  }

  private void guessIntegers( final GuessAnnotationsData guessAnnotationsData, final ModelAnnotationGroup modelAnnotations ) {
    guessAnnotationsData.valueMap.keySet().stream().filter( ValueMetaInterface::isNumeric ).forEach(
      valueMeta -> {
        if ( isUniqueId( guessAnnotationsData, valueMeta ) ) {
          CreateMeasure createMeasure = new CreateMeasure();
          createMeasure.setAggregateType( AggregationType.COUNT );
          createMeasure.setName( beautifyName( valueMeta.getName() ) + " Count" );
          createMeasure.setField( valueMeta.getName() );
          modelAnnotations.add( new ModelAnnotation<>( createMeasure ) );
        }
        if ( shouldBeAttribute( guessAnnotationsData, valueMeta ) ) {
          CreateAttribute createAttribute = new CreateAttribute();
          createAttribute.setField( valueMeta.getName() );
          createAttribute.setName( beautifyName( valueMeta.getName() ) );
          createAttribute.setDimension( beautifyName( valueMeta.getName() ) );
          createAttribute.setHierarchy( beautifyName( valueMeta.getName() ) );
          createAttribute.setLevel( valueMeta.getName() );
          modelAnnotations.add( new ModelAnnotation<>( createAttribute ) );
        } else {
          CreateMeasure createMeasure = new CreateMeasure();
          createMeasure.setAggregateType( AggregationType.SUM );
          createMeasure.setName( beautifyName( valueMeta.getName() ) );
          createMeasure.setField( valueMeta.getName() );
          modelAnnotations.add( new ModelAnnotation<>( createMeasure ) );
        }
      }
    );
  }

  private void addAnnotations( final Map<String, Object> map, final ModelAnnotationGroup modelAnnotations ) {
    if ( map.containsKey( JobEntryBuildModel.KEY_MODEL_ANNOTATIONS ) ) {
      ( (ModelAnnotationGroup) map.get( JobEntryBuildModel.KEY_MODEL_ANNOTATIONS ) ).addAll( modelAnnotations );
    } else {
      map.put( JobEntryBuildModel.KEY_MODEL_ANNOTATIONS, modelAnnotations );
    }
  }

  private boolean shouldBeAttribute( final GuessAnnotationsData guessAnnotationsData, final ValueMetaInterface valueMeta ) {
    return isUniqueId( guessAnnotationsData, valueMeta ) || isNumericCode( guessAnnotationsData, valueMeta );
  }

  private boolean isNumericCode( final GuessAnnotationsData guessAnnotationsData, final ValueMetaInterface valueMeta ) {
    return
      allValuesSameLength( guessAnnotationsData, valueMeta )
      || ( smallNumberUniqueValues( guessAnnotationsData, valueMeta ) && valuesContiguous( guessAnnotationsData, valueMeta ) );
  }

  private boolean valuesContiguous( final GuessAnnotationsData guessAnnotationsData, final ValueMetaInterface valueMeta ) {
    return valueMeta.isInteger() && guessAnnotationsData.valueMap.get( valueMeta ).stream().distinct().map( v -> (Long) v )
      .allMatch(
        new Predicate<Long>() {
          Long last = 0L;
          @Override public boolean test( final Long aLong ) {
            last++;
            return aLong.equals( last );
          }
        } );
  }

  private boolean smallNumberUniqueValues( final GuessAnnotationsData guessAnnotationsData,
                                           final ValueMetaInterface valueMeta ) {
    return
      guessAnnotationsData.valueMap.get( valueMeta ).stream().distinct().count() < guessAnnotationsData.rowCount - ( .99 * guessAnnotationsData.rowCount );
  }

  private boolean allValuesSameLength( final GuessAnnotationsData guessAnnotationsData, final ValueMetaInterface valueMeta ) {
    return
      guessAnnotationsData.valueMap.get( valueMeta ).stream().map( n -> n.toString().length() ).distinct().count() == 1;
  }

  private boolean isUniqueId( final GuessAnnotationsData guessAnnotationsData, final ValueMetaInterface valueMeta ) {
    return guessAnnotationsData.rowCount == guessAnnotationsData.valueMap.get( valueMeta ).size();
  }
}
