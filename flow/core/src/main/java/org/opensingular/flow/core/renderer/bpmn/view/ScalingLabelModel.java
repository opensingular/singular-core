/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
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
package org.opensingular.flow.core.renderer.bpmn.view;

import com.yworks.yfiles.annotations.DefaultValue;
import com.yworks.yfiles.geometry.InsetsD;
import com.yworks.yfiles.geometry.IOrientedRectangle;
import com.yworks.yfiles.geometry.OrientedRectangle;
import com.yworks.yfiles.geometry.RectD;
import com.yworks.yfiles.graph.ILabel;
import com.yworks.yfiles.graph.ILabelOwner;
import com.yworks.yfiles.graph.ILookup;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.labelmodels.ILabelModel;
import com.yworks.yfiles.graph.labelmodels.ILabelModelParameter;
import com.yworks.yfiles.graph.labelmodels.InteriorStretchLabelModel;
import com.yworks.yfiles.graph.SimpleLabel;
import com.yworks.yfiles.graph.SimpleNode;

class ScalingLabelModel implements ILabelModel {
  //region Initialize static fields

  private static final InteriorStretchLabelModel STRETCH_MODEL;

  private static final ILabelModelParameter STRETCH_PARAMETER;

  private static final SimpleNode DUMMY_NODE;

  private static final SimpleLabel DUMMY_LABEL;

  //endregion

  private InsetsD insets = new InsetsD();

  /**
   * Gets the insets to use within the node's {@link INode#getLayout() Layout}.
   * @return The Insets.
   * @see #setInsets(InsetsD)
   */
  @DefaultValue(stringValue = "0", classValue = InsetsD.class)
  public InsetsD getInsets() {
    return this.insets.clone();
  }

  /**
   * Sets the insets to use within the node's {@link INode#getLayout() Layout}.
   * @param value The Insets to set.
   * @see #getInsets()
   */
  @DefaultValue(stringValue = "0", classValue = InsetsD.class)
  public void setInsets( InsetsD value ) {
    this.insets = value.clone();
  }

  public final <TLookup> TLookup lookup( Class<TLookup> type ) {
    return STRETCH_MODEL.lookup(type);
  }

  public final ILookup getContext( ILabel label, ILabelModelParameter parameter ) {
    return STRETCH_MODEL.getContext(label, parameter);
  }

  public final IOrientedRectangle getGeometry( ILabel label, ILabelModelParameter parameter ) {
    ScalingParameter scalingParameter = (ScalingParameter)parameter;
    ILabelOwner owner = label.getOwner();
    if(!(owner instanceof INode)) {
      return OrientedRectangle.EMPTY;
    }
    RectD availableRect = ((INode)owner).getLayout().toRectD();
    double horizontalInsets = getInsets().left + getInsets().right;
    double verticalInsets = getInsets().top + getInsets().bottom;

    // consider fix insets
    double x = availableRect.getMinX() + (availableRect.width > horizontalInsets ? getInsets().left : 0);
    double y = availableRect.getMinY() + (availableRect.height > verticalInsets ? getInsets().top : 0);
    double width = availableRect.width - (availableRect.width > horizontalInsets ? horizontalInsets : 0);
    double height = availableRect.height - (availableRect.height > verticalInsets ? verticalInsets : 0);

    // consider scaling insets
    x += scalingParameter.getScalingInsets().left * width;
    y += scalingParameter.getScalingInsets().top * height;
    width = width * (1 - scalingParameter.getScalingInsets().left - scalingParameter.getScalingInsets().right);
    height = height * (1 - scalingParameter.getScalingInsets().top - scalingParameter.getScalingInsets().bottom);

    if (scalingParameter.isKeepRatio()) {
      double fixRatio = scalingParameter.getRatio();
      double availableRatio = height > 0 && width > 0 ? width / height : 1;

      if (fixRatio > availableRatio) {
        // keep width
        double cy = y + height / 2;
        height *= availableRatio / fixRatio;
        y = cy - height / 2;
      } else {
        double cx = x + width / 2;
        width *= fixRatio / availableRatio;
        x = cx - width / 2;
      }
    }

    DUMMY_NODE.setLayout(new RectD(x, y, width, height));
    DUMMY_LABEL.setPreferredSize(label.getPreferredSize());
    return STRETCH_MODEL.getGeometry(DUMMY_LABEL, STRETCH_PARAMETER);
  }

  //region Create Parameter Methods

  public final ILabelModelParameter createDefaultParameter() {
    ScalingParameter newInstance = new ScalingParameter();
    newInstance.setModel(this);
    newInstance.setScalingInsets(InsetsD.EMPTY);
    return newInstance;
  }

