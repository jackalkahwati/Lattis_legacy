//
//  ContentView.swift
//  EllipseLockUI
//
//  Created by Ravil Khusainov on 01.03.2020.
//

import SwiftUI
import EllipseLock

struct ContentView<Lock: EllipseProtocol>: View {
    @ObservedObject var viewModel = ViewModel<Lock>()
    @State var selected: Lock?
    var body: some View {
        NavigationView {
            List(viewModel.locks) { lock in
                Button(action: {
                    self.viewModel.connect(lock: lock) {
                        switch lock.connection {
                        case .paired:
                            self.selected = lock
                        case .disconnected:
                            self.selected = nil
                        default:
                            break
                        }
                    }
                }, label: {
                    Text(lock.name)
                })
            }
            .navigationBarTitle(Text("Locks"))
            .sheet(item: $selected) { (lock) in
                LockDetailView(viewModel: .init(lock))
            }
        }
    }
}


extension ContentView {
    class ViewModel<Lock: EllipseProtocol>: ObservableObject {
        @Published var locks: [Lock] = Lock.all
        let handler = EllipseHandler<Lock>()
        
        init() {
            handler.discovered = { [unowned self] lock in
                self.locks.append(lock)
            }
        }
        
        func connect(lock: Lock, completion: @escaping () -> ()) {
            handler.connectionUpdated = { l in
                guard l == lock else { return }
                completion()
            }
            lock.connect(with: handler)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView<FakeEllipse>()
    }
}
