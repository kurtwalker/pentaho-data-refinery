package org.pentaho.di.trans.steps.annotation;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Created by kwalker on 12/28/16.
 */

@Step( id = "AutoAnnotate", image = "ModelAnnotation.svg",
  i18nPackageName = "org.pentaho.di.trans.steps.annotation", name = "ModelAnnotation.AutoName",
  description = "ModelAnnotation.AutoDescription",
  documentationUrl = "0N0/060/0B0/020/0B0",
  categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Flow" )
public class AutoAnnotateMeta extends BaseStepMeta implements StepMetaInterface {
  @Override public void setDefault() {

  }

  @Override
  public StepInterface getStep( final StepMeta stepMeta, final StepDataInterface stepDataInterface, final int copyNr,
                                final TransMeta transMeta,
                                final Trans trans ) {
    return new AutoAnnotateStep( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override public StepDataInterface getStepData() {
    return new AutoAnnotateData();
  }
}
