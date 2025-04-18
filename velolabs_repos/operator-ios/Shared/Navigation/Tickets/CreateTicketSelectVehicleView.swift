//
//  CreateTicketSelectVehicleView.swift
//  Operator
//
//  Created by Ravil Khusainov on 22.03.2021.
//

import SwiftUI
import Combine

struct CreateTicketSelectVehicleView: View {
    
    @EnvironmentObject var logic: VehiclesLogicController
    let vehicle: PassthroughSubject<Vehicle, Never>
    @Environment(\.presentationMode) fileprivate var presentationMode
    @State var bikeNmae: String = ""
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack {
                    HStack {
                        TextField("vehicle-name", text: $bikeNmae)
                            .padding(10)
                            .background(Color.accentColor.opacity(0.3))
                            .cornerRadius(5)
                        Button(action: search) {
                            Text("search")
                        }
                        .disabled(bikeNmae.isEmpty)
                        .buttonStyle(PlainButtonStyle())
                    }
                    ForEach(logic.vehicles) { vehicle in
                        VehicleListItem(vehicle: vehicle)
                            .onTapGesture {
                                self.vehicle.send(vehicle)
                                presentationMode.wrappedValue.dismiss()
                            }
                    }
                }
                .padding()
            }
            .toolbar(content: {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { presentationMode.wrappedValue.dismiss() }, label: {
                        Text("cancel")
                    })
                }
            })
            .animation(nil)
            .viewState($logic.viewState, onAppear: logic.fetch)
            .navigationTitle("select-vehicle")
        }
    }
    
    func search() {
        bikeNmae = bikeNmae.trimmingCharacters(in: .whitespacesAndNewlines)
        logic.filters = [.name(bikeNmae)]
        logic.search()
    }
}

#if os(iOS)
struct CreateTicketSelectVehicleView_Previews: PreviewProvider {
    static var previews: some View {
        CreateTicketSelectVehicleView(vehicle: .init())
            .environmentObject(VehiclesLogicController(.init()))
    }
}
#endif
