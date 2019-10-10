package org.rdengine.widget.taglayout;

import android.view.View;

import java.util.List;

public interface ITagLayoutProperty
{
    void invalidte();

    List<ITagLayoutProperty> getParents();

    List<ITagLayoutProperty> getChildren();

    boolean canCalcuate();

    boolean isValueReady();

    void calculate(TagLayoutContext context);

    int getValue();

    String getExpression();

    View getOwner();
}
