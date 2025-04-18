//
//  QRCodeScannerView.swift
//  Operator
//
//  Created by Ravil Khusainov on 23.03.2021.
//

import SwiftUI
import QRCodeView

struct QRCodeScannerView: View {
    
    @StateObject var viewModel: QRCodeScannerViewModel
    @Environment(\.presentationMode) fileprivate var presentationMode
    @EnvironmentObject var settings: UserSettings
    @State var statusUpdates: Bool = false
    
    var body: some View {
        NavigationView {
            GeometryReader { geometry in
                VStack {
                    vehiclesView
                    if viewModel.scanning {
                        qrView
                        .frame(height: geometry.size.width)
                        .background(Color.black)
                        .cornerRadius(10)
                    }
                    Button(action: {
                        withAnimation {
                            viewModel.scanning.toggle()
                        }
                    }, label: {
                        scanTitle
                    })
                    .buttonStyle(CreateButtonStyle())
                }
                .animation(nil)
                .navigationTitle("\(String(viewModel.vehicles.count)) qr-codes")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .navigationBarLeading) {
                        Button(action: {
                            presentationMode.wrappedValue.dismiss()
                        }) {
                            Text("close")
                        }
                        .foregroundColor(.white)
                    }
                    ToolbarItem(placement: .automatic) {
                        Menu(content: {
                            Button(action: handleStatusUpdate, label: {
                                Label("change-status", systemImage: "character")
                            })
                        }, label: {
                            Image(systemName: "ellipsis")
                        })
                        .foregroundColor(.white)
                    }
                }
            }
            .padding()
        }
        .viewState($viewModel.viewState)
        .sheet(isPresented: $statusUpdates) {
            VehicleStatusView()
                .environmentObject(VehicleStatusViewModel.init(viewModel))
        }
        .alert(item: $viewModel.popUpVehicle) { vehicle in
            Alert(title: Text("warning"), message: Text("batch-actions-on-trip"), primaryButton: .default(Text(vehicle.name), action: {
                viewModel.selectedVehicle = vehicle
            }), secondaryButton: .cancel())
        }
    }
    
    private var qrView: some View {
        ZStack(alignment: .bottomLeading) {
            QRCodeVuewFinder(found: viewModel.found, scanning: $viewModel.scanning)
            Button(action: viewModel.toggleTorch, label: {
                ZStack {
                    Circle()
                        .fill(viewModel.isTorchOn ? Color.white.opacity(0.7) : Color.black.opacity(0.7))
                    Image(systemName: viewModel.isTorchOn ? "flashlight.on.fill" : "flashlight.off.fill")
                }
            })
            .foregroundColor(viewModel.isTorchOn ? .black : .white)
            .frame(width: 44, height: 44)
            .padding()
        }
    }
    
    private var vehiclesView: some View {
        ScrollView {
            ForEach(viewModel.vehicles) { veh in
                NavigationLink(
                    destination: VehicleDetailsView(logic: .init(veh, settings: settings)),
                    tag: veh,
                    selection: $viewModel.selectedVehicle,
                    label: {
                        VehicleListItem(vehicle: veh)
                    })
                    .buttonStyle(PlainButtonStyle())
            }
        }
    }
    
    private var scanTitle: Text {
        Text(viewModel.scanning ? "done" : "scan")
    }
    
    private func handleStatusUpdate() {
        guard viewModel.canUseBatchActions() else {
            return
        }
        statusUpdates = true
    }
}

struct QRCodeScannerView_Previews: PreviewProvider {
    static var previews: some View {
        QRCodeScannerView(viewModel: .init(0))
            .onAppear {
                UINavigationBar.appearance().backgroundColor = .clear
                UINavigationBar.appearance().barTintColor = .accentColor
                UINavigationBar.appearance().tintColor = .black
            }
    }
}
