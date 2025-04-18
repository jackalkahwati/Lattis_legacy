
package com.lattis.ellipse.presentation.view.toolTip;

import androidx.annotation.NonNull;

/**
 * Interface definition for a callback to be invoked when a Tooltip is clicked.
 */
public interface OnClickListener {

    /**
     * Called when a Tooltip has been clicked.
     *
     * @param tooltip The Tooltip that was clicked.
     */
    void onClick(@NonNull Tooltip tooltip);
}
