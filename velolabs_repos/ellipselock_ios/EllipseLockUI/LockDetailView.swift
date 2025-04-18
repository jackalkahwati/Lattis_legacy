//
//  LockDetailView.swift
//  EllipseLockUI
//
//  Created by Ravil Khusainov on 01.03.2020.
//

import SwiftUI
import EllipseLock

struct LockDetailView<Lock: EllipseProtocol>: View {
    @ObservedObject var viewModel: ViewModel<Lock>
    var body: some View {
        VStack {
            Text(viewModel.lock.name)
            Text(String(describing: viewModel.lock.security))
            Spacer()
            Button(action: self.viewModel.lock.disconnect, label: {
                Text("Disconnect")
            })
        }
        .padding()
    }
}

extension LockDetailView {
    final class ViewModel<Lock: EllipseProtocol>: ObservableObject {
        @Published fileprivate var lock: Lock
        fileprivate let handler = EllipseHandler<Lock>()
        
        init(_ lock: Lock) {
            self.lock = lock
            handler.securityUpdated = { [unowned self] _ in
                self.objectWillChange.send()
            }
            handler.add(lock)
        }
    }
}

struct LockDetailView_Previews: PreviewProvider {
    static let stab = FakeEllipse("Fake")
    static var previews: some View {
        LockDetailView(viewModel: .init(stab))
    }
}
