//
//  VehiclePageItem.swift
//  Operator
//
//  Created by Ravil Khusainov on 09.03.2021.
//

import SwiftUI
import URLImage

struct VehiclePageItem: View {
    
    @StateObject var logic: VehicleDetailsLogicController
    @State fileprivate var qrCodeShown = false
    @Environment(\.openURL) var openURL
    
    var vehicle: Vehicle { logic.vehicle }
    
    var body: some View {
        VStack {
            Form {
                Section(header: Text("Info")) {
                    FormLabel(title: "Name", value: vehicle.name)
                    if let level = vehicle.metadata.batteryLevel {
                        FormLabel(title: "Battery level", value: "\(Int(level))%")
                    }
                    if let qr = logic.qrCode {
                        Button(action: { qrCodeShown.toggle() }, label: {
                            if qrCodeShown {
                                switch qr {
                                case .lattis(let code):
                                    QRCodeImageView<Vehicle.LattisQRCode>.lattis(code)
                                case .url(let code):
                                    QRCodeImageView<String>.url("http://operator.lattis.io/code/\(code)")
                                }
                            } else {
                                FormLabel(title: "qr-code", value: qr.stringValue)
                            }
                        })
                    }
                    if vehicle.coordinate != nil {
                        NavigationLink(
                            destination: VehicleMapView(viewModel: .init(vehicle)),
                            isActive: $logic.isMapShown,
                            label: {
                                Label("locate", systemImage: "globe")
                            })
                    }
                }
                Section(header: Text("status")) {
                    FormLabel(title: vehicle.status, localizedValue: vehicle.metadata.usage.displayValue)
                    if let meta = logic.tripMeta {
                        ForEach(meta.trips) { trip in
                            NavigationLink(
                                destination: TripDetailsView(viewModel: .init(trip, vehicle: logic.vehicle, finished: logic.tripFinish)),
                                tag: trip,
                                selection: $logic.selestedTrip,
                                label: {
                                    TripFormItem(trip: trip)
                                })
                        }
                        if meta.trips.isEmpty {
                            ForEach(meta.bookings) { booking in
                                NavigationLink(
                                    destination: BookingDetailsView(viewModel: .init(booking, vehicle: logic.vehicle)),
                                    tag: booking,
                                    selection: $logic.selectedBooking,
                                    label: {
                                        BookingListItem(booking: booking)
                                    })
                            }
                        }
                        NavigationLink(
                            destination: TripHistoryView(logic: .init(logic.vehicle)),
                            isActive: $logic.isHistoryShown,
                            label: {
                                FormLabel(title: "trips", value: "\(meta.history)")
                            })
                    }
                }
                Section(header: Text("Group")) {
                    FormLabel(title: "Ride type", value: vehicle.metadata.group.type.rawValue)
                    if let make = vehicle.metadata.group.make {
                        FormLabel(title: "Make", value: make)
                    }
                    if let model = vehicle.metadata.group.model {
                        FormLabel(title: "Model", value: model)
                    }
                    if let text = vehicle.metadata.group.description {
                        Text(text)
                    }
                    if let url = vehicle.metadata.group.image {
                        URLImage(url: url) { (image) in
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fit)
                        }
                    }
                }
            }
            Button(action: logic.changeStatus, label: {
                Text("change-status")
            })
            .buttonStyle(CreateButtonStyle())
            .padding([.leading, .bottom, .trailing])
        }
        .animation(nil)
        .fullScreenCover(isPresented: $logic.vehicleStatusEdit) {
            VehicleStatusView()
                .environmentObject(VehicleStatusViewModel.init(logic))
        }
        .alert(item: $logic.sheetStatus) { status in
            Alert(title: status.title, message: status.message, primaryButton: .default(status.actionTitle, action: logic.action(for: status)), secondaryButton: .cancel())
        }
    }
}

extension VehicleDetailsLogicController.SheetStatus {
    var title: Text {
        switch self {
        case .trip:
            return Text("Active trip!")
        case .booking:
            return Text("Active booking!")
        case .statusAlert:
            return Text("Change status")
        }
    }
    var message: Text {
        switch self {
        case .trip(let trip):
            return Text("This vehicle is in active trip with \(trip.user.fullName).\nYou need to end that trip before you can change the vehicle status.")
        case .booking(let booking):
            return Text("This vehicle has active booking with \(booking.user.fullName).\nYou should end that booking before changing the vehicle status.")
        case .statusAlert:
            return Text("no-trips-remaining")
        }
    }
    
    var actionTitle: Text {
        switch self {
        case .trip:
            return Text("End trip")
        case .booking:
            return Text("Cancel booking")
        case .statusAlert:
            return Text("status")
        }
    }
}

struct VehiclePageItem_Previews: PreviewProvider {
    static var previews: some View {
        VehiclePageItem(logic: .init([Vehicle].dummy.first!, settings: UserSettings()))
    }
}
