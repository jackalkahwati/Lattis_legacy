//
//  EquipmentPageItem.swift
//  Operator
//
//  Created by Ravil Khusainov on 13.04.2021.
//

import SwiftUI

struct EquipmentPageItem: View {
    
    @StateObject var logic: EquipmentPageLogicController
    
    var body: some View {
        Form {
            if let main = logic.mainThing {
                formVeiw(for: main)
            }
            if logic.equipment.count > 1 {
                Section(header: Text("Other equipment")) {
                    ForEach(logic.otherThings) { thing in
                        NavigationLink(
                            destination: Form {
                                formVeiw(for: thing)
                            },
                            label: {
                                ThingSectionListItem(thing: thing)
                            })
                    }
                }
            }
        }
    }
    
    @ViewBuilder
    func formVeiw(for thing: Thing) -> some View {
        if thing.metadata.vendor == "Ellipse" {
            EllipseDetailFormView(viewModel: .init(thing: thing))
        } else if thing.metadata.vendor == "AXA" {
            AxaDetailFormView()
                .environmentObject(AxaDetailFromViewModel(thing))
        } else if thing.metadata.vendor == "Invers" {
            InversDetailFormView()
                .environmentObject(InversDetailFormViewModel(thing))
        } else if thing.metadata.vendor == "Kisi" {
            KisiDetailFormView()
                .environmentObject(KisiDetailFormViewModel(thing))
        } else {
            ThingDetailFormView(logic: .init(thing))
        }
    }
}

//struct EquipmentPageItem_Previews: PreviewProvider {
//    static var previews: some View {
//        EquipmentPageItem()
//    }
//}
