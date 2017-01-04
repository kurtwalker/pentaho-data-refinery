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

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.List;

@Step( id = "GuessAnnotations", image = "ModelAnnotation.svg",
  i18nPackageName = "org.pentaho.di.trans.steps.annotation", name = "ModelAnnotation.GuessName",
  description = "ModelAnnotation.GuessDescription",
  documentationUrl = "0N0/060/0B0/020/0B0",
  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Flow" )
public class GuessAnnotationsMeta extends BaseStepMeta implements StepMetaInterface {
  private int sampleSize = 0;

  @Override public void setDefault() {
  }

  @Override public void loadXML( final Node stepnode, final List<DatabaseMeta> databases, final IMetaStore metaStore )
    throws KettleXMLException {
    try {
      this.sampleSize = Integer.parseInt( XMLHandler.getTagValue( stepnode, "sampleSize" ) );
    } catch ( NumberFormatException e ) {
      e.printStackTrace();
    }
  }

  @Override public String getXML() throws KettleException {
    final StringBuilder xml = new StringBuilder();

    xml.append( "    " ).append( XMLHandler.addTagValue( "sampleSize", getSampleSize() ) );

    return xml.toString();
  }

  @Override
  public StepInterface getStep( final StepMeta stepMeta, final StepDataInterface stepDataInterface, final int copyNr,
                                final TransMeta transMeta,
                                final Trans trans ) {
    return new GuessAnnotationsStep( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override public StepDataInterface getStepData() {
    return new GuessAnnotationsData();
  }

  public void setSampleSize( final int sampleSize ) {
    this.sampleSize = sampleSize;
  }

  public int getSampleSize() {
    return sampleSize;
  }
}
