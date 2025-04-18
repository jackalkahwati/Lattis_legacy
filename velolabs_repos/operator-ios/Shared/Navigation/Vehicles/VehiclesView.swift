//
//  VehiclesView.swift
//  Operator
//
//  Created by Ravil Khusainov on 23.02.2021.
//

import SwiftUI

struct VehiclesView: View {
    
    @EnvironmentObject var settings: UserSettings
    @EnvironmentObject var logic: VehiclesLogicController
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        VStack {
            if !logic.filters.isEmpty {
                Button(action: {
                    logic.sheetState = .search
                }, label: {
                    VStack {
                        Text("Filters applied. Tap here to review")
                            .foregroundColor(.white)
                            .font(.footnote)
                            .frame(maxWidth: .infinity)
                            .padding(8)
                    }
                })
                .frame(maxWidth: .infinity)
                .background(Color.accentColor)
            }
            ScrollView {
                LazyVGrid(
                    columns: [GridItem(.adaptive(minimum: 300))],
                    spacing: 5,
                    content: {
                        Section(footer: sectionFooter) {
                            ForEach(logic.vehicles) { vehicle in
                                NavigationLink(
                                    destination: VehicleDetailsView(logic: .init(vehicle, settings: settings)),
                                    tag: vehicle,
                                    selection: $logic.selectedVehicle,
                                    label: {
                                        VehicleListItem(vehicle: vehicle)
                                    })
                                    .buttonStyle(PlainButtonStyle())
                            }
                        }
                    })
                    .padding(.horizontal)
            }
            .padding(.top)
            .animation(.none)
            .placeholder(logic.vehicles.isEmpty, view: .init(message: "no-vehicles"))
            Button(action: {
                logic.sheetState = .qrCode
            }, label: {
                Label("Scan QR-code", systemImage: "qrcode.viewfinder")
            })
            .buttonStyle(CreateButtonStyle())
            .padding(.horizontal)
        }
        .padding(.bottom)
        .viewState($logic.viewState, onAppear: logic.fetch)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { logic.sheetState = .search }) {
                    Image(systemName: "magnifyingglass")
                }
                .font(.title)
            }
        }
        .sheet(item: $logic.sheetState, content: { (sheet) in
            switch sheet {
            case .search:
                VehiclesFilterView()
                    .environmentObject(VehiclesFilterViewModel($logic.filters))
//                VehicleSearchView()
//                    .environmentObject(logic)
            case .qrCode:
                QRCodeScannerView(viewModel: .init(logic.fleetId))
                    .environmentObject(settings)
            }
        })
        .onChange(of: logic.filters, perform: { _ in
            logic.search()
        })
        .navigationTitle("Vehicles")
    }
    
    @ViewBuilder
    var sectionFooter: some View {
        switch logic.listState {
        case .full:
            EmptyView()
        case .loading:
            ProgressView()
                .padding()
        case .part:
            Button(action: logic.loadMore, label: {
                Text("Load more")
                    .font(.headline)
            })
            .padding()
        }
    }
}

extension VehiclesView {
    enum Sheet: Identifiable {
        case search
        case qrCode
        
        var id: Sheet { self }
    }
}


#if os(iOS)
struct VehiclesView_Previews: PreviewProvider {
    @StateObject static var settings: UserSettings = {
        let s = UserSettings()
        s.inject.vehiclesLogic.viewState = .screen
        return s
    }()
    static var previews: some View {
        NavigationView {
            VehiclesView()
                .environmentObject(settings.inject.vehiclesLogic)
                .navigationBarTitleDisplayMode(.inline)
        }
        .onAppear {
            UINavigationBar.appearance().backgroundColor = .clear
            UINavigationBar.appearance().barTintColor = .accentColor
            UINavigationBar.appearance().tintColor = .black
        }
    }
}
#endif
