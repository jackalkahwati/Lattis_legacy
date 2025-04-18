//
//  QRCodeViewFinder.swift
//  LeMond
//
//  Created by Ravil Khusainov on 04.01.2022.
//

import SwiftUI
import QRCodeView

struct QRCodeViewFinder: UIViewRepresentable {
    @Binding var scanning: Bool
    let found: (String) -> Bool
    
    func makeUIView(context: Context) -> QRCodeView {
        let viewFinder = QRCodeView()
        viewFinder.found = found
        return viewFinder
    }
    
    func updateUIView(_ uiView: QRCodeView, context: Context) {
        if scanning {
            uiView.startScan()
        } else {
            uiView.stopScan()
        }
    }
}

struct QRCodeViewFinder_Previews: PreviewProvider {
    @State static var scanning: Bool = true
    static var previews: some View {
        QRCodeViewFinder(scanning: $scanning) {_ in return true}
    }
}
