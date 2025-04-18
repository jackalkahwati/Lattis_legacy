//
//  ScanView+ViewModel.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 03.02.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import Foundation
import QRCodeView

extension ScanView {
    final class ViewModel: ObservableObject {
        
        let completion: (QRType) -> Void
        
        init(_ completion: @escaping (QRType) -> Void) {
            self.completion = completion
        }
        
        @Published var scanning: Bool = true
        fileprivate let decoder: QRCodeView.Decoder<LattisQR> = .json()
        fileprivate let urlDecoder: QRCodeView.Decoder<URL> = .url()
        
        func found(code: String) -> Bool {
            if let model = decoder.decode(code) {
                scanning = false
                completion(.lattis(model.qr_id))
                return true
            } else if let url = urlDecoder.decode(code) {
                scanning = false
                completion(.url(url.lastPathComponent))
                return true
            } else {
                return false
            }
        }
    }
    
    enum QRType {
        case lattis(Int)
        case url(String)
    }
}


struct LattisQR: Codable {
    let qr_id: Int
    let bike_name: String
}
