package org.rdengine.widget.drawableview.gestures.creator;

import org.rdengine.widget.drawableview.draw.SerializablePath;

import java.util.ArrayList;

public interface GestureCreatorListener
{
    void onGestureCreated(SerializablePath serializablePath);

    void onCurrentGestureChanged(SerializablePath currentDrawingPath);

    ArrayList<SerializablePath> getAllPath();
}
