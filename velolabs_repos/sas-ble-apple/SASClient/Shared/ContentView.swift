//
//  ContentView.swift
//  Shared
//
//  Created by Ravil Khusainov on 15.02.2022.
//

import SwiftUI
import SasBle
import Combine
import OvalBackend

struct ContentView: View {
    
    @StateObject var viewModel = ViewModel()
    
    var body: some View {
        List(viewModel.locks) { lock in
            HStack {
                Text(lock.name)
                Spacer()
                Text(lock.status.value.title)
            }
            .contextMenu {
                Button(action: {
                    viewModel.unlock(device: lock)
                }) {
                    Text("Unlock")
                }
            }
        }
    }
}

extension ContentView {
    
    final class ViewModel: ObservableObject {
        @Published var locks: [SAS.BLE.Device] = []
        var storage: Set<AnyCancellable> = []
        
        let manager: SAS.BLE
        
        init() {
            var backend = OvalBackend("http://lattisapp-development.lattisapi.io")
            backend.signIn(with: "f63d73d6749734f58222a6e931ea5b78cc709389977e2808e07c4e66f4d192a01b17df6d6427aad6b098def6029d0772")
            backend.userAgent = "lattis"
            manager = .init(backend)
            manager.found
                .sink { device in
                    self.locks.append(device)
                }
                .store(in: &storage)
            manager.scan()
        }
        
        func unlock(device: SAS.BLE.Device) {
            device.unlock()
        }
    }
}

extension SAS.BLE.Device.Status {
    var title: String {
        switch self {
        case .disconnected:
            return "Disconnected"
        case .connecting:
            return "Connecting"
        case .connected:
            return "Connected"
        case .unlocking:
            return "Unlocking"
        case .unlocked:
            return "Unlocked"
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
