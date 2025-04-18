package com.lattis.ellipse.presentation.ui.profile.delete;

import com.lattis.ellipse.presentation.ui.base.BaseView;

/**
 * Created by raverat on 2/22/17.
 */

public interface DeleteAccountView extends BaseView {

    void onAccountDeleted();
    void onLogOutSuccessful();

    void onAccountDeletionFailed();
}
