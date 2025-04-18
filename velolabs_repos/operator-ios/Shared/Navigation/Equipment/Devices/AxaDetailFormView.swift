//
//  AxaDetailFormView.swift
//  Operator
//
//  Created by Ravil Khusainov on 28.10.2021.
//

import SwiftUI

struct AxaDetailFormView: View {
    
    @EnvironmentObject var viewModel: AxaDetailFromViewModel
    
    var body: some View {
        Group {
            FormLabel(title: "vendor", value: viewModel.thing.metadata.vendor)
            FormLabel(title: "identifier", value: viewModel.thing.metadata.key)
        }
        switch viewModel.link {
        case .nearby, .disconnected:
            nearbyView()
        case .connected:
            connectedView()
        case .connecting:
            FormProgressLabel(title: "connecting")
        default:
            FormProgressLabel(title: "searching")
        }
    }
    
    @ViewBuilder
    fileprivate func connectedView() -> some View {
        Group {
            FormLabel(title: "model", value: viewModel.modelNumber)
            FormLabel(title: "firmware-version", value: viewModel.fwVersion)
            FormLabel(title: "hardware-version", value: viewModel.hwVersion)
            FormLabel(title: "software-version", value: viewModel.sfVersion)
            if let battery = viewModel.batteryLevel {
                FormLabel(title: "battery-level", value: battery)
            }
        }
        Section {
            PrettyToggleButton(
                action: viewModel.device.toggle,
                title: "Security",
                offImage: "lock.open",
                onImage: "lock",
                isOn: viewModel.security == .locked,
                processing: viewModel.security.in([.locking, .unlocking])
            )
        }
        Group {
            Button(action: viewModel.device.disconnect) {
                Label("Disconnect", systemImage: "power")
            }
        }
    }
    
    @ViewBuilder
    fileprivate func nearbyView() -> some View {
        Section {
            Button(action: viewModel.device.connect) {
                Label("Connect", systemImage: "link")
            }
        }
    }
}

