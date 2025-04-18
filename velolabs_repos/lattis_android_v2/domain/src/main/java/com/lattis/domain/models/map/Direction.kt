package com.lattis.domain.models.map

import com.lattis.domain.models.Location


class Direction {

    var duration: String? = null
    var distance: String? = null
    var startAddress: String? = null
    var endAddress: String? = null
    var stepList: List<Step>? = null
    var path: List<Location>? = null

}
