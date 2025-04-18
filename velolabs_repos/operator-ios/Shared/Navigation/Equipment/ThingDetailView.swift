//
//  ThingDetailView.swift
//  Operator
//
//  Created by Ravil Khusainov on 01.03.2021.
//

import SwiftUI

struct ThingDetailFormView: View {
    @StateObject var logic: ThingDetailsLogicController
    var body: some View {
        FormLabel(title: "Vendor", value: logic.thing.metadata.vendor)
        if let make = logic.thing.metadata.make {
            FormLabel(title: "Make", value: make)
        }
        if let model = logic.thing.metadata.model {
            FormLabel(title: "Model", value: model)
        }
        FormLabel(title: "Key", value: logic.key)
        FormLabel(title: "Device Type", value: logic.deviceType)
        if let status = logic.status, let level = status.batteryLevel {
            FormLabel(title: "Battery level", value: "\(level)%")
        }
        if let online = logic.status?.online {
            FormLabel(title: "Connection", value: online ? "Online" : "Offline")
            Button(action: logic.fetch, label: {
                Text("refresh")
            })
            .progress(indicator: logic.isFetching)
        }
        if logic.viewState == .status {
            FormProgressLabel(title: "Connecting...")
        } else if logic.viewState == .noIntegration {
            Text("No integration for \(logic.thing.metadata.vendor) yet")
        } else if logic.viewState == .statusFailed {
            FormLabel(title: "Connection", value: "Offline")
            Text("Could not get IoT status")
        } else if let online = logic.status?.online, online {
            Section(header: Text("Control")) {
                PrettyToggleButton(
                    action: logic.toggleLock,
                    title: "Security",
                    offImage: "lock.open",
                    onImage: "lock",
                    isOn: logic.viewState == .locked,
                    processing: logic.viewState == .processing
                )
                if logic.thing.lightControl {
                    Menu(content: {
                        Button(action: {
                            logic.control(light: .init(headLight: .on, tailLight: nil))
                        }) {
                            Label("on", systemImage: "lightbulb.fill")
                        }
                        Button(action: {
                            logic.control(light: .init(headLight: .off, tailLight: nil))
                        }) {
                            Label("off", systemImage: "lightbulb.slash")
                        }
                        Button(action: {
                            logic.control(light: .init(headLight: .flicker, tailLight: nil))
                        }) {
                            Label("flicker", systemImage: "rays")
                        }
                    }, label: {
                        HStack {
                            Label("headlight", systemImage: "lightbulb")
                            Spacer()
                        }
                    })
                    Menu(content: {
                        Button(action: {
                            logic.control(light: .init(headLight: nil, tailLight: .on))
                        }) {
                            Label("on", systemImage: "lightbulb.fill")
                        }
                        Button(action: {
                            logic.control(light: .init(headLight: nil, tailLight: .off))
                        }) {
                            Label("off", systemImage: "lightbulb.slash")
                        }
                        Button(action: {
                            logic.control(light: .init(headLight: nil, tailLight: .flicker))
                        }) {
                            Label("flicker", systemImage: "rays")
                        }
                    }, label: {
                        HStack {
                            Label("taillight", systemImage: "lightbulb")
                            Spacer()
                        }
                        
                    })
                    
                }
                if logic.thing.soundControl {
                    Menu(content: {
                        Button(action: {
                            logic.control(sound: .init(controlType: nil, workMode: .on))
                        }) {
                            Label("on", systemImage: "speaker.wave.2.fill")
                        }
                        Button(action: {
                            logic.control(sound: .init(controlType: nil, workMode: .off))
                        }) {
                            Label("off", systemImage: "speaker.slash.fill")
                        }
                        Button(action: {
                            logic.control(sound: .init(controlType: .toot, workMode: nil))
                        }) {
                            Label("horn", systemImage: "speaker.wave.3.fill")
                        }
                    }, label: {
                        HStack {
                            Label("sound", systemImage: "speaker")
                            Spacer()
                        }
                        
                    })
                }
                if logic.thing.batteryCovered {
                    Button(action: logic.uncoverBattery) {
                        HStack {
                            Label("Unlock battery cover", systemImage: "minus.plus.batteryblock")
                            Spacer()
                            if logic.coverState == .processing {
                                ProgressView()
                            }
                        }
                    }
                    .disabled(logic.coverState != .standby)
                    switch logic.coverState {
                    case .notSupported:
                        Text("This feature might not be available for this vehicle.")
                    case .failed:
                        Text("Battery uncover failed with unknown error")
                    default:
                        EmptyView()
                    }
                }
            }
        }
    }
}

struct ThingDetailView: View {
    
    @StateObject var logic: ThingDetailsLogicController
    
    var body: some View {
        Form {
            ThingDetailFormView(logic: logic)
        }
        .navigationTitle(logic.thing.metadata.vendor)
    }
}

extension ThingDetailView {
    enum ControlState {
        case standby
        case processing
        case notSupported
        case failed
    }
    enum ViewState {
        case status
        case statusFailed
        case noIntegration
        case processing
        case locked
        case unlocked
    }
}

struct ThingDetailView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            ThingDetailView(logic: .init([Thing].dummy.last!))
                .navigationBarTitleDisplayMode(.inline)
        }
        .onAppear {
            UINavigationBar.appearance().backgroundColor = .clear
            UINavigationBar.appearance().barTintColor = .accentColor
            UINavigationBar.appearance().tintColor = .black
        }
    }
}
