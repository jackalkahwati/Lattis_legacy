//
//  EquipmentView.swift
//  Operator
//
//  Created by Ravil Khusainov on 23.02.2021.
//

import SwiftUI

struct EquipmentView: View {
    
    @State var onboarding = false
    
    let things: [Thing] = .dummy
    var body: some View {
        VStack {
            ScrollView {
                ForEach(things) { thing in
                    NavigationLink(
                        destination: ThingDetailView(logic: .init(thing)),
                        label: {
                            ThingListItem(thing: thing)
                        })
                        .buttonStyle(PlainButtonStyle())
                }
            }
            Button(action: { onboarding.toggle() }, label: {
                Label("Onboard equipment", systemImage: "plus.circle")
            })
            .buttonStyle(CreateButtonStyle())
        }
        .padding()
        .sheet(isPresented: $onboarding, content: {
            OnboardEquipmentView()
        })
        .navigationTitle("Equipment")
        .redacted(reason: .placeholder)
        .disabled(true)
    }
}

struct EquipmentView_Previews: PreviewProvider {
    static var previews: some View {
        EquipmentView()
    }
}
