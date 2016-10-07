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
import com.yworks.yfiles.annotations.Obfuscation;
import com.yworks.yfiles.geometry.SizeD;
import com.yworks.yfiles.graph.ILabel;
import com.yworks.yfiles.graph.ILabelOwner;
import com.yworks.yfiles.graph.ILookup;
import com.yworks.yfiles.graph.INode;
import com.yworks.yfiles.graph.labelmodels.InteriorLabelModel;
import com.yworks.yfiles.graph.portlocationmodels.FreeNodePortLocationModel;
import com.yworks.yfiles.graph.styles.common.VoidLabelStyle;
import com.yworks.yfiles.graph.styles.ILabelStyle;
import com.yworks.yfiles.graph.styles.ILabelStyleRenderer;
import com.yworks.yfiles.graph.styles.SimpleLabelStyle;
import com.yworks.yfiles.view.IBoundsProvider;
import com.yworks.yfiles.view.input.IHitTestable;
import com.yworks.yfiles.view.input.IMarqueeTestable;
import com.yworks.yfiles.view.IRenderContext;
import com.yworks.yfiles.view.IVisibilityTestable;
import com.yworks.yfiles.view.IVisual;
import com.yworks.yfiles.view.IVisualCreator;
import com.yworks.yfiles.view.VisualGroup;

/**
 * A label style for annotations according to the BPMN.
 */
@Obfuscation(stripAfterObfuscation = false, exclude = true, applyToMembers = false)
public class AnnotationLabelStyle implements ILabelStyle {
  //region Initialize static fields

  private static final AnnotationLabelStyleRenderer RENDERER = new AnnotationLabelStyleRenderer();

  private static final BpmnEdgeStyle CONNECTOR_STYLE;

  private static final SimpleLabelStyle TEXT_STYLE;

  private static final AnnotationNodeStyle LEFT_ANNOTATION_STYLE;

  private static final AnnotationNodeStyle RIGHT_ANNOTATION_STYLE;

  static final BpmnNodeStyle getLeftAnnotationStyle() {
    return LEFT_ANNOTATION_STYLE;
  }

  static final BpmnNodeStyle getRightAnnotationStyle() {
    return RIGHT_ANNOTATION_STYLE;
  }

  //endregion

  //region Properties

  private double insets = 5.0;

  /**
   * Gets the insets around the text.
   * @return The Insets.
   * @see #setInsets(double)
   */
  @Obfuscation(stripAfterObfuscation = false, exclude = true)
  @DefaultValue(doubleValue = 5.0, valueType = DefaultValue.ValueType.DOUBLE_TYPE)
  public final double getInsets() {
    return insets;
  }

  /**
   * Sets the insets around the text.
   * @param value The Insets to set.
   * @see #getInsets()
   */
  @Obfuscation(stripAfterObfuscation = false, exclude = true)
  @DefaultValue(doubleValue = 5.0, valueType = DefaultValue.ValueType.DOUBLE_TYPE)
  public final void setInsets( double value ) {
    insets = value;
  }

  private ConnectedIconLabelStyle delegateStyle;

  final ConnectedIconLabelStyle getDelegateStyle() {
    return delegateStyle;
  }

  //endregion

  /**
   * Creates a new instance.
   */
  public AnnotationLabelStyle() {
    delegateStyle = new ConnectedIconLabelStyle();
    delegateStyle.setIconStyle(getLeftAnnotationStyle());
    delegateStyle.setTextStyle(TEXT_STYLE);
    delegateStyle.setTextPlacement(InteriorLabelModel.CENTER);
    delegateStyle.setConnectorStyle(CONNECTOR_STYLE);
    delegateStyle.setLabelConnectorLocation(FreeNodePortLocationModel.NODE_LEFT_ANCHORED);
    delegateStyle.setNodeConnectorLocation(FreeNodePortLocationModel.NODE_CENTER_ANCHORED);
  }

  @Obfuscation(stripAfterObfuscation = false, exclude = true)
  public final AnnotationLabelStyle clone() {
    return new AnnotationLabelStyle();
  }

  @Obfuscation(stripAfterObfuscation = false, exclude = true)
  public final ILabelStyleRenderer getRenderer() {
    return RENDERER;
  }

  //region Renderer Class

  /**
   * An {@link ILabelStyleRenderer} implementation used by {@link AnnotationLabelStyle}.
   */
  private static class AnnotationLabelStyleRenderer implements ILabelStyleRenderer, IVisualCreator {
    private ILabel label;

    private ILabelStyle labelStyle;

    private boolean left;

    private double insets;

