//
//  TicketPageItem.swift
//  Operator
//
//  Created by Ravil Khusainov on 09.03.2021.
//

import SwiftUI

struct FormProgressLabel: View {
    let title: String
    
    var body: some View {
        HStack {
            Text(title)
                .foregroundColor(.secondary)
            Spacer()
            ProgressView()
        }
    }
}

struct TicketPageItem: View {
    
    @StateObject var logic: TicketDetailsLogicController
    @Environment(\.presentationMode) fileprivate var presentationMode
    @State fileprivate var editNotes = false
    @State fileprivate var selectAssignee = false
    
    var body: some View {
        VStack {
            Form {
                FormLabel(title: "created", value: logic.ticket.createdAt())
                FormLabel(title: "category", localizedValue: logic.get(\.category).title)
                if let oper = logic.assingee {
                    if oper == .unassigned {
                        Button(action: { selectAssignee.toggle() }) {
                            Label("Assign to:", systemImage: "person")
                        }
                    } else {
                        FormLabel(title: "Assignee", value: oper.fullName)
                        Button(action: { selectAssignee.toggle() }) {
                            Label("Reassign", systemImage: "person")
                        }
                    }
                } else {
                    FormProgressLabel(title: "Assignee")
                }
                if let note = logic.get(\.riderNotes) {
                    Section(header: Text("Rider notes")) {
                        Text(note)
                    }
                }
                Section(header: Text("Operator notes")) {
                    if let note = logic.get(\.operatorNotes) {
                        Text(note)
                        Button(action: { editNotes.toggle() }) {
                            Label("Edit", systemImage: "square.and.pencil")
                        }
                    } else {
                        Button(action: { editNotes.toggle() }) {
                            Label("Add notes", systemImage: "square.and.pencil")
                        }
                    }
                }
            }
            Button(action: {
                logic.resolve {
                    presentationMode.wrappedValue.dismiss()
                }
            }, label: {
                Text("Resolve")
            })
            .buttonStyle(CreateButtonStyle())
            .padding()
        }
        .sheet(isPresented: $editNotes) {
            EditView(logic.notes, title: "Notes", completion: logic.save(notes:))
        }
        .actionSheet(isPresented: $selectAssignee) {
            ActionSheet(title: Text("Assignee:"), message: Text("Select your colleague"), buttons: operatorButtons)
        }
        .animation(nil)
    }
    
    fileprivate var operatorButtons: [ActionSheet.Button] {
        var buttons: [ActionSheet.Button] = logic.operators.map { oper in
            .default(Text(oper.fullName), action: { logic.assign(to: oper) })
        }
        buttons.append(.cancel())
        return buttons
    }
}

//struct TicketPageItem_Previews: PreviewProvider {
//    static var previews: some View {
//        TicketPageItem()
//    }
//}
