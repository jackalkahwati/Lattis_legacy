//
//  QRCode.swift
//  Operator
//
//  Created by Ravil Khusainov on 09.04.2021.
//

import Foundation

enum QRCode {
    case lattis(Vehicle.LattisQRCode)
    case url(String)
    
    var stringValue: String {
        switch self {
        case .lattis(let code):
            return "\(code.qr_id)"
        case .url(let code):
            return code
        }
    }
}
