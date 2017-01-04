package org.pentaho.di.ui.trans.steps.annotation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.annotation.GuessAnnotationsMeta;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import static org.pentaho.di.core.refinery.UIBuilder.SHELL_MARGIN_HEIGHT;
import static org.pentaho.di.core.refinery.UIBuilder.SHELL_MARGIN_WIDTH;
import static org.pentaho.di.core.refinery.UIBuilder.SHELL_MIN_WIDTH;

public class GuessAnnotationsDialog extends BaseStepDialog implements StepDialogInterface {
  protected static Class<?> PKG = GuessAnnotationsMeta.class; // for i18n purposes, needed by Translator2!!

  private final GuessAnnotationsMeta input;
  protected Label wlSampleSize;
  protected Text wSampleSize;

  public GuessAnnotationsDialog( Shell parent, Object baseStepMeta,
                                 TransMeta transMeta, String stepname ) {
    super( parent, (StepMetaInterface) baseStepMeta, transMeta, stepname );
    input = (GuessAnnotationsMeta) baseStepMeta;
  }
  @Override public String open() {
    configureShell();
    wlSampleSize = new Label( shell, SWT.NONE );
    wlSampleSize.setText( "Sample Size:" );
    FormData fdSampleLabel = new FormData();
    fdSampleLabel.left = new FormAttachment( shell, 0 );
    wlSampleSize.setLayoutData( fdSampleLabel );
    wSampleSize = new Text( shell, SWT.SINGLE | SWT.BORDER );
    wSampleSize.setText( Integer.toString( input.getSampleSize() ) );
    FormData fdSampleSize = new FormData();
    fdSampleSize.left = new FormAttachment( wlSampleSize, 10 );
    fdSampleSize.width = 100;
    wSampleSize.setLayoutData( fdSampleSize );
    props.setLook( wlSampleSize );
    props.setLook( wSampleSize );
    createOkCancelButtons();

    // Open the dialog
    shell.open();

    // Wait for close
    while ( !shell.isDisposed() ) {
      if ( !getParent().getDisplay().readAndDispatch() ) {
        getParent().getDisplay().sleep();
      }
    }
    return stepname;
  }

  protected void configureShell() {
    shell = new Shell( getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    FormLayout shellLayout = new FormLayout();
    shellLayout.marginWidth = Const.FORM_MARGIN;
    shellLayout.marginHeight = Const.FORM_MARGIN;
    shell.setLayout( shellLayout );
    props.setLook( shell );
    setShellImage( shell, input );
    shell.setText( "Auto Annotate" );

    // set min size of dialog
    shell.setMinimumSize( SHELL_MIN_WIDTH, SHELL_MIN_WIDTH );
    shell.setSize( SHELL_MIN_WIDTH, SHELL_MIN_WIDTH );
    props.setLook( shell );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = SHELL_MARGIN_WIDTH;
    formLayout.marginHeight = SHELL_MARGIN_HEIGHT;
    formLayout.marginBottom = 5; // Adjust
    shell.setLayout( formLayout );
  }

  protected void createOkCancelButtons() {
    // Cancel Button
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) + "  " ); // add spaces to push left
    FormData fdCancel = new FormData();
    fdCancel.bottom = new FormAttachment( shell, 300 );
    fdCancel.right = new FormAttachment( shell, 300 );
    wCancel.setLayoutData( fdCancel );
    props.setLook( wCancel );

    // OK Button
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    FormData fdOK = new FormData();
    fdOK.top = new FormAttachment( wCancel, 0, SWT.TOP );
    fdOK.right = new FormAttachment( wCancel, -10, SWT.LEFT );
    wOK.setLayoutData( fdOK );
    props.setLook( wOK );

    lsOK = e -> {
      input.setSampleSize( Integer.parseInt( wSampleSize.getText() ) );
      dispose();
    };

    lsCancel = e -> dispose();
    // Add listeners
    wOK.addListener( SWT.Selection, lsOK );
    wCancel.addListener( SWT.Selection, lsCancel );
  }
}
