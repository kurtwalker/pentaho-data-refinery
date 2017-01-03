package org.pentaho.di.trans.steps.annotation;

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by kwalker on 12/28/16.
 */
public class AutoAnnotateData extends BaseStepData implements StepDataInterface {
  public final Map<ValueMetaInterface, Set<Long>> valueMap = new HashMap<>();
  public int rowCount = 0;
}
