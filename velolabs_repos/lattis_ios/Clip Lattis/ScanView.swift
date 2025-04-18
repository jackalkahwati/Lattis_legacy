//
//  ScanView.swift
//  Clip Lattis
//
//  Created by Ravil Khusainov on 03.02.2022.
//  Copyright © 2022 Lattis inc. All rights reserved.
//

import SwiftUI
import QRCodeView

struct ScanView: View {
    @StateObject var viewModel: ViewModel
    var body: some View {
        QRCodeViewFinder(found: viewModel.found, scanning: $viewModel.scanning)
            .background(Color.black)
            .cornerRadius(12)
    }
}

struct ScanView_Previews: PreviewProvider {
    static var previews: some View {
        ScanView(viewModel: .init({_ in}))
    }
}

struct QRCodeViewFinder: UIViewRepresentable {
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
