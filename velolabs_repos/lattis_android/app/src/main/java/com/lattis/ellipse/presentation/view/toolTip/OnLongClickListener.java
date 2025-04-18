
package com.lattis.ellipse.presentation.view.toolTip;

import androidx.annotation.NonNull;

/**
 * Interface definition for a callback to be invoked when a Tooltip has been clicked and held.
 */
public interface OnLongClickListener {

    /**
     * Called when a Tooltip has been clicked and held.
     *
     * @param tooltip The Tooltip that was clicked and held.
     *
     * @return true if the callback consumed the long click, false otherwise.
     */
    boolean onLongClick(@NonNull Tooltip tooltip);
}
