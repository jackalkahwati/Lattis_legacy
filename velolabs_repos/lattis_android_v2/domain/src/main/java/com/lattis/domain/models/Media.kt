package com.lattis.domain.models

class Media {

    var id: String? = null
    var url: String? = null

    constructor() {}

    constructor(id: String, url: String?) {
        this.id = id
        this.url = url
    }

}
