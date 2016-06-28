/****************************************************************************
 **
 ** This demo file is part of yFiles for Java 3.0.0.1.
 **
 ** Copyright (c) 2000-2016 by yWorks GmbH, Vor dem Kreuzberg 28,
 ** 72070 Tuebingen, Germany. All rights reserved.
 **
 ** yFiles demo files exhibit yFiles for Java functionalities. Any redistribution
 ** of demo files in source code or binary form, with or without
 ** modification, is not permitted.
 **
 ** Owners of a valid software license for a yFiles for Java version that this
 ** demo is shipped with are allowed to use the demo source code as basis
 ** for their own yFiles for Java powered applications. Use of such programs is
 ** governed by the rights and conditions as set out in the yFiles for Java
 ** license agreement.
 **
 ** THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESS OR IMPLIED
 ** WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 ** MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 ** NO EVENT SHALL yWorks BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 ** SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 ** TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 ** PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 ** LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 ** NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 ** SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **
 ***************************************************************************/
package br.net.mirante.singular.flow.core.renderer.bpmn.view;

import com.yworks.yfiles.annotations.Obfuscation;
import com.yworks.yfiles.graphml.GraphML;

/**
 * Specifies if an Activity is an expanded or collapsed Sub-Process according to BPMN.
 * @see ActivityNodeStyle
 */
@Obfuscation(stripAfterObfuscation = false, exclude = true, applyToMembers = true)
public enum SubState {
  /**
   * Specifies that an Activity is either no Sub-Process according to BPMN or should use no Sub-Process marker.
   * @see ActivityNodeStyle
   */
  @GraphML(name = "None")
  NONE(0),

  /**
   * Specifies that an Activity is an expanded Sub-Process according to BPMN.
   * @see ActivityNodeStyle
   */
  @GraphML(name = "Expanded")
  EXPANDED(1),

  /**
   * Specifies that an Activity is a collapsed Sub-Process according to BPMN.
   * @see ActivityNodeStyle
   */
  @GraphML(name = "Collapsed")
  COLLAPSED(2),

  /**
   * Specifies that the folding state of an {@link com.yworks.yfiles.graph.INode} determines if an Activity is an expanded or
   * collapsed Sub-Process according to BPMN.
   * @see ActivityNodeStyle
   * @see com.yworks.yfiles.graph.IFoldingView#isExpanded(com.yworks.yfiles.graph.INode)
   */
  @GraphML(name = "Dynamic")
  DYNAMIC(3);

  private final int value;

  SubState(final int value) {
    this.value = value;
  }

  public int value() {
    return this.value;
  }

  public static final SubState fromOrdinal( int ordinal ) {
    for (SubState current : values()) {
      if (ordinal == current.value) {
        return current;
      }
    }
    throw new IllegalArgumentException("Enum has no value " + ordinal);
  }
}
