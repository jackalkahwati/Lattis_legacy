//
//  QRCodeView.swift
//  BLUF
//
//  Created by Ravil Khusainov on 19.11.2020.
//

import SwiftUI
import CoreImage.CIFilterBuiltins

struct QRCodeView: View {
    
    let code: String
    
    fileprivate let context = CIContext()
    fileprivate let filter = CIFilter.qrCodeGenerator()
    
    var body: some View {
        generateQRCode()
            .interpolation(.none)
            .resizable()
            .scaledToFit()
    }
    
    fileprivate func generateQRCode() -> Image {
        let data = Data(code.utf8)
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

extension QRCodeView {
    init(bike: Bike) {
        if let qr = bike.things.compactMap(\.qrCode).first {
            self.init(code: "http://some.com/\(qr)")
            return
        }
        if let name = bike.name, let qr = bike.qrCode {
            self.init(code: "{\"bike_name\":\"\(name)\",\"qr_id\":\(qr)}")
            return
        }
        self.init(code: "none")
    }
}

struct QRCodeView_Previews: PreviewProvider {
    static var previews: some View {
        QRCodeView(bike: .init(id: 23, name: "Super bike", qrCode: 12309, lockId: 345, things: [], fleet: nil))
    }
}
