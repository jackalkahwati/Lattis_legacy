//
//  BikeSearchBikeSearchInteractorProtocols.swift
//  Lattis O&M
//
//  Created by Ravil Khusainov on 28/04/2017.
//  Copyright © 2017 Lattis. All rights reserved.
//

import Foundation

enum SearchType: String {
    case bike
    case lock
    case member
    
    var display: String {
        switch self {
        case .bike:
            return "bike_search_type_bike".localized()
        case .lock:
            return "bike_search_type_lock".localized()
        case .member:
            return "bike_search_type_member".localized()
        }
    }
}

protocol BikeSearchInteractorInput {
    func select(searchType: SearchType)
    func search(by term: String)
    func select(bike: Bike)
}

protocol BikeSearchInteractorOutput: class {
    func show(bikes: [Bike])
}
