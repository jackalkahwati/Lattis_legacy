//
//  BikesView.swift
//  BLUF
//
//  Created by Ravil Khusainov on 13.08.2020.
//

import SwiftUI

struct BikesView: View {
    
    @ObservedObject fileprivate var viewModel = BikesViewModel()
    @State var isFilterShown = false
    var body: some View {
        ScrollView {
            LazyVGrid(
                columns: [GridItem(.adaptive(minimum: 250))],
                spacing: 16,
                content: {
                    ForEach(viewModel.bikes) { bike in
                        NavigationLink(
                            destination: BikeDetailView(bike: bike),
                            label: {
                                BikeItem(bike: bike)
                            })
                    }
                })
                .padding()
        }
        .navigationTitle("Bikes")
        .toolbar {
            ToolbarItem {
                Button() {
                    isFilterShown.toggle()
                } label: {
                    Image(systemName: "magnifyingglass")
                }
            }
        }
        .sheet(isPresented: $isFilterShown, content: {
            BikeFilterView(viewModel: viewModel)
        })
    }
}

struct BikesView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            BikesView()
        }
    }
}
