//
//  TicketsFilterView.swift
//  Operator
//
//  Created by Ravil Khusainov on 28.04.2021.
//

import SwiftUI
import Combine

extension Ticket {
    enum SearchTag: TagRepresentable, Identifiable {
        case assignee(FleetOperator)
        case vehicle(Int)
        case status(Status)
        
        var title: LocalizedStringKey {
            switch self {
            case .assignee:
                return "assignee"
            case .vehicle:
                return "vehicle"
            case .status:
                return "status"
            }
        }
        
        var value: String {
            switch self {
            case .assignee(let assignee):
                return assignee.fullName
            default:
                return ""
            }
        }
        
        var localizedValue: LocalizedStringKey? {
            switch self {
            case .assignee(let assignee) where assignee == .unassigned:
                return "unassigned"
            default:
                return nil
            }
        }
        
        var id: String { "\(self)" }
    }
}

final class TicketsFilterViewModel: ObservableObject {
    
    let logic: TicketsLogicController
    @Published var assignee: FleetOperator?
    @Published fileprivate(set) var colleagues: [FleetOperator] = []
    fileprivate var cancellables: Set<AnyCancellable> = []
    
    init(_ logic: TicketsLogicController) {
        self.logic = logic
        restoreValues()
    }
    
    func search() {
        var tags: [Ticket.SearchTag] = []
        if let a = assignee {
            tags.append(.assignee(a))
        }
        logic.search(tags: tags)
    }
    
    func clean() {
        assignee = nil
        logic.search(tags: [])
    }
    
    func restoreValues() {
        logic.settings.storage.fetch(type: [FleetOperator].self)
            .sink { _ in
            } receiveValue: { colleagues in
                self.colleagues = colleagues
            }
            .store(in: &cancellables)
        for tag in logic.searchTags {
            switch tag {
            case .assignee(let oper):
                self.assignee = oper
            case .status(let status) where status == .created:
                self.assignee = .unassigned
            default:
                continue
            }
        }
    }
}

struct TicketsFilterView: View {
    
    @EnvironmentObject var viewModel: TicketsFilterViewModel
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            VStack {
                Form {
                    Section(header: Text("assignee")) {
                        RadioButton(title: "status-any", value: nil, selected: $viewModel.assignee)
                        RadioButton(title: "unassigned", value: .unassigned, selected: $viewModel.assignee)
                        ForEach(viewModel.colleagues) { col in
                            RadioButton(title: .init(col.fullName), value: col, selected: $viewModel.assignee)
                        }
                    }
                }
                Button(action: search, label: {
                    Text("search")
                })
                .buttonStyle(CreateButtonStyle())
                .padding([.leading, .trailing, .bottom])
            }
            .navigationTitle("filter")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Text("cancel")
                    }
                    .foregroundColor(.white)
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        viewModel.clean()
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Text("clean")
                    }
                    .disabled(viewModel.assignee == nil)
                    .foregroundColor(.white)
                }
            }
        }
    }
    
    func search() {
        viewModel.search()
        presentationMode.wrappedValue.dismiss()
    }
}

struct TicketsFilterView_Previews: PreviewProvider {
    static var previews: some View {
        TicketsFilterView()
    }
}
