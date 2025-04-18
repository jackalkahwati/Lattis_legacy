//
//  EBikesService.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 7/21/17.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

struct EBikeInfo {
    var batteryLevel: Double
    var key: String
    
    enum Result {
        case success(EBikeInfo)
        case error(Error, String)
    }
}

protocol EBikesService {
    var key: String {get}
    init(key: String, callback:@escaping (EBikeInfo.Result) -> ())
}

