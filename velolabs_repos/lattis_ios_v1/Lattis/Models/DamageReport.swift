//
//  DamageReport.swift
//  Lattis
//
//  Created by Ravil Khusainov on 5/26/17.
//  Copyright © 2017 Velo Labs. All rights reserved.
//

import Foundation

struct DamageReport {
    let bike: Bike
    let category: Category
    let picture: Data
    var notes: String?
    let tripId: Int?
    
    struct Request: Encodable {
        let bikeId: Int
        let category: Category
        let image: URL
        let notes: String
        let damageType: Category
        let tripId: Int?
    }
    
    func request(image: URL) -> Request {
        return .init(bikeId: bike.bikeId, category: category, image: image, notes: notes ?? "", damageType: category, tripId: tripId)
    }
}

extension DamageReport {
    enum Category: String, Encodable {
        case frameScratched
        case frameBent
        case frameGraffiti
        case frameMissingSticker
        case frontWheelBent
        case frontWheelMissing
        case backWheelBent
        case backWheelMissing
        case frontTireLow
        case frontTireFlat
        case frontTireMissing
        case backTireLow
        case backTireFlat
        case backTireMissing
        case chainFaulty
        case chainBroken
        case chainMissing
        case seatDamaged
        case seatMissing
        case pedalsDamaged
        case pedalsMissing
        case handlebarsDamaged
        case handlebarsMissing
        case spokesDamaged
        case spokesMissing
        case lockDamaged
        case lockMissing
        case damageCategoryOther
        static let all: [Category] = [.frameScratched,
                                      .frameBent,
                                      .frameGraffiti,
                                      .frameMissingSticker,
                                      .frontWheelBent,
                                      .frontWheelMissing,
                                      .backWheelBent,
                                      .backWheelMissing,
                                      .frontTireLow,
                                      .frontTireFlat,
                                      .frontTireMissing,
                                      .backTireLow,
                                      .backTireFlat,
                                      .backTireMissing,
                                      .chainFaulty,
                                      .chainBroken,
                                      .chainMissing,
                                      .seatDamaged,
                                      .seatMissing,
                                      .pedalsDamaged,
                                      .pedalsMissing,
                                      .handlebarsDamaged,
                                      .handlebarsMissing,
                                      .spokesDamaged,
                                      .spokesMissing,
                                      .lockDamaged,
                                      .lockMissing,
                                      .damageCategoryOther]
        var displayTitle: String {
            switch self {
            case .frameScratched:
                return "frame_scratched".localized()
            case .frameBent:
                return "frame_bent".localized()
            case .frameGraffiti:
                return "frame_graffiti".localized()
            case .frameMissingSticker:
                return "frame_missing_sticker".localized()
            case .frontWheelBent:
                return "front_wheel_bent".localized()
            case .frontWheelMissing:
                return "front_wheel_missing".localized()
            case .backWheelBent:
                return "back_wheel_bent".localized()
            case .backWheelMissing:
                return "back_wheel_missing".localized()
            case .frontTireLow:
                return "front_tire_low".localized()
            case .frontTireFlat:
                return "front_tire_flat".localized()
            case .frontTireMissing:
                return "front_tire_missing".localized()
            case .backTireLow:
                return "back_tire_low".localized()
            case .backTireFlat:
                return "back_tire_flat".localized()
            case .backTireMissing:
                return "back_tire_missing".localized()
            case .chainFaulty:
                return "chain_faulty".localized()
            case .chainBroken:
                return "chain_broken".localized()
            case .chainMissing:
                return "chain_missing".localized()
            case .seatDamaged:
                return "seat_damaged".localized()
            case .seatMissing:
                return "seat_missing".localized()
            case .pedalsDamaged:
                return "pedals_damaged".localized()
            case .pedalsMissing:
                return "pedals_missing".localized()
            case .handlebarsDamaged:
                return "handlebars_damaged".localized()
            case .handlebarsMissing:
                return "handlebars_missing".localized()
            case .spokesDamaged:
                return "spokes_damaged".localized()
            case .spokesMissing:
                return "spokes_missing".localized()
            case .lockDamaged:
                return "lock_damaged".localized()
            case .lockMissing:
                return "lock_missing".localized()
            case .damageCategoryOther:
                return "damage_category_other".localized()
            }
        }
    }
}
