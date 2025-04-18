//
//  CreateTicketView.swift
//  Operator
//
//  Created by Ravil Khusainov on 25.02.2021.
//

import SwiftUI

struct CreateTicketView: View {
    
    @Environment(\.presentationMode) var presentationMode
    @EnvironmentObject var logic: CreateTicketLogicController
    @State fileprivate var selectSheetState: SelectSheet?
    @State fileprivate var sheetState: Sheet?
    
    var body: some View {
        NavigationView {
            VStack {
                Form {
                    Section {
                        Menu {
                            Button(action: { sheetState = .vehicleQrCode }, label: {
                                Label("Scan QR-code", systemImage: "qrcode.viewfinder")
                            })
                            Button(action: { sheetState = .vehicleSearch }, label: {
                                Label("Search", systemImage: "magnifyingglass")
                            })
                        } label: {
                            HStack {
                                Label("Vehicle", systemImage: "bicycle")
                                Spacer()
                                if let name = logic.vehicle?.name {
                                    Text(name)
                                        .foregroundColor(.primary)
                                }
                            }
                        }
                        .disabled(!logic.canChangeVehicle)
                        HStack {
                            Button(action: { selectSheetState = .category }) {
                                Label("category", systemImage: logic.categoryImage)
                            }
                            Spacer()
                            if let cat = logic.category {
                                Text(cat.title)
                            }
                        }
                        HStack {
                            VStack(alignment: .leading) {
                                Button(action: { sheetState = .notes }) {
                                    Label("Notes", systemImage: "square.and.pencil")
                                }
                                if let notes = logic.notes {
                                    Text(notes)
                                        .padding(.top, 1)
                                }
                            }
                            Spacer()
                        }
                        HStack {
                            Button(action: { selectSheetState = .assignee }) {
                                Label("Assignee", systemImage: "person")
                            }
                            .disabled(logic.colleagues.isEmpty)
                            Spacer()
                            if let name = logic.assignee?.fullName {
                                Text(name)
                            } else if logic.colleagues.isEmpty {
                                ProgressView()
                            }
                        }
                    }
                }
                Button(action: {
                    logic.createTicket {
                        presentationMode.wrappedValue.dismiss()
                    }
                }, label: {
                    Text("create")
                })
                .buttonStyle(CreateButtonStyle())
                .disabled(!logic.validate())
                .padding([.leading, .bottom, .trailing])
            }
            .actionSheet(item: $selectSheetState) { sheet in
                ActionSheet(title: actionTitle, buttons: actionSheetButtons())
            }
            .fullScreenCover(item: $sheetState) { sheet in
                switch sheet {
                case .vehicleSearch:
                    CreateTicketSelectVehicleView(vehicle: logic.vehicleSelected)
                        .environmentObject(VehiclesLogicController(logic.settings))
                case .vehicleQrCode:
                    QRCodeScannerView(viewModel: .init(logic.fleetId))
                case .notes:
                    EditView(logic.notes, title: "notes") { (n) in
                        logic.notes = n
                    }
                }
            }
            .animation(nil)
            .navigationTitle("New Ticket")
            .viewState($logic.viewState)
            .toolbar(content: {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: { presentationMode.wrappedValue.dismiss() }, label: {
                        Text("cancel")
                    })
                    .foregroundColor(.white)
                }
            })
        }
    }
    
    fileprivate var actionTitle: Text {
        guard let sheet = selectSheetState else { return Text("") }
        switch sheet {
        case .assignee:
            return Text("Assignee")
        case .category:
            return Text("category")
        }
    }
    
    fileprivate func actionSheetButtons() -> [ActionSheet.Button] {
        var buttons = [ActionSheet.Button]()
        if let sheet = selectSheetState {
            switch sheet {
            case .assignee:
                buttons = logic.colleagues.map { oper in
                    ActionSheet.Button.default(Text(oper.fullName)) { logic.assignee = oper }
                }
            case .category:
                buttons = Ticket.Category.allCases.map{ cat in
                    ActionSheet.Button.default(Text(cat.title)) { logic.category = cat }
                }
            }
        }
        buttons.append(.cancel())
        return buttons
    }
}

extension CreateTicketView {
    enum Sheet: Identifiable {
        case vehicleSearch
        case vehicleQrCode
        case notes
        
        var id: Self { self }
    }
    
    enum SelectSheet: Identifiable {
        case assignee
        case category
        
        var id: Self { self }
    }
}

struct CreateTicketView_Previews: PreviewProvider {
    static var previews: some View {
        CreateTicketView()
            .environmentObject(CreateTicketLogicController(.init(), created: .init()))
    }
}
