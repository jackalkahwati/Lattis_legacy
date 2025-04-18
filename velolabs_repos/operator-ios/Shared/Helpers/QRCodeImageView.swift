//
//  QRCodeImageView.swift
//  Operator
//
//  Created by Ravil Khusainov on 24.03.2021.
//

import SwiftUI
import CoreImage.CIFilterBuiltins

struct QRCodeImageView<Value>: View {
    
    let value: Value
    let encode: (Value) -> Data?
    
    fileprivate let context = CIContext()
    fileprivate let filter = CIFilter.qrCodeGenerator()
    
    var body: some View {
        if let data = encode(value) {
            generateQRCode(data)
                .interpolation(.none)
                .resizable()
                .scaledToFit()
        } else {
            Text("Incorrect value for QR-code provided")
        }
    }
    
    fileprivate func generateQRCode(_ data: Data) -> Image {
        filter.setValue(data, forKey: "inputMessage")

        guard let outputImage = filter.outputImage,
              let cg = context.createCGImage(outputImage, from: outputImage.extent) else { return Image("xmark.circle") }
        #if os(macOS)
        let size = outputImage.extent.size
        return Image(nsImage: .init(cgImage: cg, size: .init(width: size.width, height: size.height)))
        #else
        return Image(uiImage: .init(cgImage: cg))
        #endif
    }
}

extension QRCodeImageView {
    static func lattis(_ code: Vehicle.LattisQRCode) -> QRCodeImageView<Vehicle.LattisQRCode> {
        .init(value: code, encode: { json in
            guard let data = try? JSONEncoder().encode(json) else { return nil }
            return data
        })
    }
    static func url(_ code: String) -> QRCodeImageView<String> {
        .init(value: code) { (str) in
            Data(str.utf8)
        }
    }
}
