//
//  LocksView.swift
//  BLUF
//
//  Created by Ravil Khusainov on 13.08.2020.
//

import SwiftUI

struct LocksView: View {
    
    @ObservedObject fileprivate var viewModel = LocksViewModel()
    @State var selection: EllipseLock?
    @State var filterIsPresented = false
    #if os(iOS)
    @Environment(\.horizontalSizeClass) var horizontalSizeClass
    #endif
    
    var body: some View {
        ScrollView {
            LazyVGrid(
                columns: [GridItem(.adaptive(minimum: 250))],
                spacing: 16,
                content: {
                    ForEach(viewModel.locks) { lock in
                        #if os(iOS)
                        NavigationLink(
                            destination: LockDetailView(lock: lock),
                            label: {
                                LockItem(lock: lock)
                            })
                        #else
                        LockItem(lock: lock)
                            .gesture(TapGesture().onEnded{
                                selection = lock
                            })
                        #endif
                    }
                })
            .padding()
        }
        .sheet(isPresented: $filterIsPresented, content: {
            LocksFilterView(viewModel: viewModel)
        })
//        .sheet(item: $selection, content: { (lock) in
//            LockDetailView(lock: lock)
//        })
        .toolbar {
            ToolbarItem {
                Button(action: {filterIsPresented.toggle()}) {
                    Image(systemName: "slider.horizontal.3")
                }
            }
        }
        .navigationTitle("Locks")
        .onAppear(perform: viewModel.fetch)
    }
    
    fileprivate func openFilters() {
        
    }
}

struct LocksView_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            NavigationView {
                LocksView()
            }
            .previewDevice("iPhone 8")
        }
    }
}
