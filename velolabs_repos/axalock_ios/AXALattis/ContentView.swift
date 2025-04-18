//
//  ContentView.swift
//  AXALattis
//
//  Created by Ravil Khusainov on 10.02.2020.
//

import SwiftUI
import AXALock

class LocksViewModel: ObservableObject {
    @Published var locks = AxaBLE.Lock.all
    fileprivate let handler = AxaBLE.Handler()
    
    init() {
        handler.discovered = { [unowned self] lock in
            self.locks.append(lock)
        }
        AxaBLE.Lock.scan(with: handler)
    }
    
    func didFound(lock: AxaBLE.Lock) {
        locks.append(lock)
    }
    
    func connectionUpdated(lock: AxaBLE.Lock) {
        
    }
    
    func lockDidfail(lock: AxaBLE.Lock, with error: Error) {
        
    }
    
    func connect(lock: AxaBLE.Lock, completion: @escaping () -> ()) {
//        AxaBLE.Lock.claim(code: .init(lock_uid: "E22510006CC3253BD418", claim_code: "8219F9A3E3074665BD3B22CE931B3948")) { (result) in
//            print(result)
//        }
        handler.connectionChanged = { l in
            guard l == lock else { return }
            completion()
        }
        lock.connect(with: handler)
    }
}

struct ContentView: View {
    @ObservedObject var viewModel = LocksViewModel()
    @State var connected: AxaBLE.Lock?
    var body: some View {
        NavigationView {
            List(viewModel.locks) { lock in
                Button(action: {
                    self.viewModel.connect(lock: lock) {
                        switch lock.connection {
                        case .paired:
                            self.connected = lock
                        case .disconnected:
                            self.connected = nil
                        default:
                            break
                        }
                    }
                }) {
                    Text(lock.name)
                }
            }
            .sheet(item: $connected) { (lock) in
                DetailView(viewModel: .init(lock))
            }
        }
    }
}

class DetailsViewModel: ObservableObject {
    @Published var status: AxaBLE.Lock.Status
    @Published var connection: AxaBLE.Lock.Connection
    let lock: AxaBLE.Lock
    fileprivate let handler = AxaBLE.Handler()
    
    init(_ lock: AxaBLE.Lock) {
        self.lock = lock
        self.status = lock.status
        self.connection = lock.connection
        handler.connectionChanged = { [unowned self] lock in
            self.connection = lock.connection
        }
        handler.statusChanged = { [unowned self] lock in
            self.status = lock.status
        }
        handler.add(lock)
    }
}

struct DetailView: View {
    @ObservedObject var viewModel: DetailsViewModel
    var body: some View {
        VStack(alignment: .center, spacing: 16) {
            Text(viewModel.lock.name)
            Text(String(describing: viewModel.status))
            Text(String(describing: viewModel.connection))
            HStack(alignment: .center, spacing: 34) {
                Button(action: viewModel.lock.lock) { Text("Lock") }
                Button(action: viewModel.lock.unlock) { Text("Unlock") }
            }
            Spacer()
            Button(action: viewModel.lock.disconnect) { Text("Disconnect")}
        }
        .padding()
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
