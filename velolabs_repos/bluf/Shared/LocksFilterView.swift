//
//  LocksFilterView.swift
//  BLUF
//
//  Created by Ravil Khusainov on 23.08.2020.
//

import SwiftUI

enum LocksFilter: String, Identifiable, CaseIterable {
    var id: String { rawValue }
    
    case macId = "Mac ID"
    case name = "Name"
    case fleet = "Fleet"
    case customer = "Customer"
}

extension TagButton: Identifiable {
    var id: String {
        title + value
    }
}

struct LocksFilterView: View {
    @ObservedObject var viewModel: LocksViewModel
    @Environment(\.presentationMode) var presentationMode
    @State var filter: LocksFilter = .macId
    @State var menuFilters: [LocksFilter] = [.macId]
    @State var tags: [TagButton] = []
    var body: some View {
        VStack {
            HStack {
                Menu {
                    Picker(filter.rawValue, selection: $filter) {
                        ForEach(menuFilters) { f in
                            Text(f.rawValue).tag(f)
                        }
                    }
                } label: {
                    Text(filter.rawValue)
                        .foregroundColor(.white)
                }
                .padding(.horizontal)
                .padding(.vertical, 10)
                .background(Color.blue)
                TextField("Enter value", text: $viewModel.macId)
                    .disableAutocorrection(true)
                Button(action: addFilter) {
                    Image(systemName: "plus.circle")
                        .font(.title)
                }
                .padding(5)
                .foregroundColor(.blue)
            }
            
            .overlay(Capsule(style: .continuous)
                        .stroke(Color.gray, lineWidth: 1))
            .cornerRadius(10)
            ScrollView {
                HStack {
                    VStack(alignment: .leading, spacing: 10) {
                        ForEach(tags) { $0 }
                    }
                    Spacer()
                }
                
            }
            
            .padding(.vertical)
            Spacer()
            Button(action: search) {
                Text("Search")
                    .frame(maxWidth: .infinity)
                .padding()
            }
            .font(.title2)
            .foregroundColor(.white)
            .background(Color.blue)
            .cornerRadius(10)
        }
        .padding()
    }
    
    fileprivate func addFilter() {
        let filter = self.filter
        tags.append(
            .init(title: filter.rawValue, value: viewModel.macId, remove: { remove(filter: filter) })
        )
    }
    
    fileprivate func remove(filter: LocksFilter) {
        if let idx = tags.firstIndex(where: { $0.title == filter.rawValue }) {
            tags.remove(at: idx)
        }
    }
    
    fileprivate func search() {
        presentationMode.wrappedValue.dismiss()
    }
}

struct LocksFilterView_Previews: PreviewProvider {
    @ObservedObject static var viewModel = LocksViewModel()
    static var previews: some View {
        LocksFilterView(viewModel: viewModel)
    }
}
