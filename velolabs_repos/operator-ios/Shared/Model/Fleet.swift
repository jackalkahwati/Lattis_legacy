//
//  Fleet.swift
//  Operator
//
//  Created by Ravil Khusainov on 23.02.2021.
//

import SwiftUI

struct Fleet: Codable, Identifiable, Hashable {
    let id: Int
    let name: String?
    let email: String?
    let logo: URL?
    let address: Address?
    let vehiclesCount: Int
}

extension Fleet {
    var fullAddress: String? {
        guard let address = address else { return nil }
        var addr = address.address2 ?? ""
        let devider = ", "
        if let st = address.address1 {
            addr += devider + st
        }
        if let city = address.city {
            addr += devider + city
        }
        if let state = address.state {
            addr += devider + state
        }
        if let country = address.country {
            addr += devider + country
        }
        if let code = address.postalCode {
            addr += devider + code
        }
        return addr.trimmingCharacters(in: .init(charactersIn: devider))
    }
}

