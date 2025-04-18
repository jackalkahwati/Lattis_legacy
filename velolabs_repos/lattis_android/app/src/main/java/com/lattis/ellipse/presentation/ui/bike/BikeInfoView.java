package com.lattis.ellipse.presentation.ui.bike;

import com.lattis.ellipse.domain.model.Card;
import com.lattis.ellipse.domain.model.Location;
import com.lattis.ellipse.domain.model.map.Direction;
import com.lattis.ellipse.presentation.ui.base.BaseView;

import java.util.List;

/**
 * Created by lattis on 31/03/17.
 */

public interface BikeInfoView extends BaseView {
    void onDirectionLoaded(Direction direction);
    void onDirectionFailed();
    void setUserPosition(Location location);
    void showCardDetailsList(List<Card> cardList);

}
