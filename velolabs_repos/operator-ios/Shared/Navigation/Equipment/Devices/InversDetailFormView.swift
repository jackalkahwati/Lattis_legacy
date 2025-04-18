//
//  InversDetailFormView.swift
//  Operator
//
//  Created by Ravil Khusainov on 01.11.2021.
//

import SwiftUI

struct InversDetailFormView: View {
    
    @EnvironmentObject var viewModel: InversDetailFormViewModel
    
    var body: some View {
        Group {
            FormLabel(title: "vendor", value: viewModel.thing.metadata.vendor)
            FormLabel(title: "Unit ID", value: viewModel.thing.metadata.key)
        }
        switch viewModel.link {
        case .connected:
            connectedView
        case .connecting:
            FormProgressLabel(title: "connecting")
        default:
            Text("Can't connect to device")
        }
    }
    
    @ViewBuilder
    fileprivate var connectedView: some View {
        FormLabel(title: "status", localizedValue: "online")
        Section {
            PrettyToggleButton(
                action: viewModel.toggleCentralLock,
                title: "Central Lock",
                offImage: "lock.open",
                onImage: "lock",
                isOn: viewModel.centralLock == .locked,
                processing: viewModel.processingLock)
            PrettyToggleButton(
                action: viewModel.toggleImmobilizer,
                title: "Immobilizer",
                offImage: "lock.open",
                onImage: "lock",
                isOn: viewModel.immobilizer == .locked,
                processing: viewModel.processingImmo)
        }
    }
}

