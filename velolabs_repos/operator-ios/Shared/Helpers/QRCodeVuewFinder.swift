//
//  QRCodeVuewFinder.swift
//  Operator
//
//  Created by Ravil Khusainov on 23.03.2021.
//

import SwiftUI
import QRCodeView

struct QRCodeVuewFinder: UIViewRepresentable {
    let found: (String) -> Bool
    @Binding var scanning: Bool
    
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