  public final ILabelModelParameter createScaledParameter( double scale ) {
    if (scale <= 0 || scale > 1) {
      throw new IllegalArgumentException("Argument '" + scale + "' not allowed. Valid values are in ]0; 1].");
    }
    ScalingParameter newInstance = new ScalingParameter();
    newInstance.setModel(this);
    newInstance.setScalingInsets(new InsetsD((1 - scale) / 2));
    return newInstance;
  }

  public final ILabelModelParameter createScaledParameter( double leftScale, double topScale, double rightScale, double bottomScale ) {
    if (leftScale < 0 || rightScale < 0 || topScale < 0 || bottomScale < 0) {
      throw new IllegalArgumentException("Negative Arguments are not allowed.");
    }
    if (leftScale + rightScale >= 1 || topScale + bottomScale >= 1) {
      throw new IllegalArgumentException("Arguments not allowed. The sum of left and right scale respectively top and bottom scale must be below 1.");
    }
    ScalingParameter newInstance = new ScalingParameter();
    newInstance.setModel(this);
    newInstance.setScalingInsets(InsetsD.fromLTRB(leftScale, topScale, rightScale, bottomScale));
    return newInstance;
  }

  public final ILabelModelParameter createScaledParameterWithRatio( double scale, double ratio ) {
    if (scale <= 0 || scale > 1) {
      throw new IllegalArgumentException("Argument '" + scale + "' not allowed. Valid values are in ]0; 1].");
    }
    if (ratio <= 0) {
      throw new IllegalArgumentException("Argument '" + ratio + "' not allowed. Ratio must be positive.");
    }
    ScalingParameter newInstance = new ScalingParameter();
    newInstance.setModel(this);
    newInstance.setScalingInsets(new InsetsD((1 - scale) / 2));
    newInstance.setKeepRatio(true);
    newInstance.setRatio(ratio);
    return newInstance;
  }

  public final ILabelModelParameter createScaledParameterWithRatio( double leftScale, double topScale, double rightScale, double bottomScale, double ratio ) {
    if (leftScale < 0 || rightScale < 0 || topScale < 0 || bottomScale < 0) {
      throw new IllegalArgumentException("Negative Arguments are not allowed.");
    }
    if (leftScale + rightScale >= 1 || topScale + bottomScale >= 1) {
      throw new IllegalArgumentException("Arguments not allowed. The sum of left and right scale respectively top and bottom scale must be below 1.");
    }
    if (ratio <= 0) {
      throw new IllegalArgumentException("Argument '" + ratio + "' not allowed. Ratio must be positive.");
    }
    ScalingParameter newInstance = new ScalingParameter();
    newInstance.setModel(this);
    newInstance.setScalingInsets(InsetsD.fromLTRB(leftScale, topScale, rightScale, bottomScale));
    newInstance.setKeepRatio(true);
    newInstance.setRatio(ratio);
    return newInstance;
  }

  //endregion

  //region ScalingParameter

  private static class ScalingParameter implements ILabelModelParameter {
    private ILabelModel model;

    public final ILabelModel getModel() {
      return this.model;
    }

    public final void setModel( ILabelModel value ) {
      this.model = value;
    }

    private InsetsD scalingInsets = new InsetsD();

    public final InsetsD getScalingInsets() {
      return this.scalingInsets.clone();
    }

    public final void setScalingInsets( InsetsD value ) {
      this.scalingInsets = value.clone();
    }

    private boolean keepRatio;

    public final boolean isKeepRatio() {
      return this.keepRatio;
    }

    public final void setKeepRatio( boolean value ) {
      this.keepRatio = value;
    }

    private double ratio;

    public final double getRatio() {
      return this.ratio;
    }

    public final void setRatio( double value ) {
      this.ratio = value;
    }

    public final ScalingParameter clone() {
      ScalingParameter newInstance = new ScalingParameter();
      {
        newInstance.setModel(getModel());
        newInstance.setScalingInsets(getScalingInsets());
        newInstance.setKeepRatio(isKeepRatio());
      }
      return newInstance;
    }

    public final boolean supports( ILabel label ) {
      return label.getOwner() instanceof INode;
    }
  }

  //endregion
  static {
    STRETCH_MODEL = new InteriorStretchLabelModel();
    STRETCH_PARAMETER = STRETCH_MODEL.createParameter(InteriorStretchLabelModel.Position.CENTER);
    DUMMY_NODE = new SimpleNode();
    DUMMY_LABEL = new SimpleLabel(DUMMY_NODE, "", STRETCH_PARAMETER);
  }

}