    private ILabelStyle getCurrentStyle( ILabel item, ILabelStyle style ) {
      AnnotationLabelStyle annotationLabelStyle = (style instanceof AnnotationLabelStyle) ? (AnnotationLabelStyle)style : null;
      ILabelOwner owner = item.getOwner();
      INode nodeOwner = (owner instanceof INode) ? (INode)owner : null;
      if (annotationLabelStyle == null || nodeOwner == null) {
        return VoidLabelStyle.INSTANCE;
      }

      left = item.getLayout().getCenter().x > nodeOwner.getLayout().getCenter().x;
      insets = annotationLabelStyle.getInsets();

      ConnectedIconLabelStyle delegateStyle = annotationLabelStyle.getDelegateStyle();
      delegateStyle.setIconStyle(left ? getLeftAnnotationStyle() : getRightAnnotationStyle());
      delegateStyle.setLabelConnectorLocation(left ? FreeNodePortLocationModel.NODE_LEFT_ANCHORED : FreeNodePortLocationModel.NODE_RIGHT_ANCHORED);
      return delegateStyle;
    }

    public final IVisualCreator getVisualCreator( ILabel item, ILabelStyle style ) {
      label = item;
      labelStyle = style;
      return this;
    }

    public final IBoundsProvider getBoundsProvider( ILabel item, ILabelStyle style ) {
      ILabelStyle delegateStyle = getCurrentStyle(item, style);
      return delegateStyle.getRenderer().getBoundsProvider(item, delegateStyle);
    }

    public final IVisibilityTestable getVisibilityTestable( ILabel item, ILabelStyle style ) {
      ILabelStyle delegateStyle = getCurrentStyle(item, style);
      return delegateStyle.getRenderer().getVisibilityTestable(item, delegateStyle);
    }

    public final IHitTestable getHitTestable( ILabel item, ILabelStyle style ) {
      ILabelStyle delegateStyle = getCurrentStyle(item, style);
      return delegateStyle.getRenderer().getHitTestable(item, delegateStyle);
    }

    public final IMarqueeTestable getMarqueeTestable( ILabel item, ILabelStyle style ) {
      ILabelStyle delegateStyle = getCurrentStyle(item, style);
      return delegateStyle.getRenderer().getMarqueeTestable(item, delegateStyle);
    }

    public final ILookup getContext( ILabel item, ILabelStyle style ) {
      ILabelStyle delegateStyle = getCurrentStyle(item, style);
      return delegateStyle.getRenderer().getContext(item, delegateStyle);
    }

    public final SizeD getPreferredSize( ILabel label, ILabelStyle style ) {
      SizeD preferredTextSize = TEXT_STYLE.getRenderer().getPreferredSize(label, TEXT_STYLE);
      double insets = ((AnnotationLabelStyle)style).getInsets();
      return new SizeD(2 * insets + preferredTextSize.width, 2 * insets + preferredTextSize.height);
    }

    public final IVisual createVisual( IRenderContext context ) {
      MyVisual container = new MyVisual(left, insets);
      ILabelStyle delegateStyle = getCurrentStyle(label, labelStyle);
      container.add(delegateStyle.getRenderer().getVisualCreator(label, delegateStyle).createVisual(context));
      return container;
    }

    public final IVisual updateVisual( IRenderContext context, IVisual oldVisual ) {
      MyVisual container = (oldVisual instanceof MyVisual) ? (MyVisual)oldVisual : null;
      if(container == null) {
        return createVisual(context);
      }
      ILabelStyle delegateStyle = getCurrentStyle(label, labelStyle);
      if (container.isLeft() != left || container.getInsets() != insets || container.getChildren().size() != 1) {
        return createVisual(context);
      }
      IVisual oldDelegateVisual = container.getChildren().get(0);
      IVisual newDelegateVisual = delegateStyle.getRenderer().getVisualCreator(label, delegateStyle).updateVisual(context, oldDelegateVisual);
      if (oldDelegateVisual != newDelegateVisual) {
        container.getChildren().set(0, newDelegateVisual);
      }
      container.setInsets(insets);
      container.setLeft(left);
      return container;
    }

    private static class MyVisual extends VisualGroup {
      private boolean left;

      private final boolean isLeft() {
        return this.left;
      }

      public final void setLeft( boolean value ) {
        this.left = value;
      }

      private double insets;

      private final double getInsets() {
        return this.insets;
      }

      public final void setInsets( double value ) {
        this.insets = value;
      }

      public MyVisual(boolean left, double insets) {
        this.left = left;
        this.insets = insets;
      }
    }
  }

  //endregion
  static {
    LEFT_ANNOTATION_STYLE = new AnnotationNodeStyle();
    LEFT_ANNOTATION_STYLE.setLeft(true);

    RIGHT_ANNOTATION_STYLE = new AnnotationNodeStyle();
    RIGHT_ANNOTATION_STYLE.setLeft(false);

    CONNECTOR_STYLE = new BpmnEdgeStyle();
    CONNECTOR_STYLE.setType(EdgeType.ASSOCIATION);

    TEXT_STYLE = new SimpleLabelStyle();
  }

}
