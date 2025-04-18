//
//  FleetsView.swift
//  Operator
//
//  Created by Ravil Khusainov on 23.02.2021.
//

import SwiftUI

struct FleetsListView: View {
    @StateObject var logic: FleetsLogicController
    var body: some View {
        VStack {
            TextField("fleet-name", text: $logic.searchName)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .padding(.horizontal)
            ScrollView {
                LazyVGrid(
                    columns: [GridItem(.adaptive(minimum: 300))],
                    spacing: 5,
                    content: {
                    ForEach(logic.fleets) { fleet in
                        Button(action: {
                            logic.selected = fleet
                            logic.done()
                        }, label: {
                            FleetItem(fleet: fleet, selected: logic.selected == fleet)
                        })
                        .buttonStyle(PlainButtonStyle())
                    }
                })
                .padding(.vertical)
            }
        }
        .animation(nil)
        .padding(.vertical)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: logic.cancel, label: {
                    Text("cancel")
                })
                .disabled(!logic.validate())
                .foregroundColor(.white)
            }
        }
        .navigationTitle("fleets")
        .navigationBarTitleDisplayMode(.inline)
        .onChange(of: logic.searchName, perform: { value in
            logic.aplyFilter()
        })
        .viewState($logic.viewState, onAppear: logic.fetch)
    }
}

struct FleetsView: View {
    
    @StateObject var logic: FleetsLogicController
    
    var body: some View {
        VStack {
            VStack {
                HStack {
                    Label(logic.userName, systemImage: "person.circle")
                    Spacer()
                    Button(action: logic.logout) {
                        Text("log-out")
                    }
                }
                .font(.headline)
                .padding()
            }
            .background(Color.background)
            .cornerRadius(10)
            NavigationView {
                FleetsListView(logic: logic)
            }
            .background(Color.background)
            .cornerRadius(10)
        }
        .padding()
        .popView()
    }
}

struct FleetsView_Previews: PreviewProvider {
    static var previews: some View {
        FleetsView(logic: .init(UserSettings()))
    }
}
