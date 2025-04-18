//
//  BikeFilterView.swift
//  BLUF
//
//  Created by Ravil Khusainov on 22.11.2020.
//

import SwiftUI

struct BikeFilterView: View {
    
    @ObservedObject var viewModel: BikesViewModel
    @State var term: String = ""
    @State var selected: Bike.Filter.Key = .name
    fileprivate let keys: [Bike.Filter.Key] = [.name, .status, .usage, .lock, .fleet]
    
    var body: some View {
        VStack {
            HStack {
                Menu(selected.title) {
                    Picker(selection: $selected, label: /*@START_MENU_TOKEN@*/Text("Picker")/*@END_MENU_TOKEN@*/, content: {
                        ForEach(keys) { key in
                            Text(key.title).tag(key)
                        }
                    })
                }
                TextField("Search", text: $term)
                Button(action: addFilter, label: {
                    Image(systemName: "plus.circle.fill")
                })
            }
            .font(.title2)
            .padding()
            List(viewModel.filters) { filter in
                BikeFilterItem(filter: filter) {
                    if let idx = viewModel.filters.firstIndex(where: {$0.id == filter.id}) {
                        viewModel.filters.remove(at: idx)
                    }
                }
            }
            Button(action: viewModel.fetch, label: {
                Text("Search")
            })
            .padding()
        }
    }
    
    fileprivate func addFilter() {
        if let idx = viewModel.filters.firstIndex(where: {$0.key == selected}) {
            viewModel.filters.remove(at: idx)
        }
        viewModel.filters.insert(.init(key: selected, value: term), at: 0)
        term = ""
    }
}

//struct BikeFilterView_Previews: PreviewProvider {
//    static var previews: some View {
//        BikeFilterView()
//    }
//}
