//
//  MapTabView.swift
//  Operator
//
//  Created by Ravil Khusainov on 08.04.2021.
//

import SwiftUI

//struct VehiclesListMapView: View {
//    @StateObject var viewModel = CustomMapMockViewModel()
//    var body: some View {
//        CustomMap(
//            annotations: $viewModel.annotations,
//            onMove: viewModel.updated
//        )
//    }
//}

struct VehiclesListMapView: View {

    @EnvironmentObject var viewModel: VehiclesListMapViewModel
    @EnvironmentObject var logic: VehiclesLogicController
    @EnvironmentObject var settings: UserSettings

    var body: some View {
        VStack {
            if !viewModel.filters.isEmpty {
                Button(action: {
                    viewModel.sheetState = .filter
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
            ZStack {
                CustomMap(
                    annotations: $viewModel.annotations,
                    onMove: viewModel.update(bbox:),
                    onSelect: viewModel.select(annotation:),
                    onDeselect: { viewModel.selected = nil },
                    reFocus: $viewModel.shouldFocusOnUser
                )
                .zIndex(0)
                .ignoresSafeArea()
                VStack {
                    Spacer()
                    HStack {
                        Button(action: {viewModel.refresh()}) {
                            Text("refresh")
                        }
                        .buttonStyle(CreateButtonStyle())
                        .opacity(viewModel.timeToRefresh ? 1 : 0)
                        .animation(.easeInOut, value: viewModel.timeToRefresh)
                        Spacer()
                        Button(action: { viewModel.shouldFocusOnUser = true }, label: {
                            Image(systemName: "location.fill")
                                .padding(10)
                        })
                        .background(Circle().fill(Color.white))
                        .padding()
                    }
                    .padding(.leading)
                    if let vehicle = viewModel.selected {
                        NavigationLink(destination: VehicleDetailsView(logic: .init(vehicle, settings: settings)), isActive: $viewModel.vehicleDetailsShown, label: EmptyView.init)
                        VehicleListItem(vehicle: vehicle)
                            .padding([.leading, .bottom, .trailing])
                            .onTapGesture {
                                viewModel.vehicleDetailsShown = true
                            }
                            .transition(.move(edge: .bottom))
                            .zIndex(1)
                    }

                }
            }
        }
        .onChange(of: viewModel.filters, perform: { _ in
            CustomMap.cleanCache.toggle()
            viewModel.refresh()
        })
        .sheet(item: $viewModel.sheetState) { state in
            switch state {
            case .filter:
                VehiclesFilterView()
                    .environmentObject(VehiclesFilterViewModel($viewModel.filters))
            }
        }
    }
}

extension VehiclesListMapView {
    enum SheetState: Identifiable {
        case filter

        var id: Self { self }
    }
}
//
//struct MapTabView_Previews: PreviewProvider {
//    static var previews: some View {
//        VehiclesListMapView(viewModel: .init(settings: .init()))
//    }
//}
