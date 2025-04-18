//
//  LockDetailView.swift
//  BLUF
//
//  Created by Ravil Khusainov on 22.08.2020.
//

import SwiftUI

struct LockDetailView: View {
    let lock: EllipseLock
    var body: some View {
        VStack {
            Spacer()
            HStack {
                Spacer()
                Text(lock.name)
                Text(lock.macId)
                Text(lock.name)
                Spacer()
            }
            Spacer()
        }
            .navigationTitle(lock.macId)
    }
}

struct LockDetailView_Previews: PreviewProvider {
    static var previews: some View {
        LockDetailView(lock: .init(metadata: .init(id: 0, macId: "", name: ""), device: nil))
    }
}
