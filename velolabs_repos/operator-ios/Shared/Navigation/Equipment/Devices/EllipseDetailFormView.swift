//
//  EllipseDetailFormView.swift
//  Operator
//
//  Created by Ravil Khusainov on 25.10.2021.
//

import SwiftUI

struct EllipseDetailFormView: View {
    
    @StateObject var viewModel: EllipseDetailFormViewModel
    
    var body: some View {
        Group {
            FormLabel(title: "vendor", value: viewModel.thing.metadata.vendor)
            FormLabel(title: "name", value: viewModel.thing.name)
            FormLabel(title: "mac-id", value: viewModel.credentials?.macId)
            FormLabel(title: "serial-number", value: viewModel.serialNumber)
            FormLabel(title: "fw-version", value: viewModel.fwVersion)
            FormLabel(title: "battery-level", value: viewModel.batteryLevel)
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
        Section {
            PrettyToggleButton(
                action: viewModel.device.toggle,
                title: "Security",
                offImage: "lock.open",
                onImage: "lock",
                isOn: viewModel.security == .locked,
                processing: viewModel.security.in([.locking, .unlocking])
            )
            PrettyToggleButton(
                action: viewModel.toggleCapTouch,
                title: "Cap touch",
                offImage: "arrowtriangle.up.square",
                onImage: "arrowtriangle.up.square.fill",
                isOn: viewModel.capTouchEnabled,
                processing: false
            )
            PrettyToggleButton(
                action: viewModel.toggleAutoLock,
                title: "Auto lock",
                offImage: "a.square",
                onImage: "a.square.fill",
                isOn: viewModel.autoLockEnabled ?? false,
                processing: viewModel.autoLockEnabled == nil
            )
        }
        Group {
            Button(action: viewModel.device.reset) {
                Label("Reset", systemImage: "arrow.counterclockwise")
            }
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
            Button(action: viewModel.device.blink) {
                Label("Blink LED", systemImage: "light.max")
            }
            Button(action: { viewModel.device.setCapTouch(enabled: true) }) {
                Label("Enable Cap touch", systemImage: "arrowtriangle.up.square.fill")
            }
            Button(action: { viewModel.device.setCapTouch(enabled: false) }) {
                Label("Disable Cap touch", systemImage: "arrowtriangle.up.square")
            }
            Button(action: viewModel.reset) {
                Label("unlock", systemImage: "lock.open")
            }
            Button(action: viewModel.reset) {
                Label("reset", systemImage: "arrow.counterclockwise")
            }
            .progress(indicator: viewModel.resetting)
        }
        .disabled(viewModel.resetting)
    }
}
