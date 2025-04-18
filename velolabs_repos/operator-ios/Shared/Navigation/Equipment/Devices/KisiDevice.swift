//
//  KisiDevice.swift
//  Operator
//
//  Created by Ravil Khusainov on 24.11.2021.
//

import Foundation

enum KisiDevice {
    struct Lock: Codable {
        let id: Int
        let online: Bool
        let unlocked: Bool
        let name: String?
        let description: String?
    }
}
